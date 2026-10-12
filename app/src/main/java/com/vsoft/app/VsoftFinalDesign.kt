package com.vsoft.app

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Final visual pass for the Vsoft home page.
 * This is presentation-only: transaction/card/work data and calculations remain in existing models.
 */
@Composable
fun VsoftFinalDashboardPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>,
    onNavigate: (Int) -> Unit
) {
    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val workIncome = workDays.sumOf { it.income }
    val balance = cards.sumOf { it.openingBalance } + income + workIncome - expense
    val totalHours = workDays.sumOf { calculateHours(it.start, it.end) }
    val currentJalali = today()
    val monthPrefix = currentJalali.substringBeforeLast("/") + "/"
    val monthWorkDays = workDays.filter { it.date.startsWith(monthPrefix) }
    val monthWorkIncome = monthWorkDays.sumOf { it.income }
    val monthExpenses = transactions.filter { it.type == "expense" && it.date.startsWith(monthPrefix) }.sumOf { it.amount }
    val monthIncome = transactions.filter { it.type == "income" && it.date.startsWith(monthPrefix) }.sumOf { it.amount }
    val monthNet = monthWorkIncome + monthIncome - monthExpenses
    val recent = transactions.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id }).take(4)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("سلام، خوش اومدی", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("مدیریت مالی تو", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(today(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF1C8B83), Color(0xFF4F75D8)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Wallet, null, tint = Color.White, modifier = Modifier.size(27.dp))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(3) },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    Modifier.fillMaxWidth().background(
                        Brush.linearGradient(listOf(Color(0xFF172844), Color(0xFF233E65), Color(0xFF166F75)))
                    ).padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("موجودی کل حساب‌ها", color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
                                Text(money(balance), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("مجموع کارت‌ها و تراکنش‌های ثبت‌شده", color = Color.White.copy(alpha = .68f), fontSize = 11.sp)
                            }
                            Box(Modifier.size(49.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(25.dp))
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = .18f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDownward, null, tint = Color(0xFF8DF0CD), modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("درآمد", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
                                }
                                Text(money(income + workIncome), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, null, tint = Color(0xFFFFC3B9), modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("هزینه", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
                                }
                                Text(money(expense), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VsoftFinalMetric("تراکنش‌ها", transactions.size.toString(), Icons.Default.ReceiptLong, Color(0xFF456DE2), Modifier.weight(1f).clickable { onNavigate(1) })
                VsoftFinalMetric("ساعت کاری", String.format(Locale.US, "%.1f", totalHours), Icons.Default.AccessTime, Color(0xFF0C9A81), Modifier.weight(1f).clickable { onNavigate(2) })
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("کارت‌های بانکی", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text("برای دیدن و مدیریت کارت‌ها انتخاب کن", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onNavigate(5) }) {
                    Text("همه کارت‌ها")
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(15.dp))
                }
            }
        }
        if (cards.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth().clickable { onNavigate(5) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .1f))
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddCard, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("هنوز کارتی اضافه نشده", fontWeight = FontWeight.Bold)
                            Text("برای شروع، کارت بانکی خودت رو اضافه کن", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                    items(cards, key = { it.id }) { card ->
                        VsoftFinalBankCard(
                            card = card,
                            balance = cardCurrentBalance(card, transactions, workDays),
                            onClick = { onNavigate(5) }
                        )
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("دسترسی سریع", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text("بخش موردنظرت رو انتخاب کن", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VsoftFinalQuickAction("تراکنش جدید", Icons.Default.AddCard, Color(0xFF456DE2), Modifier.weight(1f)) { onNavigate(1) }
                VsoftFinalQuickAction("ثبت روز کاری", Icons.Default.WorkHistory, Color(0xFF0C9A81), Modifier.weight(1f)) { onNavigate(2) }
                VsoftFinalQuickAction("گزارش مالی", Icons.Default.Insights, Color(0xFFB16DDB), Modifier.weight(1f)) { onNavigate(3) }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(3) },
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .08f))
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("خلاصهٔ این ماه", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("مرور سریع درآمد و هزینه", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.clip(RoundedCornerShape(13.dp)).background((if (monthNet >= 0) Color(0xFF0C9A81) else MaterialTheme.colorScheme.error).copy(alpha = .12f)).padding(horizontal = 11.dp, vertical = 8.dp)) {
                            Text(money(monthNet), color = if (monthNet >= 0) Color(0xFF0C9A81) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VsoftFinalMiniStat("درآمد", money(monthIncome + monthWorkIncome), Icons.Default.TrendingUp, Color(0xFF0C9A81), Modifier.weight(1f))
                        VsoftFinalMiniStat("هزینه", money(monthExpenses), Icons.Default.TrendingDown, Color(0xFFD66A5E), Modifier.weight(1f))
                        VsoftFinalMiniStat("روز کاری", monthWorkDays.size.toString(), Icons.Default.EventAvailable, Color(0xFF456DE2), Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("آخرین تراکنش‌ها", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onNavigate(1) }) { Text("مشاهده همه") }
            }
        }
        if (recent.isEmpty()) {
            item { EmptyState("هنوز تراکنشی ثبت نشده", Icons.Default.ReceiptLong) }
        } else {
            items(recent, key = { it.id }) { transaction ->
                Box(Modifier.fillMaxWidth().clickable { onNavigate(1) }) {
                    TransactionCard(transaction, onDelete = {}, showActions = false)
                }
            }
        }
    }
}

@Composable
private fun VsoftFinalMetric(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = .13f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = .11f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun VsoftFinalBankCard(card: BankCard, balance: Long, onClick: () -> Unit) {
    val (startColor, endColor) = bankCardColors(card.bank)
    Card(
        modifier = Modifier.width(282.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(27.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(startColor, startColor.copy(alpha = .94f), endColor))).padding(19.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .17f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(card.bank.ifBlank { "کارت بانکی" }, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text(card.name.ifBlank { "Vsoft" }, fontSize = 11.sp, color = Color.White.copy(alpha = .78f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.Contactless, null, tint = Color.White.copy(alpha = .85f), modifier = Modifier.size(22.dp))
                }
                Text(
                    if (card.cardNumber.isNotBlank()) "••••  ••••  ••••  " + card.cardNumber.filter(Char::isDigit).takeLast(4) else "••••  ••••  ••••  ••••",
                    letterSpacing = 1.5.sp, fontSize = 14.sp, color = Color.White.copy(alpha = .94f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("موجودی فعلی", fontSize = 10.sp, color = Color.White.copy(alpha = .72f))
                        Text(money(balance), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text("VSOFT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = Color.White.copy(alpha = .65f))
                }
            }
        }
    }
}

@Composable
private fun VsoftFinalQuickAction(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick).animateContentSize(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = .14f))
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun VsoftFinalMiniStat(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(17.dp)).background(accent.copy(alpha = .07f)).padding(horizontal = 9.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(19.dp))
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
