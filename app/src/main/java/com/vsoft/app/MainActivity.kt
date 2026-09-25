package com.vsoft.app

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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

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

    var selectedPage by remember {
        mutableIntStateOf(0)
    }

    var transactions by remember {
        mutableStateOf(emptyList<Transaction>())
    }

    var workDays by remember {
        mutableStateOf(emptyList<WorkDay>())
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {

        MaterialTheme {

            Scaffold(

                bottomBar = {

                    NavigationBar {

                        NavigationBarItem(
                            selected = selectedPage == 0,
                            onClick = { selectedPage = 0 },
                            icon = {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "خانه"
                                )
                            },
                            label = {
                                Text("خانه")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 1,
                            onClick = { selectedPage = 1 },
                            icon = {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = "مالی"
                                )
                            },
                            label = {
                                Text("مالی")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 2,
                            onClick = { selectedPage = 2 },
                            icon = {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = "کار"
                                )
                            },
                            label = {
                                Text("کار")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedPage == 3,
                            onClick = { selectedPage = 3 },
                            icon = {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = "گزارش"
                                )
                            },
                            label = {
                                Text("گزارش")
                            }
                        )
                    }
                }

            ) { paddingValues ->

                when (selectedPage) {

                    0 -> HomePage(
                        transactions = transactions,
                        workDays = workDays,
                        modifier = Modifier.padding(paddingValues)
                    )

                    1 -> FinancePage(
                        transactions = transactions,
                        onTransactionsChanged = {
                            transactions = it
                        },
                        modifier = Modifier.padding(paddingValues)
                    )

                    2 -> WorkPage(
                        workDays = workDays,
                        onWorkDaysChanged = {
                            workDays = it
                        },
                        modifier = Modifier.padding(paddingValues)
                    )

                    3 -> ReportPage(
                        transactions = transactions,
                        workDays = workDays,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun HomePage(
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
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
            text = "مدیریت مالی و کاری",
            fontSize = 16.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("موجودی")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = money(balance),
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

                    Text("درآمد")

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        money(income)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("هزینه")

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        money(expense)
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

                Text("روزهای کاری")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "${workDays.size} روز",
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
                text = "مدیریت مالی",
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

                Text("افزودن")
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

                Text("هنوز تراکنشی ثبت نشده است")
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = transactions.reversed(),
                    key = { it.id }
                ) { transaction ->

                    TransactionItem(
                        transaction = transaction,
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
                    if (transaction.isIncome) {
                        "درآمد"
                    } else {
                        "هزینه"
                    }
                )
            }

            Text(
                text = if (transaction.isIncome) {
                    "+${money(transaction.amount)}"
                } else {
                    "-${money(transaction.amount)}"
                }
            )

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف"
                )
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
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
            Text("ثبت تراکنش")
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
                        Text("عنوان")
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
                        Text("مبلغ")
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

                    Text("درآمد")

                    RadioButton(
                        selected = !isIncome,
                        onClick = {
                            isIncome = false
                        }
                    )

                    Text("هزینه")
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value = amount.toLongOrNull()

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

                Text("ذخیره")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("لغو")
            }
        }
    )
}

@Composable
fun WorkPage(
    workDays: List<WorkDay>,
    onWorkDaysChanged: (List<WorkDay>) -> Unit,
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
                text = "روزهای کاری",
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

                Text("ثبت روز")
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

                Text("هنوز روز کاری ثبت نشده است")
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = workDays.reversed(),
                    key = { it.id }
                ) { workDay ->

                    WorkDayItem(
                        workDay = workDay,
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
            onDismiss = {
                showDialog = false
            },
            onSave = { place, date, start, end, income, note ->

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
                        contentDescription = "حذف"
                    )
                }
            }

            Text("تاریخ: ${workDay.date}")

            Text(
                "ساعت: ${workDay.startTime} تا ${workDay.endTime}"
            )

            Text(
                "دریافتی: ${money(workDay.income)}"
            )

            if (workDay.note.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    "یادداشت: ${workDay.note}"
                )
            }
        }
    }
}

@Composable
fun AddWorkDayDialog(
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
            Text("ثبت روز کاری")
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
                        Text("محل کار")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                    },
                    label = {
                        Text("تاریخ")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = start,
                    onValueChange = {
                        start = it
                    },
                    label = {
                        Text("ساعت شروع")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = end,
                    onValueChange = {
                        end = it
                    },
                    label = {
                        Text("ساعت پایان")
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
                        Text("مبلغ دریافتی")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = {
                        Text("یادداشت")
                    }
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val value = income.toLongOrNull() ?: 0L

                    if (place.isNotBlank() && date.isNotBlank()) {

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

                Text("ذخیره")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("لغو")
            }
        }
    )
}

@Composable
fun ReportPage(
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
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
            text = "گزارش‌ها",
            fontSize = 28.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text("مجموع درآمد")

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    money(income),
                    fontSize = 20.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text("مجموع هزینه")

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    money(expense),
                    fontSize = 20.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text("دریافتی از روزهای کاری")

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    money(workIncome),
                    fontSize = 20.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text("تعداد روزهای کاری")

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    "${workDays.size} روز",
                    fontSize = 20.sp
                )
            }
        }
    }
}

fun money(amount: Long): String {

    return NumberFormat
        .getNumberInstance(Locale("fa", "IR"))
        .format(amount) + " تومان"
}
