package com.vsoft.app

import android.Manifest
import android.content.SharedPreferences
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SmsImportPage(
    currency: String,
    cards: List<BankCard>,
    transactions: List<Transaction>,
    onTransactionsChange: (MutableList<Transaction>) -> Unit,
    onCardsChange: (MutableList<BankCard>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf(SmsStore.pending(context)) }
    var granted by remember { mutableStateOf(smsPermissionGranted(context)) }
    var enabled by remember { mutableStateOf(SmsStore.isEnabled(context)) }
    var message by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            pending = SmsStore.pending(context)
        }
        SmsStore.register(context, listener)
        onDispose { SmsStore.unregister(context, listener) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        granted = smsPermissionGranted(context)
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text("پیامک بانکی", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "با تعیین بانک یک پیامک، تمام پیامک‌های همان فرستنده در همان بخش قرار می‌گیرند.",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("دریافت پیامک بانکی", fontWeight = FontWeight.Bold)
                            Text(
                                if (enabled) "فعال؛ پیامک‌های جدید فقط وارد صف بررسی می‌شوند."
                                else "خاموش؛ پیامک‌های جدید پردازش نمی‌شوند.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { turnOn ->
                                if (turnOn && !granted) {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                    )
                                    message = "برای فعال‌سازی، ابتدا دسترسی پیامک را تأیید کن."
                                } else {
                                    SmsStore.setEnabled(context, turnOn)
                                    enabled = turnOn
                                    message = if (turnOn) "دریافت پیامک بانکی فعال شد."
                                        else "دریافت پیامک خاموش شد؛ اطلاعات و صف قبلی حفظ شدند."
                                }
                            }
                        )
                    }
                    if (!granted) {
                        Text("برای خواندن پیامک‌های بانکی، دسترسی پیامک لازم است.", fontWeight = FontWeight.Bold)
                        Text("اگر پنجره درخواست باز نشد، دسترسی SMS برنامه Vsoft را از تنظیمات گوشی فعال کن.",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
                        }, modifier = Modifier.fillMaxWidth()) { Text("درخواست دسترسی") }
                        OutlinedButton(onClick = { granted = smsPermissionGranted(context) }, modifier = Modifier.fillMaxWidth()) {
                            Text("بررسی مجدد")
                        }
                    } else if (!enabled) {
                        Text("دریافت پیامک بانکی خاموش است؛ تا زمان فعال‌کردن دوباره، پیامک جدیدی پردازش نمی‌شود.", fontSize = 13.sp)
                        Button(onClick = {
                            SmsStore.setEnabled(context, true)
                            enabled = true
                            message = "دریافت پیامک بانکی فعال شد."
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Sms, null); Spacer(Modifier.width(8.dp)); Text("فعال‌کردن دریافت پیامک")
                        }
                    } else {
                        Text("پیامک‌ها ابتدا برای بررسی می‌آیند و بدون تأیید تو تراکنشی ثبت نمی‌شود.", fontSize = 13.sp)
                        OutlinedButton(onClick = {
                            scope.launch {
                                val n = withContext(Dispatchers.IO) { SmsStore.importNewInbox(context) }
                                message = when {
                                    n >= 0 -> "$n مورد جدید برای بررسی پیدا شد."
                                    n == -1 -> "دسترسی پیامک فعال نیست."
                                    n == -3 -> "دریافت پیامک بانکی خاموش است."
                                    else -> "خواندن پیامک‌ها با خطا روبه‌رو شد."
                                }
                                pending = SmsStore.pending(context)
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Download, null); Spacer(Modifier.width(8.dp)); Text("بررسی پیامک‌های جدید")
                        }
                    }
                    if (message.isNotBlank()) Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        val grouped = pending.sortedByDescending { it.time }.groupBy { it.bank.ifBlank { "مشخص نشده" } }
        if (pending.isEmpty()) {
            item { Text("پیامکی در انتظار تأیید نیست.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            item { Text("پیامک‌های مشخص‌شده", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            grouped.forEach { (bank, bankItems) ->
                item(key = "bank_$bank") {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(bank, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(bankItems.size.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(bankItems, key = { it.id }) { p ->
                    SmsPendingCard(
                        p = p, cards = cards, currency = currency,
                        onBankSelected = { bankName ->
                            SmsStore.setBankForSender(context, p.sender, bankName)
                            pending = SmsStore.pending(context)
                        },
                        onReject = {
                            SmsStore.remove(context, p.id)
                            pending = SmsStore.pending(context)
                        },
                        onConfirm = { cardName ->
                            val amount = if (currency == "IRT") p.amountRial / 10 else p.amountRial
                            val list = transactions.toMutableList()
                            list.add(Transaction(
                                id = System.currentTimeMillis(),
                                type = p.type,
                                amount = amount,
                                category = if (p.type == "income") "Other income — سایر درآمدها" else "Other expense — سایر هزینه‌ها",
                                description = "ثبت خودکار از پیامک " + p.bank.ifBlank { "بانکی" },
                                date = jalaliDateOf(p.time),
                                card = cardName,
                                person = ""
                            ))
                            onTransactionsChange(list)

                            // موجودی اعلام‌شده پیامک فقط برای بررسی است؛ موجودی اولیه کارت هرگز خودکار تغییر نمی‌کند.

                            SmsStore.remove(context, p.id)
                            pending = SmsStore.pending(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsPendingCard(
    p: PendingSms, cards: List<BankCard>, currency: String,
    onBankSelected: (String) -> Unit, onReject: () -> Unit, onConfirm: (String) -> Unit
) {
    val matched = cards.firstOrNull { p.last4.isNotEmpty() && it.last4 == p.last4 }
    var selectedCard by remember(p.id) { mutableStateOf(matched?.name ?: "") }
    var cardMenuOpen by remember { mutableStateOf(false) }
    var bankMenuOpen by remember { mutableStateOf(false) }
    val isIncome = p.type == "income"
    val accent = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    val shownAmount = if (currency == "IRT") p.amountRial / 10 else p.amountRial
    val shownBalance = if (currency == "IRT") p.balanceRial / 10 else p.balanceRial

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = accent)
                Spacer(Modifier.width(8.dp))
                Text((if (isIncome) "+" else "−") + money(shownAmount), fontWeight = FontWeight.ExtraBold, color = accent, fontSize = 18.sp)
            }
            if (p.balanceRial >= 0) Text("مانده پیامک: " + money(shownBalance), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(p.body, fontSize = 12.sp, maxLines = 4, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { bankMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AccountBalance, null); Spacer(Modifier.width(8.dp))
                    Text(if (p.bank.isBlank()) "این پیام مال کدام بانک است؟" else p.bank)
                }
                DropdownMenu(expanded = bankMenuOpen, onDismissRequest = { bankMenuOpen = false }) {
                    VSOFT_BANKS.forEach { bank ->
                        DropdownMenuItem(text = { Text(bank) }, onClick = {
                            onBankSelected(bank); bankMenuOpen = false
                        })
                    }
                }
            }

            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { cardMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CreditCard, null); Spacer(Modifier.width(8.dp))
                    Text(if (selectedCard.isBlank()) "انتخاب کارت" else selectedCard)
                }
                DropdownMenu(expanded = cardMenuOpen, onDismissRequest = { cardMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("بدون کارت") }, onClick = { selectedCard = ""; cardMenuOpen = false })
                    cards.filter { p.bank.isBlank() || it.bank == p.bank }.forEach { card ->
                        DropdownMenuItem(text = { Text(card.name) }, onClick = {
                            selectedCard = card.name; cardMenuOpen = false
                        })
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("رد") }
                Button(onClick = { onConfirm(selectedCard) }, enabled = selectedCard.isNotBlank(), modifier = Modifier.weight(1f)) {
                    Text("تأیید و ثبت")
                }
            }
        }
    }
}
