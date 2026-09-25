package com.vsoft.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.isSystemInDarkTheme
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

enum class AppLanguage {
    PERSIAN,
    ENGLISH,
    ARABIC
}

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
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

    var language by remember {
        mutableStateOf(AppLanguage.PERSIAN)
    }

    var theme by remember {
        mutableStateOf(AppTheme.SYSTEM)
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        val preferences = context.dataStore.data.first()

        transactions =
            decodeTransactions(
                preferences[TRANSACTIONS_KEY] ?: "[]"
            )

        workDays =
            decodeWorkDays(
                preferences[WORK_DAYS_KEY] ?: "[]"
            )

        language =
            when (preferences[LANGUAGE_KEY]) {
                "english" -> AppLanguage.ENGLISH
                "arabic" -> AppLanguage.ARABIC
                else -> AppLanguage.PERSIAN
            }

        theme =
            when (preferences[THEME_KEY]) {
                "light" -> AppTheme.LIGHT
                "dark" -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }

        loaded = true
    }

    fun saveTransactions(
        newTransactions: List<Transaction>
    ) {

        transactions = newTransactions

        scope.launch {

            context.dataStore.edit { preferences ->

                preferences[TRANSACTIONS_KEY] =
                    encodeTransactions(newTransactions)
            }
        }
    }

    fun saveWorkDays(
        newWorkDays: List<WorkDay>
    ) {

        workDays = newWorkDays

        scope.launch {

            context.dataStore.edit { preferences ->

                preferences[WORK_DAYS_KEY] =
                    encodeWorkDays(newWorkDays)
            }
        }
    }

    fun saveLanguage(
        newLanguage: AppLanguage
    ) {

        language = newLanguage

        scope.launch {

            context.dataStore.edit { preferences ->

                preferences[LANGUAGE_KEY] =
                    when (newLanguage) {

                        AppLanguage.PERSIAN ->
                            "persian"

                        AppLanguage.ENGLISH ->
                            "english"

                        AppLanguage.ARABIC ->
                            "arabic"
                    }
            }
        }
    }

    fun saveTheme(
        newTheme: AppTheme
    ) {

        theme = newTheme

        scope.launch {

            context.dataStore.edit { preferences ->

                preferences[THEME_KEY] =
                    when (newTheme) {

                        AppTheme.LIGHT ->
                            "light"

                        AppTheme.DARK ->
                            "dark"

                        AppTheme.SYSTEM ->
                            "system"
                    }
            }
        }
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

    val systemDark = isSystemInDarkTheme()

    val darkTheme =
        when (theme) {

            AppTheme.LIGHT ->
                false

            AppTheme.DARK ->
                true

            AppTheme.SYSTEM ->
                systemDark
        }

    val layoutDirection =
        when (language) {

            AppLanguage.ENGLISH ->
                LayoutDirection.Ltr

            AppLanguage.PERSIAN,
            AppLanguage.ARABIC ->
                LayoutDirection.Rtl
        }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection
    ) {

        MaterialTheme(
            colorScheme =
                if (darkTheme) {
                    darkColorScheme()
                } else {
                    lightColorScheme()
                }
        ) {

            Scaffold(

                bottomBar = {

                    NavigationBar {

                        NavigationBarItem(
                            selected = selectedPage == 0,
                            onClick = {
                                selectedPage = 0
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = textHome(language)
                                )
                            },
                            label = {
                                Text(textHome(language))
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 1,
                            onClick = {
                                selectedPage = 1
                            },
                            icon = {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription =
                                        textFinance(language)
                                )
                            },
                            label = {
                                Text(textFinance(language))
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 2,
                            onClick = {
                                selectedPage = 2
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription =
                                        textWork(language)
                                )
                            },
                            label = {
                                Text(textWork(language))
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 3,
                            onClick = {
                                selectedPage = 3
                            },
                            icon = {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription =
                                        textReports(language)
                                )
                            },
                            label = {
                                Text(textReports(language))
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 4,
                            onClick = {
                                selectedPage = 4
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription =
                                        textSettings(language)
                                )
                            },
                            label = {
                                Text(textSettings(language))
                            }
                        )
                    }
                }

            ) { paddingValues ->

                when (selectedPage) {

                    0 -> HomePage(
                        transactions = transactions,
                        workDays = workDays,
                        language = language,
                        modifier = Modifier.padding(paddingValues)
                    )

                    1 -> FinancePage(
                        transactions = transactions,
                        onTransactionsChanged = {
                            saveTransactions(it)
                        },
                        language = language,
                        modifier = Modifier.padding(paddingValues)
                    )

                    2 -> WorkPage(
                        workDays = workDays,
                        onWorkDaysChanged = {
                            saveWorkDays(it)
                        },
                        language = language,
                        modifier = Modifier.padding(paddingValues)
                    )

                    3 -> ReportPage(
                        transactions = transactions,
                        workDays = workDays,
                        language = language,
                        modifier = Modifier.padding(paddingValues)
                    )

                    4 -> SettingsPage(
                        language = language,
                        theme = theme,
                        onLanguageChanged = {
                            saveLanguage(it)
                        },
                        onThemeChanged = {
                            saveTheme(it)
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

fun textHome(language: AppLanguage): String =
    when (language) {
        AppLanguage.PERSIAN -> "خانه"
        AppLanguage.ENGLISH -> "Home"
        AppLanguage.ARABIC -> "الرئيسية"
    }

fun textFinance(language: AppLanguage): String =
    when (language) {
        AppLanguage.PERSIAN -> "مالی"
        AppLanguage.ENGLISH -> "Finance"
        AppLanguage.ARABIC -> "المالية"
    }

fun textWork(language: AppLanguage): String =
    when (language) {
        AppLanguage.PERSIAN -> "کار"
        AppLanguage.ENGLISH -> "Work"
        AppLanguage.ARABIC -> "العمل"
    }

fun textReports(language: AppLanguage): String =
    when (language) {
        AppLanguage.PERSIAN -> "گزارش"
        AppLanguage.ENGLISH -> "Reports"
        AppLanguage.ARABIC -> "التقارير"
    }

fun textSettings(language: AppLanguage): String =
    when (language) {
        AppLanguage.PERSIAN -> "تنظیمات"
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.ARABIC -> "الإعدادات"
    }

@Composable
fun HomePage(
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {

    val income = transactions
        .filter { it.isIncome }
        .sumOf { it.amount }

    val expense = transactions
        .filter { !it.isIncome }
        .sumOf { it.amount }

    val balance = income - expense

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = "Vsoft",
            fontSize = 30.sp
        )

        Text(
            text = when (language) {

                AppLanguage.PERSIAN ->
                    "مدیریت مالی و کاری"

                AppLanguage.ENGLISH ->
                    "Financial and Work Management"

                AppLanguage.ARABIC ->
                    "إدارة المال والعمل"
            },
            fontSize = 16.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "موجودی"

                        AppLanguage.ENGLISH ->
                            "Balance"

                        AppLanguage.ARABIC ->
                            "الرصيد"
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = money(balance, language),
                    fontSize = 26.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        when (language) {

                            AppLanguage.PERSIAN ->
                                "درآمد"

                            AppLanguage.ENGLISH ->
                                "Income"

                            AppLanguage.ARABIC ->
                                "الدخل"
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        money(income, language)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        when (language) {

                            AppLanguage.PERSIAN ->
                                "هزینه"

                            AppLanguage.ENGLISH ->
                                "Expense"

                            AppLanguage.ARABIC ->
                                "المصروفات"
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        money(expense, language)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "روزهای کاری"

                        AppLanguage.ENGLISH ->
                            "Work Days"

                        AppLanguage.ARABIC ->
                            "أيام العمل"
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "${workDays.size} ${
                        when (language) {

                            AppLanguage.PERSIAN ->
                                "روز"

                            AppLanguage.ENGLISH ->
                                "days"

                            AppLanguage.ARABIC ->
                                "أيام"
                        }
                    }",
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun FinancePage(
    transactions: List<Transaction>,
    onTransactionsChanged: (List<Transaction>) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = when (language) {

                    AppLanguage.PERSIAN ->
                        "مدیریت مالی"

                    AppLanguage.ENGLISH ->
                        "Finance"

                    AppLanguage.ARABIC ->
                        "الإدارة المالية"
                },
                fontSize = 28.sp
            )

            Button(
                onClick = {
                    showDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "افزودن"

                        AppLanguage.ENGLISH ->
                            "Add"

                        AppLanguage.ARABIC ->
                            "إضافة"
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (transactions.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "هنوز تراکنشی ثبت نشده است"

                        AppLanguage.ENGLISH ->
                            "No transactions yet"

                        AppLanguage.ARABIC ->
                            "لا توجد معاملات بعد"
                    }
                )
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = transactions.reversed(),
                    key = {
                        it.id
                    }
                ) { transaction ->

                    TransactionItem(
                        transaction = transaction,
                        language = language,
                        onDelete = {

                            onTransactionsChanged(
                                transactions.filter {
                                    it.id != transaction.id
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {

        AddTransactionDialog(

            language = language,

            onDismiss = {
                showDialog = false
            },

            onSave = { title, amount, isIncome ->

                val transaction = Transaction(
                    id = System.currentTimeMillis(),
                    title = title,
                    amount = amount,
                    isIncome = isIncome
                )

                onTransactionsChanged(
                    transactions + transaction
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    language: AppLanguage,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = transaction.title,
                    fontSize = 17.sp
                )

                Text(
                    when {

                        transaction.isIncome &&
                            language == AppLanguage.PERSIAN ->
                            "درآمد"

                        !transaction.isIncome &&
                            language == AppLanguage.PERSIAN ->
                            "هزینه"

                        transaction.isIncome &&
                            language == AppLanguage.ENGLISH ->
                            "Income"

                        !transaction.isIncome &&
                            language == AppLanguage.ENGLISH ->
                            "Expense"

                        transaction.isIncome ->
                            "الدخل"

                        else ->
                            "المصروفات"
                    }
                )
            }

            Text(
                text = if (transaction.isIncome) {
                    "+${money(transaction.amount, language)}"
                } else {
                    "-${money(transaction.amount, language)}"
                }
            )

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    Icons.Default.Delete,
                    contentDescription = when (language) {

                        AppLanguage.PERSIAN ->
                            "حذف"

                        AppLanguage.ENGLISH ->
                            "Delete"

                        AppLanguage.ARABIC ->
                            "حذف"
                    }
                )
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (String, Long, Boolean) -> Unit
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
            Text(
                when (language) {

                    AppLanguage.PERSIAN ->
                        "ثبت تراکنش"

                    AppLanguage.ENGLISH ->
                        "Add Transaction"

                    AppLanguage.ARABIC ->
                        "إضافة معاملة"
                }
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "عنوان"

                                AppLanguage.ENGLISH ->
                                    "Title"

                                AppLanguage.ARABIC ->
                                    "العنوان"
                            }
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char ->
                            char.isDigit()
                        }
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "مبلغ"

                                AppLanguage.ENGLISH ->
                                    "Amount"

                                AppLanguage.ARABIC ->
                                    "المبلغ"
                            }
                        )
                    },
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = isIncome,
                        onClick = {
                            isIncome = true
                        }
                    )

                    Text(
                        when (language) {

                            AppLanguage.PERSIAN ->
                                "درآمد"

                            AppLanguage.ENGLISH ->
                                "Income"

                            AppLanguage.ARABIC ->
                                "الدخل"
                        }
                    )

                    RadioButton(
                        selected = !isIncome,
                        onClick = {
                            isIncome = false
                        }
                    )

                    Text(
                        when (language) {

                            AppLanguage.PERSIAN ->
                                "هزینه"

                            AppLanguage.ENGLISH ->
                                "Expense"

                            AppLanguage.ARABIC ->
                                "المصروفات"
                        }
                    )
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value =
                        amount.toLongOrNull()

                    if (
                        title.isNotBlank() &&
                        value != null &&
                        value > 0
                    ) {

                        onSave(
                            title,
                            value,
                            isIncome
                        )
                    }
                }
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "ذخیره"

                        AppLanguage.ENGLISH ->
                            "Save"

                        AppLanguage.ARABIC ->
                            "حفظ"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "لغو"

                        AppLanguage.ENGLISH ->
                            "Cancel"

                        AppLanguage.ARABIC ->
                            "إلغاء"
                    }
                )
            }
        }
    )
}

@Composable
fun WorkPage(
    workDays: List<WorkDay>,
    onWorkDaysChanged: (List<WorkDay>) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = when (language) {

                    AppLanguage.PERSIAN ->
                        "روزهای کاری"

                    AppLanguage.ENGLISH ->
                        "Work Days"

                    AppLanguage.ARABIC ->
                        "أيام العمل"
                },
                fontSize = 28.sp
            )

            Button(
                onClick = {
                    showDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "ثبت روز"

                        AppLanguage.ENGLISH ->
                            "Add Day"

                        AppLanguage.ARABIC ->
                            "إضافة يوم"
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (workDays.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "هنوز روز کاری ثبت نشده است"

                        AppLanguage.ENGLISH ->
                            "No work days yet"

                        AppLanguage.ARABIC ->
                            "لا توجد أيام عمل بعد"
                    }
                )
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = workDays.reversed(),
                    key = {
                        it.id
                    }
                ) { workDay ->

                    WorkDayItem(
                        workDay = workDay,
                        language = language,
                        onDelete = {

                            onWorkDaysChanged(
                                workDays.filter {
                                    it.id != workDay.id
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {

        AddWorkDayDialog(

            language = language,

            onDismiss = {
                showDialog = false
            },

            onSave = {
                    place,
                    date,
                    start,
                    end,
                    income,
                    note ->

                val workDay = WorkDay(
                    id = System.currentTimeMillis(),
                    place = place,
                    date = date,
                    startTime = start,
                    endTime = end,
                    income = income,
                    note = note
                )

                onWorkDaysChanged(
                    workDays + workDay
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun WorkDayItem(
    workDay: WorkDay,
    language: AppLanguage,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = workDay.place,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        Icons.Default.Delete,
                        contentDescription = when (language) {

                            AppLanguage.PERSIAN ->
                                "حذف"

                            AppLanguage.ENGLISH ->
                                "Delete"

                            AppLanguage.ARABIC ->
                                "حذف"
                        }
                    )
                }
            }

            Text(
                when (language) {

                    AppLanguage.PERSIAN ->
                        "تاریخ: ${workDay.date}"

                    AppLanguage.ENGLISH ->
                        "Date: ${workDay.date}"

                    AppLanguage.ARABIC ->
                        "التاريخ: ${workDay.date}"
                }
            )

            Text(
                when (language) {

                    AppLanguage.PERSIAN ->
                        "ساعت: ${workDay.startTime} تا ${workDay.endTime}"

                    AppLanguage.ENGLISH ->
                        "Time: ${workDay.startTime} - ${workDay.endTime}"

                    AppLanguage.ARABIC ->
                        "الوقت: ${workDay.startTime} - ${workDay.endTime}"
                }
            )

            Text(
                when (language) {

                    AppLanguage.PERSIAN ->
                        "مدت کار: ${
                            calculateWorkDuration(
                                workDay.startTime,
                                workDay.endTime
                            )
                        }"

                    AppLanguage.ENGLISH ->
                        "Duration: ${
                            calculateWorkDurationEnglish(
                                workDay.startTime,
                                workDay.endTime
                            )
                        }"

                    AppLanguage.ARABIC ->
                        "مدة العمل: ${
                            calculateWorkDurationArabic(
                                workDay.startTime,
                                workDay.endTime
                            )
                        }"
                }
            )

            Text(
                when (language) {

                    AppLanguage.PERSIAN ->
                        "دریافتی: ${money(workDay.income, language)}"

                    AppLanguage.ENGLISH ->
                        "Income: ${money(workDay.income, language)}"

                    AppLanguage.ARABIC ->
                        "الدخل: ${money(workDay.income, language)}"
                }
            )

            if (workDay.note.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "یادداشت: ${workDay.note}"

                        AppLanguage.ENGLISH ->
                            "Note: ${workDay.note}"

                        AppLanguage.ARABIC ->
                            "ملاحظة: ${workDay.note}"
                    }
                )
            }
        }
    }
}

@Composable
fun AddWorkDayDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        String,
        String,
        Long,
        String
    ) -> Unit
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
            Text(
                when (language) {

                    AppLanguage.PERSIAN ->
                        "ثبت روز کاری"

                    AppLanguage.ENGLISH ->
                        "Add Work Day"

                    AppLanguage.ARABIC ->
                        "إضافة يوم عمل"
                }
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = place,
                    onValueChange = {
                        place = it
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "محل کار"

                                AppLanguage.ENGLISH ->
                                    "Workplace"

                                AppLanguage.ARABIC ->
                                    "مكان العمل"
                            }
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "تاریخ"

                                AppLanguage.ENGLISH ->
                                    "Date"

                                AppLanguage.ARABIC ->
                                    "التاريخ"
                            }
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = start,
                    onValueChange = {
                        start = it
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "ساعت شروع"

                                AppLanguage.ENGLISH ->
                                    "Start Time"

                                AppLanguage.ARABIC ->
                                    "وقت البدء"
                            }
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = end,
                    onValueChange = {
                        end = it
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "ساعت پایان"

                                AppLanguage.ENGLISH ->
                                    "End Time"

                                AppLanguage.ARABIC ->
                                    "وقت الانتهاء"
                            }
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = income,
                    onValueChange = {
                        income = it.filter { char ->
                            char.isDigit()
                        }
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "مبلغ دریافتی"

                                AppLanguage.ENGLISH ->
                                    "Income"

                                AppLanguage.ARABIC ->
                                    "المبلغ المستلم"
                            }
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = {
                        Text(
                            when (language) {

                                AppLanguage.PERSIAN ->
                                    "یادداشت"

                                AppLanguage.ENGLISH ->
                                    "Note"

                                AppLanguage.ARABIC ->
                                    "ملاحظة"
                            }
                        )
                    }
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value =
                        income.toLongOrNull() ?: 0L

                    if (
                        place.isNotBlank() &&
                        date.isNotBlank()
                    ) {

                        onSave(
                            place,
                            date,
                            start,
                            end,
                            value,
                            note
                        )
                    }
                }
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "ذخیره"

                        AppLanguage.ENGLISH ->
                            "Save"

                        AppLanguage.ARABIC ->
                            "حفظ"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    when (language) {

                        AppLanguage.PERSIAN ->
                            "لغو"

                        AppLanguage.ENGLISH ->
                            "Cancel"

                        AppLanguage.ARABIC ->
                            "إلغاء"
                    }
                )
            }
        }
    )
}

@Composable
fun ReportPage(
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {

    val income = transactions
        .filter { it.isIncome }
        .sumOf { it.amount }

    val expense = transactions
        .filter { !it.isIncome }
        .sumOf { it.amount }

    val workIncome = workDays.sumOf {
        it.income
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = when (language) {

                AppLanguage.PERSIAN ->
                    "گزارش‌ها"

                AppLanguage.ENGLISH ->
                    "Reports"

                AppLanguage.ARABIC ->
                    "التقارير"
            },
            fontSize = 28.sp
        )

        ReportCard(
            title = when (language) {

                AppLanguage.PERSIAN ->
                    "مجموع درآمد"

                AppLanguage.ENGLISH ->
                    "Total Income"

                AppLanguage.ARABIC ->
                    "إجمالي الدخل"
            },
            value = money(income, language)
        )

        ReportCard(
            title = when (language) {

                AppLanguage.PERSIAN ->
                    "مجموع هزینه"

                AppLanguage.ENGLISH ->
                    "Total Expense"

                AppLanguage.ARABIC ->
                    "إجمالي المصروفات"
            },
            value = money(expense, language)
        )

        ReportCard(
            title = when (language) {

                AppLanguage.PERSIAN ->
                    "دریافتی از روزهای کاری"

                AppLanguage.ENGLISH ->
                    "Work Income"

                AppLanguage.ARABIC ->
                    "دخل أيام العمل"
            },
            value = money(workIncome, language)
        )

        ReportCard(
            title = when (language) {

                AppLanguage.PERSIAN ->
                    "تعداد روزهای کاری"

                AppLanguage.ENGLISH ->
                    "Work Days"

                AppLanguage.ARABIC ->
                    "أيام العمل"
            },
            value = "${workDays.size} ${
                when (language) {

                    AppLanguage.PERSIAN ->
                        "روز"

                    AppLanguage.ENGLISH ->
                        "days"

                    AppLanguage.ARABIC ->
                        "أيام"
                }
            }"
        )
    }
}

@Composable
fun ReportCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(title)

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                value,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun SettingsPage(
    language: AppLanguage,
    theme: AppTheme,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = when (language) {

                AppLanguage.PERSIAN ->
                    "تنظیمات"

                AppLanguage.ENGLISH ->
                    "Settings"

                AppLanguage.ARABIC ->
                    "الإعدادات"
            },
            fontSize = 28.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = when (language) {

                AppLanguage.PERSIAN ->
                    "زبان برنامه"

                AppLanguage.ENGLISH ->
                    "App Language"

                AppLanguage.ARABIC ->
                    "لغة التطبيق"
            },
            fontSize = 19.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {

                SettingsRadioItem(
                    title = "فارسی",
                    selected = language == AppLanguage.PERSIAN,
                    onClick = {
                        onLanguageChanged(
                            AppLanguage.PERSIAN
                        )
                    }
                )

                SettingsRadioItem(
                    title = "English",
                    selected = language == AppLanguage.ENGLISH,
                    onClick = {
                        onLanguageChanged(
                            AppLanguage.ENGLISH
                        )
                    }
                )

                SettingsRadioItem(
                    title = "العربية",
                    selected = language == AppLanguage.ARABIC,
                    onClick = {
                        onLanguageChanged(
                            AppLanguage.ARABIC
                        )
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = when (language) {

                AppLanguage.PERSIAN ->
                    "ظاهر برنامه"

                AppLanguage.ENGLISH ->
                    "Appearance"

                AppLanguage.ARABIC ->
                    "المظهر"
            },
            fontSize = 19.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {

                SettingsRadioItem(
                    title = when (language) {

                        AppLanguage.PERSIAN ->
                            "روشن"

                        AppLanguage.ENGLISH ->
                            "Light"

                        AppLanguage.ARABIC ->
                            "فاتح"
                    },
                    selected = theme == AppTheme.LIGHT,
                    onClick = {
                        onThemeChanged(
                            AppTheme.LIGHT
                        )
                    }
                )

                SettingsRadioItem(
                    title = when (language) {

                        AppLanguage.PERSIAN ->
                            "تاریک"

                        AppLanguage.ENGLISH ->
                            "Dark"

                        AppLanguage.ARABIC ->
                            "داكن"
                    },
                    selected = theme == AppTheme.DARK,
                    onClick = {
                        onThemeChanged(
                            AppTheme.DARK
                        )
                    }
                )

                SettingsRadioItem(
                    title = when (language) {

                        AppLanguage.PERSIAN ->
                            "مطابق تنظیمات گوشی"

                        AppLanguage.ENGLISH ->
                            "Follow System"

                        AppLanguage.ARABIC ->
                            "حسب إعدادات الهاتف"
                    },
                    selected = theme == AppTheme.SYSTEM,
                    onClick = {
                        onThemeChanged(
                            AppTheme.SYSTEM
                        )
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Vsoft",
            fontSize = 18.sp
        )

        Text(
            text = "Version 1.0",
            fontSize = 13.sp
        )
    }
}

@Composable
fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = title,
            modifier = Modifier.padding(
                start = 4.dp
            )
        )
    }
}

fun encodeTransactions(
    transactions: List<Transaction>
): String {

    val array = JSONArray()

    transactions.forEach { transaction ->

        val objectJson = JSONObject()

        objectJson.put("id", transaction.id)
        objectJson.put("title", transaction.title)
        objectJson.put("amount", transaction.amount)
        objectJson.put("isIncome", transaction.isIncome)

        array.put(objectJson)
    }

    return array.toString()
}

fun decodeTransactions(
    json: String
): List<Transaction> {

    return try {

        val array = JSONArray(json)
        val result = mutableListOf<Transaction>()

        for (i in 0 until array.length()) {

            val objectJson =
                array.getJSONObject(i)

            result.add(
                Transaction(
                    id = objectJson.getLong("id"),
                    title = objectJson.getString("title"),
                    amount = objectJson.getLong("amount"),
                    isIncome = objectJson.getBoolean("isIncome")
                )
            )
        }

        result

    } catch (e: Exception) {

        emptyList()
    }
}

fun encodeWorkDays(
    workDays: List<WorkDay>
): String {

    val array = JSONArray()

    workDays.forEach { workDay ->

        val objectJson = JSONObject()

        objectJson.put("id", workDay.id)
        objectJson.put("place", workDay.place)
        objectJson.put("date", workDay.date)
        objectJson.put("startTime", workDay.startTime)
        objectJson.put("endTime", workDay.endTime)
        objectJson.put("income", workDay.income)
        objectJson.put("note", workDay.note)

        array.put(objectJson)
    }

    return array.toString()
}

fun decodeWorkDays(
    json: String
): List<WorkDay> {

    return try {

        val array = JSONArray(json)
        val result = mutableListOf<WorkDay>()

        for (i in 0 until array.length()) {

            val objectJson =
                array.getJSONObject(i)

            result.add(
                WorkDay(
                    id = objectJson.getLong("id"),
                    place = objectJson.getString("place"),
                    date = objectJson.getString("date"),
                    startTime = objectJson.getString("startTime"),
                    endTime = objectJson.getString("endTime"),
                    income = objectJson.getLong("income"),
                    note = objectJson.getString("note")
                )
            )
        }

        result

    } catch (e: Exception) {

        emptyList()
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

        val startHour = startParts[0].toInt()
        val startMinute = startParts[1].toInt()

        val endHour = endParts[0].toInt()
        val endMinute = endParts[1].toInt()

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

fun calculateWorkDurationEnglish(
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

        val startHour = startParts[0].toInt()
        val startMinute = startParts[1].toInt()

        val endHour = endParts[0].toInt()
        val endMinute = endParts[1].toInt()

        var startTotal =
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
                "$hours h $minutes min"

            hours > 0 ->
                "$hours h"

            minutes > 0 ->
                "$minutes min"

            else ->
                "0 min"
        }

    } catch (e: Exception) {

        ""
    }
}

fun calculateWorkDurationArabic(
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

        val startHour = startParts[0].toInt()
        val startMinute = startParts[1].toInt()

        val endHour = endParts[0].toInt()
        val endMinute = endParts[1].toInt()

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
                "$hours ساعة و $minutes دقيقة"

            hours > 0 ->
                "$hours ساعة"

            minutes > 0 ->
                "$minutes دقيقة"

            else ->
                "0 دقيقة"
        }

    } catch (e: Exception) {

        ""
    }
}

fun money(
    amount: Long,
    language: AppLanguage = AppLanguage.PERSIAN
): String {

    val locale =
        when (language) {

            AppLanguage.PERSIAN ->
                Locale("fa", "IR")

            AppLanguage.ENGLISH ->
                Locale.US

            AppLanguage.ARABIC ->
                Locale("ar")
        }

    val currency =
        when (language) {

            AppLanguage.PERSIAN ->
                "تومان"

            AppLanguage.ENGLISH ->
                "Toman"

            AppLanguage.ARABIC ->
                "تومان"
        }

    return NumberFormat
        .getNumberInstance(locale)
        .format(amount) +
            " " +
            currency
}
