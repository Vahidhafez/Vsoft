package com.vsoft.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/** توکن‌های رنگی طراحی (الهام‌گرفته از iOS). تنها منبع رنگ در اپ. */
@Immutable
data class VsoftColors(
    val isDark: Boolean,
    val background: Color,        // پس‌زمینه‌ی گروه‌بندی‌شده‌ی صفحه
    val surface: Color,           // کارت‌ها و شیت‌ها
    val surfaceRaised: Color,     // سطح بالاتر (منوها)
    val fieldBackground: Color,   // پس‌زمینه‌ی فیلدهای ورودی
    val fill: Color,              // fill نیمه‌شفاف (سرچ‌بار، سگمنت)
    val segmentSelected: Color,   // گزینه‌ی انتخاب‌شده‌ی سگمنت
    val separator: Color,         // خط جداکننده‌ی hairline
    val label: Color,             // متن اصلی
    val labelSecondary: Color,    // متن ثانویه
    val labelTertiary: Color,     // متن سوم / placeholder / آیکن غیرفعال
    val accent: Color,            // رنگ اصلی (آبی iOS)
    val income: Color,            // درآمد (سبز)
    val expense: Color,           // هزینه (قرمز)
    val warning: Color            // هشدار (نارنجی)
)

val VsoftLightColors = VsoftColors(
    isDark = false,
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFFFFFFF),
    fieldBackground = Color(0xFFEFEFF4),
    fill = Color(0x1F787880),
    segmentSelected = Color(0xFFFFFFFF),
    separator = Color(0x333C3C43),
    label = Color(0xFF000000),
    labelSecondary = Color(0xFF6C6C70),
    labelTertiary = Color(0xFF8E8E93),
    accent = Color(0xFF007AFF),
    income = Color(0xFF248A3D),   // سبز با کنتراست مناسب روی سفید
    expense = Color(0xFFD70015),  // قرمز با کنتراست مناسب روی سفید
    warning = Color(0xFFFF9500)
)

val VsoftDarkColors = VsoftColors(
    isDark = true,
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surfaceRaised = Color(0xFF2C2C2E),
    fieldBackground = Color(0xFF2C2C2E),
    fill = Color(0x3D787880),
    segmentSelected = Color(0xFF636366),
    separator = Color(0x4D545458),
    label = Color(0xFFFFFFFF),
    labelSecondary = Color(0xFF98989F),
    labelTertiary = Color(0xFF8E8E93),
    accent = Color(0xFF0A84FF),
    income = Color(0xFF30D158),
    expense = Color(0xFFFF453A),
    warning = Color(0xFFFF9F0A)
)

val LocalVsoftColors = staticCompositionLocalOf { VsoftLightColors }

object VsoftTheme {
    val colors: VsoftColors
        @Composable @ReadOnlyComposable get() = LocalVsoftColors.current
}

object VsoftSpacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val pageHorizontal = 16.dp
}

object VsoftShapes {
    val card = RoundedCornerShape(14.dp)
    val control = RoundedCornerShape(12.dp)
    val small = RoundedCornerShape(10.dp)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}

/**
 * فونت برنامه. پیش‌فرض فونت سیستم است.
 * اگر فایل‌های Vazirmatn (لایسنس OFL) را در res/font گذاشتی
 * (vazirmatn_regular.ttf, vazirmatn_medium.ttf, vazirmatn_semibold.ttf, vazirmatn_bold.ttf)
 * این خط را با FontFamily(Font(R.font.vazirmatn_regular, FontWeight.Normal), ...) عوض کن.
 */
val VsoftFontFamily: FontFamily = FontFamily.Default

private fun vsoftTypography(): Typography {
    fun s(size: Int, weight: FontWeight, line: Int) = TextStyle(
        fontFamily = VsoftFontFamily,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = line.sp,
        letterSpacing = 0.sp
    )
    return Typography(
        displayLarge = s(40, FontWeight.Bold, 48),
        displayMedium = s(34, FontWeight.Bold, 42),
        displaySmall = s(28, FontWeight.Bold, 36),
        headlineLarge = s(34, FontWeight.Bold, 42),
        headlineMedium = s(32, FontWeight.Bold, 40),   // عنوان بزرگ (Large Title)
        headlineSmall = s(22, FontWeight.SemiBold, 30),
        titleLarge = s(17, FontWeight.SemiBold, 24),   // عنوان inline نوار بالا
        titleMedium = s(17, FontWeight.SemiBold, 24),
        titleSmall = s(15, FontWeight.SemiBold, 22),
        bodyLarge = s(17, FontWeight.Normal, 24),
        bodyMedium = s(15, FontWeight.Normal, 22),
        bodySmall = s(13, FontWeight.Normal, 19),
        labelLarge = s(15, FontWeight.Medium, 20),
        labelMedium = s(13, FontWeight.Medium, 18),
        labelSmall = s(11, FontWeight.Medium, 16)
    )
}

@Composable
fun VsoftTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val c = if (darkTheme) VsoftDarkColors else VsoftLightColors
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(
        primary = c.accent,
        onPrimary = Color.White,
        primaryContainer = if (darkTheme) Color(0xFF0A2A4D) else Color(0xFFD6E8FF),
        onPrimaryContainer = c.label,
        secondary = c.income,
        onSecondary = Color.White,
        secondaryContainer = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
        onSecondaryContainer = c.label,
        tertiary = c.warning,
        onTertiary = Color.White,
        tertiaryContainer = if (darkTheme) Color(0xFF3A2A0A) else Color(0xFFFFEBCC),
        onTertiaryContainer = c.label,
        error = c.expense,
        onError = Color.White,
        errorContainer = if (darkTheme) Color(0xFF4A1512) else Color(0xFFFFE5E3),
        onErrorContainer = c.expense,
        background = c.background,
        onBackground = c.label,
        surface = c.surface,
        onSurface = c.label,
        surfaceVariant = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE9E9EE),
        onSurfaceVariant = c.labelSecondary,
        surfaceTint = Color.Transparent,
        outline = c.labelTertiary,
        outlineVariant = c.separator,
        surfaceContainerLowest = c.background,
        surfaceContainerLow = c.surface,
        surfaceContainer = c.surface,
        surfaceContainerHigh = c.surfaceRaised,
        surfaceContainerHighest = c.surfaceRaised
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalVsoftColors provides c) {
        MaterialTheme(
            colorScheme = scheme,
            typography = vsoftTypography(),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(10.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(14.dp),
                large = RoundedCornerShape(20.dp),
                extraLarge = RoundedCornerShape(24.dp)
            ),
            content = content
        )
    }
}
