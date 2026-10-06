package com.vsoft.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.provider.Telephony
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PendingSms(
    val id: Long,
    val hash: String,
    val sender: String,
    val body: String,
    val type: String,
    val amountRial: Long,
    val balanceRial: Long,
    val last4: String,
    val time: Long,
    val bank: String = ""
)

private const val NUM = """(?<![*xX•\d,])(\d[\d,]*)(?![\d*•/:])"""

private val PERSONAL_MOBILE = Regex("""^(\+98|0098|0)9\d{9}$""")
private val BANK_SMS_SENDERS = setOf("09999987641", "+989999987641", "00989999987641")
private val BALANCE_RE = Regex(
    """(?:مانده(?:\s+(?:حساب|کارت|حساب\s+شما))?|موجودی(?:\s+(?:حساب|کارت|حساب\s+شما))?|مانده\s+فعلی|موجودی\s+فعلی|available\s+balance|current\s+balance|balance)[^\d\n]{0,35}?$NUM""",
    RegexOption.IGNORE_CASE
)
private val LABELED_RE = Regex("""مبلغ[^\d\n]{0,25}?([+-]?)\s*$NUM\s*([+-]?)""")
private val KEYWORD_RE = Regex(
    """(?:برداشت|واریز|خرید|پرداخت|کارمزد|انتقال|دریافت|کسر|deposit|withdraw|purchase)[^\d\n]{0,20}?([+-]?)\s*$NUM\s*([+-]?)""",
    RegexOption.IGNORE_CASE
)
private val UNIT_RE = Regex("""$NUM\s*(?:ریال|تومان|IRR|rial)""", RegexOption.IGNORE_CASE)
private val TRANSFER_SIGN_RE = Regex(
    """(?:انتقال(?:\s+اینترنتی)?|transfer)[^\d\n]{0,20}?$NUM\s*([+-])""",
    RegexOption.IGNORE_CASE
)
private val LAST4_MASKED = Regex("""[*xX•]{2,}[\s-]*(\d{4})(?!\d)""")
private val LAST4_FULL = Regex("""(?<!\d)\d{12}(\d{4})(?!\d)""")
private val LAST4_LABEL = Regex("""کارت[^\d\n]{0,12}(\d{4})(?!\d)""")

private val BLOCK_WORDS = listOf(
    "رمز", "otp", "cvv", "کد تایید", "کد تأیید", "کد فعال", "کد پویا",
    "کد یکبار", "کد ورود", "verification"
)
private val INCOME_WORDS = listOf("واریز", "دریافت", "افزایش موجودی", "deposit", "credit")
private val EXPENSE_WORDS = listOf(
    "برداشت", "خرید", "پرداخت", "کارمزد", "انتقال", "کسر", "قبض",
    "withdraw", "purchase", "debit"
)

val VSOFT_BANKS = listOf(
    "بانک ملی", "بانک مسکن", "بلو بانک", "رد بانک", "بانک مهر",
    "بانک صادرات", "بانک ملت", "بانک تجارت", "بانک سامان",
    "بانک پاسارگاد", "بانک رفاه", "بانک کشاورزی", "بانک پارسیان",
    "بانک اقتصاد نوین", "بانک دی", "بانک شهر"
)

private val BANK_HINTS = linkedMapOf(
    "بانک ملی" to listOf("بانک ملی", "melli", "melli.ir", "bankmelli", "meli"),
    "بانک مسکن" to listOf("بانک مسکن", "maskan", "bankmaskan"),
    "بلو بانک" to listOf("بلو بانک", "بلو", "blubank", "bluebank"),
    "رد بانک" to listOf("رد بانک", "redbank", "red bank"),
    "بانک مهر" to listOf("بانک مهر", "mehr", "bankmehr"),
    "بانک صادرات" to listOf("بانک صادرات", "saderat", "banksaderat"),
    "بانک ملت" to listOf("بانک ملت", "mellat", "bankmellat"),
    "بانک تجارت" to listOf("بانک تجارت", "tejarat", "banktejarat"),
    "بانک سامان" to listOf("بانک سامان", "saman", "banksaman"),
    "بانک پاسارگاد" to listOf("بانک پاسارگاد", "pasargad", "bankpasargad"),
    "بانک رفاه" to listOf("بانک رفاه", "refah", "bankrefah"),
    "بانک کشاورزی" to listOf("بانک کشاورزی", "keshavarzi", "bankkeshavarzi"),
    "بانک پارسیان" to listOf("بانک پارسیان", "parsian", "bankparsian"),
    "بانک اقتصاد نوین" to listOf("اقتصاد نوین", "eghtesadnovin", "enbank"),
    "بانک دی" to listOf("بانک دی", "bankday", "daybank"),
    "بانک شهر" to listOf("بانک شهر", "shahr", "bankshahr")
)

private fun detectBank(sender: String, body: String): String {
    // اول خود متن پیامک؛ اگر نام بانک داخل متن نبود، فرستنده بررسی می‌شود.
    val bodySource = smsNormalize(body).lowercase(Locale.ROOT)
    val bodyBank = BANK_HINTS.entries
        .firstOrNull { (_, hints) ->
            hints.any { hint ->
                bodySource.contains(hint.lowercase(Locale.ROOT))
            }
        }
        ?.key
    if (!bodyBank.isNullOrBlank()) return bodyBank

    val senderSource = smsNormalize(sender).lowercase(Locale.ROOT)
    return BANK_HINTS.entries
        .firstOrNull { (_, hints) ->
            hints.any { hint ->
                senderSource.contains(hint.lowercase(Locale.ROOT))
            }
        }
        ?.key ?: ""
}

fun smsNormalize(s: String): String {
    val sb = StringBuilder(s.length)
    for (ch in s) {
        sb.append(
            when (ch) {
                in '۰'..'۹' -> '0' + (ch - '۰')
                in '٠'..'٩' -> '0' + (ch - '٠')
                '٬', '،' -> ','
                'ي' -> 'ی'
                'ك' -> 'ک'
                else -> ch
            }
        )
    }
    return sb.toString()
}

private fun toAmount(s: String): Long? =
    s.replace(",", "").trim().toLongOrNull()?.takeIf { it > 0 }

private fun sha(s: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(s.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

fun parseBankSms(sender: String, rawBody: String, time: Long): PendingSms? {
    val cleanSender = sender.replace(Regex("""[\s-]"""), "")
    // بلوبانک از یک شماره موبایلی پیامک می‌فرستد؛ بنابراین شماره آن نباید
    // به‌عنوان شماره شخصی و غیر بانکی فیلتر شود.
    val normalizedSender = cleanSender
        .replaceFirst(Regex("^\\+98"), "0")
        .replaceFirst(Regex("^0098"), "0")
    if (PERSONAL_MOBILE.matches(cleanSender) &&
        normalizedSender !in BANK_SMS_SENDERS) return null

    val text = smsNormalize(rawBody)
    val lower = text.lowercase(Locale.ROOT)
    if (BLOCK_WORDS.any { lower.contains(it) }) return null

    val incomeAt = INCOME_WORDS.map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull()
    val expenseAt = EXPENSE_WORDS.map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull()
    if (incomeAt == null && expenseAt == null) return null

    // در پیام‌های انتقال، ممکن است واژه «انتقال» قبل از «واریز» بیاید.
    // اگر پیام صراحتاً واریز/دریافت را اعلام کند، آن را ورودی در نظر می‌گیریم.
    val explicitIncome = listOf("واریز", "دریافت", "افزایش موجودی", "deposit", "credit")
        .any { lower.contains(it) }
    val explicitExpense = listOf("برداشت", "خرید", "پرداخت", "کارمزد", "کسر", "withdraw", "purchase", "debit")
        .any { lower.contains(it) }

    // بعضی بانک‌ها، مخصوصاً ملی و مسکن، برای «انتقال» نوع تراکنش را
    // با علامت مبلغ مشخص می‌کنند: + یعنی واریز و - یعنی کسر.
    // این علامت باید قبل از تشخیص عمومی «انتقال» بررسی شود.
    val transferSign = TRANSFER_SIGN_RE.find(text)?.groupValues?.getOrNull(2)
    val type = when {
        transferSign == "+" -> "income"
        transferSign == "-" -> "expense"
        explicitIncome && !explicitExpense -> "income"
        explicitExpense && !explicitIncome -> "expense"
        incomeAt == null -> "expense"
        expenseAt == null -> "income"
        incomeAt < expenseAt -> "income"
        else -> "expense"
    }

    val factor = if (text.contains("تومان")) 10L else 1L
    val balance = BALANCE_RE.find(text)?.groupValues?.get(1)?.let { toAmount(it) }
    val amount = LABELED_RE.find(text)?.groupValues?.get(2)?.let { toAmount(it) }
        ?: KEYWORD_RE.find(text)?.groupValues?.get(2)?.let { toAmount(it) }
        ?: UNIT_RE.findAll(text).mapNotNull { toAmount(it.groupValues[1]) }.firstOrNull { it != balance }
        ?: return null

    val last4 = (LAST4_MASKED.find(text) ?: LAST4_FULL.find(text) ?: LAST4_LABEL.find(text))
        ?.groupValues?.get(1) ?: ""
    val hasContext = balance != null || last4.isNotEmpty() ||
        text.contains("ریال") || text.contains("تومان")
    if (!hasContext) return null

    val amountRial = amount * factor
    if (amountRial < 1000L) return null

    val hash = sha(sender + "|" + rawBody)
    return PendingSms(
        id = hash.take(12).toLong(16),
        hash = hash,
        sender = sender,
        body = rawBody,
        type = type,
        amountRial = amountRial,
        balanceRial = if (balance != null) balance * factor else -1L,
        last4 = last4,
        time = time
    )
}

fun smsPermissionGranted(context: Context): Boolean =
    context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

fun jalaliDateOf(millis: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    val j = gregorianToJalali(
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
    return "%04d/%02d/%02d".format(Locale.US, j[0], j[1], j[2])
}

private fun writePending(list: List<PendingSms>): String {
    val arr = JSONArray()
    list.forEach {
        arr.put(
            JSONObject().apply {
                put("id", it.id)
                put("hash", it.hash)
                put("sender", it.sender)
                put("body", it.body)
                put("type", it.type)
                put("amountRial", it.amountRial)
                put("balanceRial", it.balanceRial)
                put("last4", it.last4)
                put("time", it.time)
                put("bank", it.bank)
            }
        )
    }
    return arr.toString()
}

private fun readPending(value: String?): List<PendingSms> {
    val out = mutableListOf<PendingSms>()
    try {
        val arr = JSONArray(value ?: "[]")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                PendingSms(
                    id = o.getLong("id"),
                    hash = o.getString("hash"),
                    sender = o.optString("sender", ""),
                    body = o.optString("body", ""),
                    type = o.optString("type", "expense"),
                    amountRial = o.getLong("amountRial"),
                    balanceRial = o.optLong("balanceRial", -1L),
                    last4 = o.optString("last4", ""),
                    time = o.optLong("time", 0L),
                    bank = o.optString("bank", "")
                )
            )
        }
    } catch (_: Exception) {
    }
    return out
}

private fun readBankMap(value: String?): MutableMap<String, String> {
    val out = mutableMapOf<String, String>()
    try {
        val obj = JSONObject(value ?: "{}")
        obj.keys().forEach { key -> out[key] = obj.optString(key, "") }
    } catch (_: Exception) {}
    return out
}

private fun writeBankMap(map: Map<String, String>): String =
    JSONObject().apply { map.forEach { (k, v) -> put(k, v) } }.toString()

private fun readStrings(value: String?): MutableList<String> {
    val out = mutableListOf<String>()
    try {
        val arr = JSONArray(value ?: "[]")
        for (i in 0 until arr.length()) out.add(arr.getString(i))
    } catch (_: Exception) {
    }
    return out
}

private fun writeStrings(list: List<String>): String {
    val arr = JSONArray()
    list.forEach { arr.put(it) }
    return arr.toString()
}

object SmsStore {
    private const val PREFS = "vsoft_sms"
    private const val PENDING = "pending"
    private const val SEEN = "seen"
    private const val BANK_MAP = "bank_map"
    private const val LAST_SCAN = "last_scan"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun initializeScanCursor(context: Context) {
        val p = prefs(context)
        if (!p.contains(LAST_SCAN)) {
            p.edit().putLong(LAST_SCAN, System.currentTimeMillis()).apply()
        }
    }

    private fun mappedBank(context: Context, sender: String): String =
        readBankMap(prefs(context).getString(BANK_MAP, "{}"))[sender.trim()] ?: ""

    fun bankFor(context: Context, sender: String, body: String, parsed: PendingSms): String {
        // نام بانک داخل متن پیامک همیشه اولویت دارد؛ mapping فرستنده فقط fallback است.
        val fromText = detectBank("", body)
        if (fromText.isNotBlank()) return fromText
        if (parsed.bank.isNotBlank()) return parsed.bank
        val mapped = mappedBank(context, sender)
        if (mapped.isNotBlank()) return mapped
        return detectBank(sender, "")
    }

    @Synchronized
    fun addIfBank(context: Context, sender: String, body: String, time: Long): Boolean {
        val parsed = parseBankSms(sender, body, time) ?: return false
        val detectedFromText = detectBank("", body)
        val mapped = mappedBank(context, sender)
        val withBank = parsed.copy(
            bank = detectedFromText.ifBlank { mapped }.ifBlank { detectBank(sender, "") }
        )
        val p = prefs(context)
        val seen = readStrings(p.getString(SEEN, "[]"))
        if (seen.contains(withBank.hash)) return false
        val pending = readPending(p.getString(PENDING, "[]")).toMutableList()
        pending.add(withBank)
        seen.add(withBank.hash)
        p.edit()
            .putString(PENDING, writePending(pending))
            .putString(SEEN, writeStrings(seen.takeLast(3000)))
            .apply()
        return true
    }

    fun pending(context: Context): List<PendingSms> {
        val p = prefs(context)
        val map = readBankMap(p.getString(BANK_MAP, "{}"))
        return readPending(p.getString(PENDING, "[]")).map {
            it.copy(bank = it.bank.ifBlank { map[it.sender.trim()] ?: "" })
        }
    }

    @Synchronized
    fun setBankForSender(context: Context, sender: String, bank: String) {
        val p = prefs(context)
        val map = readBankMap(p.getString(BANK_MAP, "{}"))
        map[sender.trim()] = bank
        val updated = readPending(p.getString(PENDING, "[]")).map {
            if (it.sender.trim() == sender.trim()) it.copy(bank = bank) else it
        }
        p.edit()
            .putString(BANK_MAP, writeBankMap(map))
            .putString(PENDING, writePending(updated))
            .apply()
    }

    @Synchronized
    fun remove(context: Context, id: Long) {
        val p = prefs(context)
        val list = readPending(p.getString(PENDING, "[]")).filter { it.id != id }
        p.edit().putString(PENDING, writePending(list)).apply()
    }

    fun register(context: Context, l: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs(context).registerOnSharedPreferenceChangeListener(l)
    }

    fun unregister(context: Context, l: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs(context).unregisterOnSharedPreferenceChangeListener(l)
    }

    fun importNewInbox(context: Context): Int {
        if (!smsPermissionGranted(context)) return -1
        initializeScanCursor(context)
        val p = prefs(context)
        val since = p.getLong(LAST_SCAN, System.currentTimeMillis())
        var count = 0
        try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                Telephony.Sms.DATE + " > ?",
                arrayOf(since.toString()),
                Telephony.Sms.DATE + " ASC"
            )?.use { c ->
                val a = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val b = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val d = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                while (c.moveToNext()) {
                    val sender = c.getString(a) ?: ""
                    val body = c.getString(b) ?: ""
                    val time = c.getLong(d)
                    if (addIfBank(context, sender, body, time)) count++
                }
            }
            p.edit().putLong(LAST_SCAN, System.currentTimeMillis()).apply()
        } catch (_: Exception) {
            return -2
        }
        return count
    }
}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (parts == null || parts.isEmpty()) return@launch
                val sender = parts[0].originatingAddress ?: ""
                val body = parts.joinToString("") { it.messageBody ?: "" }
                val time = System.currentTimeMillis()
                // پیامک فقط وارد صف بررسی می‌شود؛ ثبت مالی و تغییر موجودی
                // فقط بعد از تأیید دستی کاربر انجام خواهد شد.
                SmsStore.addIfBank(context, sender, body, time)
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
