package com.vsoft.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

private val Context.dataStore by preferencesDataStore(
    name = "vsoft_data"
)

private val TRANSACTIONS_KEY =
    stringPreferencesKey("transactions")

private val WORK_DAYS_KEY =
    stringPreferencesKey("work_days")

private val CARDS_KEY =
    stringPreferencesKey("bank_cards")

private val PEOPLE_KEY =
    stringPreferencesKey("people")

private val LANGUAGE_KEY =
    stringPreferencesKey("language")

private val THEME_KEY =
    stringPreferencesKey("theme")

data class Transaction(
    val id: Long,
    val title: String,
    val amount: Long,
    val isIncome: Boolean
)

data class WorkDay(
    val id: Long,
    val place: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val income: Long,
    val note: String
)

data class BankCard(
    val id: Long,
    val name: String,
    val number: String,
    val bank: String
)

data class Person(
    val id: Long,
    val name: String,
    val phone: String,
    val note: String
)

data class AppStrings(
    val home: String,
    val finance: String,
    val work: String,
    val reports: String,
    val settings: String,
    val income: String,
    val expense: String,
    val balance: String,
    val add: String,
    val delete: String,
    val cancel: String,
    val save: String,
    val title: String,
    val amount: String,
    val place: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val note: String,
    val duration: String,
    val cards: String,
    val people: String,
    val language: String,
    val theme: String,
    val system: String,
    val light: String,
    val dark: String,
    val persian: String,
    val english: String,
    val arabic: String,
    val addCard: String,
    val cardName: String,
    val cardNumber: String,
    val bank: String,
    val addPerson: String,
    val personName: String,
    val phone: String,
    val noData: String,
    val totalIncome: String,
    val totalExpense: String,
    val totalWorkIncome: String,
    val workDays: String
)

fun strings(language: String): AppStrings {

    return when (language) {

        "en" -> AppStrings(
            "Home", "Finance", "Work", "Reports", "Settings",
            "Income", "Expense", "Balance", "Add", "Delete",
            "Cancel", "Save", "Title", "Amount", "Place", "Date",
            "Start time", "End time", "Note", "Duration",
            "Bank cards", "People", "Language", "Theme",
            "Follow system", "Light", "Dark",
            "Persian", "English", "Arabic",
            "Add card", "Card name", "Card number", "Bank",
            "Add person", "Person name", "Phone",
            "No data", "Total income", "Total expense",
            "Work income", "Work days"
        )

        "ar" -> AppStrings(
            "الرئيسية", "المالية", "العمل", "التقارير", "الإعدادات",
            "دخل", "مصروف", "الرصيد", "إضافة", "حذف",
            "إلغاء", "حفظ", "العنوان", "المبلغ", "المكان", "التاريخ",
            "وقت البدء", "وقت الانتهاء", "ملاحظة", "المدة",
            "البطاقات البنكية", "الأشخاص", "اللغة", "المظهر",
            "النظام", "فاتح", "داكن",
            "الفارسية", "الإنجليزية", "العربية",
            "إضافة بطاقة", "اسم البطاقة", "رقم البطاقة", "البنك",
            "إضافة شخص", "اسم الشخص", "الهاتف",
            "لا توجد بيانات", "إجمالي الدخل", "إجمالي المصروف",
            "دخل العمل", "أيام العمل"
        )

        else -> AppStrings(
            "خانه", "مالی", "کار", "گزارش", "تنظیمات",
            "درآمد", "هزینه", "موجودی", "افزودن", "حذف",
            "لغو", "ذخیره", "عنوان", "مبلغ", "محل", "تاریخ",
            "ساعت شروع", "ساعت پایان", "یادداشت", "مدت کار",
            "کارت‌های بانکی", "افراد", "زبان", "ظاهر",
            "پیروی از سیستم", "روشن", "تاریک",
            "فارسی", "English", "العربية",
            "افزودن کارت", "نام کارت", "شماره کارت", "بانک",
            "افزودن شخص", "نام شخص", "شماره تماس",
            "اطلاعاتی وجود ندارد", "مجموع درآمد", "مجموع هزینه",
            "درآمد کار", "روزهای کاری"
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VsoftApp()
        }
    }
}

@Composable
fun VsoftApp() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPage by remember {
        mutableIntStateOf(0)
    }

    var transactions by remember {
        mutableStateOf(emptyList<Transaction>())
    }

    var workDays by remember {
        mutableStateOf(emptyList<WorkDay>())
    }

    var cards by remember {
        mutableStateOf(emptyList<BankCard>())
    }

    var people by remember {
        mutableStateOf(emptyList<Person>())
    }

    var language by remember {
        mutableStateOf("fa")
    }

    var themeMode by remember {
        mutableStateOf("system")
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        val preferences =
            context.dataStore.data.first()

        transactions =
            decodeTransactions(
                preferences[TRANSACTIONS_KEY] ?: "[]"
            )

        workDays =
            decodeWorkDays(
                preferences[WORK_DAYS_KEY] ?: "[]"
            )

        cards =
            decodeCards(
                preferences[CARDS_KEY] ?: "[]"
            )

        people =
            decodePeople(
                preferences[PEOPLE_KEY] ?: "[]"
            )

        language =
            preferences[LANGUAGE_KEY] ?: "fa"

        themeMode =
            preferences[THEME_KEY] ?: "system"

        loaded = true
    }

    val text = strings(language)

    val darkTheme = when (themeMode) {

        "dark" -> true

        "light" -> false

        else -> isSystemInDarkTheme()
    }

    if (!loaded) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator()
        }

        return
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides
                if (language == "en")
                    LayoutDirection.Ltr
                else
                    LayoutDirection.Rtl
    ) {

        MaterialTheme(
            colorScheme =
                if (darkTheme)
                    darkColorScheme()
                else
                    lightColorScheme(),

            shapes = Shapes(
                small = RoundedCornerShape(14.dp),
                medium = RoundedCornerShape(20.dp),
                large = RoundedCornerShape(28.dp)
            )
        ) {

            Scaffold(

                bottomBar = {

                    NavigationBar {

                        NavigationBarItem(
                            selected = selectedPage == 0,
                            onClick = { selectedPage = 0 },
                            icon = {
                                Icon(
                                    Icons.Default.Home,
                                    null
                                )
                            },
                            label = {
                                Text(text.home)
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 1,
                            onClick = { selectedPage = 1 },
                            icon = {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    null
                                )
                            },
                            label = {
                                Text(text.finance)
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 2,
                            onClick = { selectedPage = 2 },
                            icon = {
                                Icon(
                                    Icons.Default.Work,
                                    null
                                )
                            },
                            label = {
                                Text(text.work)
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 3,
                            onClick = { selectedPage = 3 },
                            icon = {
                                Icon(
                                    Icons.Default.BarChart,
                                    null
                                )
                            },
                            label = {
                                Text(text.reports)
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 4,
                            onClick = { selectedPage = 4 },
                            icon = {
                                Icon(
                                    Icons.Default.Settings,
                                    null
                                )
                            },
                            label = {
                                Text(text.settings)
                            }
                        )
                    }
                }

            ) { paddingValues ->

                AnimatedContent(
                    targetState = selectedPage,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    label = "page_transition"
                ) { page ->

                    when (page) {

                        0 -> HomePage(
                            transactions = transactions,
                            workDays = workDays,
                            text = text
                        )

                        1 -> FinancePage(
                            transactions = transactions,
                            text = text,
                            onTransactionsChanged = {

                                transactions = it

                                scope.launch {

                                    context.dataStore.edit { preferences ->

                                        preferences[TRANSACTIONS_KEY] =
                                            encodeTransactions(it)
                                    }
                                }
                            }
                        )

                        2 -> WorkPage(
                            workDays = workDays,
                            text = text,
                            onWorkDaysChanged = {

                                workDays = it

                                scope.launch {

                                    context.dataStore.edit { preferences ->

                                        preferences[WORK_DAYS_KEY] =
                                            encodeWorkDays(it)
                                    }
                                }
                            }
                        )

                        3 -> ReportPage(
                            transactions = transactions,
                            workDays = workDays,
                            text = text
                        )

                        4 -> SettingsPage(
                            language = language,
                            themeMode = themeMode,
                            cards = cards,
                            people = people,
                            text = text,

                            onLanguageChanged = {

                                language = it

                                scope.launch {

                                    context.dataStore.edit { preferences ->

                                        preferences[LANGUAGE_KEY] = it
                                    }
                                }
                            },

                            onThemeChanged = {

                                themeMode = it

                                scope.launch {

                                    context.dataStore.edit { preferences ->

                                        preferences[THEME_KEY] = it
                                    }
                                }
                            },

                            onCardsChanged = {

                                cards = it

                                scope.launch {

                                    context.dataStore.edit { preferences ->

                                        preferences[CARDS_KEY] =
                                            encodeCards(it)
                                    }
                                }
                            },

                            onPeopleChanged = {

                                people = it

                                scope.launch {

                                    context.dataStore.edit { preferences ->

                                        preferences[PEOPLE_KEY] =
                                            encodePeople(it)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomePage(
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    text: AppStrings
) {

    val income =
        transactions
            .filter { it.isIncome }
            .sumOf { it.amount }

    val expense =
        transactions
            .filter { !it.isIncome }
            .sumOf { it.amount }

    val workIncome =
        workDays.sumOf { it.income }

    val balance =
        income - expense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text = "Vsoft",
                fontSize = 30.sp
            )
        }

        item {

            SummaryCard(
                title = text.balance,
                value = money(balance)
            )
        }

        item {

            SummaryCard(
                title = text.income,
                value = money(income)
            )
        }

        item {

            SummaryCard(
                title = text.expense,
                value = money(expense)
            )
        }

        item {

            SummaryCard(
                title = text.totalWorkIncome,
                value = money(workIncome)
            )
        }

        item {

            SummaryCard(
                title = text.workDays,
                value = workDays.size.toString()
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = title,
                fontSize = 15.sp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = value,
                fontSize = 23.sp
            )
        }
    }
}

@Composable
fun FinancePage(
    transactions: List<Transaction>,
    text: AppStrings,
    onTransactionsChanged: (List<Transaction>) -> Unit
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text.finance,
                fontSize = 27.sp
            )

            FloatingActionButton(
                onClick = {
                    showDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (transactions.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(text.noData)
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(
                    transactions,
                    key = { it.id }
                ) { transaction ->

                    Card {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    transaction.title,
                                    fontSize = 18.sp
                                )

                                Text(
                                    if (transaction.isIncome)
                                        text.income
                                    else
                                        text.expense
                                )

                                Text(
                                    money(transaction.amount)
                                )
                            }

                            IconButton(
                                onClick = {

                                    onTransactionsChanged(
                                        transactions.filter {
                                            it.id != transaction.id
                                        }
                                    )
                                }
                            ) {

                                Icon(
                                    Icons.Default.Delete,
                                    text.delete
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {

        AddTransactionDialog(
            text = text,
            onDismiss = {
                showDialog = false
            },
            onSave = { transaction ->

                onTransactionsChanged(
                    transactions + transaction
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun AddTransactionDialog(
    text: AppStrings,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var isIncome by remember {
        mutableStateOf(true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(text.add)
        },

        text = {

            Column {

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = {
                        Text(text.title)
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { c ->
                            c.isDigit()
                        }
                    },
                    label = {
                        Text(text.amount)
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = isIncome,
                        onClick = {
                            isIncome = true
                        }
                    )

                    Text(text.income)

                    RadioButton(
                        selected = !isIncome,
                        onClick = {
                            isIncome = false
                        }
                    )

                    Text(text.expense)
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val value =
                        amount.toLongOrNull()

                    if (
                        title.isNotBlank() &&
                        value != null &&
                        value > 0
                    ) {

                        onSave(
                            Transaction(
                                id = System.currentTimeMillis(),
                                title = title,
                                amount = value,
                                isIncome = isIncome
                            )
                        )
                    }
                }
            ) {

                Text(text.save)
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(text.cancel)
            }
        }
    )
}

@Composable
fun WorkPage(
    workDays: List<WorkDay>,
    text: AppStrings,
    onWorkDaysChanged: (List<WorkDay>) -> Unit
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text.work,
                fontSize = 27.sp
            )

            FloatingActionButton(
                onClick = {
                    showDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (workDays.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(text.noData)
            }

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    workDays,
                    key = { it.id }
                ) { day ->

                    Card {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                day.place,
                                fontSize = 19.sp
                            )

                            Text(day.date)

                            Text(
                                "${day.startTime} → ${day.endTime}"
                            )

                            Text(
                                "${text.duration}: ${
                                    calculateWorkDuration(
                                        day.startTime,
                                        day.endTime
                                    )
                                }"
                            )

                            Text(
                                money(day.income)
                            )

                            if (day.note.isNotBlank()) {

                                Text(day.note)
                            }

                            IconButton(
                                onClick = {

                                    onWorkDaysChanged(
                                        workDays.filter {
                                            it.id != day.id
                                        }
                                    )
                                }
                            ) {

                                Icon(
                                    Icons.Default.Delete,
                                    text.delete
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {

        AddWorkDayDialog(
            text = text,
            onDismiss = {
                showDialog = false
            },
            onSave = { day ->

                onWorkDaysChanged(
                    workDays + day
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun AddWorkDayDialog(
    text: AppStrings,
    onDismiss: () -> Unit,
    onSave: (WorkDay) -> Unit
) {

    var place by remember {
        mutableStateOf("")
    }

    var date by remember {
        mutableStateOf("")
    }

    var start by remember {
        mutableStateOf("")
    }

    var end by remember {
        mutableStateOf("")
    }

    var income by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(text.add)
        },

        text = {

            LazyColumn {

                item {

                    OutlinedTextField(
                        value = place,
                        onValueChange = {
                            place = it
                        },
                        label = {
                            Text(text.place)
                        }
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = {
                            date = it
                        },
                        label = {
                            Text(text.date)
                        }
                    )

                    OutlinedTextField(
                        value = start,
                        onValueChange = {
                            start = it
                        },
                        label = {
                            Text(text.startTime)
                        }
                    )

                    OutlinedTextField(
                        value = end,
                        onValueChange = {
                            end = it
                        },
                        label = {
                            Text(text.endTime)
                        }
                    )

                    OutlinedTextField(
                        value = income,
                        onValueChange = {
                            income = it.filter { c ->
                                c.isDigit()
                            }
                        },
                        label = {
                            Text(text.amount)
                        }
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                        },
                        label = {
                            Text(text.note)
                        }
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val value =
                        income.toLongOrNull() ?: 0

                    if (place.isNotBlank()) {

                        onSave(
                            WorkDay(
                                id = System.currentTimeMillis(),
                                place = place,
                                date = date,
                                startTime = start,
                                endTime = end,
                                income = value,
                                note = note
                            )
                        )
                    }
                }
            ) {

                Text(text.save)
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(text.cancel)
            }
        }
    )
}

@Composable
fun ReportPage(
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    text: AppStrings
) {

    val income =
        transactions
            .filter { it.isIncome }
            .sumOf { it.amount }

    val expense =
        transactions
            .filter { !it.isIncome }
            .sumOf { it.amount }

    val workIncome =
        workDays.sumOf { it.income }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                text.reports,
                fontSize = 28.sp
            )
        }

        item {

            SummaryCard(
                text.totalIncome,
                money(income)
            )
        }

        item {

            SummaryCard(
                text.totalExpense,
                money(expense)
            )
        }

        item {

            SummaryCard(
                text.totalWorkIncome,
                money(workIncome)
            )
        }

        item {

            SummaryCard(
                text.balance,
                money(income - expense)
            )
        }
    }
}

@Composable
fun SettingsPage(
    language: String,
    themeMode: String,
    cards: List<BankCard>,
    people: List<Person>,
    text: AppStrings,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    onCardsChanged: (List<BankCard>) -> Unit,
    onPeopleChanged: (List<Person>) -> Unit
) {

    var showCardDialog by remember {
        mutableStateOf(false)
    }

    var showPersonDialog by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text.settings,
                fontSize = 29.sp
            )
        }

        item {

            Card {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text.language,
                        fontSize = 19.sp
                    )

                    LanguageOption(
                        text.persian,
                        language == "fa"
                    ) {
                        onLanguageChanged("fa")
                    }

                    LanguageOption(
                        text.english,
                        language == "en"
                    ) {
                        onLanguageChanged("en")
                    }

                    LanguageOption(
                        text.arabic,
                        language == "ar"
                    ) {
                        onLanguageChanged("ar")
                    }
                }
            }
        }

        item {

            Card {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text.theme,
                        fontSize = 19.sp
                    )

                    LanguageOption(
                        text.system,
                        themeMode == "system"
                    ) {
                        onThemeChanged("system")
                    }

                    LanguageOption(
                        text.light,
                        themeMode == "light"
                    ) {
                        onThemeChanged("light")
                    }

                    LanguageOption(
                        text.dark,
                        themeMode == "dark"
                    ) {
                        onThemeChanged("dark")
                    }
                }
            }
        }

        item {

            Card {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text.cards,
                            fontSize = 19.sp
                        )

                        IconButton(
                            onClick = {
                                showCardDialog = true
                            }
                        ) {

                            Icon(
                                Icons.Default.Add,
                                null
                            )
                        }
                    }

                    if (cards.isEmpty()) {

                        Text(text.noData)

                    } else {

                        cards.forEach { card ->

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 7.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(card.name)

                                    Text(
                                        "${card.bank} • ${card.number}"
                                    )
                                }

                                IconButton(
                                    onClick = {

                                        onCardsChanged(
                                            cards.filter {
                                                it.id != card.id
                                            }
                                        )
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Delete,
                                        null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {

            Card {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text.people,
                            fontSize = 19.sp
                        )

                        IconButton(
                            onClick = {
                                showPersonDialog = true
                            }
                        ) {

                            Icon(
                                Icons.Default.Add,
                                null
                            )
                        }
                    }

                    if (people.isEmpty()) {

                        Text(text.noData)

                    } else {

                        people.forEach { person ->

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 7.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(person.name)

                                    if (person.phone.isNotBlank()) {
                                        Text(person.phone)
                                    }

                                    if (person.note.isNotBlank()) {
                                        Text(person.note)
                                    }
                                }

                                IconButton(
                                    onClick = {

                                        onPeopleChanged(
                                            people.filter {
                                                it.id != person.id
                                            }
                                        )
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Delete,
                                        null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCardDialog) {

        AddCardDialog(
            text = text,
            onDismiss = {
                showCardDialog = false
            },
            onSave = {

                onCardsChanged(
                    cards + it
                )

                showCardDialog = false
            }
        )
    }

    if (showPersonDialog) {

        AddPersonDialog(
            text = text,
            onDismiss = {
                showPersonDialog = false
            },
            onSave = {

                onPeopleChanged(
                    people + it
                )

                showPersonDialog = false
            }
        )
    }
}

@Composable
fun LanguageOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun AddCardDialog(
    text: AppStrings,
    onDismiss: () -> Unit,
    onSave: (BankCard) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var number by remember {
        mutableStateOf("")
    }

    var bank by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(text.addCard)
        },

        text = {

            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text(text.cardName)
                    }
                )

                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it.filter { c ->
                            c.isDigit()
                        }
                    },
                    label = {
                        Text(text.cardNumber)
                    }
                )

                OutlinedTextField(
                    value = bank,
                    onValueChange = {
                        bank = it
                    },
                    label = {
                        Text(text.bank)
                    }
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    if (
                        name.isNotBlank() ||
                        number.isNotBlank()
                    ) {

                        onSave(
                            BankCard(
                                id = System.currentTimeMillis(),
                                name = name,
                                number = number,
                                bank = bank
                            )
                        )
                    }
                }
            ) {

                Text(text.save)
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(text.cancel)
            }
        }
    )
}

@Composable
fun AddPersonDialog(
    text: AppStrings,
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(text.addPerson)
        },

        text = {

            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text(text.personName)
                    }
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                    },
                    label = {
                        Text(text.phone)
                    }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = {
                        Text(text.note)
                    }
                )
            }
        },

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

                Text(text.save)
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(text.cancel)
            }
        }
    )
}

fun encodeTransactions(
    list: List<Transaction>
): String {

    val array = JSONArray()

    list.forEach {

        array.put(
            JSONObject().apply {

                put("id", it.id)
                put("title", it.title)
                put("amount", it.amount)
                put("income", it.isIncome)
            }
        )
    }

    return array.toString()
}

fun decodeTransactions(
    json: String
): List<Transaction> {

    val array = JSONArray(json)

    return List(array.length()) { index ->

        val item = array.getJSONObject(index)

        Transaction(
            item.getLong("id"),
            item.getString("title"),
            item.getLong("amount"),
            item.getBoolean("income")
        )
    }
}

fun encodeWorkDays(
    list: List<WorkDay>
): String {

    val array = JSONArray()

    list.forEach {

        array.put(
            JSONObject().apply {

                put("id", it.id)
                put("place", it.place)
                put("date", it.date)
                put("start", it.startTime)
                put("end", it.endTime)
                put("income", it.income)
                put("note", it.note)
            }
        )
    }

    return array.toString()
}

fun decodeWorkDays(
    json: String
): List<WorkDay> {

    val array = JSONArray(json)

    return List(array.length()) { index ->

        val item = array.getJSONObject(index)

        WorkDay(
            item.getLong("id"),
            item.getString("place"),
            item.getString("date"),
            item.getString("start"),
            item.getString("end"),
            item.getLong("income"),
            item.getString("note")
        )
    }
}

fun encodeCards(
    list: List<BankCard>
): String {

    val array = JSONArray()

    list.forEach {

        array.put(
            JSONObject().apply {

                put("id", it.id)
                put("name", it.name)
                put("number", it.number)
                put("bank", it.bank)
            }
        )
    }

    return array.toString()
}

fun decodeCards(
    json: String
): List<BankCard> {

    val array = JSONArray(json)

    return List(array.length()) { index ->

        val item = array.getJSONObject(index)

        BankCard(
            item.getLong("id"),
            item.getString("name"),
            item.getString("number"),
            item.getString("bank")
        )
    }
}

fun encodePeople(
    list: List<Person>
): String {

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

fun decodePeople(
    json: String
): List<Person> {

    val array = JSONArray(json)

    return List(array.length()) { index ->

        val item = array.getJSONObject(index)

        Person(
            item.getLong("id"),
            item.getString("name"),
            item.getString("phone"),
            item.getString("note")
        )
    }
}

fun calculateWorkDuration(
    startTime: String,
    endTime: String
): String {

    return try {

        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        if (
            startParts.size != 2 ||
            endParts.size != 2
        ) {
            return ""
        }

        val startHour =
            startParts[0].toInt()

        val startMinute =
            startParts[1].toInt()

        val endHour =
            endParts[0].toInt()

        val endMinute =
            endParts[1].toInt()

        if (
            startHour !in 0..23 ||
            endHour !in 0..23 ||
            startMinute !in 0..59 ||
            endMinute !in 0..59
        ) {
            return ""
        }

        val startTotal =
            startHour * 60 + startMinute

        var endTotal =
            endHour * 60 + endMinute

        if (endTotal < startTotal) {
            endTotal += 24 * 60
        }

        val duration =
            endTotal - startTotal

        val hours =
            duration / 60

        val minutes =
            duration % 60

        when {

            hours > 0 && minutes > 0 ->
                "$hours ساعت و $minutes دقیقه"

            hours > 0 ->
                "$hours ساعت"

            minutes > 0 ->
                "$minutes دقیقه"

            else ->
                "۰ دقیقه"
        }

    } catch (e: Exception) {

        ""
    }
}

fun money(
    amount: Long
): String {

    return NumberFormat
        .getNumberInstance(
            Locale("fa", "IR")
        )
        .format(amount) + " تومان"
}
