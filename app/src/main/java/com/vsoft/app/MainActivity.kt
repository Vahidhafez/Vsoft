package com.vsoft.app

import android.os.Bundle
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.content.Intent
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextDirectionHeuristics
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import java.math.BigInteger

val android.content.Context.dataStore by preferencesDataStore("vsoft_data")

val TRANSACTIONS_KEY = stringPreferencesKey("transactions")
val WORK_KEY = stringPreferencesKey("work_days")
val CARDS_KEY = stringPreferencesKey("cards")
val PEOPLE_KEY = stringPreferencesKey("people")
val WORKPLACES_KEY = stringPreferencesKey("workplaces")
val WORK_PURCHASES_KEY = stringPreferencesKey("work_purchases")
val CONSTRUCTION_TASKS_KEY = stringPreferencesKey("construction_tasks")
val LANGUAGE_KEY = stringPreferencesKey("language")
val CURRENCY_KEY = stringPreferencesKey("currency")
val THEME_KEY = stringPreferencesKey("theme")
val GLASS_KEY = stringPreferencesKey("glass")
val FONT_KEY = stringPreferencesKey("font")

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
    val endDate: String = "",
    val card: String = ""
)

data class Workplace(
    val id: Long,
    val name: String
)

data class BankCard(
    val id: Long,
    val bank: String,
    val name: String,
    val cardNumber: String,
    val last4: String,
    // مبلغ ثابت تعیین‌شده توسط کاربر؛ تراکنش‌ها فقط موجودی فعلی را محاسبه می‌کنند.
    val openingBalance: Long
)

data class Person(
    val id: Long,
    val name: String,
    val phone: String,
    val note: String,
    val job: String = ""
)

data class WorkPurchase(
    val id: Long,
    val title: String,
    val amount: Long,
    val category: String,
    val date: String,
    val workplace: String,
    val card: String,
    val note: String = ""
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
    val currency: String,
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
            "Save", "Cancel", "Bank Cards", "People", "Language", "Currency",
            "Theme", "Light", "Dark", "System", "Search", "Category",
            "Description", "Date", "Amount", "Workplace", "Start",
            "End", "Hours", "No data", "Monthly Report"
        )

        "ar" -> AppStrings(
            "الرئيسية", "المالية", "العمل", "التقارير", "الإعدادات",
            "الرصيد", "الدخل", "المصروف", "إضافة", "حذف", "تعديل",
            "حفظ", "إلغاء", "البطاقات البنكية", "الأشخاص", "اللغة", "العملة",
            "المظهر", "فاتح", "داكن", "النظام", "بحث", "الفئة",
            "الوصف", "التاريخ", "المبلغ", "مكان العمل", "البداية",
            "النهاية", "الساعات", "لا توجد بيانات", "التقرير الشهري"
        )

        else -> AppStrings(
            "داشبورد", "مالی", "کار", "گزارش‌ها", "تنظیمات",
            "موجودی", "درآمد", "هزینه", "افزودن", "حذف", "ویرایش",
            "ذخیره", "لغو", "کارت‌های بانکی", "اشخاص", "زبان", "واحد پول",
            "تم", "روشن", "تاریک", "سیستم", "جستجو", "دسته‌بندی",
            "توضیحات", "تاریخ", "مبلغ", "محل کار", "شروع",
            "پایان", "ساعت", "اطلاعاتی وجود ندارد", "گزارش ماهانه"
        )
    }
}

val LocalVsoftLanguage = compositionLocalOf { "fa" }

@Composable
fun uiText(key: String): String {
    return when (LocalVsoftLanguage.current) {
        "en" -> when (key) {
            "وضعیت مالی و کاری شما در یک نگاه" -> "Your financial and work overview at a glance"
            "موجودی کل" -> "Total balance"
            "ساعت کاری" -> "Work hours"
            "کارت‌های بانکی" -> "Bank cards"
            "آخرین تراکنش‌ها" -> "Recent transactions"
            "هنوز تراکنشی ثبت نشده" -> "No transactions yet"
            "با دکمه + اولین مورد را اضافه کنید" -> "Use the + button to add your first item"
            "تراکنشی با این فیلتر پیدا نشد" -> "No transactions match this filter"
            "ثبت تراکنش جدید" -> "New transaction"
            "ویرایش تراکنش" -> "Edit transaction"
            "اطلاعات مالی را دقیق و سریع ثبت کنید" -> "Record financial details quickly and accurately"
            "بستن" -> "Close"
            "انتخاب کارت مبدا / مقصد" -> "Select source / destination card"
            "بدون کارت" -> "No card"
            "انتخاب شخص" -> "Select person"
            "بدون شخص" -> "No person"
            "ثبت روز کاری" -> "Add workday"
            "ویرایش روز کاری" -> "Edit workday"
            "ساعت، درآمد و جزئیات کار را یکجا ثبت کنید" -> "Record hours, income and work details together"
            "انتخاب محل کار" -> "Select workplace"
            "درآمد کار" -> "Work income"
            "درآمد به کدام کارت برود؟" -> "Which card should receive the income?"
            "واریز به:" -> "Deposit to:"
            "انتخاب شخص / کارفرما" -> "Select person / employer"
            "شرح کار" -> "Work description"
            "مثلاً نصب تابلو، تعمیر موتور، سیم‌کشی..." -> "e.g. panel installation, motor repair, wiring..."
            "هر کارت را جداگانه بررسی کنید؛ گزارش‌ها شلوغ نمی‌شوند." -> "Review each card separately without clutter."
            "انتخاب کارت" -> "Select card"
            "هنوز کارت بانکی ثبت نشده" -> "No bank card has been added yet"
            "موجودی فعلی کارت" -> "Current card balance"
            "درآمد کارت" -> "Card income"
            "هزینه کارت" -> "Card expenses"
            "موجودی اولیه" -> "Opening balance"
            "درآمد کاری واریزشده" -> "Work income deposited"
            "ساعات کاری مرتبط" -> "Related work hours"
            "تم شیشه‌ای / Liquid Glass" -> "Glass / Liquid Glass"
            "ظاهر شفاف و چندلایه" -> "Layered translucent appearance"
            "شفافیت کنترل‌شده با حرکت و عمق بیشتر" -> "Controlled translucency with subtle depth"
            "فونت برنامه" -> "App font"
            "مدرن و خوانا" -> "Modern"
            "کلاسیک" -> "Classic"
            "فنی" -> "Technical"
            "دست‌نویس" -> "Handwritten"
            "کارت" -> "card"
            "افزودن کارت" -> "Add card"
            "محل" -> "place"
            "افزودن محل کار" -> "Add workplace"
            "نفر" -> "person"
            "افزودن شخص" -> "Add person"
            "شماره کارت ثبت نشده" -> "Card number not added"
            "موجودی فعلی" -> "Current balance"
            "برای ویرایش ضربه بزنید" -> "Tap to edit"
            "کارت بانکی جدید" -> "New bank card"
            "نام بانک" -> "Bank name"
            "عنوان کارت" -> "Card title"
            "شماره کامل کارت" -> "Full card number"
            "۱۶ رقم" -> "16 digits"
            "ویرایش کارت" -> "Edit card"
            "بانک" -> "Bank"
            "نام کارت" -> "Card name"
            "محل کار جدید" -> "New workplace"
            "نام محل کار" -> "Workplace name"
            "مثلاً پروژه، شرکت یا کارگاه" -> "e.g. project, company or workshop"
            "شخص جدید" -> "New person"
            "نام و نام خانوادگی" -> "Full name"
            "شماره تماس" -> "Phone number"
            "شغل / نقش" -> "Job / role"
            "یادداشت" -> "Note"
            "خروج از حساب" -> "Sign out"
            "انتخاب" -> "Select"
            "ذخیره" -> "Save"
            "لغو" -> "Cancel"
            else -> key
        }
        "ar" -> when (key) {
            "وضعیت مالی و کاری شما در یک نگاه" -> "ملخص وضعك المالي والعملي"
            "موجودی کل" -> "إجمالي الرصيد"
            "ساعت کاری" -> "ساعات العمل"
            "کارت‌های بانکی" -> "البطاقات البنكية"
            "آخرین تراکنش‌ها" -> "آخر المعاملات"
            "هنوز تراکنشی ثبت نشده" -> "لم تُسجّل معاملات بعد"
            "با دکمه + اولین مورد را اضافه کنید" -> "استخدم زر + لإضافة أول عنصر"
            "تراکنشی با این فیلتر پیدا نشد" -> "لا توجد معاملات مطابقة"
            "ثبت تراکنش جدید" -> "معاملة جديدة"
            "ویرایش تراکنش" -> "تعديل المعاملة"
            "اطلاعات مالی را دقیق و سریع ثبت کنید" -> "سجّل المعلومات المالية بسرعة ودقة"
            "بستن" -> "إغلاق"
            "انتخاب کارت مبدا / مقصد" -> "اختر بطاقة المصدر / الوجهة"
            "بدون کارت" -> "بدون بطاقة"
            "انتخاب شخص" -> "اختر شخصًا"
            "بدون شخص" -> "بدون شخص"
            "ثبت روز کاری" -> "إضافة يوم عمل"
            "ویرایش روز کاری" -> "تعديل يوم العمل"
            "ساعت، درآمد و جزئیات کار را یکجا ثبت کنید" -> "سجّل الساعات والدخل وتفاصيل العمل معًا"
            "انتخاب محل کار" -> "اختر مكان العمل"
            "درآمد کار" -> "دخل العمل"
            "درآمد به کدام کارت برود؟" -> "إلى أي بطاقة يُحوّل الدخل؟"
            "واریز به:" -> "الإيداع إلى:"
            "انتخاب شخص / کارفرما" -> "اختر الشخص / صاحب العمل"
            "شرح کار" -> "وصف العمل"
            "مثلاً نصب تابلو، تعمیر موتور، سیم‌کشی..." -> "مثال: تركيب لوحة، إصلاح محرك، تمديدات كهربائية..."
            "هر کارت را جداگانه بررسی کنید؛ گزارش‌ها شلوغ نمی‌شوند." -> "راجع كل بطاقة بشكل منفصل دون ازدحام."
            "انتخاب کارت" -> "اختر البطاقة"
            "هنوز کارت بانکی ثبت نشده" -> "لم تُضف أي بطاقة بنكية بعد"
            "موجودی فعلی کارت" -> "الرصيد الحالي للبطاقة"
            "درآمد کارت" -> "دخل البطاقة"
            "هزینه کارت" -> "مصروفات البطاقة"
            "موجودی اولیه" -> "الرصيد الافتتاحي"
            "درآمد کاری واریزشده" -> "دخل العمل المُودع"
            "ساعات کاری مرتبط" -> "ساعات العمل المرتبطة"
            "تم شیشه‌ای / Liquid Glass" -> "الزجاج / Liquid Glass"
            "ظاهر شفاف و چندلایه" -> "مظهر شفاف متعدد الطبقات"
            "شفافیت کنترل‌شده با حرکت و عمق بیشتر" -> "شفافية مضبوطة مع عمق خفيف"
            "فونت برنامه" -> "خط التطبيق"
            "مدرن و خوانا" -> "عصري"
            "کلاسیک" -> "كلاسيكي"
            "فنی" -> "تقني"
            "دست‌نویس" -> "يدوي"
            "کارت" -> "بطاقة"
            "افزودن کارت" -> "إضافة بطاقة"
            "محل" -> "مكان"
            "افزودن محل کار" -> "إضافة مكان عمل"
            "نفر" -> "شخص"
            "افزودن شخص" -> "إضافة شخص"
            "شماره کارت ثبت نشده" -> "لم يتم تسجيل رقم البطاقة"
            "موجودی فعلی" -> "الرصيد الحالي"
            "برای ویرایش ضربه بزنید" -> "اضغط للتعديل"
            "کارت بانکی جدید" -> "بطاقة بنكية جديدة"
            "نام بانک" -> "اسم البنك"
            "عنوان کارت" -> "عنوان البطاقة"
            "شماره کامل کارت" -> "رقم البطاقة الكامل"
            "۱۶ رقم" -> "16 رقمًا"
            "ویرایش کارت" -> "تعديل البطاقة"
            "بانک" -> "البنك"
            "نام کارت" -> "اسم البطاقة"
            "محل کار جدید" -> "مكان عمل جديد"
            "نام محل کار" -> "اسم مكان العمل"
            "مثلاً پروژه، شرکت یا کارگاه" -> "مثال: مشروع أو شركة أو ورشة"
            "شخص جدید" -> "شخص جديد"
            "نام و نام خانوادگی" -> "الاسم الكامل"
            "شماره تماس" -> "رقم الهاتف"
            "شغل / نقش" -> "المهنة / الدور"
            "یادداشت" -> "ملاحظة"
            "خروج از حساب" -> "تسجيل الخروج"
            "انتخاب" -> "اختيار"
            "ذخیره" -> "حفظ"
            "لغو" -> "إلغاء"
            else -> key
        }
        else -> key
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

fun normalizeAmountInput(value: String): String {
    return normalizeDigits(value).filter(Char::isDigit).trimStart('0').take(18)
}

fun formatNumberInput(value: String): String {
    val digits = normalizeAmountInput(value)
    if (digits.isBlank()) return ""
    return try {
        NumberFormat.getNumberInstance(Locale.US).format(java.math.BigInteger(digits))
    } catch (_: Exception) {
        digits
    }
}

class GroupedNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter(Char::isDigit)
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val grouped = raw.reversed().chunked(3).joinToString(",").reversed()
        val commaPositions = grouped.mapIndexedNotNull { index, ch ->
            if (ch == ',') index else null
        }

        val originalLength = raw.length
        val transformedLength = grouped.length

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safe = offset.coerceIn(0, originalLength)
                if (safe == 0) return 0
                var digitsSeen = 0
                grouped.forEachIndexed { index, ch ->
                    if (ch != ',') digitsSeen++
                    if (digitsSeen == safe) return index + 1
                }
                return transformedLength
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, transformedLength)
                return grouped.take(safe).count { it != ',' }.coerceIn(0, originalLength)
            }
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

val LocalVsoftGlass = compositionLocalOf { false }
val LocalVsoftCurrency = compositionLocalOf { "IRT" }

fun Modifier.vsoftGlass(shape: RoundedCornerShape = RoundedCornerShape(22.dp)): Modifier = this
@Composable
fun money(value: Long): String {
    return when (LocalVsoftCurrency.current) {
        "USD" -> NumberFormat.getNumberInstance(Locale.US).format(value) + " $"
        "EUR" -> NumberFormat.getNumberInstance(Locale.US).format(value) + " €"
        "GBP" -> NumberFormat.getNumberInstance(Locale.UK).format(value) + " £"
        "AED" -> NumberFormat.getNumberInstance(Locale.US).format(value) + " AED"
        "TRY" -> NumberFormat.getNumberInstance(Locale.US).format(value) + " ₺"
        "IRR" -> NumberFormat.getNumberInstance(Locale("fa", "IR")).format(value) + " ریال"
        else -> NumberFormat.getNumberInstance(Locale("fa", "IR")).format(value) + " تومان"
    }
}

fun cardCurrentBalance(card: BankCard, transactions: List<Transaction>, workDays: List<WorkDay>): Long {
    // موجودی اولیه کارت فقط همان مبلغی است که کاربر دستی تعیین کرده است.
    // پیامک بانکی هرگز آن را تغییر نمی‌دهد؛ تراکنش تأییدشده در محاسبه موجودی فعلی لحاظ می‌شود.
    val movement = transactions
        .filter { it.card == card.name }
        .sumOf { if (it.type == "income") it.amount else -it.amount }
    val workIncome = workDays.filter { it.card == card.name }.sumOf { it.income }
    return card.openingBalance + movement + workIncome
}

fun today(): String {
    val c=java.util.Calendar.getInstance()
    val j=gregorianToJalali(c.get(java.util.Calendar.YEAR),c.get(java.util.Calendar.MONTH)+1,c.get(java.util.Calendar.DAY_OF_MONTH))
    return "%04d/%02d/%02d".format(Locale.US,j[0],j[1],j[2])
}
fun shiftJalali(date:String,deltaDays:Int):String{
    val p=date.split("/").mapNotNull{it.toIntOrNull()};if(p.size!=3)return date
    var y=p[0];var m=p[1];var d=p[2];var n=kotlin.math.abs(deltaDays);val step=if(deltaDays>=0)1 else -1
    while(n-->0){d+=step;if(step>0&&d>jalaliMonthDays(y,m)){d=1;m++;if(m>12){m=1;y++}};if(step<0&&d<1){m--;if(m<1){m=12;y--};d=jalaliMonthDays(y,m)}}
    return "%04d/%02d/%02d".format(Locale.US,y,m,d)
}
fun dateInRange(date:String,start:String,end:String)=date>=minOf(start,end)&&date<=maxOf(start,end)
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
fun isJalaliLeap(y: Int) = (25 * y + 11) % 33 < 8

fun jalaliMonthDays(y: Int, m: Int) =
    if (m <= 6) 31 else if (m <= 11) 30 else if (isJalaliLeap(y)) 30 else 29
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
                put("card", it.card)
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
                    o.optString("endDate", o.getString("date")),
                    o.optString("card", "")
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
                put("cardNumber", it.cardNumber)
                put("last4", it.last4)
                put("openingBalance", it.openingBalance)
            }
        )
    }

    return array.toString()
}

/**
 * Older releases sometimes replaced the card's manually entered opening balance with
 * the balance reported by a bank SMS. In that legacy format, the JSON field was named
 * "balance". Recover the opening amount only when there is evidence of that old SMS
 * path, and reverse only movements already reflected by the latest dated SMS transaction.
 *
 * If there is no old auto-imported SMS transaction, the legacy balance is presumed to
 * still be the user's manually entered opening balance and is preserved exactly.
 */
internal fun recoverLegacyOpeningBalance(
    cardName: String,
    legacyBalance: Long,
    transactions: List<Transaction>
): Long {
    val indexedCardTransactions = transactions.withIndex().filter { it.value.card == cardName }
    val oldSmsTransactions = indexedCardTransactions.filter {
        it.value.description.startsWith("ثبت خودکار از پیامک")
    }
    if (oldSmsTransactions.isEmpty()) return legacyBalance

    // Confirmation-based legacy SMS rows use epoch-millisecond IDs, so the last
    // confirmation can be identified even when an older message was confirmed later.
    val now = System.currentTimeMillis()
    fun isEpochMillisId(id: Long) = id in 946_684_800_000L..(now + 86_400_000L)
    val lastConfirmedSms = oldSmsTransactions
        .filter { isEpochMillisId(it.value.id) }
        .maxByOrNull { it.value.id }

    val movementsAlreadyReflectedBySms = if (lastConfirmedSms != null) {
        val cutoffDate = lastConfirmedSms.value.date
        indexedCardTransactions
            .filter { (index, transaction) ->
                transaction.date <= cutoffDate && (if (isEpochMillisId(transaction.id)) {
                    // Epoch IDs are creation/confirmation times; don't reverse later
                    // entries merely because the user backdated their transaction.
                    transaction.id <= lastConfirmedSms.value.id
                } else if (transaction.description.startsWith("ثبت خودکار از پیامک")) {
                    // A hash-based legacy SMS row before this confirmation in the
                    // persisted list may already be reflected even on the same date.
                    index <= lastConfirmedSms.index
                } else {
                    // Non-SMS rows with no usable timestamp are only safe to reverse
                    // when their Jalali date is strictly earlier than the SMS date.
                    transaction.date < cutoffDate
                })
            }
            .sumOf { (_, transaction) ->
                if (transaction.type == "income") transaction.amount else -transaction.amount
            }
    } else {
        // The earliest receiver implementation used hash IDs, not timestamps.
        // The persisted list preserves insertion order: only entries through the last
        // legacy SMS row can have been reflected by that SMS. This avoids subtracting
        // a manual transaction added later on the same Jalali date.
        val lastSmsIndex = oldSmsTransactions.maxOf { it.index }
        indexedCardTransactions
            .filter { it.index <= lastSmsIndex }
            .sumOf { (_, transaction) ->
                if (transaction.type == "income") transaction.amount else -transaction.amount
            }
    }

    return legacyBalance - movementsAlreadyReflectedBySms
}

fun decodeCards(
    value: String,
    transactions: List<Transaction> = emptyList()
): MutableList<BankCard> {
    val result = mutableListOf<BankCard>()

    try {
        val array = JSONArray(value)

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val cardName = o.getString("name")
            val openingBalance = when {
                // New format is authoritative: never recalculate or overwrite a manually set opening balance.
                o.has("openingBalance") && !o.isNull("openingBalance") -> o.optLong("openingBalance", 0L)
                // Legacy builds could persist a live bank-reported balance in the old "balance" field.
                o.has("balance") && !o.isNull("balance") ->
                    recoverLegacyOpeningBalance(cardName, o.optLong("balance", 0L), transactions)
                else -> 0L
            }

            result.add(
                BankCard(
                    o.getLong("id"),
                    o.getString("bank"),
                    cardName,
                    o.optString("cardNumber", o.optString("last4", "")),
                    o.optString("last4", "").takeLast(4),
                    openingBalance
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
fun encodeWorkPurchases(list: List<WorkPurchase>): String {
    val array = JSONArray()
    list.forEach {
        array.put(JSONObject().apply {
            put("id", it.id)
            put("title", it.title)
            put("amount", it.amount)
            put("category", it.category)
            put("date", it.date)
            put("workplace", it.workplace)
            put("card", it.card)
            put("note", it.note)
        })
    }
    return array.toString()
}

fun decodeWorkPurchases(value: String): MutableList<WorkPurchase> {
    val result = mutableListOf<WorkPurchase>()
    try {
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(
                WorkPurchase(
                    o.getLong("id"),
                    o.optString("title", ""),
                    o.optLong("amount", 0L),
                    o.optString("category", ""),
                    o.optString("date", today()),
                    o.optString("workplace", ""),
                    o.optString("card", ""),
                    o.optString("note", "")
                )
            )
        }
    } catch (_: Exception) {}
    return result
}


// ---------------- BACKUP ----------------

data class VsoftBackup(
    val transactions: List<Transaction>,
    val workDays: List<WorkDay>,
    val cards: List<BankCard>,
    val people: List<Person>,
    val workplaces: List<Workplace>,
    val workPurchases: List<WorkPurchase> = emptyList(),
    val language: String,
    val currency: String,
    val theme: String,
    val glass: Boolean,
    val font: String,
    val constructionTasks: List<ConstructionTask> = emptyList()
)

fun encodeBackup(data: VsoftBackup): String {
    return JSONObject().apply {
        put("version", 1)
        put("transactions", JSONArray(encodeTransactions(data.transactions)))
        put("workDays", JSONArray(encodeWork(data.workDays)))
        put("cards", JSONArray(encodeCards(data.cards)))
        put("people", JSONArray(encodePeople(data.people)))
        put("workplaces", JSONArray(encodeWorkplaces(data.workplaces)))
         put("workPurchases", JSONArray(encodeWorkPurchases(data.workPurchases)))
        put("constructionTasks", JSONArray(encodeConstructionTasks(data.constructionTasks)))
        put("language", data.language)
        put("currency", data.currency)
        put("theme", data.theme)
        put("glass", data.glass)
        put("font", data.font)
        put("createdAt", System.currentTimeMillis())
    }.toString()
}

fun decodeBackup(value: String): VsoftBackup? {
    return try {
        val o = JSONObject(value)
        val transactions = decodeTransactions(o.optJSONArray("transactions")?.toString() ?: "[]")
        val workDays = decodeWork(o.optJSONArray("workDays")?.toString() ?: "[]")
        val cards = decodeCards(o.optJSONArray("cards")?.toString() ?: "[]", transactions)
        VsoftBackup(
            transactions,
            workDays,
            cards,
            decodePeople(o.optJSONArray("people")?.toString() ?: "[]"),
            decodeWorkplaces(o.optJSONArray("workplaces")?.toString() ?: "[]"),
            decodeWorkPurchases(o.optJSONArray("workPurchases")?.toString() ?: "[]"),
            o.optString("language", "fa"),
            o.optString("currency", "IRT"),
            o.optString("theme", "system"),
            o.optBoolean("glass", false),
            o.optString("font", "sans"),
            decodeConstructionTasks(o.optJSONArray("constructionTasks")?.toString() ?: "[]")
        )
    } catch (_: Exception) {
        null
    }
}

suspend fun restoreBackup(context: Context, data: VsoftBackup) {
    context.dataStore.edit { p ->
        p[TRANSACTIONS_KEY] = encodeTransactions(data.transactions)
        p[WORK_KEY] = encodeWork(data.workDays)
        p[CARDS_KEY] = encodeCards(data.cards)
        p[PEOPLE_KEY] = encodePeople(data.people)
        p[WORKPLACES_KEY] = encodeWorkplaces(data.workplaces)
        p[WORK_PURCHASES_KEY] = encodeWorkPurchases(data.workPurchases)
        p[CONSTRUCTION_TASKS_KEY] = encodeConstructionTasks(data.constructionTasks)
        p[LANGUAGE_KEY] = data.language
        p[CURRENCY_KEY] = data.currency
        p[THEME_KEY] = data.theme
        p[GLASS_KEY] = data.glass.toString()
        p[FONT_KEY] = data.font
    }
}

suspend fun autoRegisterSmsTransaction(context: Context, sms: PendingSms): Boolean {
    // Compatibility wrapper for older call sites: bank SMS must always enter the review queue.
    // Never write a transaction directly from a receiver/background path.
    if (!SmsStore.isEnabled(context)) return false
    return SmsStore.addIfBank(context, sms.sender, sms.body, sms.time)
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
    var currency by remember { mutableStateOf("IRT") }
    var theme by remember { mutableStateOf("system") }
    var glass by remember { mutableStateOf(false) }
    var font by remember { mutableStateOf("sans") }
    var backupRetention by remember { mutableIntStateOf(3) }
    var showRestoreConfirmation by remember { mutableStateOf(false) }

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

    var workPurchases by remember {
        mutableStateOf(mutableListOf<WorkPurchase>())
    }

    var constructionTasks by remember {
        mutableStateOf(mutableListOf<ConstructionTask>())
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "vsoft_encrypted_auto_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<VsoftAutoBackupWorker>(1, TimeUnit.DAYS).build()
        )

        SmsStore.initializeScanCursor(context)

        val preferences = context.dataStore.data.first()

        language = preferences[LANGUAGE_KEY] ?: "fa"
        currency = preferences[CURRENCY_KEY] ?: "IRT"
        theme = preferences[THEME_KEY] ?: "system"
        glass = preferences[GLASS_KEY] == "true"
        font = preferences[FONT_KEY] ?: "sans"
        backupRetention = preferences[BACKUP_RETENTION_KEY]?.toIntOrNull()?.takeIf { it == 3 || it == 5 || it == 10 } ?: 3

        transactions =
            decodeTransactions(preferences[TRANSACTIONS_KEY] ?: "[]")

        workDays =
            decodeWork(preferences[WORK_KEY] ?: "[]")

        cards =
            decodeCards(preferences[CARDS_KEY] ?: "[]", transactions)

        // Persist the normalized card format so legacy live balances are migrated only once.
        context.dataStore.edit { prefs ->
            prefs[CARDS_KEY] = encodeCards(cards)
        }

        people =
            decodePeople(preferences[PEOPLE_KEY] ?: "[]")

        workplaces =
            decodeWorkplaces(preferences[WORKPLACES_KEY] ?: "[]")

        workPurchases =
            decodeWorkPurchases(preferences[WORK_PURCHASES_KEY] ?: "[]")

        constructionTasks = decodeConstructionTasks(preferences[CONSTRUCTION_TASKS_KEY] ?: "[]")

        if (cards.isEmpty()) {
            cards = mutableListOf(
                BankCard(1L, "بانک ملی", "بانک ملی", "", "", 0L),
                BankCard(2L, "بانک مسکن", "بانک مسکن", "", "", 0L),
                BankCard(3L, "بلو بانک", "بلو بانک", "", "", 0L),
                BankCard(4L, "رد بانک", "رد بانک", "", "", 0L),
                BankCard(5L, "بانک مهر", "بانک مهر", "", "", 0L)
            )
        }

        loaded = true
    }

    val darkTheme = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    var pendingRestore by remember { mutableStateOf<VsoftBackup?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val snapshot = VsoftBackup(
                transactions, workDays, cards, people, workplaces, workPurchases,
                language, currency, theme, glass, font, constructionTasks
            )
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(encodeBackup(snapshot).toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (text != null) {
                    val decoded = decodeBackup(text)
                    if (decoded != null) {
                        pendingRestore = decoded
                        showRestoreConfirmation = true
                    }
                }
            }
        }
    }

    val appStrings = strings(language)
    val appFont = when (font) {
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }

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
        LocalLayoutDirection provides layoutDirection,
        LocalVsoftLanguage provides language,
        LocalVsoftCurrency provides currency
    ) {

        val colors = if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF91AAFF),
                onPrimary = Color(0xFF07111F),
                secondary = Color(0xFF65D9BB),
                onSecondary = Color(0xFF062018),
                tertiary = Color(0xFFFFC477),
                onTertiary = Color(0xFF2A1600),
                background = Color(0xFF0B1018),
                onBackground = Color(0xFFF2F6FC),
                surface = Color(0xFF141B26),
                onSurface = Color(0xFFF2F6FC),
                surfaceVariant = Color(0xFF202B3B),
                onSurfaceVariant = Color(0xFFC3CDDB),
                outline = Color(0xFF536177)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF365DDE),
                onPrimary = Color.White,
                secondary = Color(0xFF0E8878),
                onSecondary = Color.White,
                tertiary = Color(0xFFC17A2D),
                onTertiary = Color.White,
                background = Color(0xFFF4F6FA),
                onBackground = Color(0xFF172033),
                surface = Color.White,
                onSurface = Color(0xFF172033),
                surfaceVariant = Color(0xFFE9EDF4),
                onSurfaceVariant = Color(0xFF596579),
                outline = Color(0xFF8995A8)
            )
        }

        val themedColors = if (glass) {
            if (darkTheme) colors.copy(
                background = Color(0xFF0B1018),
                surface = Color(0xCC171F2C),
                surfaceVariant = Color(0x66232E3E)
            ) else colors.copy(
                background = Color(0xFFF0F3F9),
                surface = Color(0xEFFFFFFF),
                surfaceVariant = Color(0xBFFFFFFF)
            )
        } else colors

        MaterialTheme(
            colorScheme = themedColors,
            typography = Typography().let { t -> t.copy(
                titleLarge = t.titleLarge.copy(fontFamily = appFont),
                titleMedium = t.titleMedium.copy(fontFamily = appFont),
                bodyLarge = t.bodyLarge.copy(fontFamily = appFont),
                bodyMedium = t.bodyMedium.copy(fontFamily = appFont),
                bodySmall = t.bodySmall.copy(fontFamily = appFont),
                labelLarge = t.labelLarge.copy(fontFamily = appFont),
                labelMedium = t.labelMedium.copy(fontFamily = appFont),
                labelSmall = t.labelSmall.copy(fontFamily = appFont)
            ) },
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(18.dp),
                large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(28.dp)
            )
        ) {
            CompositionLocalProvider(
                LocalLayoutDirection provides if (language == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
            ) {
                CompositionLocalProvider(LocalVsoftGlass provides glass) {

            MainScreen(
                strings = appStrings,
                language = language,
                theme = theme,
                currency = currency,
                glass = glass,
                font = font,
                transactions = transactions,
                workDays = workDays,
                cards = cards,
                people = people,
                workplaces = workplaces,
                workPurchases = workPurchases,
                constructionTasks = constructionTasks,

                onConstructionTasksChange = {
                    constructionTasks = it
                    scope.launch {
                        context.dataStore.edit { prefs -> prefs[CONSTRUCTION_TASKS_KEY] = encodeConstructionTasks(it) }
                    }
                },

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

                onWorkPurchasesChange = {
                    workPurchases = it
                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[WORK_PURCHASES_KEY] = encodeWorkPurchases(it)
                        }
                    }
                },

                onLanguageChange = {
                    language = it
                    scope.launch {
                        context.dataStore.edit { prefs -> prefs[LANGUAGE_KEY] = it }
                    }
                },
                onCurrencyChange = {
                    currency = it
                    scope.launch { context.dataStore.edit { prefs -> prefs[CURRENCY_KEY] = it } }
                },

                onThemeChange = {
                    theme = it
                    scope.launch { context.dataStore.edit { prefs -> prefs[THEME_KEY] = it } }
                },
                onGlassChange = {
                    glass = it
                    scope.launch { context.dataStore.edit { prefs -> prefs[GLASS_KEY] = it.toString() } }
                },
                onFontChange = {
                    font = it
                    scope.launch { context.dataStore.edit { prefs -> prefs[FONT_KEY] = it } }
                },
                onBackup = { backupLauncher.launch("vsoft-backup.json") },
                onRestore = { restoreLauncher.launch(arrayOf("application/json")) },
                backupRetention = backupRetention,
                onBackupRetentionChange = { value ->
                    backupRetention = value
                    pruneStoredVsoftBackups(context, value)
                    scope.launch { context.dataStore.edit { prefs -> prefs[BACKUP_RETENTION_KEY] = value.toString() } }
                },
                backupSnapshot = VsoftBackup(
                    transactions, workDays, cards, people, workplaces, workPurchases,
                    language, currency, theme, glass, font, constructionTasks
                ),
                onStoredBackupRestore = { data ->
                    scope.launch {
                        restoreBackup(context, data)
                        transactions = data.transactions.toMutableList()
                        workDays = data.workDays.toMutableList()
                        cards = data.cards.toMutableList()
                        people = data.people.toMutableList()
                        workplaces = data.workplaces.toMutableList()
                        workPurchases = data.workPurchases.toMutableList()
                        constructionTasks = data.constructionTasks.toMutableList()
                        language = data.language
                        currency = data.currency
                        theme = data.theme
                        glass = data.glass
                        font = data.font
                    }
                }
            )

            if (showRestoreConfirmation && pendingRestore != null) {
                AlertDialog(
                    onDismissRequest = { showRestoreConfirmation = false; pendingRestore = null },
                    title = { Text("تأیید بازیابی پشتیبان") },
                    text = { Text("با بازیابی این نسخه، اطلاعات فعلی برنامه با اطلاعات پشتیبان جایگزین می‌شود. قبل از ادامه مطمئن شو که نسخه مناسبی انتخاب کرده‌ای.") },
                    confirmButton = {
                        TextButton(onClick = {
                            val data = pendingRestore
                            if (data != null) {
                                scope.launch {
                                    restoreBackup(context, data)
                                    transactions = data.transactions.toMutableList()
                                    workDays = data.workDays.toMutableList()
                                    cards = data.cards.toMutableList()
                                    people = data.people.toMutableList()
                                    workplaces = data.workplaces.toMutableList()
                                    workPurchases = data.workPurchases.toMutableList()
                                    constructionTasks = data.constructionTasks.toMutableList()
                                    language = data.language
                                    currency = data.currency
                                    theme = data.theme
                                    glass = data.glass
                                    font = data.font
                                }
                            }
                            pendingRestore = null
                            showRestoreConfirmation = false
                        }) { Text("تأیید و بازیابی") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingRestore = null; showRestoreConfirmation = false }) { Text("لغو") }
                    }
                )
            }
        }
        }
        }
    }

}

// ---------------- MAIN SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    strings: AppStrings,
    language: String,
    theme: String,
    currency: String,
    glass: Boolean,
    font: String,
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
    workPurchases: List<WorkPurchase>,
    onWorkPurchasesChange: (MutableList<WorkPurchase>) -> Unit,
    constructionTasks: List<ConstructionTask>,
    onConstructionTasksChange: (MutableList<ConstructionTask>) -> Unit,
    onLanguageChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onGlassChange: (Boolean) -> Unit,
    onFontChange: (String) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    backupRetention: Int,
    onBackupRetentionChange: (Int) -> Unit,
    backupSnapshot: VsoftBackup,
    onStoredBackupRestore: (VsoftBackup) -> Unit
) {

    var selectedPage by rememberSaveable { mutableStateOf(0) }
    var selectedWorkplaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    val pageStateHolder = rememberSaveableStateHolder()

    val pages = listOf(
        strings.dashboard,
        strings.finance,
        strings.work,
        strings.reports,
        strings.settings,
        "کارت‌ها",
        "محل‌های کار",
        strings.people,
        if (language == "en") "Tools" else if (language == "ar") "الأدوات" else "ابزارها",
        "پیامک بانکی",
        if (language == "en") "Work purchases" else if (language == "ar") "مشتريات العمل" else "خریدهای کار",
        if (language == "en") "Project details" else if (language == "ar") "تفاصيل المشروع" else "جزئیات پروژه"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selectedPage != 0) {
                Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                    Row(
                        Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 18.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("V", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("VSOFT", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (language == "en") "FINANCE  ·  WORK  ·  REPORTS" else "مدیریت مالی و کار", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (selectedPage == 10 || selectedPage == 11) {
                            IconButton(onClick = { selectedPage = 2 }, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.surface)) {
                                Icon(Icons.Default.ArrowForward, contentDescription = if (language == "en") "Back to work" else "بازگشت به کار", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(5.dp))
                        }
                        IconButton(onClick = { selectedPage = 4 }, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .10f))) {
                            Icon(Icons.Default.Settings, contentDescription = strings.settings, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 9.dp),
                shape = RoundedCornerShape(27.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .09f)),
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 5.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RedesignNavItem(strings.dashboard, Icons.Default.Home, selectedPage == 0, Modifier.weight(1f)) { selectedPage = 0 }
                    RedesignNavItem(strings.finance, Icons.Default.AccountBalanceWallet, selectedPage == 1, Modifier.weight(1f)) { selectedPage = 1 }
                    RedesignNavItem(strings.work, Icons.Default.WorkOutline, selectedPage == 2 || selectedPage == 10 || selectedPage == 11, Modifier.weight(1f)) { selectedPage = 2 }
                    RedesignNavItem(strings.reports, Icons.Default.Insights, selectedPage == 3, Modifier.weight(1f)) { selectedPage = 3 }
                    RedesignNavItem(if (language == "en") "More" else if (language == "ar") "المزيد" else "بیشتر", Icons.Default.GridView, selectedPage in 5..9, Modifier.weight(1f)) { selectedPage = 8 }
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {
        AnimatedContent(
            targetState = selectedPage,
            transitionSpec = {
                // Direction is language-aware: Persian/Arabic pages move in the RTL direction,
                // while English pages keep the conventional LTR navigation motion.
                val forward = targetState > initialState
                val direction = when {
                    language == "en" && forward -> 1
                    language == "en" && !forward -> -1
                    language != "en" && forward -> -1
                    else -> 1
                }
                (slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = 520f
                    ),
                    initialOffsetX = { direction * it / 5 }
                ) + fadeIn(tween(180)) + scaleIn(
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 520f),
                    initialScale = 0.985f
                )) togetherWith
                (slideOutHorizontally(
                    animationSpec = tween(170, easing = FastOutSlowInEasing),
                    targetOffsetX = { -direction * it / 12 }
                ) + fadeOut(tween(120)) + scaleOut(
                    animationSpec = tween(160, easing = FastOutSlowInEasing),
                    targetScale = 0.99f
                ))
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            label = "page"
        ) { page ->
            pageStateHolder.SaveableStateProvider(page) {
            when (page) {

                0 -> DashboardPage(
                    strings,
                    transactions,
                    workDays,
                    cards,
                    onNavigate = { destination -> selectedPage = destination }
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
                    cards,
                    constructionTasks,
                    onWorkChange,
                    onWorkPurchases = { selectedPage = 10 },
                    onOpenWorkplace = { place -> selectedWorkplaceId = place.id; selectedPage = 11 }
                )

                3 -> ReportsPage(strings, transactions, workDays, cards)

                4 -> SettingsPage(
                    strings, language, theme, currency,
                    onLanguageChange, onCurrencyChange, onThemeChange,
                    glass, onGlassChange,
                    font, onFontChange,
                    onBackup = onBackup,
                    onRestore = onRestore,
                    backupRetention = backupRetention,
                    onBackupRetentionChange = onBackupRetentionChange,
                    backupSnapshot = backupSnapshot,
                    onStoredBackupRestore = onStoredBackupRestore
                )
                5 -> CardsPage(cards, transactions, workDays, onCardsChange)
                6 -> WorkplacesPage(workplaces, onWorkplacesChange)
                7 -> PeoplePage(people, onPeopleChange)
                9 -> SmsImportPage(
                    currency = currency,
                    cards = cards,
                    transactions = transactions,
                    onTransactionsChange = onTransactionsChange,
                    onCardsChange = onCardsChange
                )

                8 -> VsoftToolsPage(
                    language = language,
                    transactions = transactions,
                    workDays = workDays,
                    cards = cards,
                    people = people,
                    workplaces = workplaces,
                    onNavigate = { destination -> selectedPage = destination }
                )
                11 -> selectedWorkplaceId?.let { id ->
                    workplaces.firstOrNull { it.id == id }?.let { place ->
                        ConstructionTasksPage(
                            workplace = place,
                            tasks = constructionTasks.filter { it.workplaceId == place.id },
                            people = people,
                            onTasksChange = { changed ->
                                val otherTasks = constructionTasks.filterNot { it.workplaceId == place.id }
                                onConstructionTasksChange((otherTasks + changed).toMutableList())
                            }
                        )
                    }
                }
                10 -> WorkPurchasesPage(
                    purchases = workPurchases,
                    cards = cards,
                    workplaces = workplaces,
                    onPurchasesChange = onWorkPurchasesChange,
                    onTransactionsChange = onTransactionsChange,
                    transactions = transactions
                )
            }
            }
        }
        }
    }
}


@Composable
fun RedesignNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else Color.Transparent,
        animationSpec = tween(180),
        label = "redesign_nav_background"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "redesign_nav_tint"
    )
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(background).clickable(onClick = onClick).padding(horizontal = 2.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(if (selected) 23.dp else 21.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = iconTint, fontSize = 10.sp, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun Modifier.pressScale(
    pressedScale: Float = 0.96f
): Modifier {
    val scaleState = remember { mutableFloatStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = scaleState.floatValue,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "press_scale"
    )
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(pressedScale) {
            detectTapGestures(
                onPress = {
                    scaleState.floatValue = pressedScale
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    try {
                        tryAwaitRelease()
                    } finally {
                        scaleState.floatValue = 1f
                    }
                }
            )
        }
}
@Composable
fun TopActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 480f),
        label = "top_action_scale"
    )
    IconButton(onClick = onClick, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        Box(
            Modifier.size(38.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (selected) .16f else .07f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AnimatedNavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 420f
        ),
        label = "nav_scale"
    )
    val containerAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(180),
        label = "nav_indicator"
    )

    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.10f * containerAlpha
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
    }
}

@Composable
fun VsoftEntrance(index: Int, content: @Composable () -> Unit) {
    val visible = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { visible.targetState = true }
    // No per-row stagger: rows entering the viewport must be visible immediately.
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(90)),
        exit = fadeOut(tween(70))
    ) { content() }
}

// ---------------- DASHBOARD ----------------

@Composable
fun DashboardPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>,
    onNavigate: (Int) -> Unit = {}
) {
    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val workIncome = workDays.sumOf { it.income }
    val balance = cards.sumOf { it.openingBalance } + income + workIncome - expense
    val totalHours = workDays.sumOf { calculateHours(it.start, it.end) }
    val monthPrefix = today().substringBeforeLast("/") + "/"
    val monthWorkDays = workDays.filter { it.date.startsWith(monthPrefix) }
    val monthWorkIncome = monthWorkDays.sumOf { it.income }
    val monthExpenses = transactions.filter { it.type == "expense" && it.date.startsWith(monthPrefix) }.sumOf { it.amount }
    val monthIncome = transactions.filter { it.type == "income" && it.date.startsWith(monthPrefix) }.sumOf { it.amount }
    val monthNet = monthWorkIncome + monthIncome - monthExpenses
    val recent = transactions.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id }).take(5)
    var balanceVisible by rememberSaveable { mutableStateOf(true) }
    val monthInflow = monthIncome + monthWorkIncome
    val expenseShare = if (monthInflow > 0L) (monthExpenses.toFloat() / monthInflow.toFloat()).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(17.dp))
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Wallet, null, tint = Color.White, modifier = Modifier.size(25.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(if (LocalVsoftLanguage.current == "en") "YOUR OVERVIEW" else "نمای کلی حساب و کار", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp, color = MaterialTheme.colorScheme.primary)
                    Text(if (LocalVsoftLanguage.current == "en") "Good to see you" else "همه‌چیز مرتب و یک‌جا", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(today(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onNavigate(4) }, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surface)) {
                    Icon(Icons.Default.Tune, contentDescription = strings.settings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(3) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF172746), Color(0xFF254C79), Color(0xFF147C78)))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(if (LocalVsoftLanguage.current == "en") "TOTAL AVAILABLE BALANCE" else "موجودی فعلی همه حساب‌ها", color = Color.White.copy(alpha = .76f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (balanceVisible) money(balance) else "••••••••", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                            Text(if (LocalVsoftLanguage.current == "en") "Tap to open your financial report" else "برای مشاهده جزئیات، گزارش مالی را باز کن", color = Color.White.copy(alpha = .67f), fontSize = 10.sp)
                        }
                        IconButton(onClick = { balanceVisible = !balanceVisible }, modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .13f))) {
                            Icon(if (balanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = if (balanceVisible) "پنهان کردن موجودی" else "نمایش موجودی", tint = Color.White)
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = .18f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SouthWest, null, tint = Color(0xFF8DF0CD), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(if (LocalVsoftLanguage.current == "en") "Income" else "درآمد ثبت‌شده", color = Color.White.copy(alpha = .76f), fontSize = 11.sp)
                            }
                            Text(if (balanceVisible) money(income + workIncome) else "••••••", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = .2f)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NorthEast, null, tint = Color(0xFFFFC3B9), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(if (LocalVsoftLanguage.current == "en") "Expenses" else "هزینه ثبت‌شده", color = Color.White.copy(alpha = .76f), fontSize = 11.sp)
                            }
                            Text(if (balanceVisible) money(expense) else "••••••", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (LocalVsoftLanguage.current == "en") "Quick actions" else "کارهای پرکاربرد", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onNavigate(8) }) {
                    Text(if (LocalVsoftLanguage.current == "en") "All tools" else "همه ابزارها", fontSize = 12.sp)
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(15.dp))
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    DashboardQuickAction("ثبت تراکنش", Icons.Default.AddCard, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onNavigate(1) }
                    DashboardQuickAction("ثبت روز کاری", Icons.Default.WorkHistory, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { onNavigate(2) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    DashboardQuickAction("گزارش مالی", Icons.Default.QueryStats, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { onNavigate(3) }
                    DashboardQuickAction("کارت‌های بانکی", Icons.Default.CreditCard, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onNavigate(5) }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(3) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .08f))
            ) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(if (LocalVsoftLanguage.current == "en") "This month" else "وضعیت این ماه", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(if (LocalVsoftLanguage.current == "en") "Money in vs. money out" else "مقایسه درآمد و هزینه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(shape = RoundedCornerShape(13.dp), color = (if (monthNet >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error).copy(alpha = .12f)) {
                            Text((if (monthNet >= 0) "+" else "−") + money(kotlin.math.abs(monthNet)), modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = if (monthNet >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.secondary.copy(alpha = .09f)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(if (LocalVsoftLanguage.current == "en") "Income" else "درآمد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (balanceVisible) money(monthInflow) else "••••••", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = .07f)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(if (LocalVsoftLanguage.current == "en") "Expenses" else "هزینه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (balanceVisible) money(monthExpenses) else "••••••", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (LocalVsoftLanguage.current == "en") "Expenses / income" else "سهم هزینه از درآمد", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text("${(expenseShare * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = { expenseShare }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(10.dp)), color = if (expenseShare >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardMiniStat("روزهای کاری", monthWorkDays.size.toString(), Icons.Default.EventAvailable, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        DashboardMiniStat("ساعت کاری", String.format(Locale.US, "%.1f", totalHours), Icons.Default.Schedule, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    }
                }
            }
        }
        if (cards.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(if (LocalVsoftLanguage.current == "en") "Your cards" else "کارت‌های بانکی", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(if (LocalVsoftLanguage.current == "en") "Tap a card to manage it" else "برای مدیریت کارت لمس کن", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onNavigate(5) }) { Text(if (LocalVsoftLanguage.current == "en") "Manage" else "مدیریت") }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(11.dp), contentPadding = PaddingValues(horizontal = 1.dp)) {
                    items(cards, key = { it.id }) { card ->
                        MiniBankCard(card = card, balance = cardCurrentBalance(card, transactions, workDays), onClick = { onNavigate(5) })
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(if (LocalVsoftLanguage.current == "en") "Recent transactions" else "تراکنش‌های اخیر", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(if (LocalVsoftLanguage.current == "en") "Latest recorded activity" else "آخرین فعالیت‌های ثبت‌شده", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onNavigate(1) }) { Text(if (LocalVsoftLanguage.current == "en") "View all" else "مشاهده همه") }
            }
        }
        if (recent.isEmpty()) {
            item { EmptyState(if (LocalVsoftLanguage.current == "en") "No transactions yet" else "هنوز تراکنشی ثبت نشده", Icons.Default.ReceiptLong) }
        } else {
            items(recent, key = { it.id }) { transaction ->
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onNavigate(1) }) {
                    TransactionCard(transaction, onDelete = {}, showActions = false)
                }
            }
        }
    }
}

@Composable
fun DashboardQuickAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .08f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun DashboardPill(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = .13f))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = .86f), modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(title, color = Color.White.copy(alpha = .66f), fontSize = 9.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun DashboardMiniStat(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = .08f))
            .padding(11.dp)
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(19.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun DashboardMetric(
    title: String, value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color, modifier: Modifier
) {
    Card(
        modifier = modifier.vsoftGlass(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = .07f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(11.dp))
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
fun MiniBankCard(card: BankCard, balance: Long, onClick: () -> Unit = {}) {
    val shape = RoundedCornerShape(26.dp)
    val (startColor, endColor) = bankCardColors(card.bank)
    Card(Modifier.width(270.dp).vsoftGlass(shape).clickable(onClick = onClick).pressScale(0.985f), shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Box(Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(startColor.copy(alpha = .98f), startColor.copy(alpha = .88f), endColor.copy(alpha = .94f)))
        ).padding(18.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(card.bank.ifBlank { uiText("کارت") }, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text(card.name.ifBlank { "Vsoft" }, fontSize = 11.sp, color = Color.White.copy(alpha = .78f))
                    }
                    Icon(Icons.Default.Contactless, null, tint = Color.White.copy(alpha = .82f), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    if (card.cardNumber.isNotBlank()) "••••  ••••  ••••  " + card.cardNumber.filter(Char::isDigit).takeLast(4)
                    else "••••  ••••  ••••  ••••",
                    letterSpacing = 1.6.sp, fontSize = 14.sp, color = Color.White.copy(alpha = .95f)
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(uiText("موجودی فعلی"), fontSize = 10.sp, color = Color.White.copy(alpha = .70f))
                        Text(money(balance), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Text("VSOFT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = Color.White.copy(alpha = .62f))
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(uiText("با دکمه + اولین مورد را اضافه کنید"),
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
    var show by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<Transaction?>(null) }
    var receiptTransaction by remember { mutableStateOf<Transaction?>(null) }
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }
    val normalizedSearch = normalizeVsoftSearch(search)
    val list = transactions.filter {
        (filter == "all" || it.type == filter) &&
        (normalizedSearch.isBlank() ||
            normalizeVsoftSearch(it.description).contains(normalizedSearch) ||
            normalizeVsoftSearch(it.category).contains(normalizedSearch) ||
            normalizeVsoftSearch(it.person).contains(normalizedSearch) ||
            normalizeVsoftSearch(it.card).contains(normalizedSearch) ||
            normalizeVsoftSearch(it.date).contains(normalizedSearch))
    }.sortedWith(
        compareByDescending<Transaction> { it.date }
            .thenByDescending { it.id }
    )
    // Preserve newest-first ordering, then render each calendar date as its own section.
    val transactionGroups = list.groupBy { it.date }
    val incomeTotal = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expenseTotal = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val netTotal = incomeTotal - expenseTotal

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "finance_header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().background(
                                Brush.linearGradient(listOf(Color(0xFF18284E), Color(0xFF315AC1), Color(0xFF287F86)))
                            ).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(uiText("امور مالی"), color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
                                    Text(uiText("تراکنش‌های تو"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        transactions.size.toString() + " " + if (LocalVsoftLanguage.current == "en") "transactions" else "تراکنش ثبت‌شده",
                                        color = Color.White.copy(alpha = .75f), fontSize = 11.sp
                                    )
                                }
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(25.dp))
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = .18f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(uiText("درآمد"), color = Color.White.copy(alpha = .72f), fontSize = 11.sp)
                                    Text(money(incomeTotal), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(uiText("هزینه"), color = Color.White.copy(alpha = .72f), fontSize = 11.sp)
                                    Text(money(expenseTotal), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(uiText("خالص"), color = Color.White.copy(alpha = .72f), fontSize = 11.sp)
                                    Text(money(netTotal), color = if (netTotal >= 0) Color(0xFF9AF0CF) else Color(0xFFFFC0B7), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        search, { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        label = { Text(strings.search) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = if (search.isNotBlank()) ({
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Default.Close, uiText("پاک کردن"))
                            }
                        }) else null
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        FilterChip(filter == "all", { filter = "all" },
                            label = { Text(if (LocalVsoftLanguage.current == "en") "All" else "همه") })
                        FilterChip(filter == "income", { filter = "income" }, label = { Text(strings.income) })
                        FilterChip(filter == "expense", { filter = "expense" }, label = { Text(strings.expense) })
                    }
                }
            }

            if (list.isEmpty()) {
                item(key = "finance_empty") {
                    Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        EmptyState(uiText("تراکنشی با این فیلتر پیدا نشد"), Icons.Default.SearchOff)
                    }
                }
            } else {
                transactionGroups.forEach { (transactionDate, entries) ->
                    item(key = "finance_date_$transactionDate") {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    transactionDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                val dayIncome = entries.filter { it.type == "income" }.sumOf { it.amount }
                                val dayExpense = entries.filter { it.type == "expense" }.sumOf { it.amount }
                                Text(
                                    if (LocalVsoftLanguage.current == "en")
                                        "${entries.size} transactions • +${money(dayIncome)} • −${money(dayExpense)}"
                                    else "${entries.size} تراکنش • درآمد ${money(dayIncome)} • هزینه ${money(dayExpense)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        }
                    }
                    itemsIndexed(entries, key = { _, it -> it.id }) { index, t ->
                        VsoftEntrance(index.coerceAtMost(7)) {
                            VsoftSwipeToDelete(
                                onDelete = {
                                    val x = transactions.toMutableList()
                                    x.removeAll { it.id == t.id }
                                    onTransactionsChange(x)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "تراکنش حذف شد",
                                            actionLabel = "بازگردانی",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onTransactionsChange((x + t).sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id }).toMutableList())
                                        }
                                    }
                                }
                            ) {
                                TransactionCard(
                                    t,
                                    onDelete = {
                                        val x = transactions.toMutableList()
                                        x.removeAll { it.id == t.id }
                                        onTransactionsChange(x)
                                    },
                                    onEdit = { edit = t; show = true },
                                    onReceipt = { receiptTransaction = t }
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp)
        )
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp).pressScale(),
            onClick = { edit = null; show = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Add, "Add")
        }
    }

    if (show) AddTransactionDialog(strings, cards, people, edit, { show = false }) { t ->
        val x = transactions.toMutableList()
        val i = x.indexOfFirst { it.id == t.id }
        if (i >= 0) x[i] = t else x.add(t)
        onTransactionsChange(x)
        show = false
    }
    // The receipt icon stores the selected transaction; render its dialog here so taps
    // actually open the receipt instead of silently doing nothing.
    receiptTransaction?.let { selected ->
        TransactionReceiptDialog(selected) { receiptTransaction = null }
    }
}


@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    onReceipt: () -> Unit = {},
    showActions: Boolean = true
) {
    val isIncome = transaction.type == "income"
    val accent = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    var actionsExpanded by remember(transaction.id) { mutableStateOf(false) }
    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "transaction_icon_scale"
    )
    Card(
        Modifier.fillMaxWidth().animateContentSize().pressScale(0.992f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = .15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(50.dp).clip(RoundedCornerShape(17.dp))
                    .background(accent.copy(alpha = .10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null, tint = accent, modifier = Modifier.size(23.dp * iconScale)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(transaction.category, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                if (transaction.description.isNotBlank()) {
                    Text(transaction.description, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(9.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f)) {
                        Text(transaction.date, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                    }
                    if (transaction.card.isNotBlank()) {
                        Text("• " + transaction.card, fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (isIncome) "+" else "−") + money(transaction.amount),
                    color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                )
                if (showActions) {
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onReceipt, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ReceiptLong, uiText("رسید"), modifier = Modifier.size(17.dp))
                        }
                        Box {
                            IconButton(onClick = { actionsExpanded = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.MoreVert, uiText("گزینه‌های تراکنش"), modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(
                                expanded = actionsExpanded,
                                onDismissRequest = { actionsExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(uiText("ویرایش تراکنش")) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    onClick = { actionsExpanded = false; onEdit() }
                                )
                                DropdownMenuItem(
                                    text = { Text(uiText("حذف تراکنش")) },
                                    leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { actionsExpanded = false; onDelete() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- ADD TRANSACTION ----------------
// ---------------- ADD TRANSACTION ----------------

@Composable
fun AddTransactionDialog(
    strings: AppStrings, cards: List<BankCard>, people: List<Person>, existing: Transaction?,
    onDismiss: () -> Unit, onSave: (Transaction) -> Unit
) {
    var type by remember { mutableStateOf(existing?.type ?: "expense") }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: today()) }
    var card by remember { mutableStateOf(existing?.card ?: "") }
    var person by remember { mutableStateOf(existing?.person ?: "") }
    var dateOpen by remember { mutableStateOf(false) }
    var cardOpen by remember { mutableStateOf(false) }
    var categoryOpen by remember { mutableStateOf(false) }
    var newCategoryOpen by remember { mutableStateOf(false) }
    var customCategories by remember { mutableStateOf(emptyList<String>()) }
    var customFinanceCategories by remember { mutableStateOf(emptyList<String>()) }
    var personOpen by remember { mutableStateOf(false) }
    val financeCategories = if (type == "income") listOf(
        "حقوق", "درآمد کاری", "درآمد پروژه", "درآمد کسب‌وکار", "پاداش", "هدیه", "بازگشت وجه", "سود بانکی", "انتقال وجه", "سایر درآمدها"
    ) else listOf(
        "خوراک و مواد غذایی", "رستوران و کافه", "خرید روزمره", "حمل‌ونقل", "سوخت", "تاکسی و تاکسی اینترنتی",
        "اجاره و مسکن", "آب و برق و گاز", "اینترنت و موبایل", "خرید لباس", "سلامت و دارو", "آموزش",
        "تفریح و سرگرمی", "سفر", "قبض و پرداختی", "قسط و بدهی", "کارمزد بانکی", "خرید لوازم خانه",
        "لوازم الکترونیکی", "مراقبت شخصی", "هزینه کاری", "هدیه و کمک", "مالیات", "سایر هزینه‌ها"
    )
    val allFinanceCategories = (financeCategories + customFinanceCategories).distinct()
    val typeColor by animateColorAsState(
        if (type == "income") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
        label = "transaction_type_color"
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(30.dp),
            color = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .94f) else MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp, shadowElevation = 20.dp
        ) {
            Column(
                Modifier.padding(22.dp).heightIn(max = 680.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                VsoftFormHeader(
                    title = if (existing == null) uiText("ثبت تراکنش جدید") else uiText("ویرایش تراکنش"),
                    subtitle = uiText("اطلاعات مالی را دقیق و سریع ثبت کنید"),
                    icon = Icons.Default.ReceiptLong,
                    accent = typeColor,
                    onDismiss = onDismiss
                )
                VsoftFormSectionLabel(
                    title = "نوع تراکنش",
                    subtitle = "درآمد یا هزینه را انتخاب کنید",
                    icon = Icons.Default.SwapVert
                )
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    FilterChip(
                        selected = type == "expense", onClick = { type = "expense" },
                        label = { Text(strings.expense, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.TrendingDown, null, Modifier.size(17.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "income", onClick = { type = "income" },
                        label = { Text(strings.income, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, null, Modifier.size(17.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                AnimatedContent(
                    targetState = type,
                    transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(90)) },
                    label = "transaction_type_hint"
                ) { currentType ->
                    Surface(
                        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        color = if (currentType == "income") MaterialTheme.colorScheme.secondary.copy(alpha = .08f)
                        else MaterialTheme.colorScheme.error.copy(alpha = .08f)
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (currentType == "income") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                null,
                                tint = if (currentType == "income") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (currentType == "income") "این مورد به عنوان درآمد ثبت می‌شود" else "این مورد به عنوان هزینه ثبت می‌شود",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = typeColor.copy(alpha = .065f),
                    border = BorderStroke(1.dp, typeColor.copy(alpha = .20f))
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, null, tint = typeColor, modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("مبلغ تراکنش", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text("مبلغ را وارد کنید؛ جداکننده‌ها خودکار نمایش داده می‌شوند",
                                    fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        OutlinedTextField(
                            amount, { amount = normalizeAmountInput(it) },
                            label = { Text(strings.amount) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = GroupedNumberVisualTransformation(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = typeColor,
                                focusedLabelColor = typeColor,
                                cursorColor = typeColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(17.dp)
                        )
                    }
                }
                VsoftFormSectionLabel(
                    title = "جزئیات تراکنش",
                    subtitle = "دسته‌بندی و توضیحات را مشخص کنید",
                    icon = Icons.Default.Label
                )
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        { categoryOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Label, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(if (category.isBlank()) uiText("دسته‌بندی") else category,
                                fontWeight = if (category.isBlank()) FontWeight.Normal else FontWeight.SemiBold, maxLines = 1)
                            if (category.isNotBlank()) Text(uiText("دسته‌بندی انتخاب شد"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(categoryOpen, { categoryOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(uiText("افزودن دسته‌بندی جدید")) },
                            leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) },
                            onClick = { categoryOpen = false; newCategoryOpen = true }
                        )
                        allFinanceCategories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                leadingIcon = { Icon(Icons.Default.Label, null, Modifier.size(18.dp)) },
                                onClick = { category = item; categoryOpen = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    description, { description = it }, label = { Text(strings.description) },
                    leadingIcon = { Icon(Icons.Default.Notes, null) }, minLines = 2, maxLines = 3,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)
                )
                VsoftFormSectionLabel(
                    title = "اطلاعات تکمیلی",
                    subtitle = "تاریخ، کارت و شخص مرتبط (اختیاری)",
                    icon = Icons.Default.Tune
                )
                OutlinedButton(
                    { dateOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(strings.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(date, fontWeight = FontWeight.Bold)
                    }
                    Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight, null)
                }
                val selectedCard = cards.firstOrNull { it.name == card }
                if (selectedCard != null) {
                    val colors = bankCardColors(selectedCard.bank)
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(colors.first.copy(alpha = .94f), colors.second.copy(alpha = .90f))))
                            .padding(13.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(selectedCard.bank, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                Text(selectedCard.name, color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
                            }
                            Text("•••• " + selectedCard.last4.ifBlank { selectedCard.cardNumber.takeLast(4) }, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        { cardOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(if (card.isBlank()) uiText("انتخاب کارت مبدا / مقصد") else card,
                                fontWeight = if (card.isBlank()) FontWeight.Normal else FontWeight.SemiBold)
                            Text(if (card.isBlank()) "برای محاسبه موجودی، کارت را مشخص کنید" else "کارت انتخاب‌شده",
                                fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(cardOpen, { cardOpen = false }) {
                        DropdownMenuItem(text = { Text(uiText("بدون کارت")) }, leadingIcon = { Icon(Icons.Default.Block, null, Modifier.size(18.dp)) },
                            onClick = { card = ""; cardOpen = false })
                        cards.forEach { q ->
                            DropdownMenuItem(text = { Text(q.name) }, leadingIcon = { Icon(Icons.Default.CreditCard, null, Modifier.size(18.dp)) },
                                onClick = { card = q.name; cardOpen = false })
                        }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        { personOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(if (person.isBlank()) uiText("انتخاب شخص") else person,
                                fontWeight = if (person.isBlank()) FontWeight.Normal else FontWeight.SemiBold)
                            Text(if (person.isBlank()) "اختیاری" else "شخص مرتبط با تراکنش",
                                fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(personOpen, { personOpen = false }) {
                        DropdownMenuItem(text = { Text(uiText("بدون شخص")) }, leadingIcon = { Icon(Icons.Default.PersonOff, null, Modifier.size(18.dp)) },
                            onClick = { person = ""; personOpen = false })
                        people.forEach { q ->
                            DropdownMenuItem(text = { Text(q.name) }, leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(18.dp)) },
                                onClick = { person = q.name; personOpen = false })
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onDismiss, Modifier.weight(.78f).height(52.dp), shape = RoundedCornerShape(17.dp)
                    ) { Text(strings.cancel) }
                    Button(
                        onClick = {
                            val v = normalizeDigits(amount).toLongOrNull() ?: 0L
                            if (v > 0 && category.isNotBlank())
                                onSave(Transaction(existing?.id ?: System.currentTimeMillis(), type, v, category, description, date, card, person))
                        },
                        enabled = normalizeDigits(amount).toLongOrNull()?.let { it > 0 } == true && category.isNotBlank(),
                        modifier = Modifier.weight(1.22f).height(52.dp).pressScale(),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = typeColor, contentColor = Color.White)
                    ) { Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(7.dp)); Text(strings.save, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
    if (newCategoryOpen) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { newCategoryOpen = false },
            title = { Text(uiText("دسته‌بندی جدید")) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(uiText("نام دسته‌بندی")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newName.trim()
                    if (name.isNotBlank()) {
                        customFinanceCategories = (customFinanceCategories + name).distinct()
                        category = name
                    }
                    newCategoryOpen = false
                }) { Text(uiText("افزودن")) }
            },
            dismissButton = { TextButton(onClick = { newCategoryOpen = false }) { Text(strings.cancel) } }
        )
    }
    if (newCategoryOpen) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { newCategoryOpen = false },
            title = { Text(uiText("دسته‌بندی جدید")) },
            text = { OutlinedTextField(newName, { newName = it }, label = { Text(uiText("نام دسته‌بندی")) }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    val name = newName.trim()
                    if (name.isNotBlank()) { customCategories = (customCategories + name).distinct(); category = name }
                    newCategoryOpen = false
                }) { Text(uiText("افزودن")) }
            },
            dismissButton = { TextButton(onClick = { newCategoryOpen = false }) { Text(strings.cancel) } }
        )
    }
    if (dateOpen) JalaliDatePickerDialog(date, { dateOpen = false }) { date = it; dateOpen = false }
}


// ---------------- WORK PURCHASES ----------------

@Composable
fun WorkPurchasesPage(
    purchases: List<WorkPurchase>,
    cards: List<BankCard>,
    workplaces: List<Workplace>,
    transactions: List<Transaction>,
    onPurchasesChange: (MutableList<WorkPurchase>) -> Unit,
    onTransactionsChange: (MutableList<Transaction>) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<WorkPurchase?>(null) }
    val total = purchases.sumOf { it.amount }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    VsoftPremiumPageHero(
                        title = uiText("خریدهای کار"),
                        subtitle = uiText("ابزار، تجهیزات، قطعات و سایر خریدهای مرتبط با کار"),
                        icon = Icons.Default.ShoppingCart,
                        accent = Color(0xFFB65B45)
                    )
                    Card(
                        Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DashboardMetric(
                                "تعداد خرید", purchases.size.toString(), Icons.Default.ShoppingCart,
                                MaterialTheme.colorScheme.primary, Modifier.weight(1f)
                            )
                            DashboardMetric(
                                "مجموع هزینه", money(total), Icons.Default.Payments,
                                MaterialTheme.colorScheme.error, Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            if (purchases.isEmpty()) {
                item { EmptyState("هنوز خرید کاری ثبت نشده", Icons.Default.ShoppingCart) }
            } else {
                itemsIndexed(purchases.sortedByDescending { it.id }, key = { _, it -> it.id }) { index, purchase ->
                    VsoftEntrance(index.coerceAtMost(7)) {
                        VsoftSwipeToDelete(
                            onDelete = {
                                val next = purchases.toMutableList().apply { removeAll { it.id == purchase.id } }
                                onPurchasesChange(next)
                                val tx = transactions.toMutableList().apply { removeAll { it.id == purchase.id } }
                                onTransactionsChange(tx)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar("خرید کار حذف شد","بازگردانی",duration=SnackbarDuration.Short)
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onPurchasesChange((next + purchase).toMutableList())
                                        onTransactionsChange((tx + Transaction(
                                            id = purchase.id,
                                            type = "expense",
                                            amount = purchase.amount,
                                            category = purchase.category,
                                            description = "خرید کار: " + purchase.title,
                                            date = purchase.date,
                                            card = purchase.card,
                                            person = ""
                                        )).toMutableList())
                                    }
                                }
                            }
                        ) {
                            Card(
                                Modifier.fillMaxWidth().clickable { editing = purchase; show = true },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
                            ) {
                                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = .10f)),
                                            contentAlignment = Alignment.Center
                                        ) { Icon(Icons.Default.ShoppingCart, null, tint = MaterialTheme.colorScheme.error) }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(purchase.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                            Text(purchase.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { editing = purchase; show = true }) {
                                            Icon(Icons.Default.Edit, uiText("ویرایش"), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        AssistChip(onClick = {}, label = { Text(purchase.category) }, leadingIcon = { Icon(Icons.Default.Label, null, Modifier.size(15.dp)) })
                                        if (purchase.workplace.isNotBlank())
                                            AssistChip(onClick = {}, label = { Text(purchase.workplace) }, leadingIcon = { Icon(Icons.Default.Place, null, Modifier.size(15.dp)) })
                                    }
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(money(purchase.amount), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                                        if (purchase.card.isNotBlank())
                                            Text(purchase.card, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (purchase.note.isNotBlank())
                                        Text(purchase.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp),
            onClick = { editing = null; show = true },
            containerColor = MaterialTheme.colorScheme.primary
        ) { Icon(Icons.Default.Add, "افزودن خرید") }
    }

    if (show) {
        WorkPurchaseDialog(editing, cards, workplaces, onDismiss = { show = false }) { purchase ->
            val next = purchases.toMutableList()
            val index = next.indexOfFirst { it.id == purchase.id }
            if (index >= 0) next[index] = purchase else next.add(purchase)
            onPurchasesChange(next)

            val txNext = transactions.toMutableList()
            val transaction = Transaction(
                id = purchase.id,
                type = "expense",
                amount = purchase.amount,
                category = "خرید کار",
                description = "خرید کار: " + purchase.title,
                date = purchase.date,
                card = purchase.card,
                person = ""
            )
            val txIndex = txNext.indexOfFirst { it.id == purchase.id }
            if (txIndex >= 0) txNext[txIndex] = transaction else txNext.add(transaction)
            onTransactionsChange(txNext)
            show = false
        }
    }
}

@Composable
fun WorkPurchaseDialog(
    existing: WorkPurchase?,
    cards: List<BankCard>,
    workplaces: List<Workplace>,
    onDismiss: () -> Unit,
    onSave: (WorkPurchase) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "ابزار و تجهیزات") }
    var date by remember { mutableStateOf(existing?.date ?: today()) }
    var workplace by remember { mutableStateOf(existing?.workplace ?: "") }
    var card by remember { mutableStateOf(existing?.card ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var categoryOpen by remember { mutableStateOf(false) }
    var workplaceOpen by remember { mutableStateOf(false) }
    var cardOpen by remember { mutableStateOf(false) }
    var dateOpen by remember { mutableStateOf(false) }
    var newCategoryOpen by remember { mutableStateOf(false) }
    var customCategories by remember { mutableStateOf(emptyList<String>()) }
    var newCategoryName by remember { mutableStateOf("") }

    val categories = listOf(
        "خرید ابزار", "خرید قطعات", "مواد مصرفی", "لوازم اداری", "پوشاک و لوازم شخصی",
        "حمل‌ونقل", "غذا و پذیرایی", "تعمیرات", "خرید تجهیزات", "سایر"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                Modifier.padding(20.dp).heightIn(max = 680.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VsoftFormHeader(
                    title = if (existing == null) "ثبت خرید کار" else "ویرایش خرید کار",
                    subtitle = "جزئیات خرید، پرداخت و محل کار",
                    icon = Icons.Default.ShoppingCart,
                    accent = Color(0xFFB65B45),
                    onDismiss = onDismiss
                )
                OutlinedTextField(title, { title = it }, label = { Text("عنوان خرید") }, leadingIcon = { Icon(Icons.Default.ShoppingCart, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                OutlinedTextField(amount, { amount = normalizeAmountInput(it) }, label = { Text("مبلغ") }, leadingIcon = { Icon(Icons.Default.Payments, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = GroupedNumberVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ categoryOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.Label, null); Spacer(Modifier.width(8.dp)); Text(category, Modifier.weight(1f), textAlign = TextAlign.Start); Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(categoryOpen, { categoryOpen = false }) {
                        (categories + customCategories).distinct().forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryOpen = false })
                        }
                        DropdownMenuItem(
                            text = { Text(uiText("افزودن دسته‌بندی جدید")) },
                            leadingIcon = { Icon(Icons.Default.Add, null) },
                            onClick = { categoryOpen = false; newCategoryOpen = true }
                        )
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ workplaceOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.Place, null); Spacer(Modifier.width(8.dp)); Text(if (workplace.isBlank()) "محل کار (اختیاری)" else workplace, Modifier.weight(1f), textAlign = TextAlign.Start); Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(workplaceOpen, { workplaceOpen = false }) {
                        DropdownMenuItem(text = { Text("بدون محل کار") }, onClick = { workplace = ""; workplaceOpen = false })
                        workplaces.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { workplace = item.name; workplaceOpen = false }) }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ cardOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.CreditCard, null); Spacer(Modifier.width(8.dp)); Text(if (card.isBlank()) "کارت پرداخت" else card, Modifier.weight(1f), textAlign = TextAlign.Start); Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(cardOpen, { cardOpen = false }) {
                        DropdownMenuItem(text = { Text("بدون کارت") }, onClick = { card = ""; cardOpen = false })
                        cards.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { card = item.name; cardOpen = false }) }
                    }
                }
                OutlinedButton({ dateOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(date, Modifier.weight(1f), textAlign = TextAlign.Start); Icon(Icons.Default.ChevronLeft, null)
                }
                OutlinedTextField(note, { note = it }, label = { Text("یادداشت / فاکتور") }, leadingIcon = { Icon(Icons.Default.Notes, null) }, minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onDismiss, Modifier.weight(1f)) { Text("لغو") }
                    Button(
                        onClick = {
                            val v = normalizeDigits(amount).toLongOrNull() ?: 0L
                            if (title.isNotBlank() && v > 0) onSave(WorkPurchase(existing?.id ?: System.currentTimeMillis(), title.trim(), v, category, date, workplace, card, note.trim()))
                        },
                        enabled = title.isNotBlank() && (normalizeDigits(amount).toLongOrNull() ?: 0L) > 0,
                        modifier = Modifier.weight(1f).pressScale()
                    ) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(5.dp)); Text("ذخیره") }
                }
            }
        }
    }
    if (dateOpen) JalaliDatePickerDialog(date, { dateOpen = false }) { date = it; dateOpen = false }
}

// ---------------- WORK ----------------

@Composable
fun WorkPage(
    strings: AppStrings,
    workDays: List<WorkDay>,
    people: List<Person>,
    workplaces: List<Workplace>,
    cards: List<BankCard>,
    constructionTasks: List<ConstructionTask>,
    onWorkChange: (MutableList<WorkDay>) -> Unit,
    onWorkPurchases: () -> Unit,
    onOpenWorkplace: (Workplace) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<WorkDay?>(null) }
    val listState = rememberLazyListState()
    val totalHours = workDays.sumOf { calculateHours(it.start, it.end) }
    val totalIncome = workDays.sumOf { it.income }
    val sortedWorkDays = remember(workDays) {
        workDays.sortedWith(
            compareByDescending<WorkDay> { it.startDate.ifBlank { it.date } }
                .thenByDescending { it.id }
        )
    }
    val workGroups = remember(sortedWorkDays) {
        sortedWorkDays.groupBy { it.startDate.ifBlank { it.date } }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "workplaces_top") {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (LocalVsoftLanguage.current == "en") "Workplaces" else "مکان‌های کاری", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                        Text("${workplaces.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    if (workplaces.isEmpty()) {
                        Text(if (LocalVsoftLanguage.current == "en") "Add a workplace from More to track construction tasks." else "برای پیگیری کارهای ساختمانی، ابتدا از بخش بیشتر یک مکان کاری اضافه کن.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                            items(workplaces, key = { it.id }) { place ->
                                Surface(onClick = { onOpenWorkplace(place) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .09f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .18f))) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Icon(Icons.Default.Apartment, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Text(place.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "work_header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().background(
                                Brush.linearGradient(listOf(Color(0xFF17365B), Color(0xFF245B82), Color(0xFF147C78)))
                            ).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(uiText("مدیریت کار"), color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
                                    Text(uiText("روزهای کاری من"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        if (LocalVsoftLanguage.current == "en")
                                            "${workDays.size} workdays • ${String.format(Locale.US, "%.1f", totalHours)} hours"
                                        else "${workDays.size} روز کاری • ${String.format(Locale.US, "%.1f", totalHours)} ساعت",
                                        color = Color.White.copy(alpha = .78f), fontSize = 11.sp
                                    )
                                }
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.WorkHistory, null, tint = Color.White, modifier = Modifier.size(25.dp))
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = .2f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(uiText("درآمد کاری"), color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
                                    Text(money(totalIncome), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(uiText("ساعت کاری"), color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
                                    Text(String.format(Locale.US, "%.1f", totalHours), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(uiText("محل‌های کار"), color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
                                    Text(workplaces.size.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item(key = "work_purchases_entry") {
                OutlinedButton(
                    onClick = onWorkPurchases,
                    modifier = Modifier.fillMaxWidth().pressScale(),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(uiText("خریدهای کار"), fontWeight = FontWeight.SemiBold)
                }
            }
            if (workDays.isEmpty()) {
                item(key = "work_empty") {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState("هنوز روز کاری ثبت نشده", Icons.Default.WorkHistory)
                    }
                }
            } else {
                workGroups.forEach { (workDate, entries) ->
                    item(key = "work_date_$workDate") {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    workDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    if (LocalVsoftLanguage.current == "en")
                                        "${entries.size} entries • ${money(entries.sumOf { it.income })}"
                                    else "${entries.size} مورد • ${money(entries.sumOf { it.income })}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        }
                    }
                    itemsIndexed(entries, key = { _, it -> it.id }) { index, w ->
                        VsoftEntrance(index.coerceAtMost(7)) {
                            VsoftSwipeToDelete(
                                onDelete = {
                                    val x = workDays.toMutableList()
                                    x.removeAll { it.id == w.id }
                                    onWorkChange(x)
                                }
                            ) {
                                WorkCard(
                                    w,
                                    onDelete = {
                                        val x = workDays.toMutableList()
                                        x.removeAll { it.id == w.id }
                                        onWorkChange(x)
                                    },
                                    onEdit = { edit = w; show = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 22.dp)
                .pressScale(0.90f),
            onClick = { edit = null; show = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Add, "افزودن", modifier = Modifier.size(25.dp))
        }
    }

    if (show) AddWorkDialog(strings, people, workplaces, cards, edit, { show = false }) { w ->
        val x = workDays.toMutableList()
        val i = x.indexOfFirst { it.id == w.id }
        if (i >= 0) x[i] = w else x.add(w)
        onWorkChange(x)
        show = false
    }
}

// ---------------- WORK CARD ----------------

@Composable
fun WorkCard(work: WorkDay, onDelete: () -> Unit, onEdit: () -> Unit = {}) {
    val hours = calculateHours(work.start, work.end)
    val accent = MaterialTheme.colorScheme.primary
    var actionsExpanded by remember(work.id) { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth().clickable { onEdit() }.animateContentSize().pressScale(0.985f),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = .14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(accent.copy(alpha = .10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Work, null, tint = accent, modifier = Modifier.size(23.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(work.place.ifBlank { "محل کار ثبت نشده" },
                        fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    val dateText = if (work.startDate.isNotBlank() && work.endDate.isNotBlank())
                        if (LocalLayoutDirection.current == LayoutDirection.Rtl) work.startDate + " ← " + work.endDate
                        else work.startDate + " → " + work.endDate
                    else work.date
                    Text(dateText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(Icons.Default.MoreVert, uiText("گزینه‌های روز کاری"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(uiText("ویرایش روز کاری")) },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { actionsExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text(uiText("حذف روز کاری")) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { actionsExpanded = false; onDelete() }
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkMeta(Icons.Default.Schedule,
                    if (LocalLayoutDirection.current == LayoutDirection.Rtl) work.start + " ← " + work.end
                    else work.start + " → " + work.end, Modifier.weight(1f))
                WorkMeta(Icons.Default.Timer, String.format(Locale.US, "%.1f ساعت", hours), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("درآمد", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(money(work.income), fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary, fontSize = 17.sp)
                }
                if (work.person.isNotBlank()) {
                    Surface(shape = RoundedCornerShape(11.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = .08f)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = accent)
                            Spacer(Modifier.width(4.dp))
                            Text(work.person, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
            if (work.card.isNotBlank() || work.description.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (work.card.isNotBlank())
                        Text("واریز به: " + work.card, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    if (work.description.isNotBlank())
                        Text(work.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun WorkMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 11.sp)
        }
    }
}

// ---------------- ADD WORK ----------------
// ---------------- ADD WORK ----------------

@Composable
fun AddWorkDialog(
    strings: AppStrings, people: List<Person>, workplaces: List<Workplace>, cards: List<BankCard>, existing: WorkDay?,
    onDismiss: () -> Unit, onSave: (WorkDay) -> Unit
) {
    val ctx = LocalContext.current
    var place by remember { mutableStateOf(existing?.place ?: workplaces.firstOrNull()?.name ?: "") }
    var startDate by remember { mutableStateOf(existing?.startDate?.ifBlank { existing.date } ?: today()) }
    var endDate by remember { mutableStateOf(existing?.endDate?.ifBlank { existing.date } ?: today()) }
    var start by remember { mutableStateOf(existing?.start ?: "08:00") }
    var end by remember { mutableStateOf(existing?.end ?: "16:00") }
    var income by remember { mutableStateOf(existing?.income?.toString() ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var person by remember { mutableStateOf(existing?.person ?: "") }
    var card by remember { mutableStateOf(existing?.card ?: "") }
    var dateOpen by remember { mutableStateOf(false) }
    var endDateOpen by remember { mutableStateOf(false) }
    var placeOpen by remember { mutableStateOf(false) }
    var personOpen by remember { mutableStateOf(false) }
    var cardOpen by remember { mutableStateOf(false) }
    val duration = calculateHours(start, end)
    val selectedCard = cards.firstOrNull { it.name == card }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(30.dp),
            color = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .94f) else MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp, shadowElevation = 20.dp
        ) {
            Column(
                Modifier.padding(22.dp).heightIn(max = 700.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                VsoftFormHeader(
                    title = if (existing == null) uiText("ثبت روز کاری") else uiText("ویرایش روز کاری"),
                    subtitle = uiText("زمان، درآمد و جزئیات کار"),
                    icon = Icons.Default.Work,
                    accent = Color(0xFF0B9B83),
                    onDismiss = onDismiss
                )
                VsoftFormSectionLabel(
                    title = "محل و زمان کار",
                    subtitle = "محل انجام کار و بازه زمانی را ثبت کنید",
                    icon = Icons.Default.EventAvailable
                )
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ placeOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)) {
                        Icon(Icons.Default.Place, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(if (place.isBlank()) uiText("انتخاب محل کار") else place,
                                fontWeight = if (place.isBlank()) FontWeight.Normal else FontWeight.SemiBold)
                            Text(if (place.isBlank()) "محل اجرای کار را مشخص کنید" else "محل کار انتخاب‌شده",
                                fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(placeOpen, { placeOpen = false }) {
                        if (workplaces.isEmpty()) {
                            DropdownMenuItem(text = { Text("هنوز محل کاری ثبت نشده") }, onClick = { placeOpen = false })
                        } else {
                            workplaces.forEach { q ->
                                DropdownMenuItem(text = { Text(q.name) }, leadingIcon = { Icon(Icons.Default.Place, null, Modifier.size(18.dp)) },
                                    onClick = { place = q.name; placeOpen = false })
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton({ dateOpen = true }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp)) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("شروع", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(startDate, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    OutlinedButton({ endDateOpen = true }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp)) {
                        Icon(Icons.Default.Event, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("پایان", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(endDate, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton({
                        android.app.TimePickerDialog(ctx, { _, h, m -> start = "%02d:%02d".format(h, m) },
                            start.substringBefore(":").toIntOrNull() ?: 8, start.substringAfter(":").toIntOrNull() ?: 0, true).show()
                    }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp)) {
                        Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Icons.Default.Login else Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("شروع", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(start, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton({
                        android.app.TimePickerDialog(ctx, { _, h, m -> end = "%02d:%02d".format(h, m) },
                            end.substringBefore(":").toIntOrNull() ?: 16, end.substringAfter(":").toIntOrNull() ?: 0, true).show()
                    }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp)) {
                        Icon(if (LocalLayoutDirection.current == LayoutDirection.Rtl) Icons.Default.Logout else Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("پایان", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(end, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .08f)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("مدت کار", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.1f ساعت", duration), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = .7f))
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = .07f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = .22f))
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("دستمزد این کار", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text("درآمد ثبت‌شده به محاسبات مالی اضافه می‌شود",
                                    fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        OutlinedTextField(
                            income, { income = normalizeAmountInput(it) },
                            label = { Text(uiText("درآمد کار")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = GroupedNumberVisualTransformation(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                                cursorColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(17.dp)
                        )
                    }
                }
                VsoftFormSectionLabel(
                    title = "تکمیل اطلاعات",
                    subtitle = "کارت مقصد، کارفرما و شرح کار (اختیاری)",
                    icon = Icons.Default.Assignment
                )
                if (selectedCard != null) {
                    val colors = bankCardColors(selectedCard.bank)
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(colors.first.copy(alpha = .94f), colors.second.copy(alpha = .90f)))).padding(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(selectedCard.bank, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                Text("واریز درآمد کار", color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
                            }
                            Text("•••• " + selectedCard.last4.ifBlank { selectedCard.cardNumber.takeLast(4) }, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ cardOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)) {
                        Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(if (card.isBlank()) uiText("درآمد به کدام کارت برود؟") else card,
                                fontWeight = if (card.isBlank()) FontWeight.Normal else FontWeight.SemiBold)
                            Text(if (card.isBlank()) "اختیاری؛ برای ثبت واریز انتخاب کنید" else "کارت مقصد درآمد",
                                fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(cardOpen, { cardOpen = false }) {
                        DropdownMenuItem(text = { Text(uiText("بدون کارت")) }, leadingIcon = { Icon(Icons.Default.Block, null, Modifier.size(18.dp)) },
                            onClick = { card = ""; cardOpen = false })
                        cards.forEach { q ->
                            DropdownMenuItem(text = { Text(q.name) }, leadingIcon = { Icon(Icons.Default.CreditCard, null, Modifier.size(18.dp)) },
                                onClick = { card = q.name; cardOpen = false })
                        }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ personOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(if (person.isBlank()) uiText("انتخاب شخص / کارفرما") else person,
                                fontWeight = if (person.isBlank()) FontWeight.Normal else FontWeight.SemiBold)
                            Text(if (person.isBlank()) "اختیاری" else "شخص یا کارفرمای مرتبط",
                                fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(personOpen, { personOpen = false }) {
                        DropdownMenuItem(text = { Text(uiText("بدون شخص")) }, leadingIcon = { Icon(Icons.Default.PersonOff, null, Modifier.size(18.dp)) },
                            onClick = { person = ""; personOpen = false })
                        people.forEach { q ->
                            DropdownMenuItem(text = { Text(q.name) }, leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(18.dp)) },
                                onClick = { person = q.name; personOpen = false })
                        }
                    }
                }
                OutlinedTextField(
                    description, { description = it }, label = { Text(uiText("شرح کار")) },
                    leadingIcon = { Icon(Icons.Default.Notes, null) }, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp), placeholder = { Text(uiText("مثلاً نصب تابلو، تعمیر موتور، سیم‌کشی...")) }
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onDismiss, Modifier.weight(.78f).height(52.dp), shape = RoundedCornerShape(17.dp)
                    ) { Text(strings.cancel) }
                    Button(
                        onClick = {
                            onSave(WorkDay(
                                existing?.id ?: System.currentTimeMillis(), place, startDate, start, end,
                                normalizeDigits(income).toLongOrNull() ?: 0L, description, person, startDate, endDate, card
                            ))
                        },
                        enabled = place.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
                        modifier = Modifier.weight(1.22f).height(52.dp).pressScale(),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                    ) { Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(7.dp)); Text(strings.save, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
    if (dateOpen) JalaliDatePickerDialog(startDate, { dateOpen = false }) { startDate = it; dateOpen = false }
    if (endDateOpen) JalaliDatePickerDialog(endDate, { endDateOpen = false }) { endDate = it; endDateOpen = false }
}

// ---------------- REPORTS ----------------

@Composable
fun ReportsPage(strings: AppStrings, transactions: List<Transaction>, workDays: List<WorkDay>, cards: List<BankCard>) {
    var showReceiptBuilder by remember { mutableStateOf(false) }
    if (showReceiptBuilder) ReceiptBuilderDialog(transactions, workDays, cards) { showReceiptBuilder = false }
    var selectedCardName by remember(cards) { mutableStateOf(cards.firstOrNull()?.name ?: "__ALL_CARDS__") }
    val allCardsSelected = selectedCardName == "__ALL_CARDS__"
    val selected = cards.firstOrNull { it.name == selectedCardName }
    val cardTransactions = if (allCardsSelected) transactions else transactions.filter { it.card == selectedCardName }
    val cardWork = if (allCardsSelected) workDays else workDays.filter { it.card == selectedCardName }
    val income = cardTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = cardTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val opening = if (allCardsSelected) cards.sumOf { it.openingBalance } else selected?.openingBalance ?: 0L
    val workIncome = cardWork.sumOf { it.income }
    val current = if (allCardsSelected) cards.sumOf { cardCurrentBalance(it, transactions, workDays) } else selected?.let { cardCurrentBalance(it, transactions, workDays) } ?: 0L
    val hours = cardWork.sumOf { calculateHours(it.start, it.end) }

    val now = today().split("/").mapNotNull { it.toIntOrNull() }
    val nowYear = now.getOrNull(0) ?: 1405
    val nowMonth = now.getOrNull(1) ?: 1
    val monthly = remember(selectedCardName, transactions, workDays) {
        (0..5).map { back ->
            var y = nowYear
            var m = nowMonth - back
            while (m < 1) { m += 12; y-- }
            val prefix = "%04d/%02d/".format(Locale.US, y, m)
            val tx = transactions.filter { (allCardsSelected || it.card == selectedCardName) && it.date.startsWith(prefix) }
            val wd = workDays.filter { (allCardsSelected || it.card == selectedCardName) && it.date.startsWith(prefix) }
            Triple(
                "$y/$m",
                tx.filter { it.type == "income" }.sumOf { it.amount } + wd.sumOf { it.income },
                tx.filter { it.type == "expense" }.sumOf { it.amount }
            )
        }.reversed()
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    Modifier.fillMaxWidth().background(
                        Brush.linearGradient(listOf(Color(0xFF4B327F), Color(0xFF7651B7), Color(0xFFB16DAD)))
                    ).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(if (LocalVsoftLanguage.current == "en") "Your money, clearly" else "تصویر روشن از پولت", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                        Text(uiText("گزارش مالی، روند ماهانه و عملکرد کارت را یکجا ببینید."), color = Color.White.copy(alpha = .78f), fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = { showReceiptBuilder = true },
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = .15f))
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "فاکتور", tint = Color.White)
                    }
                }
            }
        }
        item {
            Card(
                Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Analytics, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(uiText("انتخاب کارت"), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(
                                if (allCardsSelected) (if (LocalVsoftLanguage.current == "en") "All bank cards" else "همه کارت‌ها") else if (selected != null) selected.bank + " • " + selected.name else uiText("هنوز کارت بانکی ثبت نشده"),
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                            )
                        }
                    }
                    if (cards.isEmpty()) {
                        Surface(
                            Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)
                        ) {
                            Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCardOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(8.dp))
                                Text(uiText("هنوز کارت بانکی ثبت نشده"), fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            item(key = "all_cards_report_filter") {
                                Surface(
                                    onClick = { selectedCardName = "__ALL_CARDS__" },
                                    modifier = Modifier.width(150.dp).height(132.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (allCardsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f),
                                    border = BorderStroke(1.5.dp, if (allCardsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = .12f))
                                ) {
                                    Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
                                        Icon(Icons.Default.AccountBalanceWallet, null, tint = if (allCardsSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                                        Text(if (LocalVsoftLanguage.current == "en") "All cards" else "همه کارت‌ها", color = if (allCardsSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        Text(if (LocalVsoftLanguage.current == "en") "Combined report" else "گزارش تجمیعی", color = if (allCardsSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                    }
                                }
                            }
                            items(cards, key = { it.id }) { card ->
                                val cardColors = bankCardColors(card.bank)
                                val isSelected = card.name == selectedCardName
                                Surface(
                                    onClick = { selectedCardName = card.name },
                                    modifier = Modifier.width(176.dp).pressScale(0.985f),
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) cardColors.first else MaterialTheme.colorScheme.outline.copy(alpha = .10f)
                                    )
                                ) {
                                    Box(
                                        Modifier.background(
                                            Brush.linearGradient(
                                                listOf(
                                                    cardColors.first.copy(alpha = if (isSelected) .96f else .72f),
                                                    cardColors.second.copy(alpha = if (isSelected) .92f else .68f)
                                                )
                                            )
                                        ).padding(13.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(7.dp))
                                                Text(card.bank, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, maxLines = 1)
                                            }
                                            Text(card.name, color = Color.White.copy(alpha = .82f), fontSize = 10.sp, maxLines = 1)
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    "•••• " + card.last4.ifBlank { card.cardNumber.takeLast(4) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Start,
                                                    color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1
                                                )
                                            }
                                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    money(cardCurrentBalance(card, transactions, workDays)),
                                                    color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (selected == null && !allCardsSelected) {
            item { EmptyState(uiText("هنوز کارت بانکی ثبت نشده"), Icons.Default.CreditCard) }
        } else {
            item {
                Card(
                    Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(uiText("خلاصه کارت"), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            ReportStatTile(uiText("موجودی"), money(current), Icons.Default.AccountBalanceWallet, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                            ReportStatTile(uiText("درآمد"), money(income), Icons.Default.TrendingUp, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(9.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            ReportStatTile(uiText("هزینه"), money(expense), Icons.Default.TrendingDown, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                            ReportStatTile(uiText("درآمد کاری"), money(workIncome), Icons.Default.Work, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(9.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            ReportStatTile(uiText("موجودی اولیه"), money(opening), Icons.Default.CreditCard, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                            ReportStatTile(uiText("ساعت کاری"), String.format(Locale.US, "%.1f", hours), Icons.Default.AccessTime, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                        }
                    }
                }
            }
            item { VsoftReportMonthlyChart(monthly) }
            item {
                val categories = cardTransactions
                    .filter { it.type == "expense" && it.category.isNotBlank() }
                    .groupingBy { it.category.trim() }
                    .fold(0L) { acc, t -> acc + t.amount }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)

                Card(
                    Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(uiText("دسته‌بندی هزینه‌ها"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(10.dp))
                        if (categories.isEmpty()) {
                            Text(uiText("هنوز هزینه‌ای با دسته‌بندی ثبت نشده"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        } else {
                            val max = categories.maxOf { it.value }.coerceAtLeast(1L)
                            categories.forEach { entry ->
                                val fraction = (entry.value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                                Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(entry.key, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                        Text(money(entry.value), fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(5.dp))
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStatTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = .08f)).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(7.dp))
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Spacer(Modifier.height(7.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun VsoftReportMonthlyChart(monthly: List<Triple<String, Long, Long>>) {
    var selectedMonth by remember(monthly) { mutableStateOf(monthly.lastOrNull()?.first.orEmpty()) }
    val selectedPoint = monthly.firstOrNull { it.first == selectedMonth } ?: monthly.lastOrNull()
    val maxValue = monthly.flatMap { listOf(it.second, it.third) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Card(
        Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(uiText("روند ۶ ماهه"), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(uiText("درآمد و هزینه هر ماه"), fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BarChart, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().height(178.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                monthly.forEachIndexed { index, point ->
                    val targetIncome = if (point.second > 0L) (point.second.toFloat() / maxValue.toFloat()).coerceIn(0.035f, 1f) else 0f
                    val targetExpense = if (point.third > 0L) (point.third.toFloat() / maxValue.toFloat()).coerceIn(0.035f, 1f) else 0f
                    val isSelected = point.first == selectedPoint?.first
                    val incomeFraction by animateFloatAsState(
                        targetValue = targetIncome,
                        animationSpec = tween(650, delayMillis = index * 70, easing = FastOutSlowInEasing),
                        label = "report_income_bar_$index"
                    )
                    val expenseFraction by animateFloatAsState(
                        targetValue = targetExpense,
                        animationSpec = tween(650, delayMillis = index * 70 + 45, easing = FastOutSlowInEasing),
                        label = "report_expense_bar_$index"
                    )
                    val month = point.first.substringAfterLast("/").toIntOrNull() ?: 1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else Color.Transparent)
                            .clickable { selectedMonth = point.first }
                            .padding(horizontal = 2.dp, vertical = 5.dp)
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Row(
                                Modifier.fillMaxWidth().height(140.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Box(
                                    Modifier.width(15.dp).height(140.dp * incomeFraction)
                                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    Modifier.width(15.dp).height(140.dp * expenseFraction)
                                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(jalaliMonthName(month).take(3), fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1)
                    }
                }
            }
            selectedPoint?.let { point ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
                ) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                jalaliMonthName(point.first.substringAfterLast("/").toIntOrNull() ?: 1) + " " + point.first.substringBefore("/"),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                money(point.second - point.third),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (point.second >= point.third) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(uiText("درآمد"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(money(point.second), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(uiText("هزینه"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(money(point.third), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, maxLines = 1)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    VsoftChartLegend(MaterialTheme.colorScheme.primary, uiText("درآمد"))
                    VsoftChartLegend(MaterialTheme.colorScheme.error, uiText("هزینه"))
                }
                val totalIncome = monthly.sumOf { it.second }
                val totalExpense = monthly.sumOf { it.third }
                Text(
                    money(totalIncome - totalExpense),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalIncome >= totalExpense) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }

}

@Composable
private fun VsoftChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun TransactionReceiptDialog(transaction: Transaction, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isIncome = transaction.type == "income"
    val accent = if (isIncome) Color(0xFF16866B) else MaterialTheme.colorScheme.error
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(25.dp))
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(uiText("رسید تراکنش"), fontWeight = FontWeight.ExtraBold)
                Text(uiText("پیش‌نمایش اطلاعات رسید"), fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                    border = BorderStroke(1.dp, accent.copy(alpha = .25f))
                ) {
                    Column(Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(transaction.category.ifBlank { uiText("تراکنش") },
                                    fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                Text(if (isIncome) uiText("رسید درآمد") else uiText("رسید هزینه"),
                                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                null, tint = accent, modifier = Modifier.size(26.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            (if (isIncome) "+" else "−") + money(transaction.amount),
                            fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = accent
                        )
                        ReceiptDetailRow(Icons.Default.CalendarMonth, uiText("تاریخ"), transaction.date)
                        if (transaction.card.isNotBlank())
                            ReceiptDetailRow(Icons.Default.CreditCard, uiText("کارت"), transaction.card)
                        if (transaction.person.isNotBlank())
                            ReceiptDetailRow(Icons.Default.Person, uiText("شخص مرتبط"), transaction.person)
                        if (transaction.description.isNotBlank())
                            ReceiptDetailRow(Icons.Default.Notes, uiText("توضیحات"), transaction.description)
                    }
                }
                Text(uiText("با ساخت تصویر، رسید آمادهٔ ذخیره یا اشتراک‌گذاری می‌شود."),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    shareVsoftReceipt(context, listOf(transaction), emptyList(),
                        transaction.date, transaction.date, "رسید تراکنش")
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Image, null)
                Spacer(Modifier.width(7.dp))
                Text(uiText("ساخت تصویر رسید"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(uiText("بازگشت")) }
        }
    )
}

@Composable
private fun ReceiptDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
@Composable
fun ReceiptBuilderDialog(transactions:List<Transaction>,workDays:List<WorkDay>,cards:List<BankCard>,onDismiss:()->Unit){
    val context=LocalContext.current
    var mode by remember{mutableStateOf("monthly")};var start by remember{mutableStateOf(today())};var end by remember{mutableStateOf(today())};var dateOpen by remember{mutableStateOf(0)}
    var selectedCardName by remember{mutableStateOf("")}
    var cardOpen by remember{mutableStateOf(false)}
    LaunchedEffect(mode){val t=today();when(mode){"daily"->{start=t;end=t};"weekly"->{start=shiftJalali(t,-6);end=t};"monthly"->{val p=t.split("/");start="%s/%02d/01".format(Locale.US,p[0],p[1].toInt());end=t}}}
    val filtered=transactions.filter{dateInRange(it.date,start,end) && (selectedCardName.isBlank() || it.card==selectedCardName)}.sortedWith(compareBy<Transaction>{it.date}.thenBy{it.id})
    val workFiltered=workDays.filter{dateInRange(it.date,start,end) && (selectedCardName.isBlank() || it.card==selectedCardName)}
    val income=filtered.filter{it.type=="income"}.sumOf{it.amount}+workFiltered.sumOf{it.income};val expense=filtered.filter{it.type=="expense"}.sumOf{it.amount}
    AlertDialog(onDismissRequest=onDismiss,title={Column{Text(uiText("ساخت فاکتور و خلاصه"),fontWeight=FontWeight.ExtraBold);Text(uiText("هر زمان و برای هر بازه‌ای که بخواهی"),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("daily" to "روزانه","weekly" to "هفتگی","monthly" to "ماهانه","custom" to "بازه دلخواه").forEach{(key,label)->FilterChip(selected=mode==key,onClick={mode=key},label={Text(uiText(label),fontSize=10.sp)})}}
            Box(Modifier.fillMaxWidth()){
                OutlinedButton({cardOpen=true},Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){
                    Icon(Icons.Default.CreditCard,null);Spacer(Modifier.width(7.dp))
                    Text(if(selectedCardName.isBlank()) "همه کارت‌ها" else selectedCardName,Modifier.weight(1f),textAlign=TextAlign.Start)
                    Icon(Icons.Default.ExpandMore,null)
                }
                DropdownMenu(cardOpen,{cardOpen=false}){
                    DropdownMenuItem(text={Text("همه کارت‌ها")},onClick={selectedCardName="";cardOpen=false})
                    cards.forEach{card->DropdownMenuItem(text={Text(card.name)},onClick={selectedCardName=card.name;cardOpen=false})}
                }
            }
            OutlinedButton({dateOpen=1},Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Icon(Icons.Default.CalendarMonth,null);Spacer(Modifier.width(7.dp));Text(uiText("از")+" "+start,Modifier.weight(1f),textAlign=TextAlign.Start)}
            OutlinedButton({dateOpen=2},Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Icon(Icons.Default.Event,null);Spacer(Modifier.width(7.dp));Text(uiText("تا")+" "+end,Modifier.weight(1f),textAlign=TextAlign.Start)}
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.primary.copy(alpha=.08f)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                Text("${uiText("تعداد تراکنش‌ها")}: ${filtered.size}",fontWeight=FontWeight.SemiBold);Text("${uiText("درآمد")}: ${money(income)}",color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold);Text("${uiText("هزینه")}: ${money(expense)}",color=MaterialTheme.colorScheme.error,fontWeight=FontWeight.Bold);Text("${uiText("خالص")}: ${money(income-expense)}",fontWeight=FontWeight.ExtraBold)
            }}
        }},
        confirmButton={Button(onClick={shareVsoftReceipt(context,filtered,workFiltered,start,end,receiptTitle(mode));onDismiss()},enabled=filtered.isNotEmpty()||workFiltered.isNotEmpty()){Icon(Icons.Default.Image,null);Spacer(Modifier.width(6.dp));Text(uiText("ساخت عکس"))}},
        dismissButton={TextButton(onClick=onDismiss){Text(uiText("لغو"))}})
    if(dateOpen==1)JalaliDatePickerDialog(start,{dateOpen=0}){start=it;mode="custom";dateOpen=0}
    if(dateOpen==2)JalaliDatePickerDialog(end,{dateOpen=0}){end=it;mode="custom";dateOpen=0}
}
fun receiptTitle(mode:String)=when(mode){"daily"->"گزارش روزانه";"weekly"->"گزارش هفتگی";"monthly"->"گزارش ماهانه";else->"گزارش مالی"}
fun shareVsoftReceipt(context:Context,transactions:List<Transaction>,workDays:List<WorkDay>,start:String,end:String,title:String){
    fun receiptMoney(value: Long): String = String.format(Locale.US, "%,d", value).replace(",", "٬")
    val width = 1080
    val items = (transactions.map { Pair(0, it) } + workDays.map { Pair(1, it) })
        .sortedBy { pair -> if (pair.second is Transaction) (pair.second as Transaction).date else (pair.second as WorkDay).date }
        .take(80)
    val height = 1000 + items.size * 118
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val navy = android.graphics.Color.rgb(23, 36, 63)
    val primary = android.graphics.Color.rgb(49, 86, 190)
    val ink = android.graphics.Color.rgb(35, 44, 61)
    val muted = android.graphics.Color.rgb(111, 121, 139)
    val line = android.graphics.Color.rgb(228, 233, 241)
    val paper = android.graphics.Color.rgb(245, 247, 251)
    val white = android.graphics.Color.WHITE
    val green = android.graphics.Color.rgb(27, 139, 96)
    val red = android.graphics.Color.rgb(195, 67, 77)
    val paleBlue = android.graphics.Color.rgb(237, 242, 255)
    val paleGreen = android.graphics.Color.rgb(233, 247, 240)
    val paleRed = android.graphics.Color.rgb(253, 238, 239)
    fun paint(color:Int, size:Float, bold:Boolean=false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
    }
    fun round(rect:RectF,color:Int,radius:Float=24f) { canvas.drawRoundRect(rect,radius,radius,paint(color,1f)) }
    fun rule(x1:Float,y:Float,x2:Float,color:Int=line,stroke:Float=2f) {
        canvas.drawLine(x1,y,x2,y,paint(color,1f).apply { strokeWidth=stroke })
    }
    fun text(value:String,x:Float,y:Float,maxWidth:Int,size:Float,color:Int,bold:Boolean=false,
             alignment:Layout.Alignment=Layout.Alignment.ALIGN_OPPOSITE):Float {
        if (value.isBlank()) return 0f
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color=color
            textSize=size
            typeface=if(bold) Typeface.create("sans-serif",Typeface.BOLD) else Typeface.create("sans-serif",Typeface.NORMAL)
        }
        val layout = StaticLayout.Builder.obtain(value,0,value.length,tp,maxWidth)
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setLineSpacing(2f,1.08f)
            .build()
        canvas.save()
        canvas.translate(x,y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }
    canvas.drawColor(paper)
    round(RectF(24f,24f,1056f,(height-24).toFloat()),white,34f)
    round(RectF(24f,24f,1056f,252f),navy,34f)
    round(RectF(72f,64f,148f,140f),primary,20f)
    val logo = paint(white,35f,true).apply { textAlign=Paint.Align.CENTER }
    canvas.drawText("V",110f,113f,logo)
    text("VSOFT",170f,66f,810,24f,android.graphics.Color.rgb(177,196,235),true)
    text(title,170f,99f,810,42f,white,true)
    text("گزارش رسمی تراکنش‌ها و عملکرد مالی",170f,155f,810,23f,android.graphics.Color.rgb(220,227,242))
    round(RectF(72f,190f,1008f,232f),android.graphics.Color.rgb(37,54,88),14f)
    text("دوره گزارش:  " + start + "  تا  " + end,92f,199f,876,19f,white,true)
    var y=278f
    text("مشخصات سند",72f,y,936,25f,ink,true); y+=42f
    round(RectF(72f,y,1008f,y+104f),android.graphics.Color.rgb(249,250,253),20f)
    text("شماره سند",94f,y+15f,390,16f,muted)
    text("VS-" + System.currentTimeMillis().toString().takeLast(8),94f,y+42f,390,22f,ink,true)
    text("تاریخ تهیه",540f,y+15f,440,16f,muted)
    text(today(),540f,y+42f,440,22f,ink,true)
    y+=132f
    val income=transactions.filter{it.type=="income"}.sumOf{it.amount}+workDays.sumOf{it.income}
    val expense=transactions.filter{it.type=="expense"}.sumOf{it.amount}
    val net=income-expense
    val metrics=listOf(Triple("مجموع درآمد",income,green),Triple("مجموع هزینه",expense,red),Triple("مانده خالص",net,primary))
    metrics.forEachIndexed{index,m->
        val x=72f+index*320f
        val bg=when(index){0->paleGreen;1->paleRed;else->paleBlue}
        round(RectF(x,y,x+296f,y+116f),bg,20f)
        text(m.first,x+16f,y+17f,264,18f,muted,true)
        text((if(index==2 && net>0) "+" else if(index==2 && net<0) "−" else "")+receiptMoney(kotlin.math.abs(m.second)),x+16f,y+49f,264,26f,m.third,true)
        text("تومان",x+16f,y+84f,264,15f,muted)
    }
    y+=148f
    text("ریز عملیات",72f,y,936,26f,ink,true); y+=42f
    round(RectF(72f,y,1008f,y+54f),android.graphics.Color.rgb(241,244,249),14f)
    text("شرح / دسته‌بندی",94f,y+16f,500,17f,ink,true)
    text("تاریخ",600f,y+16f,145,17f,ink,true)
    text("مبلغ (تومان)",765f,y+16f,220,17f,ink,true)
    y+=62f
    if(items.isEmpty()){
        text("برای این بازه موردی ثبت نشده است.",94f,y+18f,890,21f,muted)
        y+=74f
    } else items.forEachIndexed { index,pair ->
        val rowTop=y
        if(index%2==1) round(RectF(72f,rowTop,1008f,rowTop+104f),android.graphics.Color.rgb(250,251,253),14f)
        if(pair.first==0){
            val t=pair.second as Transaction
            val color=if(t.type=="income")green else red
            round(RectF(88f,rowTop+25f,98f,rowTop+75f),color,5f)
            val label=listOf(t.category.ifBlank{"تراکنش"},t.person,t.description,t.card).filter{it.isNotBlank()}.take(2).joinToString(" • ")
            text(label,116f,rowTop+16f,470,20f,ink,true)
            text(if(t.type=="income")"دریافت" else "پرداخت",116f,rowTop+48f,470,16f,color)
            text(t.date,600f,rowTop+34f,145,17f,muted)
            text((if(t.type=="income")"+" else "−")+receiptMoney(t.amount),765f,rowTop+32f,220,20f,color,true)
        } else {
            val w=pair.second as WorkDay
            round(RectF(88f,rowTop+25f,98f,rowTop+75f),primary,5f)
            text("کار • "+w.place,116f,rowTop+16f,470,20f,ink,true)
            text(listOf(w.person,w.description,w.start+"–"+w.end).filter{it.isNotBlank()}.take(2).joinToString(" • "),116f,rowTop+48f,470,16f,muted)
            text(w.date,600f,rowTop+34f,145,17f,muted)
            text("+"+receiptMoney(w.income),765f,rowTop+32f,220,20f,green,true)
        }
        rule(88f,rowTop+103f,992f,line,1f)
        y+=118f
    }
    y+=16f
    round(RectF(72f,y,1008f,y+86f),navy,20f)
    text("جمع‌بندی نهایی",94f,y+13f,360,17f,android.graphics.Color.rgb(193,205,230))
    text("درآمد − هزینه",94f,y+42f,360,19f,white,true)
    text((if(net>0)"+" else if(net<0)"−" else "")+receiptMoney(kotlin.math.abs(net))+" تومان",470f,y+27f,510,27f,white,true)
    y+=112f
    rule(72f,y,1008f,line,2f)
    text("این سند به‌صورت خودکار توسط مدیریت مالی VSOFT تهیه شده است.",72f,y+17f,936,17f,muted)
    text("لطفاً اطلاعات را پیش از ارائه یا تسویه نهایی بررسی کنید.",72f,y+44f,936,15f,muted)
    val values=ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME,"VSOFT_Invoice_"+System.currentTimeMillis()+".png")
        put(MediaStore.Images.Media.MIME_TYPE,"image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/VSOFT")
    }
    val uri=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
    if(uri==null){bitmap.recycle();return}
    try {
        context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        val send=Intent(Intent.ACTION_SEND).apply {
            type="image/png"
            putExtra(Intent.EXTRA_STREAM,uri)
            putExtra(Intent.EXTRA_SUBJECT,title+" - VSOFT")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send,"اشتراک فاکتور حرفه‌ای VSOFT"))
    } catch(_:Exception) {
        context.contentResolver.delete(uri,null,null)
    } finally { bitmap.recycle() }
}
// ---------------- SETTINGS ----------------

@Composable
fun VsoftPremiumPageHero(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(accent, Color(0xFF243B73), Color(0xFF116F73))))
                .padding(horizontal = 19.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "VSOFT  /  PERSONAL FINANCE",
                    color = Color.White.copy(alpha = .72f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = Color.White.copy(alpha = .82f), fontSize = 11.sp)
            }
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(17.dp))
                    .background(Color.White.copy(alpha = .15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
            }
        }
    }
}

@Composable
fun VsoftFormSectionLabel(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 3.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun VsoftFormHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(accent, Color(0xFF263B70), Color(0xFF173D4C)))
            ).padding(start = 14.dp, end = 7.dp, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(45.dp).clip(RoundedCornerShape(15.dp))
                    .background(Color.White.copy(alpha = .15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "VSOFT  /  ENTRY",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = .72f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = .82f))
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = .12f))
            ) {
                Icon(Icons.Default.Close, contentDescription = uiText("بستن"), tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun SettingsPage(
    strings: AppStrings, language: String, theme: String, currency: String,
    onLanguageChange: (String) -> Unit, onCurrencyChange: (String) -> Unit,
    onThemeChange: (String) -> Unit, glass: Boolean, onGlassChange: (Boolean) -> Unit,
    font: String, onFontChange: (String) -> Unit, onBackup: () -> Unit, onRestore: () -> Unit,
    backupRetention: Int, onBackupRetentionChange: (Int) -> Unit,
    backupSnapshot: VsoftBackup, onStoredBackupRestore: (VsoftBackup) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var storedBackups by remember { mutableStateOf(listStoredVsoftBackups(context)) }
    var selectedBackup by remember { mutableStateOf<File?>(null) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var smsEnabled by remember { mutableStateOf(SmsStore.isEnabled(context)) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = 2.dp, end = 2.dp, top = 4.dp, bottom = 34.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF192B49), Color(0xFF354FC2), Color(0xFF117C83))))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("VSOFT  /  CONTROL CENTER", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp, color = Color.White.copy(alpha = .68f))
                            Text("تنظیمات برنامه", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("همه‌چیز رو مطابق سلیقه‌ات تنظیم کن", fontSize = 12.sp, color = Color.White.copy(alpha = .82f))
                        }
                        Box(
                            Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .12f)).padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Language, null, tint = Color.White.copy(alpha = .9f), modifier = Modifier.size(17.dp))
                            Text("زبان", fontSize = 10.sp, color = Color.White.copy(alpha = .7f))
                            Text(when (language) { "en" -> "English"; "ar" -> "العربية"; else -> "فارسی" }, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .12f)).padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Palette, null, tint = Color.White.copy(alpha = .9f), modifier = Modifier.size(17.dp))
                            Text("پوسته", fontSize = 10.sp, color = Color.White.copy(alpha = .7f))
                            Text(when (theme) { "light" -> "روشن"; "dark" -> "تاریک"; else -> "سیستم" }, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .12f)).padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Sms, null, tint = Color.White.copy(alpha = .9f), modifier = Modifier.size(17.dp))
                            Text("پیامک بانکی", fontSize = 10.sp, color = Color.White.copy(alpha = .7f))
                            Text(if (smsEnabled) "فعال" else "غیرفعال", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
        item {
            SettingsSection(uiText("عمومی"), Icons.Default.Tune) {
                SettingsSelectRow(
                    title = uiText("زبان برنامه"),
                    value = when (language) { "en" -> "English"; "ar" -> "العربية"; else -> "فارسی" },
                    icon = Icons.Default.Language,
                    options = listOf("فارسی" to "fa", "English" to "en", "العربية" to "ar"),
                    selected = language,
                    onSelected = onLanguageChange
                )
                SettingsSelectRow(
                    title = uiText("واحد پول"),
                    value = when (currency) {
                        "IRR" -> "ریال"
                        "USD" -> "دلار آمریکا"
                        "EUR" -> "یورو"
                        "GBP" -> "پوند"
                        "AED" -> "درهم"
                        "TRY" -> "لیر"
                        else -> "تومان"
                    },
                    icon = Icons.Default.Payments,
                    options = listOf(
                        "تومان" to "IRT", "ریال" to "IRR", "دلار آمریکا" to "USD",
                        "یورو" to "EUR", "پوند" to "GBP", "درهم" to "AED", "لیر" to "TRY"
                    ),
                    selected = currency,
                    onSelected = onCurrencyChange
                )
                SettingsSelectRow(
                    title = strings.theme,
                    value = when (theme) { "light" -> strings.light; "dark" -> strings.dark; else -> strings.system },
                    icon = Icons.Default.Palette,
                    options = listOf(strings.light to "light", strings.dark to "dark", strings.system to "system"),
                    selected = theme,
                    onSelected = onThemeChange
                )
                SettingsSwitchRow(
                    title = uiText("دریافت پیامک بانکی"),
                    subtitle = uiText("پیامک‌ها فقط پس از تأیید شما ثبت می‌شوند"),
                    icon = Icons.Default.Sms,
                    checked = smsEnabled,
                    onCheckedChange = { enabled ->
                        SmsStore.setEnabled(context, enabled)
                        smsEnabled = enabled
                    }
                )
                SettingsSwitchRow(
                    title = uiText("ظاهر شیشه‌ای"),
                    subtitle = uiText("افکت شفاف و شیشه‌ای رابط کاربری"),
                    icon = Icons.Default.BlurOn,
                    checked = glass,
                    onCheckedChange = onGlassChange
                )
                SettingsSelectRow(
                    title = uiText("فونت برنامه"),
                    value = when (font) { "serif" -> "کلاسیک"; "mono" -> "فنی"; "cursive" -> "دست‌نویس"; else -> "مدرن و خوانا" },
                    icon = Icons.Default.FontDownload,
                    options = listOf(
                        "مدرن و خوانا" to "sans", "کلاسیک" to "serif",
                        "فنی" to "mono", "دست‌نویس" to "cursive"
                    ),
                    selected = font,
                    onSelected = onFontChange
                )
            }
        }
        item {
            SettingsSection(uiText("پشتیبان‌گیری و بازیابی"), Icons.Default.Backup) {
                ListItem(
                    headlineContent = { Text(uiText("پشتیبان کامل اطلاعات")) },
                    supportingContent = { Text(uiText("نسخه‌های خودکار رمزگذاری‌شده، نسخه دستی، فهرست نسخه‌ها و بازیابی با تأیید")) },
                    leadingContent = { Icon(Icons.Default.Security, null) }
                )
                Text("حداکثر نسخه‌های نگهداری‌شده", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 10).forEach { limit ->
                        FilterChip(
                            selected = backupRetention == limit,
                            onClick = {
                                onBackupRetentionChange(limit)
                                storedBackups = listStoredVsoftBackups(context)
                            },
                            label = { Text("$limit نسخه") }
                        )
                    }
                }
                Text(
                    "نسخه خودکار هر ۲۴ ساعت ساخته می‌شود. بکاپ‌های رمزگذاری‌شده در Documents/Vsoft/Backups و در پوشه‌های Automatic و Manual ذخیره می‌شوند.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        backupMessage = null
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    createStoredVsoftBackup(context, backupSnapshot, automatic = false)
                                }
                            }.onSuccess {
                                storedBackups = listStoredVsoftBackups(context)
                                backupMessage = "نسخه دستی با موفقیت ساخته شد."
                            }.onFailure {
                                backupMessage = "ساخت نسخه پشتیبان ناموفق بود."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().pressScale()
                ) {
                    Icon(Icons.Default.AddToDrive, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ایجاد نسخه پشتیبان دستی")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onBackup, modifier = Modifier.weight(1f).pressScale()) {
                        Icon(Icons.Default.FileDownload, null)
                        Spacer(Modifier.width(6.dp))
                        Text("خروجی فایل")
                    }
                    OutlinedButton(onClick = onRestore, modifier = Modifier.weight(1f).pressScale()) {
                        Icon(Icons.Default.FileUpload, null)
                        Spacer(Modifier.width(6.dp))
                        Text("واردکردن فایل")
                    }
                }
                if (!backupMessage.isNullOrBlank()) {
                    Text(backupMessage!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("نسخه‌های موجود (${storedBackups.size})", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { storedBackups = listStoredVsoftBackups(context) }) {
                        Icon(Icons.Default.Refresh, "تازه‌سازی فهرست")
                    }
                }
                if (storedBackups.isEmpty()) {
                    Text("هنوز نسخه پشتیبانی ذخیره نشده است.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    storedBackups.forEach { file ->
                        val automatic = file.name.contains("-auto-") || file.name.startsWith("vsoft-auto-backup-")
                        val date = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
                        ListItem(
                            headlineContent = { Text(date, fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                Text((if (automatic) "خودکار" else "دستی") + " • " + String.format(Locale.getDefault(), "%.1f KB", file.length() / 1024.0))
                            },
                            leadingContent = { Icon(if (automatic) Icons.Default.Schedule else Icons.Default.Save, null) },
                            trailingContent = {
                                IconButton(onClick = { selectedBackup = file }) {
                                    Icon(Icons.Default.Restore, "بازیابی این نسخه")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
    if (selectedBackup != null) {
        AlertDialog(
            onDismissRequest = { selectedBackup = null },
            title = { Text("بازیابی نسخه پشتیبان؟") },
            text = { Text("با تأیید، اطلاعات فعلی با نسخه انتخاب‌شده جایگزین می‌شود. برای جلوگیری از ازدست‌رفتن اطلاعات جدید، ابتدا یک نسخه دستی بساز.") },
            confirmButton = {
                TextButton(onClick = {
                    val file = selectedBackup
                    selectedBackup = null
                    if (file != null) {
                        scope.launch {
                            val data = withContext(Dispatchers.IO) { readStoredVsoftBackup(file) }
                            if (data != null) {
                                onStoredBackupRestore(data)
                                backupMessage = "بازیابی نسخه انتخاب‌شده آغاز شد."
                            } else {
                                backupMessage = "این نسخه خراب است یا قابل بازیابی نیست."
                            }
                        }
                    }
                }) { Text("تأیید و جایگزینی") }
            },
            dismissButton = { TextButton(onClick = { selectedBackup = null }) { Text("لغو") } }
        )
    }
}

@Composable
fun FontOption(title: String, value: String, current: String, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = value == current, onClick = { onChange(value) })
        Text(title, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CardsPage(cards: List<BankCard>, transactions: List<Transaction>, workDays: List<WorkDay>, onCardsChange: (MutableList<BankCard>) -> Unit) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BankCard?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var search by remember { mutableStateOf("") }
    val filteredCards = cards.filter {
        val q = normalizeVsoftSearch(search)
        q.isBlank() || normalizeVsoftSearch(it.bank).contains(q) ||
            normalizeVsoftSearch(it.name).contains(q) || it.cardNumber.contains(q)
    }
    val totalBalance = cards.sumOf { cardCurrentBalance(it, transactions, workDays) }
    val positiveCount = cards.count { cardCurrentBalance(it, transactions, workDays) > 0L }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 110.dp)
        ) {
            item {
                VsoftPremiumPageHero(
                    title = uiText("کارت‌های بانکی"),
                    subtitle = if (LocalVsoftLanguage.current == "en")
                        "${cards.size} cards • ${positiveCount} active"
                    else "${cards.size} کارت • ${positiveCount} کارت دارای موجودی",
                    icon = Icons.Default.CreditCard,
                    accent = Color(0xFF3D71D9)
                )
            }
            item {
                Card(
                    Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
                ) {
                    Row(
                        Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(50.dp).clip(RoundedCornerShape(17.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = .11f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp))
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                uiText("مجموع موجودی"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                money(totalBalance),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            if (LocalVsoftLanguage.current == "en") "VSOFT" else "مدیریت مالی",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text(uiText("جستجوی کارت")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(search.isNotBlank()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Default.Close, uiText("بستن"))
                            }
                        }
                    }
                )
            }
            if (filteredCards.isEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(66.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CreditCard, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(31.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (cards.isEmpty()) uiText("هنوز کارت بانکی ثبت نشده")
                                else uiText("تراکنشی با این فیلتر پیدا نشد"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (cards.isEmpty()) {
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    uiText("برای افزودن کارت از دکمه + استفاده کنید"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            itemsIndexed(filteredCards, key = { _, it -> it.id }) { index, card ->
                VsoftEntrance(index.coerceAtMost(7)) {
                    VsoftSwipeToDelete(
                        onDelete = {
                            onCardsChange(cards.toMutableList().also { list ->
                                list.removeAll { it.id == card.id }
                            })
                        }
                    ) {
                        CardItem(
                            card,
                            cardCurrentBalance(card, transactions, workDays),
                            onEdit = { editing = card },
                            onDelete = {
                                onCardsChange(cards.toMutableList().also { list ->
                                    list.removeAll { it.id == card.id }
                                })
                            }
                        )
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp))
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp).pressScale(),
            onClick = { show = true },
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Add, "افزودن کارت")
        }
    }
    if (show) AddCardDialog(onDismiss = { show = false }, onSave = { newCard ->
        onCardsChange(cards.toMutableList().also { it.add(newCard) })
        show = false
    })
    if (editing != null) EditCardDialog(editing!!, onDismiss = { editing = null }, onSave = { updated ->
        onCardsChange(cards.toMutableList().also { list ->
            val i = list.indexOfFirst { it.id == updated.id }
            if (i >= 0) list[i] = updated
        })
        editing = null
    })
}

@Composable
fun WorkplacesPage(workplaces: List<Workplace>, onWorkplacesChange: (MutableList<Workplace>) -> Unit) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Workplace?>(null) }
    var search by remember { mutableStateOf("") }
    val filteredWorkplaces = workplaces.filter {
        val q = normalizeVsoftSearch(search)
        q.isBlank() || normalizeVsoftSearch(it.name).contains(q)
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                VsoftPremiumPageHero(
                    title = if (LocalVsoftLanguage.current == "en") "Workplaces" else "محل‌های کار",
                    subtitle = workplaces.size.toString() + " " + if (LocalVsoftLanguage.current == "en") "workplaces" else "محل کار",
                    icon = Icons.Default.Place,
                    accent = Color(0xFF0B9B83)
                )
            }
            item {
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text(uiText("جستجوی محل کار")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(search.isNotBlank()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Default.Close, uiText("پاک‌کردن جستجو"))
                            }
                        }
                    }
                )
            }
            if (filteredWorkplaces.isEmpty()) {
                item {
                    EmptyState(
                        if (workplaces.isEmpty()) uiText("هنوز محل کاری ثبت نشده")
                        else uiText("محل کاری با این جستجو پیدا نشد"),
                        Icons.Default.Place
                    )
                }
            }
            itemsIndexed(filteredWorkplaces, key = { _, it -> it.id }) { index, workplace ->
                VsoftEntrance(index.coerceAtMost(7)) {
                    VsoftSwipeToDelete(onDelete = {
                        onWorkplacesChange(workplaces.toMutableList().also { list -> list.removeAll { w -> w.id == workplace.id } })
                    }) {
                        Card(
                            Modifier.fillMaxWidth().animateContentSize().pressScale(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(50.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = .11f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                                }
                                Spacer(Modifier.width(13.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(workplace.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Text(
                                        uiText("محل کار"),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { editing = workplace }) {
                                    Icon(Icons.Default.Edit, contentDescription = uiText("ویرایش"))
                                }
                                IconButton(onClick = {
                                    onWorkplacesChange(workplaces.toMutableList().also { list -> list.removeAll { w -> w.id == workplace.id } })
                                }) { Icon(Icons.Default.DeleteOutline, contentDescription = uiText("حذف")) }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp).pressScale(),
            onClick = { show = true }, shape = RoundedCornerShape(18.dp)) { Icon(Icons.Default.Add, "افزودن محل کار") }
    }
    if (show) AddWorkplaceDialog(onDismiss = { show = false }, onSave = { newPlace ->
        onWorkplacesChange(workplaces.toMutableList().also { it.add(newPlace) }); show = false
    })
    editing?.let { place ->
        EditWorkplaceDialog(place, onDismiss = { editing = null }) { updated ->
            onWorkplacesChange(workplaces.toMutableList().also { list ->
                val i = list.indexOfFirst { it.id == updated.id }
                if (i >= 0) list[i] = updated
            })
            editing = null
        }
    }
}

@Composable
fun EditWorkplaceDialog(place: Workplace, onDismiss: () -> Unit, onSave: (Workplace) -> Unit) {
    var name by remember(place.id) { mutableStateOf(place.name) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(30.dp),
            color = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .94f) else MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp, shadowElevation = 18.dp) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                VsoftFormHeader(
                    title = uiText("ویرایش محل کار"),
                    subtitle = uiText("اطلاعات محل کار را به‌روزرسانی کنید"),
                    icon = Icons.Default.Place,
                    accent = Color(0xFF0B9B83),
                    onDismiss = onDismiss
                )
                OutlinedTextField(name,{name=it},label={Text(uiText("نام محل کار"))},leadingIcon={Icon(Icons.Default.Place,null)},
                    singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onDismiss,Modifier.weight(1f),shape=RoundedCornerShape(16.dp)){Text(uiText("لغو"))}
                    Button(onClick={if(name.isNotBlank()) onSave(place.copy(name=name.trim()))},enabled=name.isNotBlank(),modifier=Modifier.weight(1f).pressScale(),shape=RoundedCornerShape(16.dp)){
                        Icon(Icons.Default.Check,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(uiText("ذخیره"))
                    }
                }
            }
        }
    }
}


@Composable
fun PeoplePage(people: List<Person>, onPeopleChange: (MutableList<Person>) -> Unit) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Person?>(null) }
    var search by remember { mutableStateOf("") }
    val filteredPeople = people.filter {
        val q = normalizeVsoftSearch(search)
        q.isBlank() || normalizeVsoftSearch(it.name).contains(q) ||
            normalizeVsoftSearch(it.phone).contains(q) || normalizeVsoftSearch(it.job).contains(q)
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                VsoftPremiumPageHero(
                    title = if (LocalVsoftLanguage.current == "en") "People" else "افراد",
                    subtitle = people.size.toString() + " " + if (LocalVsoftLanguage.current == "en") "contacts" else "نفر ثبت‌شده",
                    icon = Icons.Default.PeopleAlt,
                    accent = Color(0xFF7357D8)
                )
            }
            item {
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text(uiText("جستجوی افراد")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(search.isNotBlank()) {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Default.Close, uiText("پاک‌کردن جستجو"))
                            }
                        }
                    }
                )
            }
            if (filteredPeople.isEmpty()) {
                item {
                    EmptyState(
                        if (people.isEmpty()) uiText("هنوز فردی ثبت نشده")
                        else uiText("فردی با این جستجو پیدا نشد"),
                        Icons.Default.PeopleAlt
                    )
                }
            }
            itemsIndexed(filteredPeople, key = { _, it -> it.id }) { index, person ->
                VsoftEntrance(index.coerceAtMost(7)) {
                    VsoftSwipeToDelete(onDelete = {
                        onPeopleChange(people.toMutableList().also { list -> list.removeAll { p -> p.id == person.id } })
                    }) {
                        PersonItem(person,
                            onDelete = {
                                onPeopleChange(people.toMutableList().also { list -> list.removeAll { p -> p.id == person.id } })
                            },
                            onEdit = { editing = person }
                        )
                    }
                }
            }
        }
        FloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp).pressScale(),
            onClick = { show = true }, shape = RoundedCornerShape(18.dp)) { Icon(Icons.Default.Add, "افزودن شخص") }
    }
    if (show) AddPersonDialog(onDismiss = { show = false }, onSave = { newPerson ->
        onPeopleChange(people.toMutableList().also { it.add(newPerson) }); show = false
    })
    editing?.let { person ->
        EditPersonDialog(person, onDismiss = { editing = null }) { updated ->
            onPeopleChange(people.toMutableList().also { list ->
                val i = list.indexOfFirst { it.id == updated.id }
                if (i >= 0) list[i] = updated
            })
            editing = null
        }
    }
}

@Composable
fun EditPersonDialog(person: Person, onDismiss: () -> Unit, onSave: (Person) -> Unit) {
    var name by remember(person.id) { mutableStateOf(person.name) }
    var phone by remember(person.id) { mutableStateOf(person.phone) }
    var job by remember(person.id) { mutableStateOf(person.job) }
    var note by remember(person.id) { mutableStateOf(person.note) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(30.dp),
            color = if (LocalVsoftGlass.current)
                MaterialTheme.colorScheme.surface.copy(alpha = .94f)
            else MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 18.dp
        ) {
            Column(
                Modifier.padding(22.dp)
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                VsoftFormHeader(
                    title = uiText("ویرایش شخص"),
                    subtitle = uiText("اطلاعات فرد را به‌روزرسانی کنید"),
                    icon = Icons.Default.Person,
                    accent = Color(0xFF7357D8),
                    onDismiss = onDismiss
                )

                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = .08f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = .14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.trim().firstOrNull()?.toString() ?: "؟",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp
                        )
                    }
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text(
                            name.ifBlank { uiText("نام و نام خانوادگی") },
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            job.ifBlank { uiText("شغل / نقش") },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    name, { name = it },
                    label = { Text(uiText("نام و نام خانوادگی")) },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    phone, { phone = it },
                    label = { Text(uiText("شماره تماس")) },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    job, { job = it },
                    label = { Text(uiText("شغل / نقش")) },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    note, { note = it },
                    label = { Text(uiText("یادداشت")) },
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text(uiText("لغو")) }

                    Button(
                        onClick = {
                            onSave(
                                person.copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    job = job.trim(),
                                    note = note.trim()
                                )
                            )
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f).pressScale(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(uiText("ذخیره"))
                    }
                }
            }
        }
    }
}


// ---------------- SETTINGS COMPONENTS ----------------

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}


@Composable
fun SettingsSelectRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(value, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) },
            leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Icon(Icons.Default.ExpandMore, null) },
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { open = true }
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (label, option) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    trailingIcon = {
                        if (option == selected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    onClick = { onSelected(option); open = false }
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

@Composable
fun LanguageOption(
    title: String,
    value: String,
    currentValue: String,
    onChange: (String) -> Unit
) {

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = value == currentValue,
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
    currentValue: String,
    onChange: (String) -> Unit
) {

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = value == currentValue,
            onClick = {
                onChange(value)
            }
        )

        Text(title)
    }
}

// ---------------- CARDS ----------------

@Composable
fun bankCardColors(bank: String): Pair<Color, Color> {
    val name = bank.trim().lowercase(Locale.ROOT)
    return when {
        "ملی" in name || "melli" in name -> Color(0xFF003B7A) to Color(0xFF1677C8)
        "مسکن" in name || "maskan" in name -> Color(0xFF00796B) to Color(0xFF26A69A)
        "بلو" in name || "blu" in name -> Color(0xFF1D4ED8) to Color(0xFF60A5FA)
        "رد" in name || "red" in name -> Color(0xFFB71C1C) to Color(0xFFFF5252)
        "مهر" in name || "mehr" in name -> Color(0xFF00695C) to Color(0xFF26A69A)
        "ملت" in name || "mellat" in name -> Color(0xFF007A53) to Color(0xFF20B486)
        "صادرات" in name || "saderat" in name -> Color(0xFF00695C) to Color(0xFF26A69A)
        "تجارت" in name || "tejarat" in name -> Color(0xFF005B96) to Color(0xFF29B6F6)
        "پارسیان" in name || "parsian" in name -> Color(0xFFB77900) to Color(0xFFFFD54F)
        "پاسارگاد" in name || "pasargad" in name -> Color(0xFF123C8C) to Color(0xFF2F80ED)
        "سامان" in name || "saman" in name -> Color(0xFF263238) to Color(0xFF78909C)
        "کشاورزی" in name || "keshavarzi" in name -> Color(0xFF2E7D32) to Color(0xFF81C784)
        "رفاه" in name || "refah" in name -> Color(0xFF1565C0) to Color(0xFF42A5F5)
        "آینده" in name || "ayandeh" in name -> Color(0xFF6A1B9A) to Color(0xFFAB47BC)
        "اقتصاد نوین" in name || "eghtesad" in name -> Color(0xFF7B1FA2) to Color(0xFFCE93D8)
        else -> Color(0xFF37474F) to Color(0xFF78909C)
    }
}

@Composable
fun CardItem(
    card: BankCard,
    currentBalance: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var reveal by remember { mutableStateOf(false) }
    val number = card.cardNumber.filter(Char::isDigit)
    val displayNumber = if (number.isBlank()) uiText("شماره کارت ثبت نشده")
        else if (reveal) number.chunked(4).joinToString("  ")
        else "••••  ••••  ••••  " + number.takeLast(4)

    val (startColor, endColor) = bankCardColors(card.bank)
    val animatedBalance by animateFloatAsState(
        targetValue = currentBalance.toFloat(),
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "card_balance"
    )

    Card(
        Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        border = if (LocalVsoftGlass.current)
            BorderStroke(1.dp, Color.White.copy(alpha = .20f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (LocalVsoftGlass.current) 10.dp else 6.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            startColor.copy(alpha = if (LocalVsoftGlass.current) .62f else if (!isSystemInDarkTheme()) .94f else .72f),
                            Color.White.copy(alpha = if (LocalVsoftGlass.current) .10f else .03f),
                            endColor.copy(alpha = if (LocalVsoftGlass.current) .46f else if (!isSystemInDarkTheme()) .76f else .52f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = .18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            card.bank,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            card.name,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = .78f)
                        )
                    }
                    IconButton(onClick = { reveal = !reveal }) {
                        Icon(
                            if (reveal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null,
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            null,
                            tint = Color.White.copy(alpha = .92f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                AnimatedContent(
                    targetState = displayNumber,
                    transitionSpec = {
                        fadeIn(tween(180)) + scaleIn(tween(180)) togetherWith
                            fadeOut(tween(120)) + scaleOut(tween(120))
                    },
                    label = "card_number_reveal"
                ) { value ->
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            value,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            uiText("موجودی فعلی"),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = .72f)
                        )
                        Text(
                            money(animatedBalance.toLong()),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Text(
                        uiText("برای ویرایش ضربه بزنید"),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = .72f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCardDialog(onDismiss:()->Unit,onSave:(BankCard)->Unit){
 var bank by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var cardNumber by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")}
 Dialog(onDismissRequest=onDismiss){Surface(Modifier.fillMaxWidth().padding(8.dp),shape=RoundedCornerShape(30.dp),color=if(LocalVsoftGlass.current)MaterialTheme.colorScheme.surface.copy(alpha=.94f)else MaterialTheme.colorScheme.surface,tonalElevation=8.dp,shadowElevation=18.dp){
  Column(Modifier.padding(22.dp).heightIn(max=660.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
   VsoftFormHeader(
       title = uiText("کارت بانکی جدید"),
       subtitle = uiText("مشخصات کارت را وارد کنید"),
       icon = Icons.Default.CreditCard,
       accent = Color(0xFF3D71D9),
       onDismiss = onDismiss
   )
   AnimatedVisibility(cardNumber.isNotBlank()){val c=bankCardColors(bank);Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(c.first.copy(alpha=.92f),c.second.copy(alpha=.88f)))).padding(16.dp)){Column{Text(bank.ifBlank{uiText("کارت")},color=Color.White,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(13.dp));Text(if(cardNumber.length>=4)"••••  ••••  ••••  "+cardNumber.takeLast(4)else cardNumber,color=Color.White,fontSize=14.sp,letterSpacing=1.5.sp);Spacer(Modifier.height(7.dp));Text(name.ifBlank{"VSOFT"},color=Color.White.copy(alpha=.72f),fontSize=11.sp)}}}
   OutlinedTextField(bank,{bank=it},label={Text(uiText("نام بانک"))},leadingIcon={Icon(Icons.Default.AccountBalance,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(name,{name=it},label={Text(uiText("عنوان کارت"))},leadingIcon={Icon(Icons.Default.CreditCard,null)},placeholder={Text(if(LocalVsoftLanguage.current=="en")"e.g. Personal card" else "مثلاً کارت شخصی")},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(cardNumber,{cardNumber=normalizeDigits(it).filter(Char::isDigit).take(16)},label={Text(uiText("شماره کامل کارت"))},leadingIcon={Icon(Icons.Default.Numbers,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,supportingText={Text(cardNumber.length.toString()+" / 16")},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(balance,{balance=normalizeAmountInput(it)},label={Text(uiText("موجودی اولیه"))},leadingIcon={Icon(Icons.Default.AccountBalanceWallet,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),visualTransformation=GroupedNumberVisualTransformation(),singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onDismiss,Modifier.weight(1f),shape=RoundedCornerShape(16.dp)){Text(uiText("لغو"))};Button(onClick={val n=normalizeDigits(cardNumber).filter(Char::isDigit).take(16);onSave(BankCard(System.currentTimeMillis(),bank.trim(),name.trim().ifBlank{bank.trim()},n,n.takeLast(4),normalizeDigits(balance).toLongOrNull()?:0L))},enabled=bank.isNotBlank(),modifier=Modifier.weight(1f).pressScale(),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.Check,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(uiText("ذخیره"))}}
  }
 }}
}

@Composable
fun EditCardDialog(card:BankCard,onDismiss:()->Unit,onSave:(BankCard)->Unit){
 var bank by remember(card.id){mutableStateOf(card.bank)};var name by remember(card.id){mutableStateOf(card.name)};var num by remember(card.id){mutableStateOf(card.cardNumber)};var balance by remember(card.id){mutableStateOf(card.openingBalance.toString())}
 Dialog(onDismissRequest=onDismiss){Surface(Modifier.fillMaxWidth().padding(8.dp),shape=RoundedCornerShape(30.dp),color=if(LocalVsoftGlass.current)MaterialTheme.colorScheme.surface.copy(alpha=.94f)else MaterialTheme.colorScheme.surface,tonalElevation=8.dp,shadowElevation=18.dp){
  Column(Modifier.padding(22.dp).heightIn(max=650.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
   VsoftFormHeader(
       title = uiText("ویرایش کارت"),
       subtitle = uiText("اطلاعات کارت را به‌روزرسانی کنید"),
       icon = Icons.Default.CreditCard,
       accent = Color(0xFF3D71D9),
       onDismiss = onDismiss
   )
   val c=bankCardColors(bank);Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(c.first.copy(alpha=.92f),c.second.copy(alpha=.88f)))).padding(15.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CreditCard,null,tint=Color.White);Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(bank.ifBlank{uiText("کارت")},color=Color.White,fontWeight=FontWeight.ExtraBold);Text(name.ifBlank{"VSOFT"},color=Color.White.copy(alpha=.72f),fontSize=11.sp)};Text("•••• "+num.filter(Char::isDigit).takeLast(4),color=Color.White,fontSize=12.sp)}}
   OutlinedTextField(bank,{bank=it},label={Text(uiText("بانک"))},leadingIcon={Icon(Icons.Default.AccountBalance,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(name,{name=it},label={Text(uiText("نام کارت"))},leadingIcon={Icon(Icons.Default.CreditCard,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(num,{num=normalizeDigits(it).filter(Char::isDigit).take(16)},label={Text(uiText("شماره کامل کارت"))},leadingIcon={Icon(Icons.Default.Numbers,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,supportingText={Text(num.length.toString()+" / 16")},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(balance,{balance=normalizeAmountInput(it)},label={Text(uiText("موجودی اولیه"))},leadingIcon={Icon(Icons.Default.AccountBalanceWallet,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),visualTransformation=GroupedNumberVisualTransformation(),singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onDismiss,Modifier.weight(1f),shape=RoundedCornerShape(16.dp)){Text(uiText("لغو"))};Button(onClick={val n=normalizeDigits(num).filter(Char::isDigit).take(16);onSave(card.copy(bank=bank.trim(),name=name.trim().ifBlank{bank.trim()},cardNumber=n,last4=n.takeLast(4),openingBalance=normalizeDigits(balance).toLongOrNull()?:0L))},enabled=bank.isNotBlank(),modifier=Modifier.weight(1f).pressScale(),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.Check,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(uiText("ذخیره"))}}
  }
 }}
}

@Composable
fun AddWorkplaceDialog(onDismiss:()->Unit,onSave:(Workplace)->Unit){
 var name by remember{mutableStateOf("")}
 Dialog(onDismissRequest=onDismiss){Surface(Modifier.fillMaxWidth().padding(8.dp),shape=RoundedCornerShape(30.dp),color=if(LocalVsoftGlass.current)MaterialTheme.colorScheme.surface.copy(alpha=.94f)else MaterialTheme.colorScheme.surface,tonalElevation=8.dp,shadowElevation=18.dp){
  Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   VsoftFormHeader(
       title = uiText("محل کار جدید"),
       subtitle = uiText("محل کار مورد استفاده را اضافه کنید"),
       icon = Icons.Default.Place,
       accent = Color(0xFF0B9B83),
       onDismiss = onDismiss
   )
   OutlinedTextField(name,{name=it},label={Text(uiText("نام محل کار"))},leadingIcon={Icon(Icons.Default.Place,null)},placeholder={Text(uiText("مثلاً پروژه، شرکت یا کارگاه"))},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onDismiss,Modifier.weight(1f),shape=RoundedCornerShape(16.dp)){Text(uiText("لغو"))};Button(onClick={onSave(Workplace(System.currentTimeMillis(),name.trim()))},enabled=name.isNotBlank(),modifier=Modifier.weight(1f).pressScale(),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.Check,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(uiText("ذخیره"))}}
  }
 }}
}

// ---------------- PEOPLE ----------------

@Composable
fun PersonItem(
    person: Person,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Card(
        Modifier
            .fillMaxWidth()
            .vsoftGlass(RoundedCornerShape(24.dp))
            .animateContentSize()
            .pressScale(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarColor = MaterialTheme.colorScheme.primary
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(avatarColor.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    person.name.trim().firstOrNull()?.toString() ?: "؟",
                    color = avatarColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(person.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                if (person.job.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(person.job, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
                if (person.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(person.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (person.note.isNotBlank()) {
                    Text(
                        person.note,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = uiText("ویرایش"))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = uiText("حذف"))
            }
        }
    }
}
@Composable
fun AddPersonDialog(onDismiss:()->Unit,onSave:(Person)->Unit){
 var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var job by remember{mutableStateOf("")};var note by remember{mutableStateOf("")}
 Dialog(onDismissRequest=onDismiss){Surface(Modifier.fillMaxWidth().padding(8.dp),shape=RoundedCornerShape(30.dp),color=if(LocalVsoftGlass.current)MaterialTheme.colorScheme.surface.copy(alpha=.94f)else MaterialTheme.colorScheme.surface,tonalElevation=8.dp,shadowElevation=18.dp){
  Column(Modifier.padding(22.dp).heightIn(max=620.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(11.dp)){
   VsoftFormHeader(
       title = uiText("شخص جدید"),
       subtitle = uiText("اطلاعات فرد را برای کار و مالی ثبت کنید"),
       icon = Icons.Default.PersonAdd,
       accent = Color(0xFF7357D8),
       onDismiss = onDismiss
   )
   OutlinedTextField(name,{name=it},label={Text(uiText("نام و نام خانوادگی"))},leadingIcon={Icon(Icons.Default.PersonOutline,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(phone,{phone=it},label={Text(uiText("شماره تماس"))},leadingIcon={Icon(Icons.Default.Phone,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(job,{job=it},label={Text(uiText("شغل / نقش"))},leadingIcon={Icon(Icons.Default.Badge,null)},placeholder={Text(if(LocalVsoftLanguage.current=="en")"e.g. Employer / Electrician" else "مثلاً کارفرما / برق‌کش")},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(note,{note=it},label={Text(uiText("یادداشت"))},leadingIcon={Icon(Icons.Default.Notes,null)},minLines=2,maxLines=3,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onDismiss,Modifier.weight(1f),shape=RoundedCornerShape(16.dp)){Text(uiText("لغو"))};Button(onClick={onSave(Person(System.currentTimeMillis(),name.trim(),phone.trim(),note.trim(),job.trim()))},enabled=name.isNotBlank(),modifier=Modifier.weight(1f).pressScale(),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.Check,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(uiText("ذخیره"))}}
  }
 }}
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
                onSelected("%04d/%02d/%02d".format(Locale.US, year, month, day.coerceAtMost(jalaliMonthDays(year, month))))
            }) { Text(uiText("انتخاب")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(uiText("لغو")) }
        },
        title = { Text("${jalaliMonthName(month)} $year") },
        text = {
            Column {
                val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        month--
                        if (month < 1) { month = 12; year-- }
                    }) { Text(if (rtl) "›" else "‹") }

                    AnimatedContent(
                        targetState = "${jalaliMonthName(month)} $year",
                        transitionSpec = {
                            val direction = if (rtl) -1 else 1
                            fadeIn(tween(160)) + slideInHorizontally(tween(180)) { direction * it / 4 } togetherWith
                                fadeOut(tween(100)) + slideOutHorizontally(tween(120)) { -direction * it / 5 }
                        },
                        label = "jalali_month_title"
                    ) { title ->
                        Text(title, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = {
                        month++
                        if (month > 12) { month = 1; year++ }
                    }) { Text(if (rtl) "‹" else "›") }
                }

                val gregorian = jalaliToGregorian(year, month, 1)
                val calendar = java.util.Calendar.getInstance().apply {
                    set(gregorian[0], gregorian[1] - 1, gregorian[2])
                }
                val language = LocalVsoftLanguage.current
                // Jalali/Persian weeks start on Saturday; English calendar starts on Sunday.
                val offset = if (language == "en") {
                    (calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1) % 7
                } else {
                    calendar.get(java.util.Calendar.DAY_OF_WEEK) % 7
                }
                val weekdays = if (language == "en") {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                } else {
                    listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
                }

                val cells = mutableListOf<Int?>()
                repeat(offset) { cells.add(null) }
                for (i in 1..jalaliMonthDays(year, month)) cells.add(i)
                while (cells.size % 7 != 0) cells.add(null)

                CompositionLocalProvider(
                    LocalLayoutDirection provides if (language == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
                ) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        weekdays.forEachIndexed { index, weekday ->
                            Box(
                                Modifier.weight(1f).height(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    weekday,
                                    fontSize = if (language == "en") 10.sp else 9.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,
                                    color = if ((language == "en" && weekday == "Sat") ||
                                        (language != "en" && weekday == "جمعه"))
                                        MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (value == day)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    Color.Transparent
                                            )
                                            .clickable { day = value },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            value.toString(),
                                            color = if (value == day)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
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
        }
    )
}

// ---------------- COMMON UI ----------------

@Composable
fun InfoCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (LocalVsoftGlass.current) 0.dp else 2.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(13.dp))
            Column {
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun SmallInfoCard(modifier: Modifier, title: String, value: String,
                  icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DashboardMetric(title, value, icon, MaterialTheme.colorScheme.primary, modifier)
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 21.sp, fontWeight = FontWeight.Bold)
}