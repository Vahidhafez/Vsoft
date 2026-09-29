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

data class PendingSms(
    val id: Long,
    val hash: String,
    val sender: String,
    val body: String,
    val type: String,
    val amountRial: Long,
    val balanceRial: Long,
    val last4: String,
    val time: Long
)

private const val NUM = """(?<![*xX•\d,])(\d[\d,]*)(?![\d*•/:])"""

private val PERSONAL_MOBILE = Regex("""^(\+98|0098|0)9\d{9}$""")
private val BALANCE_RE = Regex("""(?:مانده|موجودی|balance)[^\d\n]{0,25}?$NUM""", RegexOption.IGNORE_CASE)
private val LABELED_RE = Regex("""مبلغ[^\d\n]{0,25}?([+-]?)\s*$NUM\s*([+-]?)""")
private val KEYWORD_RE = Regex(
    """(?:برداشت|واریز|خرید|پرداخت|کارمزد|انتقال|دریافت|کسر|deposit|withdraw|purchase)[^\d\n]{0,20}?([+-]?)\s*$NUM\s*([+-]?)""",
    RegexOption.IGNORE_CASE
)
private val UNIT_RE = Regex("""$NUM\s*(?:ریال|تومان|IRR|rial)""", RegexOption.IGNORE_CASE)
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
    if (PERSONAL_MOBILE.matches(cleanSender)) return null

    val text = smsNormalize(rawBody)
    val lower = text.lowercase(Locale.ROOT)
    if (BLOCK_WORDS.any { lower.contains(it) }) return null

    val incomeAt = INCOME_WORDS.map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull()
    val expenseAt = EXPENSE_WORDS.map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull()
    if (incomeAt == null && expenseAt == null) return null
    val type = when {
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
                    time = o.optLong("time", 0L)
                )
            )
        }
    } catch (_: Exception) {
    }
    return out
}

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

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized
    fun addIfBank(context: Context, sender: String, body: String, time: Long): Boolean {
        val parsed = parseBankSms(sender, body, time) ?: return false
        val p = prefs(context)
        val seen = readStrings(p.getString(SEEN, "[]"))
        if (seen.contains(parsed.hash)) return false
        val pending = readPending(p.getString(PENDING, "[]")).toMutableList()
        pending.add(parsed)
        seen.add(parsed.hash)
        p.edit()
            .putString(PENDING, writePending(pending))
            .putString(SEEN, writeStrings(seen.takeLast(3000)))
            .apply()
        return true
    }

    fun pending(context: Context): List<PendingSms> =
        readPending(prefs(context).getString(PENDING, "[]"))

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

    fun importInbox(context: Context, days: Int): Int {
        if (!smsPermissionGranted(context)) return -1
        val since = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
        var count = 0
        try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                Telephony.Sms.DATE + " >= ?",
                arrayOf(since.toString()),
                Telephony.Sms.DATE + " ASC"
            )?.use { c ->
                val a = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val b = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val d = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                while (c.moveToNext()) {
                    if (addIfBank(context, c.getString(a) ?: "", c.getString(b) ?: "", c.getLong(d))) count++
                }
            }
        } catch (_: Exception) {
            return -2
        }
        return count
    }
}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        try {
            val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (parts == null || parts.isEmpty()) return
            val sender = parts[0].originatingAddress ?: ""
            val body = parts.joinToString("") { it.messageBody ?: "" }
            SmsStore.addIfBank(context, sender, body, System.currentTimeMillis())
        } catch (_: Exception) {
        }
    }
}
