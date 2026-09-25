package com.vsoft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val android.content.Context.dataStore by preferencesDataStore("vsoft_data")

private val TRANSACTIONS_KEY = stringPreferencesKey("transactions")
private val WORK_KEY = stringPreferencesKey("work_days")
private val CARDS_KEY = stringPreferencesKey("cards")
private val PEOPLE_KEY = stringPreferencesKey("people")
private val LANGUAGE_KEY = stringPreferencesKey("language")
private val THEME_KEY = stringPreferencesKey("theme")

// ---------------- MODELS ----------------

data class Transaction(
    val id: Long,
    val type: String,
    val amount: Long,
    val category: String,
    val description: String,
    val date: String,
    val card: String,
    val person: String
)

data class WorkDay(
    val id: Long,
    val place: String,
    val date: String,
    val start: String,
    val end: String,
    val income: Long,
    val description: String,
    val person: String
)

data class BankCard(
    val id: Long,
    val bank: String,
    val name: String,
    val last4: String,
    val balance: Long
)

data class Person(
    val id: Long,
    val name: String,
    val phone: String,
    val note: String
)

// ---------------- TEXT ----------------

data class AppStrings(
    val dashboard: String,
    val finance: String,
    val work: String,
    val reports: String,
    val settings: String,
    val balance: String,
    val income: String,
    val expense: String,
    val add: String,
    val delete: String,
    val edit: String,
    val save: String,
    val cancel: String,
    val cards: String,
    val people: String,
    val language: String,
    val theme: String,
    val light: String,
    val dark: String,
    val system: String,
    val search: String,
    val category: String,
    val description: String,
    val date: String,
    val amount: String,
    val place: String,
    val start: String,
    val end: String,
    val hours: String,
    val noData: String,
    val monthlyReport: String
)

fun strings(language: String): AppStrings {
    return when (language) {
        "en" -> AppStrings(
            "Dashboard", "Finance", "Work", "Reports", "Settings",
            "Balance", "Income", "Expense", "Add", "Delete", "Edit",
            "Save", "Cancel", "Bank Cards", "People", "Language",
            "Theme", "Light", "Dark", "System", "Search", "Category",
            "Description", "Date", "Amount", "Workplace", "Start",
            "End", "Hours", "No data", "Monthly Report"
        )

        "ar" -> AppStrings(
            "الرئيسية", "المالية", "العمل", "التقارير", "الإعدادات",
            "الرصيد", "الدخل", "المصروف", "إضافة", "حذف", "تعديل",
            "حفظ", "إلغاء", "البطاقات البنكية", "الأشخاص", "اللغة",
            "المظهر", "فاتح", "داكن", "النظام", "بحث", "الفئة",
            "الوصف", "التاريخ", "المبلغ", "مكان العمل", "البداية",
            "النهاية", "الساعات", "لا توجد بيانات", "التقرير الشهري"
        )

        else -> AppStrings(
            "داشبورد", "مالی", "کار", "گزارش‌ها", "تنظیمات",
            "موجودی", "درآمد", "هزینه", "افزودن", "حذف", "ویرایش",
            "ذخیره", "لغو", "کارت‌های بانکی", "اشخاص", "زبان",
            "تم", "روشن", "تاریک", "سیستم", "جستجو", "دسته‌بندی",
            "توضیحات", "تاریخ", "مبلغ", "محل کار", "شروع",
            "پایان", "ساعت", "اطلاعاتی وجود ندارد", "گزارش ماهانه"
        )
    }
}

// ---------------- HELPERS ----------------

fun money(value: Long): String {
    return NumberFormat.getNumberInstance(Locale("fa", "IR")).format(value) + " تومان"
}

fun today(): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
}

fun calculateHours(start: String, end: String): Double {
    return try {
        val s = start.split(":")
        val e = end.split(":")

        val startMinutes = s[0].toInt() * 60 + s[1].toInt()
        val endMinutes = e[0].toInt() * 60 + e[1].toInt()

        var diff = endMinutes - startMinutes

        if (diff < 0) diff += 24 * 60

        diff / 60.0
    } catch (_: Exception) {
        0.0
    }
}

// ---------------- JSON ----------------

fun encodeTransactions(list: List<Transaction>): String {
    val array = JSONArray()

    list.forEach {
        array.put(
            JSONObject().apply {
                put("id", it.id)
                put("type", it.type)
                put("amount", it.amount)
                put("category", it.category)
                put("description", it.description)
                put("date", it.date)
                put("card", it.card)
                put("person", it.person)
            }
        )
    }

    return array.toString()
}

fun decodeTransactions(value: String): MutableList<Transaction> {
    val result = mutableListOf<Transaction>()

    try {
        val array = JSONArray(value)

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            result.add(
                Transaction(
                    o.getLong("id"),
                    o.getString("type"),
                    o.getLong("amount"),
                    o.getString("category"),
                    o.getString("description"),
                    o.getString("date"),
                    o.getString("card"),
                    o.getString("person")
                )
            )
        }
    } catch (_: Exception) {
    }

    return result
}

fun encodeWork(list: List<WorkDay>): String {
    val array = JSONArray()

    list.forEach {
        array.put(
            JSONObject().apply {
                put("id", it.id)
                put("place", it.place)
                put("date", it.date)
                put("start", it.start)
                put("end", it.end)
                put("income", it.income)
                put("description", it.description)
                put("person", it.person)
            }
        )
    }

    return array.toString()
}

fun decodeWork(value: String): MutableList<WorkDay> {
    val result = mutableListOf<WorkDay>()

    try {
        val array = JSONArray(value)

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            result.add(
                WorkDay(
                    o.getLong("id"),
                    o.getString("place"),
                    o.getString("date"),
                    o.getString("start"),
                    o.getString("end"),
                    o.getLong("income"),
                    o.getString("description"),
                    o.getString("person")
                )
            )
        }
    } catch (_: Exception) {
    }

    return result
}

fun encodeCards(list: List<BankCard>): String {
    val array = JSONArray()

    list.forEach {
        array.put(
            JSONObject().apply {
                put("id", it.id)
                put("bank", it.bank)
                put("name", it.name)
                put("last4", it.last4)
                put("balance", it.balance)
            }
        )
    }

    return array.toString()
}

fun decodeCards(value: String): MutableList<BankCard> {
    val result = mutableListOf<BankCard>()

    try {
        val array = JSONArray(value)

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            result.add(
                BankCard(
                    o.getLong("id"),
                    o.getString("bank"),
                    o.getString("name"),
                    o.getString("last4"),
                    o.getLong("balance")
                )
            )
        }
    } catch (_: Exception) {
    }

    return result
}

fun encodePeople(list: List<Person>): String {
    val array = JSONArray()

    list.forEach {
        array.put(
            JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("phone", it.phone)
                put("note", it.note)
            }
        )
    }

    return array.toString()
}

fun decodePeople(value: String): MutableList<Person> {
    val result = mutableListOf<Person>()

    try {
        val array = JSONArray(value)

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)

            result.add(
                Person(
                    o.getLong("id"),
                    o.getString("name"),
                    o.getString("phone"),
                    o.getString("note")
                )
            )
        }
    } catch (_: Exception) {
    }

    return result
}

// ---------------- ACTIVITY ----------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VsoftApp()
        }
    }
}

// ---------------- APP ----------------

@Composable
fun VsoftApp() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var language by remember { mutableStateOf("fa") }
    var theme by remember { mutableStateOf("system") }

    var transactions by remember {
        mutableStateOf(mutableListOf<Transaction>())
    }

    var workDays by remember {
        mutableStateOf(mutableListOf<WorkDay>())
    }

    var cards by remember {
        mutableStateOf(mutableListOf<BankCard>())
    }

    var people by remember {
        mutableStateOf(mutableListOf<Person>())
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        val preferences = context.dataStore.data.first()

        language = preferences[LANGUAGE_KEY] ?: "fa"
        theme = preferences[THEME_KEY] ?: "system"

        transactions =
            decodeTransactions(preferences[TRANSACTIONS_KEY] ?: "[]")

        workDays =
            decodeWork(preferences[WORK_KEY] ?: "[]")

        cards =
            decodeCards(preferences[CARDS_KEY] ?: "[]")

        people =
            decodePeople(preferences[PEOPLE_KEY] ?: "[]")

        loaded = true
    }

    val darkTheme = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val appStrings = strings(language)

    if (!loaded) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    val layoutDirection =
        if (language == "en")
            LayoutDirection.Ltr
        else
            LayoutDirection.Rtl

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection
    ) {

        MaterialTheme(
            colorScheme =
                if (darkTheme)
                    darkColorScheme()
                else
                    lightColorScheme(),

            shapes = Shapes(
                small = RoundedCornerShape(18.dp),
                medium = RoundedCornerShape(24.dp),
                large = RoundedCornerShape(30.dp)
            )
        ) {

            MainScreen(
                strings = appStrings,
                transactions = transactions,
                workDays = workDays,
                cards = cards,
                people = people,

                onTransactionsChange = {
                    transactions = it

                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[TRANSACTIONS_KEY] =
                                encodeTransactions(it)
                        }
                    }
                },

                onWorkChange = {
                    workDays = it

                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[WORK_KEY] =
                                encodeWork(it)
                        }
                    }
                },

                onCardsChange = {
                    cards = it

                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[CARDS_KEY] =
                                encodeCards(it)
                        }
                    }
                },

                onPeopleChange = {
                    people = it

                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[PEOPLE_KEY] =
                                encodePeople(it)
                        }
                    }
                },

                onLanguageChange = {
                    language = it

                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[LANGUAGE_KEY] = it
                        }
                    }
                },

                onThemeChange = {
                    theme = it

                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[THEME_KEY] = it
                        }
                    }
                }
            )
        }
    }
}

// ---------------- MAIN SCREEN ----------------

@Composable
fun MainScreen(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>,
    people: List<Person>,
    onTransactionsChange: (MutableList<Transaction>) -> Unit,
    onWorkChange: (MutableList<WorkDay>) -> Unit,
    onCardsChange: (MutableList<BankCard>) -> Unit,
    onPeopleChange: (MutableList<Person>) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {

    var selectedPage by remember { mutableStateOf(0) }

    val pages = listOf(
        strings.dashboard,
        strings.finance,
        strings.work,
        strings.reports,
        strings.settings
    )

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedPage == 0,
                    onClick = { selectedPage = 0 },
                    icon = {
                        Icon(Icons.Default.Home, null)
                    },
                    label = {
                        Text(strings.dashboard)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 1,
                    onClick = { selectedPage = 1 },
                    icon = {
                        Icon(Icons.Default.AccountBalanceWallet, null)
                    },
                    label = {
                        Text(strings.finance)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 2,
                    onClick = { selectedPage = 2 },
                    icon = {
                        Icon(Icons.Default.Work, null)
                    },
                    label = {
                        Text(strings.work)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 3,
                    onClick = { selectedPage = 3 },
                    icon = {
                        Icon(Icons.Default.BarChart, null)
                    },
                    label = {
                        Text(strings.reports)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 4,
                    onClick = { selectedPage = 4 },
                    icon = {
                        Icon(Icons.Default.Settings, null)
                    },
                    label = {
                        Text(strings.settings)
                    }
                )
            }
        }
    ) { padding ->

        AnimatedContent(
            targetState = selectedPage,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            label = "page"
        ) { page ->

            when (page) {

                0 -> DashboardPage(
                    strings,
                    transactions,
                    workDays,
                    cards
                )

                1 -> FinancePage(
                    strings,
                    transactions,
                    cards,
                    people,
                    onTransactionsChange
                )

                2 -> WorkPage(
                    strings,
                    workDays,
                    people,
                    onWorkChange
                )

                3 -> ReportsPage(
                    strings,
                    transactions,
                    workDays
                )

                4 -> SettingsPage(
                    strings,
                    cards,
                    people,
                    onCardsChange,
                    onPeopleChange,
                    onLanguageChange,
                    onThemeChange
                )
            }
        }
    }
}

// ---------------- DASHBOARD ----------------

@Composable
fun DashboardPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>
) {

    val income =
        transactions
            .filter { it.type == "income" }
            .sumOf { it.amount }

    val expense =
        transactions
            .filter { it.type == "expense" }
            .sumOf { it.amount }

    val workIncome =
        workDays.sumOf { it.income }

    val balance =
        income + workIncome - expense

    val totalHours =
        workDays.sumOf {
            calculateHours(it.start, it.end)
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                "Vsoft",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                strings.dashboard,
                fontSize = 16.sp
            )
        }

        item {

            InfoCard(
                title = strings.balance,
                value = money(balance),
                icon = Icons.Default.AccountBalance
            )
        }

        item {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                SmallInfoCard(
                    modifier = Modifier.weight(1f),
                    title = strings.income,
                    value = money(income + workIncome),
                    icon = Icons.Default.TrendingUp
                )

                SmallInfoCard(
                    modifier = Modifier.weight(1f),
                    title = strings.expense,
                    value = money(expense),
                    icon = Icons.Default.TrendingDown
                )
            }
        }

        item {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                SmallInfoCard(
                    modifier = Modifier.weight(1f),
                    title = strings.hours,
                    value = String.format(
                        Locale.US,
                        "%.1f ساعت",
                        totalHours
                    ),
                    icon = Icons.Default.AccessTime
                )

                SmallInfoCard(
                    modifier = Modifier.weight(1f),
                    title = strings.cards,
                    value = cards.size.toString(),
                    icon = Icons.Default.CreditCard
                )
            }
        }

        item {

            SectionTitle("آخرین تراکنش‌ها")
        }

        items(
            transactions
                .sortedByDescending { it.id }
                .take(5)
        ) {

            TransactionCard(
                transaction = it,
                onDelete = {}
            )
        }
    }
}

// ---------------- FINANCE ----------------

@Composable
fun FinancePage(
    strings: AppStrings,
    transactions: List<Transaction>,
    cards: List<BankCard>,
    people: List<Person>,
    onTransactionsChange: (MutableList<Transaction>) -> Unit
) {

    var showAdd by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val filtered =
        transactions
            .filter {
                search.isBlank() ||
                        it.description.contains(search, true) ||
                        it.category.contains(search, true) ||
                        it.person.contains(search, true)
            }
            .sortedByDescending { it.id }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                strings.finance,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = {
                    showAdd = true
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = search,
            onValueChange = {
                search = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            },
            label = {
                Text(strings.search)
            }
        )

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(strings.noData)
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(
                    filtered,
                    key = { it.id }
                ) { transaction ->

                    TransactionCard(
                        transaction = transaction,
                        onDelete = {

                            val list =
                                transactions.toMutableList()

                            list.removeAll {
                                it.id == transaction.id
                            }

                            onTransactionsChange(list)
                        }
                    )
                }
            }
        }
    }

    if (showAdd) {

        AddTransactionDialog(
            strings = strings,
            cards = cards,
            people = people,
            onDismiss = {
                showAdd = false
            },
            onSave = { transaction ->

                val list =
                    transactions.toMutableList()

                list.add(transaction)

                onTransactionsChange(list)

                showAdd = false
            }
        )
    }
}

// ---------------- TRANSACTION CARD ----------------

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                if (transaction.type == "income")
                    Icons.Default.TrendingUp
                else
                    Icons.Default.TrendingDown,
                null,
                modifier = Modifier.size(34.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    transaction.category,
                    fontWeight = FontWeight.Bold
                )

                if (transaction.description.isNotBlank()) {

                    Text(
                        transaction.description,
                        fontSize = 13.sp
                    )
                }

                Text(
                    transaction.date,
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    money(transaction.amount),
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
    }
}

// ---------------- ADD TRANSACTION ----------------

@Composable
fun AddTransactionDialog(
    strings: AppStrings,
    cards: List<BankCard>,
    people: List<Person>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {

    var type by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var card by remember { mutableStateOf("") }
    var person by remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    val value =
                        amount.toLongOrNull() ?: 0L

                    if (value > 0 && category.isNotBlank()) {

                        onSave(
                            Transaction(
                                id = System.currentTimeMillis(),
                                type = type,
                                amount = value,
                                category = category,
                                description = description,
                                date = date,
                                card = card,
                                person = person
                            )
                        )
                    }
                }
            ) {
                Text(strings.save)
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text(strings.cancel)
            }
        },

        title = {
            Text("تراکنش جدید")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row {

                    FilterChip(
                        selected = type == "expense",
                        onClick = {
                            type = "expense"
                        },
                        label = {
                            Text(strings.expense)
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    FilterChip(
                        selected = type == "income",
                        onClick = {
                            type = "income"
                        },
                        label = {
                            Text(strings.income)
                        }
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter(Char::isDigit)
                    },
                    label = {
                        Text(strings.amount)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    label = {
                        Text(strings.category)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = {
                        Text(strings.description)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                    },
                    label = {
                        Text(strings.date)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (cards.isNotEmpty()) {

                    Text("کارت: ${cards.joinToString { it.name }}")
                }

                if (people.isNotEmpty()) {

                    Text("اشخاص: ${people.joinToString { it.name }}")
                }
            }
        }
    )
}

// ---------------- WORK ----------------

@Composable
fun WorkPage(
    strings: AppStrings,
    workDays: List<WorkDay>,
    people: List<Person>,
    onWorkChange: (MutableList<WorkDay>) -> Unit
) {

    var showAdd by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                strings.work,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = {
                    showAdd = true
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(
                workDays.sortedByDescending { it.id },
                key = { it.id }
            ) { work ->

                WorkCard(
                    work = work,
                    onDelete = {

                        val list =
                            workDays.toMutableList()

                        list.removeAll {
                            it.id == work.id
                        }

                        onWorkChange(list)
                    }
                )
            }
        }
    }

    if (showAdd) {

        AddWorkDialog(
            strings = strings,
            people = people,
            onDismiss = {
                showAdd = false
            },
            onSave = {

                val list =
                    workDays.toMutableList()

                list.add(it)

                onWorkChange(list)

                showAdd = false
            }
        )
    }
}

// ---------------- WORK CARD ----------------

@Composable
fun WorkCard(
    work: WorkDay,
    onDelete: () -> Unit
) {

    val hours =
        calculateHours(work.start, work.end)

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {

            Row {

                Column(
                    Modifier.weight(1f)
                ) {

                    Text(
                        work.place,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(work.date)

                    Text(
                        "${work.start} → ${work.end}"
                    )

                    Text(
                        String.format(
                            Locale.US,
                            "%.1f ساعت",
                            hours
                        )
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                money(work.income),
                fontWeight = FontWeight.Bold
            )

            if (work.person.isNotBlank()) {
                Text(work.person)
            }

            if (work.description.isNotBlank()) {
                Text(work.description)
            }
        }
    }
}

// ---------------- ADD WORK ----------------

@Composable
fun AddWorkDialog(
    strings: AppStrings,
    people: List<Person>,
    onDismiss: () -> Unit,
    onSave: (WorkDay) -> Unit
) {

    var place by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var start by remember { mutableStateOf("08:00") }
    var end by remember { mutableStateOf("16:00") }
    var income by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var person by remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    val amount =
                        income.toLongOrNull() ?: 0L

                    if (place.isNotBlank()) {

                        onSave(
                            WorkDay(
                                id = System.currentTimeMillis(),
                                place = place,
                                date = date,
                                start = start,
                                end = end,
                                income = amount,
                                description = description,
                                person = person
                            )
                        )
                    }
                }
            ) {
                Text(strings.save)
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text(strings.cancel)
            }
        },

        title = {
            Text("روز کاری جدید")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    place,
                    { place = it },
                    label = {
                        Text(strings.place)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    date,
                    { date = it },
                    label = {
                        Text(strings.date)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    start,
                    { start = it },
                    label = {
                        Text(strings.start)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    end,
                    { end = it },
                    label = {
                        Text(strings.end)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    income,
                    {
                        income = it.filter(Char::isDigit)
                    },
                    label = {
                        Text(strings.income)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    person,
                    { person = it },
                    label = {
                        Text("شخص / کارفرما")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    description,
                    { description = it },
                    label = {
                        Text(strings.description)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// ---------------- REPORTS ----------------

@Composable
fun ReportsPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>
) {

    val income =
        transactions
            .filter { it.type == "income" }
            .sumOf { it.amount }

    val expense =
        transactions
            .filter { it.type == "expense" }
            .sumOf { it.amount }

    val workIncome =
        workDays.sumOf { it.income }

    val hours =
        workDays.sumOf {
            calculateHours(it.start, it.end)
        }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                strings.monthlyReport,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {

            InfoCard(
                strings.income,
                money(income + workIncome),
                Icons.Default.TrendingUp
            )
        }

        item {

            InfoCard(
                strings.expense,
                money(expense),
                Icons.Default.TrendingDown
            )
        }

        item {

            InfoCard(
                "درآمد کاری",
                money(workIncome),
                Icons.Default.Work
            )
        }

        item {

            InfoCard(
                strings.hours,
                String.format(
                    Locale.US,
                    "%.1f ساعت",
                    hours
                ),
                Icons.Default.AccessTime
            )
        }

        item {

            InfoCard(
                "تعداد روزهای کاری",
                workDays.size.toString(),
                Icons.Default.CalendarMonth
            )
        }

        item {

            Text(
                "دسته‌بندی هزینه‌ها",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val categories =
            transactions
                .filter { it.type == "expense" }
                .groupBy { it.category }

        items(categories.entries.toList()) { entry ->

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        entry.key,
                        Modifier.weight(1f)
                    )

                    Text(
                        money(
                            entry.value.sumOf {
                                it.amount
                            }
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ---------------- SETTINGS ----------------

@Composable
fun SettingsPage(
    strings: AppStrings,
    cards: List<BankCard>,
    people: List<Person>,
    onCardsChange: (MutableList<BankCard>) -> Unit,
    onPeopleChange: (MutableList<Person>) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {

    var showCard by remember { mutableStateOf(false) }
    var showPerson by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                strings.settings,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {

            SettingsSection(strings.language) {

                LanguageOption(
                    "فارسی",
                    "fa",
                    onLanguageChange
                )

                LanguageOption(
                    "English",
                    "en",
                    onLanguageChange
                )

                LanguageOption(
                    "العربية",
                    "ar",
                    onLanguageChange
                )
            }
        }

        item {

            SettingsSection(strings.theme) {

                ThemeOption(
                    strings.light,
                    "light",
                    onThemeChange
                )

                ThemeOption(
                    strings.dark,
                    "dark",
                    onThemeChange
                )

                ThemeOption(
                    strings.system,
                    "system",
                    onThemeChange
                )
            }
        }

        item {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    strings.cards,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showCard = true
                    }
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }

        items(
            cards,
            key = { it.id }
        ) { card ->

            CardItem(
                card = card,
                onDelete = {

                    val list =
                        cards.toMutableList()

                    list.removeAll {
                        it.id == card.id
                    }

                    onCardsChange(list)
                }
            )
        }

        item {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    strings.people,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showPerson = true
                    }
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }

        items(
            people,
            key = { it.id }
        ) { person ->

            PersonItem(
                person = person,
                onDelete = {

                    val list =
                        people.toMutableList()

                    list.removeAll {
                        it.id == person.id
                    }

                    onPeopleChange(list)
                }
            )
        }
    }

    if (showCard) {

        AddCardDialog(
            onDismiss = {
                showCard = false
            },
            onSave = {

                val list =
                    cards.toMutableList()

                list.add(it)

                onCardsChange(list)

                showCard = false
            }
        )
    }

    if (showPerson) {

        AddPersonDialog(
            onDismiss = {
                showPerson = false
            },
            onSave = {

                val list =
                    people.toMutableList()

                list.add(it)

                onPeopleChange(list)

                showPerson = false
            }
        )
    }
}

// ---------------- SETTINGS COMPONENTS ----------------

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            Modifier.padding(16.dp),
            content = {
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                content()
            }
        )
    }
}

@Composable
fun LanguageOption(
    title: String,
    value: String,
    onChange: (String) -> Unit
) {

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = false,
            onClick = {
                onChange(value)
            }
        )

        Text(title)
    }
}

@Composable
fun ThemeOption(
    title: String,
    value: String,
    onChange: (String) -> Unit
) {

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = false,
            onClick = {
                onChange(value)
            }
        )

        Text(title)
    }
}

// ---------------- CARDS ----------------

@Composable
fun CardItem(
    card: BankCard,
    onDelete: () -> Unit
) {

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp)
    ) {

        Column(
            Modifier.padding(18.dp)
        ) {

            Row {

                Column(
                    Modifier.weight(1f)
                ) {

                    Text(
                        card.bank,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(card.name)

                    Text(
                        "•••• ${card.last4}"
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        money(card.balance),
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
    }
}

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (BankCard) -> Unit
) {

    var bank by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    onSave(
                        BankCard(
                            id = System.currentTimeMillis(),
                            bank = bank,
                            name = name,
                            last4 = last4.takeLast(4),
                            balance =
                                balance.toLongOrNull() ?: 0L
                        )
                    )
                }
            ) {
                Text("ذخیره")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("لغو")
            }
        },

        title = {
            Text("کارت بانکی جدید")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    bank,
                    { bank = it },
                    label = {
                        Text("بانک")
                    }
                )

                OutlinedTextField(
                    name,
                    { name = it },
                    label = {
                        Text("نام کارت")
                    }
                )

                OutlinedTextField(
                    last4,
                    {
                        last4 =
                            it.filter(Char::isDigit)
                                .take(4)
                    },
                    label = {
                        Text("۴ رقم آخر کارت")
                    }
                )

                OutlinedTextField(
                    balance,
                    {
                        balance =
                            it.filter(Char::isDigit)
                    },
                    label = {
                        Text("موجودی")
                    }
                )
            }
        }
    )
}

// ---------------- PEOPLE ----------------

@Composable
fun PersonItem(
    person: Person,
    onDelete: () -> Unit
) {

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {

        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.Person,
                null,
                modifier = Modifier.size(35.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    person.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (person.phone.isNotBlank()) {
                    Text(person.phone)
                }

                if (person.note.isNotBlank()) {
                    Text(person.note)
                }
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    if (name.isNotBlank()) {

                        onSave(
                            Person(
                                id = System.currentTimeMillis(),
                                name = name,
                                phone = phone,
                                note = note
                            )
                        )
                    }
                }
            ) {
                Text("ذخیره")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("لغو")
            }
        },

        title = {
            Text("شخص جدید")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    name,
                    { name = it },
                    label = {
                        Text("نام")
                    }
                )

                OutlinedTextField(
                    phone,
                    { phone = it },
                    label = {
                        Text("شماره تماس")
                    }
                )

                OutlinedTextField(
                    note,
                    { note = it },
                    label = {
                        Text("توضیحات")
                    }
                )
            }
        }
    )
}

// ---------------- COMMON UI ----------------

@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {

        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(Modifier.width(15.dp))

            Column {

                Text(
                    title,
                    fontSize = 14.sp
                )

                Text(
                    value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SmallInfoCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Card(
        modifier,
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            Modifier.padding(15.dp)
        ) {

            Icon(
                icon,
                null,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(title)

            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SectionTitle(
    text: String
) {

    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}
