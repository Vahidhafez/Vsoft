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
private val WORKPLACES_KEY = stringPreferencesKey("workplaces")
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
    val person: String,
    val startDate: String = "",
    val endDate: String = ""
)

data class Workplace(
    val id: Long,
    val name: String
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
    val note: String,
    val job: String = ""
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

fun normalizeDigits(value: String): String {
    return value
        .replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3')
        .replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7')
        .replace('۸','8').replace('۹','9')
        .replace(",", "").replace("٬", "").replace(" ", "")
}

fun formatNumberInput(value: String): String {
    val digits = normalizeDigits(value).filter(Char::isDigit)
    if (digits.isBlank()) return ""
    return NumberFormat.getNumberInstance(Locale.US).format(digits.toLongOrNull() ?: 0L)
}

fun money(value: Long): String {
    return NumberFormat.getNumberInstance(Locale("fa", "IR")).format(value) + " تومان"
}

fun cardCurrentBalance(card: BankCard, transactions: List<Transaction>): Long {
    val movement = transactions.filter { it.card == card.name }.sumOf {
        if (it.type == "income") it.amount else -it.amount
    }
    return card.balance + movement
}

fun today(): String {
    val c=java.util.Calendar.getInstance()
    val j=gregorianToJalali(c.get(java.util.Calendar.YEAR),c.get(java.util.Calendar.MONTH)+1,c.get(java.util.Calendar.DAY_OF_MONTH))
    return "%04d/%02d/%02d".format(Locale.US,j[0],j[1],j[2])
}
fun gregorianToJalali(gy:Int,gm:Int,gd:Int):IntArray{
    val md=intArrayOf(0,31,28,31,30,31,30,31,31,30,31,30,31)
    val y=gy-1600; val m=gm-1; val d=gd-1
    var n=365*y+(y+3)/4-(y+99)/100+(y+399)/400
    for(i in 0 until m)n+=md[i+1]
    if(gm>2&&(gy%4==0&&gy%100!=0||gy%400==0))n++
    n+=d; var j=n-79; val cy=j/12053; j%=12053
    var jy=979+33*cy+4*(j/1461); j%=1461
    if(j>=366){jy+=(j-1)/365;j=(j-1)%365}
    val jm=if(j<186)1+j/31 else 7+(j-186)/30
    val jd=if(j<186)1+j%31 else 1+(j-186)%30
    return intArrayOf(jy,jm,jd)
}
fun jalaliToGregorian(jy:Int,jm:Int,jd:Int):IntArray{
    val y=jy-979; var n=365*y+y/33*8+((y%33)+3)/4
    n+=if(jm<7)(jm-1)*31 else (jm-7)*30+186; n+=jd-1
    var g=n+79; var gy=1600+400*(g/146097); g%=146097; var leap=true
    if(g>=36525){g--;gy+=100*(g/36524);g%=36524;if(g>=365)g++ else leap=false}
    gy+=4*(g/1461);g%=1461
    if(g>=366){leap=false;g--;gy+=g/365;g%=365}
    val ms=intArrayOf(31,if(leap)29 else 28,31,30,31,30,31,31,30,31,30,31)
    var gm=0;while(g>=ms[gm]){g-=ms[gm];gm++}
    return intArrayOf(gy,gm+1,g+1)
}
fun jalaliMonthDays(m:Int)=if(m<=6)31 else if(m<=11)30 else 30
fun jalaliMonthName(m:Int)=listOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")[m-1]

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
                put("startDate", it.startDate)
                put("endDate", it.endDate)
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
                    o.getString("person"),
                    o.optString("startDate", o.getString("date")),
                    o.optString("endDate", o.getString("date"))
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
                put("job", it.job)
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
                    o.getString("note"),
                    o.optString("job", "")
                )
            )
        }
    } catch (_: Exception) {
    }

    return result
}

fun encodeWorkplaces(list: List<Workplace>): String {
    val array = JSONArray()
    list.forEach {
        array.put(JSONObject().apply {
            put("id", it.id)
            put("name", it.name)
        })
    }
    return array.toString()
}

fun decodeWorkplaces(value: String): MutableList<Workplace> {
    val result = mutableListOf<Workplace>()
    try {
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(Workplace(o.getLong("id"), o.getString("name")))
        }
    } catch (_: Exception) {}
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

    var workplaces by remember {
        mutableStateOf(mutableListOf<Workplace>())
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

        workplaces =
            decodeWorkplaces(preferences[WORKPLACES_KEY] ?: "[]")

        if (cards.isEmpty()) {
            cards = mutableListOf(
                BankCard(1L, "بانک ملی", "بانک ملی", "", 0L),
                BankCard(2L, "بانک مسکن", "بانک مسکن", "", 0L),
                BankCard(3L, "بلو بانک", "بلو بانک", "", 0L),
                BankCard(4L, "رد بانک", "رد بانک", "", 0L),
                BankCard(5L, "بانک مهر", "بانک مهر", "", 0L)
            )
        }

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
                workplaces = workplaces,

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

                onWorkplacesChange = {
                    workplaces = it
                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[WORKPLACES_KEY] = encodeWorkplaces(it)
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
    workplaces: List<Workplace>,
    onWorkplacesChange: (MutableList<Workplace>) -> Unit,
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
        topBar = {
            TopAppBar(
                title = { Text(pages[selectedPage]) },
                navigationIcon = {
                    IconButton(onClick = { selectedPage = 4 }) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settings)
                    }
                }
            )
        },
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
                    workplaces,
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
                    workplaces,
                    transactions,
                    onCardsChange,
                    onPeopleChange,
                    onWorkplacesChange,
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
fun FinancePage(strings: AppStrings,transactions: List<Transaction>,cards: List<BankCard>,people: List<Person>,onTransactionsChange:(MutableList<Transaction>)->Unit){
 var show by remember{mutableStateOf(false)};var edit by remember{mutableStateOf<Transaction?>(null)};var search by remember{mutableStateOf("")};var filter by remember{mutableStateOf("all")}
 val list=transactions.filter{(filter=="all"||it.type==filter)&&(search.isBlank()||it.description.contains(search,true)||it.category.contains(search,true)||it.person.contains(search,true)||it.card.contains(search,true))}.sortedByDescending{it.id}
 Column(Modifier.fillMaxSize().padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(strings.finance,fontSize=27.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));FloatingActionButton(onClick={edit=null;show=true}){Icon(Icons.Default.Add,null)}};Spacer(Modifier.height(8.dp));OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text(strings.search)},leadingIcon={Icon(Icons.Default.Search,null)});Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(filter=="all",{filter="all"},{Text("همه")});FilterChip(filter=="income",{filter="income"},{Text(strings.income)});FilterChip(filter=="expense",{filter="expense"},{Text(strings.expense)})};Spacer(Modifier.height(8.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(list,key={it.id}){t->TransactionCard(t,{val x=transactions.toMutableList();x.removeAll{it.id==t.id};onTransactionsChange(x)},{edit=t;show=true})}}}
 if(show)AddTransactionDialog(strings,cards,people,edit,{show=false}){t->val x=transactions.toMutableList();val i=x.indexOfFirst{it.id==t.id};if(i>=0)x[i]=t else x.add(t);onTransactionsChange(x);show=false}
}

// ---------------- TRANSACTION CARD ----------------

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
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

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
        }
    }
}

// ---------------- ADD TRANSACTION ----------------

@Composable
fun AddTransactionDialog(strings:AppStrings,cards:List<BankCard>,people:List<Person>,existing:Transaction?,onDismiss:()->Unit,onSave:(Transaction)->Unit){
 var type by remember{mutableStateOf(existing?.type?:"expense")};var amount by remember{mutableStateOf(existing?.amount?.toString()?.let(::formatNumberInput)?:"")};var category by remember{mutableStateOf(existing?.category?:"")};var description by remember{mutableStateOf(existing?.description?:"")};var date by remember{mutableStateOf(existing?.date?:today())};var card by remember{mutableStateOf(existing?.card?:"")};var person by remember{mutableStateOf(existing?.person?:"")};var dateOpen by remember{mutableStateOf(false)};var cardOpen by remember{mutableStateOf(false)};var personOpen by remember{mutableStateOf(false)}
 AlertDialog(onDismissRequest=onDismiss,confirmButton={TextButton(onClick={val v=normalizeDigits(amount).toLongOrNull()?:0L;if(v>0&&category.isNotBlank())onSave(Transaction(existing?.id?:System.currentTimeMillis(),type,v,category,description,date,card,person))}){Text(strings.save)}},dismissButton={TextButton(onClick=onDismiss){Text(strings.cancel)}},title={Text(if(existing==null)"تراکنش جدید" else "ویرایش تراکنش")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Row{FilterChip(type=="expense",{type="expense"},{Text(strings.expense)});Spacer(Modifier.width(8.dp));FilterChip(type=="income",{type="income"},{Text(strings.income)})};OutlinedTextField(amount,{amount=formatNumberInput(it)},label={Text(strings.amount)},modifier=Modifier.fillMaxWidth());OutlinedTextField(category,{category=it},label={Text(strings.category)},modifier=Modifier.fillMaxWidth());OutlinedTextField(description,{description=it},label={Text(strings.description)},modifier=Modifier.fillMaxWidth());OutlinedButton(onClick={dateOpen=true},modifier=Modifier.fillMaxWidth()){Text(strings.date+" : "+date)};Box{OutlinedButton(onClick={cardOpen=true},modifier=Modifier.fillMaxWidth()){Text(if(card.isBlank())"انتخاب کارت" else "کارت: "+card)};DropdownMenu(cardOpen,{cardOpen=false}){DropdownMenuItem(text={Text("بدون کارت")},onClick={cardOpen=false;card=""});cards.forEach{q->DropdownMenuItem(text={Text(q.name)},onClick={cardOpen=false;card=q.name})}}};Box{OutlinedButton(onClick={personOpen=true},modifier=Modifier.fillMaxWidth()){Text(if(person.isBlank())"انتخاب شخص" else "شخص: "+person)};DropdownMenu(personOpen,{personOpen=false}){DropdownMenuItem(text={Text("بدون شخص")},onClick={personOpen=false;person=""});people.forEach{q->DropdownMenuItem(text={Text(q.name)},onClick={personOpen=false;person=q.name})}}}}})
 if(dateOpen)JalaliDatePickerDialog(date,{dateOpen=false}){date=it;dateOpen=false}
}

// ---------------- WORK ----------------

@Composable
fun WorkPage(strings:AppStrings,workDays:List<WorkDay>,people:List<Person>,workplaces:List<Workplace>,onWorkChange:(MutableList<WorkDay>)->Unit){
 var show by remember{mutableStateOf(false)};var edit by remember{mutableStateOf<WorkDay?>(null)}
 Column(Modifier.fillMaxSize().padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(strings.work,fontSize=28.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));FloatingActionButton(onClick={edit=null;show=true}){Icon(Icons.Default.Add,null)}};Spacer(Modifier.height(12.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(workDays.sortedByDescending{it.id},key={it.id}){w->WorkCard(w,{val x=workDays.toMutableList();x.removeAll{it.id==w.id};onWorkChange(x)},{edit=w;show=true})}}}
 if(show)AddWorkDialog(strings,people,workplaces,edit,{show=false}){w->val x=workDays.toMutableList();val i=x.indexOfFirst{it.id==w.id};if(i>=0)x[i]=w else x.add(w);onWorkChange(x);show=false}
}

// ---------------- WORK CARD ----------------

@Composable
fun WorkCard(
    work: WorkDay,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
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

                    Text(
                        if (work.startDate.isNotBlank() && work.endDate.isNotBlank())
                            "${work.startDate} → ${work.endDate}"
                        else work.date
                    )

                    Text("${work.start} → ${work.end}")

                    Text(
                        String.format(
                            Locale.US,
                            "%.1f ساعت",
                            hours
                        )
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null)
                    }
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
    workplaces: List<Workplace>,
    existing: WorkDay?,
    onDismiss: () -> Unit,
    onSave: (WorkDay) -> Unit
) {
    val ctx = LocalContext.current
    var place by remember { mutableStateOf(existing?.place ?: workplaces.firstOrNull()?.name ?: "") }
    var startDate by remember { mutableStateOf(existing?.startDate?.ifBlank { existing.date } ?: today()) }
    var endDate by remember { mutableStateOf(existing?.endDate?.ifBlank { existing.date } ?: today()) }
    var start by remember { mutableStateOf(existing?.start ?: "08:00") }
    var end by remember { mutableStateOf(existing?.end ?: "16:00") }
    var income by remember { mutableStateOf(existing?.income?.toString()?.let(::formatNumberInput) ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var person by remember { mutableStateOf(existing?.person ?: "") }
    var dateOpen by remember { mutableStateOf(false) }
    var endDateOpen by remember { mutableStateOf(false) }
    var placeOpen by remember { mutableStateOf(false) }
    var personOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    WorkDay(
                        existing?.id ?: System.currentTimeMillis(),
                        place,
                        startDate,
                        start,
                        end,
                        normalizeDigits(income).toLongOrNull() ?: 0L,
                        description,
                        person,
                        startDate,
                        endDate
                    )
                )
            }) { Text(strings.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        },
        title = { Text(if (existing == null) "روز کاری جدید" else "ویرایش روز کاری") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { placeOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (place.isBlank()) "انتخاب محل کار" else place)
                    }
                    DropdownMenu(
                        expanded = placeOpen,
                        onDismissRequest = { placeOpen = false }
                    ) {
                        workplaces.forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q.name) },
                                onClick = { place = q.name; placeOpen = false }
                            )
                        }
                    }
                }

                OutlinedButton(onClick = { dateOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("تاریخ شروع: $startDate")
                }
                OutlinedButton(onClick = { endDateOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("تاریخ پایان: $endDate")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            android.app.TimePickerDialog(
                                ctx,
                                { _, h, m -> start = "%02d:%02d".format(h, m) },
                                start.substringBefore(":").toIntOrNull() ?: 8,
                                start.substringAfter(":").toIntOrNull() ?: 0,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(strings.start + " : " + start) }

                    OutlinedButton(
                        onClick = {
                            android.app.TimePickerDialog(
                                ctx,
                                { _, h, m -> end = "%02d:%02d".format(h, m) },
                                end.substringBefore(":").toIntOrNull() ?: 16,
                                end.substringAfter(":").toIntOrNull() ?: 0,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(strings.end + " : " + end) }
                }

                OutlinedTextField(
                    income,
                    { income = formatNumberInput(it) },
                    label = { Text(strings.income) },
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedButton(onClick = { personOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (person.isBlank()) "انتخاب شخص / کارفرما" else person)
                    }
                    DropdownMenu(
                        expanded = personOpen,
                        onDismissRequest = { personOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون شخص") },
                            onClick = { personOpen = false; person = "" }
                        )
                        people.forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q.name) },
                                onClick = { personOpen = false; person = q.name }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    description,
                    { description = it },
                    label = { Text(strings.description) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

    if (dateOpen) {
        JalaliDatePickerDialog(startDate, { dateOpen = false }) {
            startDate = it
            dateOpen = false
        }
    }
    if (endDateOpen) {
        JalaliDatePickerDialog(endDate, { endDateOpen = false }) {
            endDate = it
            endDateOpen = false
        }
    }
}

// ---------------- REPORTS ----------------

@Composable
fun ReportsPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>
) {
    val cards = transactions.map { it.card }.filter { it.isNotBlank() }.distinct()
    var selectedCard by remember { mutableStateOf("") }
    val cardTransactions = if (selectedCard.isBlank()) transactions else transactions.filter { it.card == selectedCard }
    val income = cardTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = cardTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val workIncome = if (selectedCard.isBlank()) workDays.sumOf { it.income } else 0L
    val hours = workDays.sumOf { calculateHours(it.start, it.end) }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.monthlyReport, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Box {
                OutlinedButton(onClick = { selectedCard = if (selectedCard.isBlank()) cards.firstOrNull().orEmpty() else "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selectedCard.isBlank()) "همه کارت‌ها" else selectedCard)
                }
            }
        }
        item { InfoCard(strings.income, money(income + workIncome), Icons.Default.TrendingUp) }
        item { InfoCard(strings.expense, money(expense), Icons.Default.TrendingDown) }
        item { InfoCard(strings.balance, money(income + workIncome - expense), Icons.Default.AccountBalance) }
        item { InfoCard("درآمد کاری", money(workIncome), Icons.Default.Work) }
        item {
            InfoCard(strings.hours, String.format(Locale.US, "%.1f ساعت", hours), Icons.Default.AccessTime)
        }
        item {
            Text("دسته‌بندی هزینه‌ها", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        val categories = cardTransactions.filter { it.type == "expense" }.groupBy { it.category }
        items(categories.entries.toList()) { entry ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(entry.key, Modifier.weight(1f))
                    Text(money(entry.value.sumOf { it.amount }), fontWeight = FontWeight.Bold)
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
    workplaces: List<Workplace>,
    transactions: List<Transaction>,
    onCardsChange: (MutableList<BankCard>) -> Unit,
    onPeopleChange: (MutableList<Person>) -> Unit,
    onWorkplacesChange: (MutableList<Workplace>) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {

    var showCard by remember { mutableStateOf(false) }
    var showPerson by remember { mutableStateOf(false) }
    var showWorkplace by remember { mutableStateOf(false) }

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
                currentBalance = cardCurrentBalance(card, transactions),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("محل‌های کار", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showWorkplace = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }

        items(workplaces, key = { it.id }) { workplace ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null)
                    Spacer(Modifier.width(12.dp))
                    Text(workplace.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        val list = workplaces.toMutableList()
                        list.removeAll { it.id == workplace.id }
                        onWorkplacesChange(list)
                    }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
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

    if (showWorkplace) {
        AddWorkplaceDialog(
            onDismiss = { showWorkplace = false },
            onSave = {
                val list = workplaces.toMutableList()
                list.add(it)
                onWorkplacesChange(list)
                showWorkplace = false
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
    currentBalance: Long,
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
                        money(currentBalance),
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

@Composable
fun AddWorkplaceDialog(
    onDismiss: () -> Unit,
    onSave: (Workplace) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(Workplace(System.currentTimeMillis(), name.trim()))
                }
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } },
        title = { Text("محل کار جدید") },
        text = {
            OutlinedTextField(
                name,
                { name = it },
                label = { Text("نام محل کار") },
                modifier = Modifier.fillMaxWidth()
            )
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

                if (person.job.isNotBlank()) {
                    Text(person.job)
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
    var job by remember { mutableStateOf("") }

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
                                note = note,
                                job = job
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
                    job,
                    { job = it },
                    label = { Text("شغل / نقش") }
                )

                OutlinedTextField(
                    note,
                    { note = it },
                    label = { Text("توضیحات") }
                )
            }
        }
    )
}

@Composable
fun JalaliDatePickerDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    val parts = initial.split("/").mapNotNull { it.toIntOrNull() }
    var year by remember { mutableStateOf(parts.getOrNull(0) ?: 1405) }
    var month by remember { mutableStateOf(parts.getOrNull(1) ?: 1) }
    var day by remember { mutableStateOf(parts.getOrNull(2) ?: 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSelected("%04d/%02d/%02d".format(Locale.US, year, month, day))
            }) { Text("انتخاب") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو") }
        },
        title = { Text("${jalaliMonthName(month)} $year") },
        text = {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        month--
                        if (month < 1) { month = 12; year-- }
                    }) { Text("‹") }

                    Text("${jalaliMonthName(month)} $year", fontWeight = FontWeight.Bold)

                    TextButton(onClick = {
                        month++
                        if (month > 12) { month = 1; year++ }
                    }) { Text("›") }
                }

                val gregorian = jalaliToGregorian(year, month, 1)
                val calendar = java.util.Calendar.getInstance().apply {
                    set(gregorian[0], gregorian[1] - 1, gregorian[2])
                }
                val offset = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7

                val cells = mutableListOf<Int?>()
                repeat(offset) { cells.add(null) }
                for (i in 1..jalaliMonthDays(month)) cells.add(i)
                while (cells.size % 7 != 0) cells.add(null)

                cells.chunked(7).forEach { row ->
                    Row(Modifier.fillMaxWidth()) {
                        row.forEach { value ->
                            Box(
                                Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value == null) {
                                    Spacer(Modifier.size(40.dp))
                                } else {
                                    TextButton(onClick = { day = value }) {
                                        Text(
                                            value.toString(),
                                            fontWeight =
                                                if (value == day) FontWeight.Bold
                                                else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
