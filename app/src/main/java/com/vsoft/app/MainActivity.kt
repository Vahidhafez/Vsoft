package com.vsoft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.text.NumberFormat
import java.util.Locale

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
                                androidx.compose.material3.Icon(
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
                                androidx.compose.material3.Icon(
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
                                androidx.compose.material3.Icon(
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
                                androidx.compose.material3.Icon(
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
                        Modifier.padding(paddingValues)
                    )

                    1 -> SimplePage(
                        "مدیریت مالی",
                        Modifier.padding(paddingValues)
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
fun HomePage(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = "Vsoft",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "مدیریت مالی و کاری",
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "موجودی",
                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = money(0),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
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
                        money(0),
                        fontWeight = FontWeight.Bold
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
                        money(0),
                        fontWeight = FontWeight.Bold
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
                    "۰ روز",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun money(amount: Long): String {

    return NumberFormat
        .getNumberInstance(Locale("fa", "IR"))
        .format(amount) + " تومان"
}
