package com.vsoft.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────── ساختار صفحه‌ها ─────────────
// 0 داشبورد | 1 مالی | 2 کار | 3 گزارش‌ها | 8 بیشتر            → صفحه‌های ریشه (تب)
// 4 تنظیمات | 5 کارت‌ها | 6 محل‌های کار | 7 اشخاص | 9 پیامک بانکی → زیرصفحه‌ی «بیشتر»
// 10 خرید کار | 11 جزئیات پروژه                                  → زیرصفحه‌ی «کار»

fun vsoftIsRootPage(page: Int): Boolean = page == 0 || page == 1 || page == 2 || page == 3 || page == 8

fun vsoftParentPage(page: Int): Int? = when (page) {
    4, 5, 6, 7, 9 -> 8
    10, 11 -> 2
    else -> null
}

fun vsoftTabIndexFor(page: Int): Int = when (page) {
    0 -> 0
    1 -> 1
    2, 10, 11 -> 2
    3 -> 3
    else -> 4
}

fun vsoftPageForTab(tab: Int): Int = when (tab) {
    0 -> 0
    1 -> 1
    2 -> 2
    3 -> 3
    else -> 8
}

// ───────────── دکمه‌ی «+» نوار بالا ─────────────
// هر صفحه می‌تواند با VsoftRegisterAddAction یک اکشن «افزودن» در نوار بالا ثبت کند
// (جایگزین FloatingActionButton).

class VsoftActionSink {
    var onAdd by mutableStateOf<(() -> Unit)?>(null)
}

val LocalVsoftActionSink = compositionLocalOf { VsoftActionSink() }

@Composable
fun VsoftRegisterAddAction(onAdd: () -> Unit) {
    val sink = LocalVsoftActionSink.current
    val current by rememberUpdatedState(onAdd)
    val handler: () -> Unit = remember { { current() } }
    DisposableEffect(sink) {
        sink.onAdd = handler
        onDispose { if (sink.onAdd === handler) sink.onAdd = null }
    }
}

// ───────────── تب‌ها ─────────────

data class VsoftTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

fun vsoftTabs(strings: AppStrings, moreLabel: String): List<VsoftTab> = listOf(
    VsoftTab(strings.dashboard, Icons.Outlined.Home, Icons.Filled.Home),
    VsoftTab(strings.finance, Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    VsoftTab(strings.work, Icons.Outlined.WorkOutline, Icons.Filled.Work),
    VsoftTab(strings.reports, Icons.Outlined.Insights, Icons.Filled.Insights),
    VsoftTab(moreLabel, Icons.Outlined.GridView, Icons.Filled.GridView)
)

@Composable
fun VsoftTabBar(tabs: List<VsoftTab>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val c = VsoftTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface)) {
        HorizontalDivider(thickness = 0.5.dp, color = c.separator)
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    if (selected) c.accent else c.labelTertiary, tween(150), label = "tab_tint"
                )
                Column(
                    Modifier.weight(1f).fillMaxHeight().vsoftTap { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ───────────── نوار بالا ─────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VsoftTopBar(
    title: String,
    isRoot: Boolean,
    backDescription: String,
    onBack: () -> Unit,
    showSettings: Boolean,
    settingsDescription: String,
    onSettings: () -> Unit,
    onAdd: (() -> Unit)?,
    addDescription: String,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val c = VsoftTheme.colors
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = c.background,
        scrolledContainerColor = c.background,
        navigationIconContentColor = c.accent,
        titleContentColor = c.label,
        actionIconContentColor = c.accent
    )
    val actions: @Composable RowScope.() -> Unit = {
        if (onAdd != null) {
            IconButton(onClick = onAdd) { Icon(Icons.Filled.Add, addDescription) }
        }
        if (showSettings) {
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, settingsDescription) }
        }
    }
    if (isRoot) {
        // عنوان بزرگ که با اسکرول جمع می‌شود (Large Title)
        LargeTopAppBar(
            title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            actions = actions,
            colors = colors,
            expandedHeight = 112.dp,
            scrollBehavior = scrollBehavior
        )
    } else {
        // عنوان inline وسط‌چین + دکمه‌ی برگشت
        CenterAlignedTopAppBar(
            title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, backDescription, modifier = Modifier.size(20.dp))
                }
            },
            actions = actions,
            colors = colors
        )
    }
}
