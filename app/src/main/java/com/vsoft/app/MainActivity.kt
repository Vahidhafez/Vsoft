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
                                Text("گزارش"
                                )
                            }
                        )
                    }
                }

            ) { paddingValues ->

                when (selectedPage) {

                    0 -> HomePage(
                        transactions = transactions,
                        modifier = Modifier.padding(paddingValues)
                    )

                    1 -> FinancePage(
                        transactions = transactions,
                        onTransactionsChanged = {
                            transactions = it
                        },
                        modifier = Modifier.padding(paddingValues)
                    )

                    2 -> SimplePage(
                        "روزهای کاری",
                        Modifier.padding(paddingValues)
                    )

                    3 -> SimplePage(
                        "گزارش‌ها",
                        Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun HomePage(
    transactions: List<Transaction>,
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

                Text(
                    text = "هنوز تراکنشی ثبت نشده است"
                )
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
                    text = if (transaction.isIncome) {
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
                },
                fontSize = 16.sp
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
fun SimplePage(
    title: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = title,
            fontSize = 28.sp
        )
    }
}

fun money(amount: Long): String {

    return NumberFormat
        .getNumberInstance(Locale("fa", "IR"))
        .format(amount) + " تومان"
}
