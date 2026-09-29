package com.vsoft.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private fun toolsText(language: String, key: String): String = when (language) {
    "en" -> when (key) {
        "tools" -> "Tools"; "calendar" -> "Calendar"; "search" -> "Search"; "insights" -> "Smart insights"
        "hint" -> "Search transactions, work, people, cards..."; "none" -> "Nothing found"
        "work" -> "Workday"; "transaction" -> "Transaction"; "workIncome" -> "Work income"
        "expenses" -> "Total expenses"; "net" -> "Net result"; "days" -> "Workdays"
        "hours" -> "Hours"; "avgDay" -> "Average daily work income"; "avgHour" -> "Average hourly work income"
        "noActivity" -> "No activity on this day"; "previous" -> "Previous"; "next" -> "Next"; "month" -> "This month"
        else -> key
    }
    "ar" -> when (key) {
        "tools" -> "الأدوات"; "calendar" -> "التقويم"; "search" -> "البحث"; "insights" -> "التحليلات الذكية"
        "hint" -> "ابحث في المعاملات والعمل والأشخاص والبطاقات..."; "none" -> "لم يتم العثور على شيء"
        "work" -> "يوم عمل"; "transaction" -> "معاملة"; "workIncome" -> "دخل العمل"
        "expenses" -> "إجمالي المصروفات"; "net" -> "النتيجة الصافية"; "days" -> "أيام العمل"
        "hours" -> "الساعات"; "avgDay" -> "متوسط دخل يوم العمل"; "avgHour" -> "متوسط دخل الساعة"
        "noActivity" -> "لا توجد أنشطة في هذا اليوم"; "previous" -> "السابق"; "next" -> "التالي"; "month" -> "هذا الشهر"
        else -> key
    }
    else -> when (key) {
        "tools" -> "ابزارها"; "calendar" -> "تقویم"; "search" -> "جستجو"; "insights" -> "تحلیل هوشمند"
        "hint" -> "جستجو در تراکنش‌ها، کارها، افراد و کارت‌ها..."; "none" -> "موردی پیدا نشد"
        "work" -> "روز کاری"; "transaction" -> "تراکنش"; "workIncome" -> "درآمد کاری"
        "expenses" -> "مجموع هزینه‌ها"; "net" -> "خالص نتیجه"; "days" -> "روزهای کاری"
        "hours" -> "ساعت"; "avgDay" -> "میانگین درآمد هر روز کاری"; "avgHour" -> "میانگین درآمد هر ساعت کاری"
        "noActivity" -> "در این روز فعالیتی ثبت نشده"; "previous" -> "قبلی"; "next" -> "بعدی"; "month" -> "این ماه"
        else -> key
    }
}

@Composable
fun VsoftToolsPage(
    language: String,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>,
    people: List<Person>,
    workplaces: List<Workplace>
) {
    var mode by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(toolsText(language, "tools"), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(mode == 0, { mode = 0 }, label = { Text(toolsText(language, "calendar")) }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(17.dp)) })
                FilterChip(mode == 1, { mode = 1 }, label = { Text(toolsText(language, "search")) }, leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(17.dp)) })
                FilterChip(mode == 2, { mode = 2 }, label = { Text(toolsText(language, "insights")) }, leadingIcon = { Icon(Icons.Default.AutoGraph, null, Modifier.size(17.dp)) })
            }
        }
        when (mode) {
            0 -> VsoftCalendarContent(language, transactions, workDays)
            1 -> VsoftSearchContent(language, transactions, workDays, cards, people, workplaces)
            else -> VsoftInsightsContent(language, transactions, workDays)
        }
    }
}

@Composable
private fun VsoftCalendarContent(language: String, transactions: List<Transaction>, workDays: List<WorkDay>) {
    val now = today().split("/").mapNotNull { it.toIntOrNull() }
    var year by remember { mutableStateOf(now.getOrNull(0) ?: 1405) }
    var month by remember { mutableStateOf(now.getOrNull(1) ?: 1) }
    var selectedDay by remember { mutableStateOf(now.getOrNull(2) ?: 1) }
    val selectedDate = "%04d/%02d/%02d".format(Locale.US, year, month, selectedDay)
    val dayTransactions = transactions.filter { it.date == selectedDate }
    val dayWork = workDays.filter { it.date == selectedDate }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            Card(Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { month--; if (month < 1) { month = 12; year-- }; selectedDay = 1 }) { Text("‹ " + toolsText(language, "previous")) }
                        Text(jalaliMonthName(month) + " " + year, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        TextButton(onClick = { month++; if (month > 12) { month = 1; year++ }; selectedDay = 1 }) { Text(toolsText(language, "next") + " ›") }
                    }
                    val gregorian = jalaliToGregorian(year, month, 1)
                    val cal = java.util.Calendar.getInstance().apply { set(gregorian[0], gregorian[1] - 1, gregorian[2]) }
                    val offset = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                    val cells = buildList<Int?> {
                        repeat(offset) { add(null) }
                        for (d in 1..jalaliMonthDays(year, month)) add(d)
                        while (size % 7 != 0) add(null)
                    }
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                                    if (day == null) Spacer(Modifier.size(42.dp))
                                    else Box(Modifier.size(42.dp).clip(CircleShape).background(if (day == selectedDay) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { selectedDay = day }, contentAlignment = Alignment.Center) {
                                        Text(day.toString(), color = if (day == selectedDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Text(selectedDate, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        if (dayTransactions.isEmpty() && dayWork.isEmpty()) {
            item { Text(toolsText(language, "noActivity"), Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(dayTransactions, key = { "t" + it.id }) { t ->
                ListItem(headlineContent = { Text(t.description.ifBlank { t.category }, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(toolsText(language, "transaction") + " • " + t.category) },
                    leadingContent = { Icon(if (t.type == "income") Icons.Default.TrendingUp else Icons.Default.TrendingDown, null) },
                    trailingContent = { Text(money(t.amount), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
            items(dayWork, key = { "w" + it.id }) { w ->
                ListItem(headlineContent = { Text(w.place.ifBlank { toolsText(language, "work") }, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(w.start + " → " + w.end + " • " + String.format(Locale.US, "%.1f", calculateHours(w.start, w.end)) + " " + toolsText(language, "hours")) },
                    leadingContent = { Icon(Icons.Default.Work, null) },
                    trailingContent = { Text(money(w.income), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
        }
    }
}

@Composable
private fun VsoftSearchContent(language: String, transactions: List<Transaction>, workDays: List<WorkDay>, cards: List<BankCard>, people: List<Person>, workplaces: List<Workplace>) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val tx = transactions.filter { q.isNotBlank() && listOf(it.category, it.description, it.date, it.card, it.person).any { s -> s.lowercase().contains(q) } }
    val work = workDays.filter { q.isNotBlank() && listOf(it.place, it.description, it.person, it.card, it.date, it.startDate, it.endDate).any { s -> s.lowercase().contains(q) } }
    val cardMatches = cards.filter { q.isNotBlank() && listOf(it.bank, it.name, it.cardNumber, it.last4).any { s -> s.lowercase().contains(q) } }
    val peopleMatches = people.filter { q.isNotBlank() && listOf(it.name, it.phone, it.note, it.job).any { s -> s.lowercase().contains(q) } }
    val placeMatches = workplaces.filter { q.isNotBlank() && it.name.lowercase().contains(q) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(toolsText(language, "search")) }, placeholder = { Text(toolsText(language, "hint")) },
                leadingIcon = { Icon(Icons.Default.Search, null) })
        }
        if (q.isNotBlank()) {
            val total = tx.size + work.size + cardMatches.size + peopleMatches.size + placeMatches.size
            item { Text(total.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (total == 0) item { Text(toolsText(language, "none"), Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(tx, key = { "tx" + it.id }) { t ->
                ListItem(headlineContent = { Text(t.description.ifBlank { t.category }) }, supportingContent = { Text(toolsText(language, "transaction") + " • " + t.date) }, trailingContent = { Text(money(t.amount), fontWeight = FontWeight.Bold) }, leadingContent = { Icon(Icons.Default.ReceiptLong, null) }, modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
            items(work, key = { "wk" + it.id }) { w ->
                ListItem(headlineContent = { Text(w.place.ifBlank { toolsText(language, "work") }) }, supportingContent = { Text(toolsText(language, "work") + " • " + w.date) }, trailingContent = { Text(money(w.income), fontWeight = FontWeight.Bold) }, leadingContent = { Icon(Icons.Default.Work, null) }, modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
            items(cardMatches, key = { "cd" + it.id }) { c ->
                ListItem(headlineContent = { Text(c.name) }, supportingContent = { Text(c.bank + " • " + c.last4) }, leadingContent = { Icon(Icons.Default.CreditCard, null) }, modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
            items(peopleMatches, key = { "pp" + it.id }) { p ->
                ListItem(headlineContent = { Text(p.name) }, supportingContent = { Text(p.job.ifBlank { p.phone }) }, leadingContent = { Icon(Icons.Default.Person, null) }, modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
            items(placeMatches, key = { "pl" + it.id }) { p ->
                ListItem(headlineContent = { Text(p.name) }, supportingContent = { Text(toolsText(language, "work")) }, leadingContent = { Icon(Icons.Default.Place, null) }, modifier = Modifier.vsoftGlass(RoundedCornerShape(18.dp)))
            }
        }
    }
}

@Composable
private fun VsoftInsightsContent(language: String, transactions: List<Transaction>, workDays: List<WorkDay>) {
    val now = today().split("/").mapNotNull { it.toIntOrNull() }
    val year = now.getOrNull(0) ?: 1405
    val month = now.getOrNull(1) ?: 1
    val prefix = "%04d/%02d/".format(Locale.US, year, month)
    val monthTransactions = transactions.filter { it.date.startsWith(prefix) }
    val monthWork = workDays.filter { it.date.startsWith(prefix) }
    val income = monthTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val expenses = monthTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val workIncome = monthWork.sumOf { it.income }
    val hours = monthWork.sumOf { calculateHours(it.start, it.end) }
    val days = monthWork.size
    val net = income + workIncome - expenses
    val avgDay = if (days == 0) 0L else workIncome / days
    val avgHour = if (hours <= 0.0) 0L else (workIncome / hours).toLong()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
        item { Text(toolsText(language, "month"), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item { ToolMetric(toolsText(language, "workIncome"), money(workIncome), Icons.Default.Work) }
        item { ToolMetric(toolsText(language, "expenses"), money(expenses), Icons.Default.TrendingDown) }
        item { ToolMetric(toolsText(language, "net"), money(net), Icons.Default.AccountBalanceWallet) }
        item { ToolMetric(toolsText(language, "days"), days.toString(), Icons.Default.EventAvailable) }
        item { ToolMetric(toolsText(language, "hours"), String.format(Locale.US, "%.1f", hours), Icons.Default.AccessTime) }
        item { ToolMetric(toolsText(language, "avgDay"), money(avgDay), Icons.Default.Today) }
        item { ToolMetric(toolsText(language, "avgHour"), money(avgHour), Icons.Default.Schedule) }
    }
}

@Composable
private fun ToolMetric(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(22.dp)), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
