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
    onTransactionsChange: (MutableList<Transaction>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf(SmsStore.pending(context)) }
    var granted by remember { mutableStateOf(smsPermissionGranted(context)) }
    var message by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            pending = SmsStore.pending(context)
        }
        SmsStore.register(context, listener)
        onDispose { SmsStore.unregister(context, listener) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        granted = smsPermissionGranted(context)
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("پیامک بانکی", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "تراکنش‌های تشخیص‌داده‌شده از پیامک بانک اینجا می‌آیند و بعد از تأیید تو ثبت می‌شوند.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!granted) {
                        Text("برای خواندن پیامک‌های بانکی، دسترسی پیامک لازم است.", fontWeight = FontWeight.Bold)
                        Text(
                            "اگر پنجره‌ی درخواست باز نشد یا گزینه خاکستری بود: تنظیمات گوشی ← برنامه‌ها ← Vsoft ← منوی ⋮ ← Allow restricted settings، سپس Permissions ← SMS ← Allow.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("درخواست دسترسی") }
                        OutlinedButton(
                            onClick = { granted = smsPermissionGranted(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("بررسی مجدد") }
                    } else {
                        Text(
                            "دسترسی پیامک فعال است. پیامک‌های بانکی جدید خودکار به لیست اضافه می‌شوند.",
                            fontSize = 13.sp
                        )
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val n = withContext(Dispatchers.IO) { SmsStore.importInbox(context, 30) }
                                    message = when {
                                        n >= 0 -> "$n مورد جدید پیدا شد."
                                        n == -1 -> "دسترسی پیامک فعال نیست."
                                        else -> "خواندن پیامک‌ها با خطا روبه‌رو شد."
                                    }
                                    pending = SmsStore.pending(context)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text("خواندن پیامک‌های ۳۰ روز اخیر")
                        }
                    }
                    if (message.isNotBlank()) {
                        Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        if (pending.isEmpty()) {
            item {
                Text(
                    "پیامکی در انتظار تأیید نیست.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            item {
                Text("در انتظار تأیید (" + pending.size + ")", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(pending.sortedByDescending { it.time }, key = { it.id }) { p ->
                SmsPendingCard(
                    p = p,
                    cards = cards,
                    currency = currency,
                    onReject = {
                        SmsStore.remove(context, p.id)
                        pending = SmsStore.pending(context)
                    },
                    onConfirm = { cardName ->
                        val amount = if (currency == "IRT") p.amountRial / 10 else p.amountRial
                        val t = Transaction(
                            id = System.currentTimeMillis(),
                            type = p.type,
                            amount = amount,
                            category = if (p.type == "income") "Other income — سایر درآمدها" else "Other expense — سایر هزینه‌ها",
                            description = "ثبت خودکار از پیامک بانکی",
                            date = jalaliDateOf(p.time),
                            card = cardName,
                            person = ""
                        )
                        val list = transactions.toMutableList()
                        list.add(t)
                        onTransactionsChange(list)
                        SmsStore.remove(context, p.id)
                        pending = SmsStore.pending(context)
                    }
                )
            }
        }
    }
}

@Composable
private fun SmsPendingCard(
    p: PendingSms,
    cards: List<BankCard>,
    currency: String,
    onReject: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val matched = cards.firstOrNull { p.last4.isNotEmpty() && it.last4 == p.last4 }
    var selected by remember(p.id) { mutableStateOf(matched?.name ?: "") }
    var open by remember { mutableStateOf(false) }
    val isIncome = p.type == "income"
    val accent = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val shownAmount = if (currency == "IRT") p.amountRial / 10 else p.amountRial
    val shownBalance = if (currency == "IRT") p.balanceRial / 10 else p.balanceRial

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null,
                    tint = accent
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    (if (isIncome) "+" else "−") + money(shownAmount),
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                    fontSize = 18.sp
                )
            }
            if (p.balanceRial >= 0) {
                Text(
                    "مانده: " + money(shownBalance),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                p.body,
                fontSize = 12.sp,
                maxLines = 4,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { open = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CreditCard, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (selected.isBlank()) "انتخاب کارت" else selected)
                }
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    DropdownMenuItem(
                        text = { Text("بدون کارت") },
                        onClick = { selected = ""; open = false }
                    )
                    cards.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = { selected = c.name; open = false }
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("رد") }
                Button(onClick = { onConfirm(selected) }, modifier = Modifier.weight(1f)) { Text("تأیید و ثبت") }
            }
        }
    }
}
