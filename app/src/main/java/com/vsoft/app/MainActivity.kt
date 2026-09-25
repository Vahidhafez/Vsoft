package com.vsoft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val android.content.Context.dataStore by preferencesDataStore("vsoft_data")

private val TRANSACTIONS_KEY = stringPreferencesKey("transactions")
private val WORK_KEY = stringPreferencesKey("work_days")
private val CARDS_KEY = stringPreferencesKey("cards")
private val PEOPLE_KEY = stringPreferencesKey("people")
private val CATEGORIES_KEY = stringPreferencesKey("categories")
private val LANGUAGE_KEY = stringPreferencesKey("language")
private val THEME_KEY = stringPreferencesKey("theme")
private val CURRENCY_KEY = stringPreferencesKey("currency")
private val PIN_KEY = stringPreferencesKey("pin")
private val LOCK_KEY = stringPreferencesKey("lock")

// ============================================================
// MODELS
// ============================================================

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

// ============================================================
// STRINGS
// ============================================================

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
    val monthlyReport: String,
    val backup: String,
    val restore: String,
    val lock: String,
    val currency: String
)

fun strings(language: String): AppStrings {

    return when (language) {

        "en" -> AppStrings(
            "Dashboard",
            "Finance",
            "Work",
            "Reports",
            "Settings",
            "Balance",
            "Income",
            "Expense",
            "Add",
            "Delete",
            "Edit",
            "Save",
            "Cancel",
            "Bank Cards",
            "People",
            "Language",
            "Theme",
            "Light",
            "Dark",
            "System",
            "Search",
            "Category",
            "Description",
            "Date",
            "Amount",
            "Workplace",
            "Start",
            "End",
            "Hours",
            "No data",
            "Monthly Report",
            "Backup",
            "Restore",
            "App Lock",
            "Currency"
        )

        "ar" -> AppStrings(
            "الرئيسية",
            "المالية",
            "العمل",
            "التقارير",
            "الإعدادات",
            "الرصيد",
            "الدخل",
            "المصروف",
            "إضافة",
            "حذف",
            "تعديل",
            "حفظ",
            "إلغاء",
            "البطاقات",
            "الأشخاص",
            "اللغة",
            "المظهر",
            "فاتح",
            "داكن",
            "النظام",
            "بحث",
            "الفئة",
            "الوصف",
            "التاريخ",
            "المبلغ",
            "مكان العمل",
            "البداية",
            "النهاية",
            "الساعات",
            "لا توجد بيانات",
            "التقرير الشهري",
            "نسخ احتياطي",
            "استعادة",
            "قفل التطبيق",
            "العملة"
        )

        else -> AppStrings(
            "داشبورد",
            "مالی",
            "کار",
            "گزارش‌ها",
            "تنظیمات",
            "موجودی",
            "درآمد",
            "هزینه",
            "افزودن",
            "حذف",
            "ویرایش",
            "ذخیره",
            "لغو",
            "کارت‌های بانکی",
            "اشخاص",
            "زبان",
            "تم",
            "روشن",
            "تاریک",
            "سیستم",
            "جستجو",
            "دسته‌بندی",
            "توضیحات",
            "تاریخ",
            "مبلغ",
            "محل کار",
            "شروع",
            "پایان",
            "ساعت",
            "اطلاعاتی وجود ندارد",
            "گزارش ماهانه",
            "پشتیبان‌گیری",
            "بازیابی",
            "قفل برنامه",
            "واحد پول"
        )
    }
}

// ============================================================
// HELPERS
// ============================================================

fun money(
    value: Long,
    currency: String
): String {

    return NumberFormat
        .getNumberInstance(Locale("fa", "IR"))
        .format(value) + " $currency"
}

fun today(): String {

    return SimpleDateFormat(
        "yyyy/MM/dd",
        Locale.US
    ).format(Date())
}

fun calculateHours(
    start: String,
    end: String
): Double {

    return try {

        val s = start.split(":")
        val e = end.split(":")

        val startMinutes =
            s[0].toInt() * 60 + s[1].toInt()

        val endMinutes =
            e[0].toInt() * 60 + e[1].toInt()

        var difference =
            endMinutes - startMinutes

        if (difference < 0) {
            difference += 24 * 60
        }

        difference / 60.0

    } catch (_: Exception) {

        0.0
    }
}

fun monthOf(date: String): String {

    return if (date.length >= 7)
        date.substring(0, 7)
    else
        date
}

// ============================================================
// JSON - TRANSACTIONS
// ============================================================

fun encodeTransactions(
    list: List<Transaction>
): String {

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

fun decodeTransactions(
    value: String
): MutableList<Transaction> {

    val result = mutableListOf<Transaction>()

    try {

        val array = JSONArray(value)

        for (i in 0 until array.length()) {

            val o =
                array.getJSONObject(i)

            result.add(
                Transaction(
                    id = o.optLong("id"),
                    type = o.optString("type"),
                    amount = o.optLong("amount"),
                    category = o.optString("category"),
                    description = o.optString("description"),
                    date = o.optString("date"),
                    card = o.optString("card"),
                    person = o.optString("person")
                )
            )
        }

    } catch (_: Exception) {
    }

    return result
}

// ============================================================
// JSON - WORK
// ============================================================

fun encodeWork(
    list: List<WorkDay>
): String {

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

fun decodeWork(
    value: String
): MutableList<WorkDay> {

    val result = mutableListOf<WorkDay>()

    try {

        val array = JSONArray(value)

        for (i in 0 until array.length()) {

            val o =
                array.getJSONObject(i)

            result.add(
                WorkDay(
                    id = o.optLong("id"),
                    place = o.optString("place"),
                    date = o.optString("date"),
                    start = o.optString("start"),
                    end = o.optString("end"),
                    income = o.optLong("income"),
                    description = o.optString("description"),
                    person = o.optString("person")
                )
            )
        }

    } catch (_: Exception) {
    }

    return result
}

// ============================================================
// JSON - CARDS
// ============================================================

fun encodeCards(
    list: List<BankCard>
): String {

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

fun decodeCards(
    value: String
): MutableList<BankCard> {

    val result = mutableListOf<BankCard>()

    try {

        val array = JSONArray(value)

        for (i in 0 until array.length()) {

            val o =
                array.getJSONObject(i)

            result.add(
                BankCard(
                    id = o.optLong("id"),
                    bank = o.optString("bank"),
                    name = o.optString("name"),
                    last4 = o.optString("last4"),
                    balance = o.optLong("balance")
                )
            )
        }

    } catch (_: Exception) {
    }

    return result
}

// ============================================================
// JSON - PEOPLE
// ============================================================

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
    value: String
): MutableList<Person> {

    val result = mutableListOf<Person>()

    try {

        val array = JSONArray(value)

        for (i in 0 until array.length()) {

            val o =
                array.getJSONObject(i)

            result.add(
                Person(
                    id = o.optLong("id"),
                    name = o.optString("name"),
                    phone = o.optString("phone"),
                    note = o.optString("note")
                )
            )
        }

    } catch (_: Exception) {
    }

    return result
}

// ============================================================
// JSON - CATEGORIES
// ============================================================

fun encodeCategories(
    list: List<String>
): String {

    return JSONArray(list).toString()
}

fun decodeCategories(
    value: String
): MutableList<String> {

    val result = mutableListOf<String>()

    try {

        val array = JSONArray(value)

        for (i in 0 until array.length()) {

            result.add(
                array.getString(i)
            )
        }

    } catch (_: Exception) {
    }

    return result
}

// ============================================================
// ACTIVITY
// ============================================================

class MainActivity :
    ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContent {

            VsoftApp()
        }
    }
}

// ============================================================
// APP
// ============================================================

@Composable
fun VsoftApp() {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var language by
        remember { mutableStateOf("fa") }

    var theme by
        remember { mutableStateOf("system") }

    var currency by
        remember { mutableStateOf("تومان") }

    var lockEnabled by
        remember { mutableStateOf(false) }

    var pin by
        remember { mutableStateOf("") }

    var transactions by
        remember {
            mutableStateOf(
                mutableListOf<Transaction>()
            )
        }

    var workDays by
        remember {
            mutableStateOf(
                mutableListOf<WorkDay>()
            )
        }

    var cards by
        remember {
            mutableStateOf(
                mutableListOf<BankCard>()
            )
        }

    var people by
        remember {
            mutableStateOf(
                mutableListOf<Person>()
            )
        }

    var categories by
        remember {
            mutableStateOf(
                mutableListOf(
                    "خوراک",
                    "حمل‌ونقل",
                    "خرید",
                    "قبض",
                    "تفریح",
                    "حقوق",
                    "سایر"
                )
            )
        }

    var loaded by
        remember { mutableStateOf(false) }

    var unlocked by
        remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        val preferences =
            context.dataStore.data.first()

        language =
            preferences[LANGUAGE_KEY] ?: "fa"

        theme =
            preferences[THEME_KEY] ?: "system"

        currency =
            preferences[CURRENCY_KEY] ?: "تومان"

        pin =
            preferences[PIN_KEY] ?: ""

        lockEnabled =
            preferences[LOCK_KEY] == "1"

        transactions =
            decodeTransactions(
                preferences[TRANSACTIONS_KEY] ?: "[]"
            )

        workDays =
            decodeWork(
                preferences[WORK_KEY] ?: "[]"
            )

        cards =
            decodeCards(
                preferences[CARDS_KEY] ?: "[]"
            )

        people =
            decodePeople(
                preferences[PEOPLE_KEY] ?: "[]"
            )

        categories =
            decodeCategories(
                preferences[CATEGORIES_KEY]
                    ?: encodeCategories(categories)
            )

        loaded = true

        unlocked =
            !lockEnabled || pin.isBlank()
    }

    if (!loaded) {

        Box(
            Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            CircularProgressIndicator()
        }

        return
    }

    if (
        lockEnabled &&
        pin.isNotBlank() &&
        !unlocked
    ) {

        LockScreen(pin) {

            unlocked = true
        }

        return
    }

    val darkTheme =
        when (theme) {

            "dark" -> true

            "light" -> false

            else ->
                isSystemInDarkTheme()
        }

    val direction =
        if (language == "en")
            LayoutDirection.Ltr
        else
            LayoutDirection.Rtl

    CompositionLocalProvider(
        LocalLayoutDirection provides direction
    ) {

        MaterialTheme(

            colorScheme =
                if (darkTheme)
                    darkColorScheme()
                else
                    lightColorScheme(),

            shapes =
                Shapes(
                    small =
                        RoundedCornerShape(16.dp),

                    medium =
                        RoundedCornerShape(22.dp),

                    large =
                        RoundedCornerShape(28.dp)
                )
        ) {

            MainScreen(

                strings =
                    strings(language),

                transactions =
                    transactions,

                workDays =
                    workDays,

                cards =
                    cards,

                people =
                    people,

                categories =
                    categories,

                currency =
                    currency,

                onTransactionsChange = {

                    transactions = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                TRANSACTIONS_KEY
                            ] =
                                encodeTransactions(it)
                        }
                    }
                },

                onWorkChange = {

                    workDays = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                WORK_KEY
                            ] =
                                encodeWork(it)
                        }
                    }
                },

                onCardsChange = {

                    cards = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                CARDS_KEY
                            ] =
                                encodeCards(it)
                        }
                    }
                },

                onPeopleChange = {

                    people = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                PEOPLE_KEY
                            ] =
                                encodePeople(it)
                        }
                    }
                },

                onCategoriesChange = {

                    categories = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                CATEGORIES_KEY
                            ] =
                                encodeCategories(it)
                        }
                    }
                },

                onLanguageChange = {

                    language = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                LANGUAGE_KEY
                            ] = it
                        }
                    }
                },

                onThemeChange = {

                    theme = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                THEME_KEY
                            ] = it
                        }
                    }
                },

                onCurrencyChange = {

                    currency = it

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[
                                CURRENCY_KEY
                            ] = it
                        }
                    }
                },

                onLockChange = { enabled, newPin ->

                    lockEnabled = enabled
                    pin = newPin

                    scope.launch {

                        context.dataStore.edit { prefs ->

                            prefs[LOCK_KEY] =
                                if (enabled)
                                    "1"
                                else
                                    "0"

                            prefs[PIN_KEY] =
                                newPin
                        }
                    }
                }
            )
        }
    }
}

// ============================================================
// LOCK SCREEN
// ============================================================

@Composable
fun LockScreen(
    pin: String,
    onUnlock: () -> Unit
) {

    var entered by
        remember { mutableStateOf("") }

    var error by
        remember { mutableStateOf(false) }

    Column(

        Modifier
            .fillMaxSize()
            .padding(28.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Icon(
            Icons.Default.Lock,
            null,
            Modifier.size(65.dp)
        )

        Spacer(
            Modifier.height(18.dp)
        )

        Text(
            "مدیریت مالی وحیدینیا",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "برای ورود رمز برنامه را وارد کنید"
        )

        Spacer(
            Modifier.height(20.dp)
        )

        OutlinedTextField(

            value = entered,

            onValueChange = {

                entered =
                    it
                        .filter(Char::isDigit)
                        .take(8)
            },

            label = {
                Text("رمز")
            },

            singleLine = true,

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.NumberPassword
                )
        )

        Spacer(
            Modifier.height(12.dp)
        )

        Button(

            onClick = {

                if (entered == pin) {

                    onUnlock()

                } else {

                    error = true
                    entered = ""
                }
            }
        ) {

            Text("ورود")
        }

        if (error) {

            Text(
                "رمز اشتباه است",
                color =
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

// ============================================================
// MAIN SCREEN
// ============================================================

@Composable
fun MainScreen(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>,
    people: List<Person>,
    categories: List<String>,
    currency: String,

    onTransactionsChange:
        (MutableList<Transaction>) -> Unit,

    onWorkChange:
        (MutableList<WorkDay>) -> Unit,

    onCardsChange:
        (MutableList<BankCard>) -> Unit,

    onPeopleChange:
        (MutableList<Person>) -> Unit,

    onCategoriesChange:
        (MutableList<String>) -> Unit,

    onLanguageChange:
        (String) -> Unit,

    onThemeChange:
        (String) -> Unit,

    onCurrencyChange:
        (String) -> Unit,

    onLockChange:
        (Boolean, String) -> Unit
) {

    var selectedPage by
        remember { mutableStateOf(0) }

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected =
                        selectedPage == 0,

                    onClick = {
                        selectedPage = 0
                    },

                    icon = {
                        Icon(
                            Icons.Default.Home,
                            null
                        )
                    },

                    label = {
                        Text(strings.dashboard)
                    }
                )

                NavigationBarItem(
                    selected =
                        selectedPage == 1,

                    onClick = {
                        selectedPage = 1
                    },

                    icon = {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            null
                        )
                    },

                    label = {
                        Text(strings.finance)
                    }
                )

                NavigationBarItem(
                    selected =
                        selectedPage == 2,

                    onClick = {
                        selectedPage = 2
                    },

                    icon = {
                        Icon(
                            Icons.Default.Work,
                            null
                        )
                    },

                    label = {
                        Text(strings.work)
                    }
                )

                NavigationBarItem(
                    selected =
                        selectedPage == 3,

                    onClick = {
                        selectedPage = 3
                    },

                    icon = {
                        Icon(
                            Icons.Default.BarChart,
                            null
                        )
                    },

                    label = {
                        Text(strings.reports)
                    }
                )

                NavigationBarItem(
                    selected =
                        selectedPage == 4,

                    onClick = {
                        selectedPage = 4
                    },

                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            null
                        )
                    },

                    label = {
                        Text(strings.settings)
                    }
                )
            }
        }

    ) { padding ->

        AnimatedContent(

            targetState =
                selectedPage,

            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },

            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),

            label = "page"
        ) { page ->

            when (page) {

                0 -> DashboardPage(
                    strings,
                    transactions,
                    workDays,
                    cards,
                    currency
                )

                1 -> FinancePage(
                    strings,
                    transactions,
                    cards,
                    people,
                    categories,
                    currency,
                    onTransactionsChange
                )

                2 -> WorkPage(
                    strings,
                    workDays,
                    people,
                    currency,
                    onWorkChange
                )

                3 -> ReportsPage(
                    strings,
                    transactions,
                    workDays,
                    currency
                )

                4 -> SettingsPage(
                    strings,
                    cards,
                    people,
                    categories,
                    currency,
                    onCardsChange,
                    onPeopleChange,
                    onCategoriesChange,
                    onLanguageChange,
                    onThemeChange,
                    onCurrencyChange,
                    onLockChange
                )
            }
        }
    }
}

// ============================================================
// DASHBOARD
// ============================================================

@Composable
fun DashboardPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>,
    currency: String
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
        income +
                workIncome -
                expense

    val totalHours =
        workDays.sumOf {
            calculateHours(
                it.start,
                it.end
            )
        }

    LazyColumn(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                "VSoft",
                fontSize = 31.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "مدیریت مالی وحیدینیا"
            )
        }

        item {

            InfoCard(
                strings.balance,
                money(
                    balance,
                    currency
                ),
                Icons.Default.AccountBalance
            )
        }

        item {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                SmallInfoCard(
                    Modifier.weight(1f),
                    strings.income,
                    money(
                        income + workIncome,
                        currency
                    ),
                    Icons.Default.TrendingUp
                )

                SmallInfoCard(
                    Modifier.weight(1f),
                    strings.expense,
                    money(
                        expense,
                        currency
                    ),
                    Icons.Default.TrendingDown
                )
            }
        }

        item {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                SmallInfoCard(
                    Modifier.weight(1f),
                    strings.hours,
                    "%.1f ساعت".format(
                        Locale.US,
                        totalHours
                    ),
                    Icons.Default.AccessTime
                )

                SmallInfoCard(
                    Modifier.weight(1f),
                    strings.cards,
                    cards.size.toString(),
                    Icons.Default.CreditCard
                )
            }
        }

        item {

            SectionTitle(
                "آخرین تراکنش‌ها"
            )
        }

        items(
            transactions
                .sortedByDescending {
                    it.id
                }
                .take(5)
        ) {

            TransactionCard(
                transaction = it,
                currency = currency,
                onDelete = {}
            )
        }
    }
}

// ============================================================
// FINANCE
// ============================================================

@Composable
fun FinancePage(
    strings: AppStrings,
    transactions: List<Transaction>,
    cards: List<BankCard>,
    people: List<Person>,
    categories: List<String>,
    currency: String,
    onTransactionsChange:
        (MutableList<Transaction>) -> Unit
) {

    var search by
        remember { mutableStateOf("") }

    var showAdd by
        remember { mutableStateOf(false) }

    var editing by
        remember {
            mutableStateOf<Transaction?>(null)
        }

    val filtered =
        transactions
            .filter {

                search.isBlank() ||

                        it.description
                            .contains(
                                search,
                                true
                            ) ||

                        it.category
                            .contains(
                                search,
                                true
                            ) ||

                        it.person
                            .contains(
                                search,
                                true
                            ) ||

                        it.card
                            .contains(
                                search,
                                true
                            )
            }
            .sortedByDescending {
                it.id
            }

    Column(

        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                strings.finance,
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = {
                    showAdd = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )
            }
        }

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedTextField(

            value = search,

            onValueChange = {
                search = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    null
                )
            },

            label = {
                Text(strings.search)
            }
        )

        Spacer(
            Modifier.height(10.dp)
        )

        if (filtered.isEmpty()) {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(strings.noData)
            }

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    filtered,
                    key = { it.id }
                ) { transaction ->

                    TransactionCard(

                        transaction =
                            transaction,

                        currency =
                            currency,

                        onDelete = {

                            val list =
                                transactions
                                    .toMutableList()

                            list.removeAll {
                                it.id ==
                                        transaction.id
                            }

                            onTransactionsChange(
                                list
                            )
                        },

                        onEdit = {

                            editing =
                                transaction
                        }
                    )
                }
            }
        }
    }

    if (
        showAdd ||
        editing != null
    ) {

        TransactionDialog(

            strings =
                strings,

            cards =
                cards,

            people =
                people,

            categories =
                categories,

            existing =
                editing,

            onDismiss = {

                showAdd = false
                editing = null
            },

            onSave = { transaction ->

                val list =
                    transactions
                        .toMutableList()

                list.removeAll {
                    it.id ==
                            transaction.id
                }

                list.add(transaction)

                onTransactionsChange(
                    list
                )

                showAdd = false
                editing = null
            }
        )
    }
}

// ============================================================
// TRANSACTION CARD
// ============================================================

@Composable
fun TransactionCard(
    transaction: Transaction,
    currency: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {

    Card(
        Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(23.dp)
    ) {

        Row(

            Modifier
                .fillMaxWidth()
                .padding(15.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(

                if (
                    transaction.type ==
                    "income"
                )
                    Icons.Default.TrendingUp
                else
                    Icons.Default.TrendingDown,

                null,

                Modifier.size(34.dp)
            )

            Spacer(
                Modifier.width(10.dp)
            )

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    transaction.category,
                    fontWeight =
                        FontWeight.Bold
                )

                if (
                    transaction.description
                        .isNotBlank()
                ) {

                    Text(
                        transaction.description,
                        fontSize = 13.sp
                    )
                }

                Text(
                    transaction.date,
                    fontSize = 12.sp
                )

                if (
                    transaction.person
                        .isNotBlank()
                ) {

                    Text(
                        "👤 ${transaction.person}",
                        fontSize = 12.sp
                    )
                }

                if (
                    transaction.card
                        .isNotBlank()
                ) {

                    Text(
                        "💳 ${transaction.card}",
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    money(
                        transaction.amount,
                        currency
                    ),
                    fontWeight =
                        FontWeight.Bold
                )

                Row {

                    IconButton(
                        onClick = onEdit
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            null
                        )
                    }

                    IconButton(
                        onClick = onDelete
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

// ============================================================
// TRANSACTION DIALOG
// ============================================================

@Composable
fun TransactionDialog(
    strings: AppStrings,
    cards: List<BankCard>,
    people: List<Person>,
    categories: List<String>,
    existing: Transaction?,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {

    var type by
        remember {
            mutableStateOf(
                existing?.type ?: "expense"
            )
        }

    var amount by
        remember {
            mutableStateOf(
                existing?.amount
                    ?.toString()
                    ?: ""
            )
        }

    var category by
        remember {
            mutableStateOf(
                existing?.category ?: ""
            )
        }

    var description by
        remember {
            mutableStateOf(
                existing?.description ?: ""
            )
        }

    var date by
        remember {
            mutableStateOf(
                existing?.date
                    ?: today()
            )
        }

    var card by
        remember {
            mutableStateOf(
                existing?.card ?: ""
            )
        }

    var person by
        remember {
            mutableStateOf(
                existing?.person ?: ""
            )
        }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                if (existing == null)
                    "تراکنش جدید"
                else
                    "ویرایش تراکنش"
            )
        },

        confirmButton = {

            TextButton(

                onClick = {

                    val value =
                        amount.toLongOrNull()
                            ?: 0L

                    if (
                        value > 0 &&
                        category.isNotBlank()
                    ) {

                        onSave(

                            Transaction(

                                id =
                                    existing?.id
                                        ?: System.currentTimeMillis(),

                                type = type,

                                amount = value,

                                category =
                                    category,

                                description =
                                    description,

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

        text = {

            LazyColumn(

                modifier =
                    Modifier.heightIn(
                        max = 520.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                item {

                    Row {

                        FilterChip(

                            selected =
                                type ==
                                    "expense",

                            onClick = {
                                type =
                                    "expense"
                            },

                            label = {
                                Text(
                                    strings.expense
                                )
                            }
                        )

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        FilterChip(

                            selected =
                                type ==
                                    "income",

                            onClick = {
                                type =
                                    "income"
                            },

                            label = {
                                Text(
                                    strings.income
                                )
                            }
                        )
                    }
                }

                item {

                    OutlinedTextField(

                        value = amount,

                        onValueChange = {
                            amount =
                                it.filter(
                                    Char::isDigit
                                )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text(strings.amount)
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            )
                    )
                }

                item {

                    OutlinedTextField(

                        value = category,

                        onValueChange = {
                            category = it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text(
                                strings.category
                            )
                        }
                    )
                }

                item {

                    if (
                        categories.isNotEmpty()
                    ) {

                        Text(
                            "پیشنهاد: " +
                                    categories.joinToString(
                                        "، "
                                    ),
                            fontSize = 12.sp
                        )
                    }
                }

                item {

                    OutlinedTextField(

                        value =
                            description,

                        onValueChange = {
                            description = it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text(
                                strings.description
                            )
                        }
                    )
                }

                item {

                    OutlinedTextField(

                        value = date,

                        onValueChange = {
                            date = it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text(strings.date)
                        }
                    )
                }

                item {

                    OutlinedTextField(

                        value = card,

                        onValueChange = {
                            card = it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text("کارت")
                        }
                    )
                }

                item {

                    if (
                        cards.isNotEmpty()
                    ) {

                        Text(
                            "کارت‌ها: " +
                                    cards.joinToString(
                                        "، "
                                    ) {
                                        it.name
                                    },
                            fontSize = 12.sp
                        )
                    }
                }

                item {

                    OutlinedTextField(

                        value = person,

                        onValueChange = {
                            person = it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text(
                                "شخص / کارفرما"
                            )
                        }
                    )
                }

                item {

                    if (
                        people.isNotEmpty()
                    ) {

                        Text(
                            "اشخاص: " +
                                    people.joinToString(
                                        "، "
                                    ) {
                                        it.name
                                    },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    )
}

// ============================================================
// WORK PAGE
// ============================================================

@Composable
fun WorkPage(
    strings: AppStrings,
    workDays: List<WorkDay>,
    people: List<Person>,
    currency: String,
    onWorkChange:
        (MutableList<WorkDay>) -> Unit
) {

    var showAdd by
        remember { mutableStateOf(false) }

    var editing by
        remember {
            mutableStateOf<WorkDay?>(null)
        }

    var search by
        remember { mutableStateOf("") }

    val filtered =
        workDays
            .filter {

                search.isBlank() ||

                        it.place.contains(
                            search,
                            true
                        ) ||

                        it.person.contains(
                            search,
                            true
                        ) ||

                        it.description.contains(
                            search,
                            true
                        )
            }
            .sortedByDescending {
                it.id
            }

    Column(

        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                strings.work,
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = {
                    showAdd = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )
            }
        }

        Spacer(
            Modifier.height(10.dp)
        )

        OutlinedTextField(

            value = search,

            onValueChange = {
                search = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    null
                )
            },

            label = {
                Text(strings.search)
            }
        )

        Spacer(
            Modifier.height(10.dp)
        )

        if (filtered.isEmpty()) {

            Box(
                Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(strings.noData)
            }

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    filtered,
                    key = {
                        it.id
                    }
                ) { work ->

                    WorkCard(

                        work = work,

                        currency =
                            currency,

                        onDelete = {

                            val list =
                                workDays
                                    .toMutableList()

                            list.removeAll {
                                it.id ==
                                    work.id
                            }

                            onWorkChange(
                                list
                            )
                        },

                        onEdit = {

                            editing =
                                work
                        }
                    )
                }
            }
        }
    }

    if (
        showAdd ||
        editing != null
    ) {

        WorkDialog(

            strings =
                strings,

            people =
                people,

            existing =
                editing,

            onDismiss = {

                showAdd = false
                editing = null
            },

            onSave = { work ->

                val list =
                    workDays
                        .toMutableList()

                list.removeAll {
                    it.id == work.id
                }

                list.add(work)

                onWorkChange(list)

                showAdd = false
                editing = null
            }
        )
    }
}

// ============================================================
// WORK CARD
// ============================================================

@Composable
fun WorkCard(
    work: WorkDay,
    currency: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    val hours =
        calculateHours(
            work.start,
            work.end
        )

    Card(
        Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(23.dp)
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.Top
            ) {

                Column(
                    Modifier.weight(1f)
                ) {

                    Text(
                        work.place,
                        fontSize = 19.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(work.date)

                    Text(
                        "${work.start} → ${work.end}"
                    )

                    Text(
                        "%.1f ساعت".format(
                            Locale.US,
                            hours
                        )
                    )

                    Text(
                        money(
                            work.income,
                            currency
                        ),
                        fontWeight =
                            FontWeight.Bold
                    )

                    if (
                        work.person
                            .isNotBlank()
                    ) {

                        Text(
                            "کارفرما: ${work.person}"
                        )
                    }

                    if (
                        work.description
                            .isNotBlank()
                    ) {

                        Text(
                            work.description
                        )
                    }
                }

                Row {

                    IconButton(
                        onClick = onEdit
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            null
                        )
                    }

                    IconButton(
                        onClick = onDelete
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

// ============================================================
// WORK DIALOG
// ============================================================

@Composable
fun WorkDialog(
    strings: AppStrings,
    people: List<Person>,
    existing: WorkDay?,
    onDismiss: () -> Unit,
    onSave: (WorkDay) -> Unit
) {

    var place by
        remember {
            mutableStateOf(
                existing?.place ?: ""
            )
        }

    var date by
        remember {
            mutableStateOf(
                existing?.date
                    ?: today()
            )
        }

    var start by
        remember {
            mutableStateOf(
                existing?.start
                    ?: "08:00"
            )
        }

    var end by
        remember {
            mutableStateOf(
                existing?.end
                    ?: "16:00"
            )
        }

    var income by
        remember {
            mutableStateOf(
                existing?.income
                    ?.toString()
                    ?: ""
            )
        }

    var person by
        remember {
            mutableStateOf(
                existing?.person ?: ""
            )
        }

    var description by
        remember {
            mutableStateOf(
                existing?.description ?: ""
            )
        }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                if (existing == null)
                    "روز کاری جدید"
                else
                    "ویرایش روز کاری"
            )
        },

        confirmButton = {

            TextButton(

                onClick = {

                    if (
                        place.isNotBlank()
                    ) {

                        onSave(

                            WorkDay(

                                id =
                                    existing?.id
                                        ?: System.currentTimeMillis(),

                                place =
                                    place,

                                date =
                                    date,

                                start =
                                    start,

                                end =
                                    end,

                                income =
                                    income
                                        .toLongOrNull()
                                        ?: 0L,

                                description =
                                    description,

                                person =
                                    person
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

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    place,
                    { place = it },
                    label = {
                        Text(strings.place)
                    }
                )

                OutlinedTextField(
                    date,
                    { date = it },
                    label = {
                        Text(strings.date)
                    }
                )

                OutlinedTextField(
                    start,
                    { start = it },
                    label = {
                        Text(strings.start)
                    }
                )

                OutlinedTextField(
                    end,
                    { end = it },
                    label = {
                        Text(strings.end)
                    }
                )

                OutlinedTextField(
                    income,
                    {
                        income =
                            it.filter(
                                Char::isDigit
                            )
                    },
                    label = {
                        Text(strings.income)
                    }
                )

                OutlinedTextField(
                    person,
                    { person = it },
                    label = {
                        Text(
                            "شخص / کارفرما"
                        )
                    }
                )

                OutlinedTextField(
                    description,
                    { description = it },
                    label = {
                        Text(strings.description)
                    }
                )

                if (
                    people.isNotEmpty()
                ) {

                    Text(
                        "اشخاص: " +
                                people.joinToString(
                                    "، "
                                ) {
                                    it.name
                                },
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}

// ============================================================
// REPORTS
// ============================================================

@Composable
fun ReportsPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    currency: String
) {

    val currentMonth =
        monthOf(today())

    var selectedMonth by
        remember {
            mutableStateOf(
                currentMonth
            )
        }

    val availableMonths =
        (
                transactions.map {
                    monthOf(it.date)
                } +
                        workDays.map {
                            monthOf(it.date)
                        }
                )
            .distinct()
            .sortedDescending()

    val monthTransactions =
        transactions.filter {
            monthOf(it.date) ==
                    selectedMonth
        }

    val monthWork =
        workDays.filter {
            monthOf(it.date) ==
                    selectedMonth
        }

    val income =
        monthTransactions
            .filter {
                it.type == "income"
            }
            .sumOf {
                it.amount
            }

    val expense =
        monthTransactions
            .filter {
                it.type == "expense"
            }
            .sumOf {
                it.amount
            }

    val workIncome =
        monthWork.sumOf {
            it.income
        }

    val categories =
        monthTransactions
            .filter {
                it.type == "expense"
            }
            .groupBy {
                it.category
            }
            .mapValues {
                it.value.sumOf {
                    transaction ->
                    transaction.amount
                }
            }

    LazyColumn(

        Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                strings.monthlyReport,
                fontSize = 28.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        item {

            OutlinedTextField(

                value =
                    selectedMonth,

                onValueChange = {
                    selectedMonth = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                label = {
                    Text(
                        "ماه YYYY/MM"
                    )
                }
            )
        }

        if (
            availableMonths.isNotEmpty()
        ) {

            item {

                Text(
                    "ماه‌های موجود: " +
                            availableMonths
                                .joinToString("، "),
                    fontSize = 12.sp
                )
            }
        }

        item {

            InfoCard(
                strings.income,
                money(
                    income + workIncome,
                    currency
                ),
                Icons.Default.TrendingUp
            )
        }

        item {

            InfoCard(
                strings.expense,
                money(
                    expense,
                    currency
                ),
                Icons.Default.TrendingDown
            )
        }

        item {

            InfoCard(
                "خالص",
                money(
                    income +
                            workIncome -
                            expense,
                    currency
                ),
                Icons.Default.AccountBalance
            )
        }

        item {

            InfoCard(
                "درآمد کاری",
                money(
                    workIncome,
                    currency
                ),
                Icons.Default.Work
            )
        }

        item {

            InfoCard(
                strings.hours,
                "%.1f ساعت".format(
                    Locale.US,
                    monthWork.sumOf {
                        calculateHours(
                            it.start,
                            it.end
                        )
                    }
                ),
                Icons.Default.AccessTime
            )
        }

        item {

            InfoCard(
                "تعداد روز کاری",
                monthWork.size.toString(),
                Icons.Default.CalendarMonth
            )
        }

        item {

            Text(
                "نمودار درآمد و هزینه",
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        item {

            FinanceChart(
                income =
                    income +
                            workIncome,

                expense =
                    expense
            )
        }

        item {

            Text(
                "هزینه بر اساس دسته‌بندی",
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        if (categories.isEmpty()) {

            item {

                Text(
                    strings.noData
                )
            }

        } else {

            items(
                categories.entries
                    .toList()
            ) { entry ->

                Card(
                    Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(20.dp)
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
                                entry.value,
                                currency
                            ),
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// SIMPLE CHART
// ============================================================

@Composable
fun FinanceChart(
    income: Long,
    expense: Long
) {

    val maximum =
        max(
            1L,
            max(
                income,
                expense
            )
        ).toFloat()

    Row(

        Modifier
            .fillMaxWidth()
            .height(180.dp),

        horizontalArrangement =
            Arrangement.spacedBy(25.dp),

        verticalAlignment =
            Alignment.Bottom
    ) {

        ChartBar(
            "درآمد",
            income,
            maximum,
            Modifier.weight(1f)
        )

        ChartBar(
            "هزینه",
            expense,
            maximum,
            Modifier.weight(1f)
        )
    }
}

@Composable
fun ChartBar(
    label: String,
    value: Long,
    maximum: Float,
    modifier: Modifier
) {

    val ratio =
        value.toFloat() /
                maximum

    Column(

        modifier =
            modifier.fillMaxHeight(),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Bottom
    ) {

        Text(
            NumberFormat
                .getNumberInstance(
                    Locale("fa", "IR")
                )
                .format(value),

            fontSize = 11.sp
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Box(

            Modifier
                .fillMaxWidth()
                .height(
                    max(
                        5f,
                        110f * ratio
                    ).dp
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .primary
                )
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Text(
            label,
            fontWeight =
                FontWeight.Bold
        )
    }
}

// ============================================================
// SETTINGS
// ============================================================

@Composable
fun SettingsPage(
    strings: AppStrings,
    cards: List<BankCard>,
    people: List<Person>,
    categories: List<String>,
    currency: String,

    onCardsChange:
        (MutableList<BankCard>) -> Unit,

    onPeopleChange:
        (MutableList<Person>) -> Unit,

    onCategoriesChange:
        (MutableList<String>) -> Unit,

    onLanguageChange:
        (String) -> Unit,

    onThemeChange:
        (String) -> Unit,

    onCurrencyChange:
        (String) -> Unit,

    onLockChange:
        (Boolean, String) -> Unit
) {

    var showCard by
        remember { mutableStateOf(false) }

    var showPerson by
        remember { mutableStateOf(false) }

    var showCategory by
        remember { mutableStateOf(false) }

    var showLock by
        remember { mutableStateOf(false) }

    LazyColumn(

        Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                strings.settings,
                fontSize = 30.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        item {

            SettingsSection(
                strings.language
            ) {

                OptionRow(
                    "فارسی"
                ) {
                    onLanguageChange(
                        "fa"
                    )
                }

                OptionRow(
                    "English"
                ) {
                    onLanguageChange(
                        "en"
                    )
                }

                OptionRow(
                    "العربية"
                ) {
                    onLanguageChange(
                        "ar"
                    )
                }
            }
        }

        item {

            SettingsSection(
                strings.theme
            ) {

                OptionRow(
                    strings.light
                ) {
                    onThemeChange(
                        "light"
                    )
                }

                OptionRow(
                    strings.dark
                ) {
                    onThemeChange(
                        "dark"
                    )
                }

                OptionRow(
                    strings.system
                ) {
                    onThemeChange(
                        "system"
                    )
                }
            }
        }

        item {

            SettingsSection(
                strings.currency
            ) {

                OptionRow(
                    "تومان"
                ) {

                    onCurrencyChange(
                        "تومان"
                    )
                }

                OptionRow(
                    "ریال"
                ) {

                    onCurrencyChange(
                        "ریال"
                    )
                }
            }
        }

        item {

            SettingsSection(
                strings.lock
            ) {

                Button(

                    onClick = {
                        showLock = true
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Icon(
                        Icons.Default.Lock,
                        null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "تنظیم رمز برنامه"
                    )
                }
            }
        }

        // ---------------- CARDS ----------------

        item {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    strings.cards,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showCard = true
                    }
                ) {

                    Icon(
                        Icons.Default.Add,
                        null
                    )
                }
            }
        }

        items(
            cards,
            key = {
                it.id
            }
        ) { card ->

            CardItem(

                card = card,

                onDelete = {

                    val list =
                        cards.toMutableList()

                    list.removeAll {
                        it.id ==
                            card.id
                    }

                    onCardsChange(
                        list
                    )
                }
            )
        }

        // ---------------- PEOPLE ----------------

        item {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    strings.people,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showPerson = true
                    }
                ) {

                    Icon(
                        Icons.Default.Add,
                        null
                    )
                }
            }
        }

        items(
            people,
            key = {
                it.id
            }
        ) { person ->

            PersonItem(

                person = person,

                onDelete = {

                    val list =
                        people.toMutableList()

                    list.removeAll {
                        it.id ==
                            person.id
                    }

                    onPeopleChange(
                        list
                    )
                }
            )
        }

        // ---------------- CATEGORIES ----------------

        item {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    "دسته‌بندی‌ها",
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showCategory = true
                    }
                ) {

                    Icon(
                        Icons.Default.Add,
                        null
                    )
                }
            }
        }

        itemsIndexed(
            categories
        ) { _, category ->

            Card(
                Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(18.dp)
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        category,
                        Modifier.weight(1f)
                    )

                    IconButton(

                        onClick = {

                            val list =
                                categories
                                    .toMutableList()

                            list.remove(category)

                            onCategoriesChange(
                                list
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

    if (showCategory) {

        AddCategoryDialog(

            onDismiss = {
                showCategory = false
            },

            onSave = { value ->

                val list =
                    categories
                        .toMutableList()

                if (
                    value.isNotBlank() &&
                    !list.contains(value)
                ) {

                    list.add(value)
                }

                onCategoriesChange(
                    list
                )

                showCategory = false
            }
        )
    }

    if (showLock) {

        LockSettingsDialog(

            onDismiss = {
                showLock = false
            },

            onChange = { enabled, pin ->

                onLockChange(
                    enabled,
                    pin
                )
            }
        )
    }
}

// ============================================================
// SETTINGS COMPONENTS
// ============================================================

@Composable
fun SettingsSection(
    title: String,
    content:
        @Composable ColumnScope.() -> Unit
) {

    Card(

        Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp)
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {

            Text(
                title,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(8.dp)
            )

            content()
        }
    }
}

@Composable
fun OptionRow(
    title: String,
    onClick: () -> Unit
) {

    TextButton(

        onClick = onClick,

        modifier =
            Modifier.fillMaxWidth(),

        contentPadding =
            PaddingValues(
                vertical = 10.dp
            )
    ) {

        Text(
            title,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

// ============================================================
// CARD
// ============================================================

@Composable
fun CardItem(
    card: BankCard,
    onDelete: () -> Unit
) {

    Card(

        Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp)
    ) {

        Row(

            Modifier
                .fillMaxWidth()
                .padding(17.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.CreditCard,
                null,
                Modifier.size(40.dp)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    card.bank,
                    fontSize = 19.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(card.name)

                Text(
                    "•••• ${card.last4}"
                )

                Text(
                    money(
                        card.balance,
                        "تومان"
                    ),
                    fontWeight =
                        FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    Icons.Default.Delete,
                    null
                )
            }
        }
    }
}

// ============================================================
// ADD CARD
// ============================================================

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (BankCard) -> Unit
) {

    var bank by
        remember { mutableStateOf("") }

    var name by
        remember { mutableStateOf("") }

    var last4 by
        remember { mutableStateOf("") }

    var balance by
        remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "کارت بانکی جدید"
            )
        },

        confirmButton = {

            TextButton(

                onClick = {

                    if (
                        bank.isNotBlank()
                    ) {

                        onSave(

                            BankCard(

                                id =
                                    System.currentTimeMillis(),

                                bank =
                                    bank,

                                name =
                                    name,

                                last4 =
                                    last4
                                        .filter(
                                            Char::isDigit
                                        )
                                        .takeLast(4),

                                balance =
                                    balance
                                        .toLongOrNull()
                                        ?: 0L
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

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
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
                            it
                                .filter(
                                    Char::isDigit
                                )
                                .take(4)
                    },
                    label = {
                        Text(
                            "۴ رقم آخر کارت"
                        )
                    }
                )

                OutlinedTextField(
                    balance,
                    {
                        balance =
                            it.filter(
                                Char::isDigit
                            )
                    },
                    label = {
                        Text("موجودی")
                    }
                )
            }
        }
    )
}

// ============================================================
// PEOPLE
// ============================================================

@Composable
fun PersonItem(
    person: Person,
    onDelete: () -> Unit
) {

    Card(

        Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp)
    ) {

        Row(

            Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.Person,
                null,
                Modifier.size(36.dp)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    person.name,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (
                    person.phone
                        .isNotBlank()
                ) {

                    Text(
                        person.phone
                    )
                }

                if (
                    person.note
                        .isNotBlank()
                ) {

                    Text(
                        person.note
                    )
                }
            }

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    Icons.Default.Delete,
                    null
                )
            }
        }
    }
}

// ============================================================
// ADD PERSON
// ============================================================

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit
) {

    var name by
        remember { mutableStateOf("") }

    var phone by
        remember { mutableStateOf("") }

    var note by
        remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text("شخص جدید")
        },

        confirmButton = {

            TextButton(

                onClick = {

                    if (
                        name.isNotBlank()
                    ) {

                        onSave(

                            Person(
                                id =
                                    System.currentTimeMillis(),
                                name =
                                    name,
                                phone =
                                    phone,
                                note =
                                    note
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

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
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

// ============================================================
// CATEGORY
// ============================================================

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {

    var value by
        remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "دسته‌بندی جدید"
            )
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onSave(value)
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

        text = {

            OutlinedTextField(

                value = value,

                onValueChange = {
                    value = it
                },

                label = {
                    Text(
                        "نام دسته‌بندی"
                    )
                }
            )
        }
    )
}

// ============================================================
// LOCK SETTINGS
// ============================================================

@Composable
fun LockSettingsDialog(
    onDismiss: () -> Unit,
    onChange:
        (Boolean, String) -> Unit
) {

    var enabled by
        remember {
            mutableStateOf(true)
        }

    var pin1 by
        remember {
            mutableStateOf("")
        }

    var pin2 by
        remember {
            mutableStateOf("")
        }

    var error by
        remember {
            mutableStateOf(false)
        }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "قفل برنامه"
            )
        },

        confirmButton = {

            TextButton(

                onClick = {

                    if (!enabled) {

                        onChange(
                            false,
                            ""
                        )

                        onDismiss()

                    } else if (
                        pin1.length >= 4 &&
                        pin1 == pin2
                    ) {

                        onChange(
                            true,
                            pin1
                        )

                        onDismiss()

                    } else {

                        error = true
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

        text = {

            Column(

                verticalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Switch(
                        checked =
                            enabled,

                        onCheckedChange = {
                            enabled = it
                        }
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "فعال بودن قفل"
                    )
                }

                if (enabled) {

                    OutlinedTextField(

                        value = pin1,

                        onValueChange = {

                            pin1 =
                                it
                                    .filter(
                                        Char::isDigit
                                    )
                                    .take(8)
                        },

                        label = {
                            Text(
                                "رمز جدید"
                            )
                        },

                        singleLine = true,

                        visualTransformation =
                            PasswordVisualTransformation(),

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.NumberPassword
                            )
                    )

                    OutlinedTextField(

                        value = pin2,

                        onValueChange = {

                            pin2 =
                                it
                                    .filter(
                                        Char::isDigit
                                    )
                                    .take(8)
                        },

                        label = {
                            Text(
                                "تکرار رمز"
                            )
                        },

                        singleLine = true,

                        visualTransformation =
                            PasswordVisualTransformation(),

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.NumberPassword
                            )
                    )

                    if (error) {

                        Text(
                            "رمزها باید یکسان و حداقل ۴ رقمی باشند.",
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }
            }
        }
    )
}

// ============================================================
// COMMON UI
// ============================================================

@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector
) {

    Card(

        Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(28.dp)
    ) {

        Row(

            Modifier.padding(20.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                icon,
                null,
                Modifier.size(40.dp)
            )

            Spacer(
                Modifier.width(15.dp)
            )

            Column {

                Text(title)

                Text(
                    value,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold
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
    icon: ImageVector
) {

    Card(

        modifier,

        shape =
            RoundedCornerShape(24.dp)
    ) {

        Column(
            Modifier.padding(15.dp)
        ) {

            Icon(
                icon,
                null,
                Modifier.size(28.dp)
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(title)

            Text(
                value,
                fontWeight =
                    FontWeight.Bold,
                fontSize = 15.sp
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
        fontWeight =
            FontWeight.Bold
    )
}
