package com.vsoft.app

import android.os.Bundle
import android.content.Context
import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
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
import java.math.BigInteger

val android.content.Context.dataStore by preferencesDataStore("vsoft_data")

val TRANSACTIONS_KEY = stringPreferencesKey("transactions")
val WORK_KEY = stringPreferencesKey("work_days")
val CARDS_KEY = stringPreferencesKey("cards")
val PEOPLE_KEY = stringPreferencesKey("people")
val WORKPLACES_KEY = stringPreferencesKey("workplaces")
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
            "حساب و همگام‌سازی" -> "Account & sync"
            "با ورود به حساب Google، آماده اتصال امن اطلاعات Vsoft به حساب شما می‌شویم." -> "Sign in with Google to securely connect your Vsoft data to your account."
            "ورود با Google" -> "Sign in with Google"
            "حساب Google" -> "Google account"
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
            "حساب و همگام‌سازی" -> "الحساب والمزامنة"
            "با ورود به حساب Google، آماده اتصال امن اطلاعات Vsoft به حساب شما می‌شویم." -> "سجّل الدخول باستخدام Google لربط بيانات Vsoft بحسابك بأمان."
            "ورود با Google" -> "تسجيل الدخول باستخدام Google"
            "حساب Google" -> "حساب Google"
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
    // تراکنش‌های ثبت‌شده از پیامک بانکی، وقتی موجودی داخل همان پیامک وجود دارد،
    // قبلاً موجودی واقعی کارت را مستقیماً به‌روزرسانی کرده‌اند؛ بنابراین نباید
    // دوباره روی موجودی گزارش‌شده جمع/کم شوند.
    val movement = transactions
        .filter { it.card == card.name && !it.description.startsWith("ثبت خودکار از پیامک") }
        .sumOf {
            if (it.type == "income") it.amount else -it.amount
        }
    val workIncome = workDays.filter { it.card == card.name }.sumOf { it.income }
    return card.balance + movement + workIncome
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
                    o.optString("cardNumber", o.optString("last4", "")),
                    o.optString("last4", "").takeLast(4),
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

// ---------------- BACKUP ----------------

data class VsoftBackup(
    val transactions: List<Transaction>,
    val workDays: List<WorkDay>,
    val cards: List<BankCard>,
    val people: List<Person>,
    val workplaces: List<Workplace>,
    val language: String,
    val currency: String,
    val theme: String,
    val glass: Boolean,
    val font: String
)

fun encodeBackup(data: VsoftBackup): String {
    return JSONObject().apply {
        put("version", 1)
        put("transactions", JSONArray(encodeTransactions(data.transactions)))
        put("workDays", JSONArray(encodeWork(data.workDays)))
        put("cards", JSONArray(encodeCards(data.cards)))
        put("people", JSONArray(encodePeople(data.people)))
        put("workplaces", JSONArray(encodeWorkplaces(data.workplaces)))
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
        VsoftBackup(
            decodeTransactions(o.optJSONArray("transactions")?.toString() ?: "[]"),
            decodeWork(o.optJSONArray("workDays")?.toString() ?: "[]"),
            decodeCards(o.optJSONArray("cards")?.toString() ?: "[]"),
            decodePeople(o.optJSONArray("people")?.toString() ?: "[]"),
            decodeWorkplaces(o.optJSONArray("workplaces")?.toString() ?: "[]"),
            o.optString("language", "fa"),
            o.optString("currency", "IRT"),
            o.optString("theme", "system"),
            o.optBoolean("glass", false),
            o.optString("font", "sans")
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
        p[LANGUAGE_KEY] = data.language
        p[CURRENCY_KEY] = data.currency
        p[THEME_KEY] = data.theme
        p[GLASS_KEY] = data.glass.toString()
        p[FONT_KEY] = data.font
    }
}

suspend fun autoRegisterSmsTransaction(context: Context, sms: PendingSms): Boolean {
    val preferences = context.dataStore.data.first()
    val currency = preferences[CURRENCY_KEY] ?: "IRT"
    val transactions = decodeTransactions(preferences[TRANSACTIONS_KEY] ?: "[]")
    val cards = decodeCards(preferences[CARDS_KEY] ?: "[]")

    var bank = sms.bank
    var card = cards.firstOrNull { sms.last4.isNotBlank() && it.last4 == sms.last4 }

    fun sameBank(a: String, b: String): Boolean {
        val x = smsNormalize(a).lowercase(Locale.ROOT).trim()
        val y = smsNormalize(b).lowercase(Locale.ROOT).trim()
        return x == y || x.removePrefix("بانک ") == y.removePrefix("بانک ") ||
            x.contains(y) || y.contains(x)
    }

    if (bank.isBlank() && card != null) bank = card.bank
    if (card == null && bank.isNotBlank()) {
        val bankCards = cards.filter { sameBank(it.bank, bank) }
        if (bankCards.size == 1) card = bankCards.first()
    }

    if (card == null) return false

    val amount = if (currency == "IRT") sms.amountRial / 10 else sms.amountRial
    if (amount <= 0L) return false

    val duplicate = transactions.any {
        it.card == card.name &&
            it.amount == amount &&
            it.type == sms.type &&
            it.date == jalaliDateOf(sms.time) &&
            it.description.startsWith("ثبت خودکار از پیامک")
    }
    if (duplicate) return false

    val updatedTransactions = transactions.toMutableList()
    updatedTransactions.add(
        Transaction(
            id = sms.id,
            type = sms.type,
            amount = amount,
            category = if (sms.type == "income") "Other income — سایر درآمدها" else "Other expense — سایر هزینه‌ها",
            description = "ثبت خودکار از پیامک " + bank.ifBlank { "بانکی" },
            date = jalaliDateOf(sms.time),
            card = card.name,
            person = ""
        )
    )

    var updatedCards = cards.toMutableList()
    if (sms.balanceRial >= 0L) {
        val reportedBalance = if (currency == "IRT") sms.balanceRial / 10 else sms.balanceRial

        // موجودی درج‌شده در خود پیامک، موجودی فعلی کارت است؛
        // بنابراین همان مقدار مستقیماً روی کارت ذخیره می‌شود.
        updatedCards = updatedCards.map {
            if (it.id == card!!.id) it.copy(balance = reportedBalance) else it
        }.toMutableList()
    }

    context.dataStore.edit { p ->
        p[TRANSACTIONS_KEY] = encodeTransactions(updatedTransactions)
        p[CARDS_KEY] = encodeCards(updatedCards)
    }
    return true
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
        glass = false
        font = preferences[FONT_KEY] ?: "sans"

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

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    var firebaseUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth.currentUser) }
    var authError by remember { mutableStateOf<String?>(null) }

    val googleClient = remember(context) {
        createGoogleSignInClient(context, context.getString(R.string.default_web_client_id))
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            authError = null
            val intent = result.data
            val signInTask = GoogleSignIn.getSignedInAccountFromIntent(intent)

            runCatching {
                val account = signInTask.getResult(ApiException::class.java)
                val idToken = account.idToken ?: error("Google did not return an ID token")
                firebaseSignInWithGoogleIdToken(idToken)
            }.onFailure { error ->
                val apiError = error as? ApiException
                val statusCode = apiError?.statusCode
                val statusMessage = apiError?.status?.statusMessage
                authError = buildString {
                    append("Google sign-in failed")
                    if (statusCode != null) append(" (code $statusCode)")
                    if (!statusMessage.isNullOrBlank()) append(": $statusMessage")
                    else if (!error.message.isNullOrBlank()) append(": " + error.message)
                    if (result.resultCode != Activity.RESULT_OK) {
                        append(" [resultCode=" + result.resultCode + "]")
                    }
                }
            }
        }
    }

    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth -> firebaseUser = auth.currentUser }
        firebaseAuth.addAuthStateListener(listener)
        onDispose { firebaseAuth.removeAuthStateListener(listener) }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val snapshot = VsoftBackup(
                transactions, workDays, cards, people, workplaces,
                language, currency, theme, glass, font
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
                if (text != null) pendingRestore = decodeBackup(text)
            }
        }
    }

    LaunchedEffect(pendingRestore) {
        val data = pendingRestore ?: return@LaunchedEffect
        restoreBackup(context, data)
        transactions = data.transactions.toMutableList()
        workDays = data.workDays.toMutableList()
        cards = data.cards.toMutableList()
        people = data.people.toMutableList()
        workplaces = data.workplaces.toMutableList()
        language = data.language
        currency = data.currency
        theme = data.theme
        glass = false
        font = data.font
        pendingRestore = null
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
                primary = Color(0xFF9DBBFF),
                onPrimary = Color(0xFF07111F),
                secondary = Color(0xFF7EE2C4),
                onSecondary = Color(0xFF062018),
                tertiary = Color(0xFFFFC078),
                onTertiary = Color(0xFF2A1600),
                background = Color(0xFF070B12),
                onBackground = Color(0xFFF2F6FC),
                surface = Color(0xFF111824),
                onSurface = Color(0xFFF2F6FC),
                surfaceVariant = Color(0xFF202A38),
                onSurfaceVariant = Color(0xFFC3CDDB),
                outline = Color(0xFF66758A)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF315EFB),
                onPrimary = Color.White,
                secondary = Color(0xFF087F68),
                onSecondary = Color.White,
                tertiary = Color(0xFFC96F12),
                onTertiary = Color.White,
                background = Color(0xFFF5F7FB),
                onBackground = Color(0xFF101828),
                surface = Color.White,
                onSurface = Color(0xFF101828),
                surfaceVariant = Color(0xFFEDF1F7),
                onSurfaceVariant = Color(0xFF596579),
                outline = Color(0xFF7A8799)
            )
        }

        val themedColors = if (glass) {
            if (darkTheme) colors.copy(
                background = Color(0xFF070B12),
                surface = Color(0xCC18212C),
                surfaceVariant = Color(0x661F2B38)
            ) else colors.copy(
                background = Color(0xFFF1F5FC),
                surface = Color(0xBFFFFFFF),
                surfaceVariant = Color(0x88FFFFFF)
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
                extraSmall = RoundedCornerShape(10.dp),
                small = RoundedCornerShape(14.dp),
                medium = RoundedCornerShape(20.dp),
                large = RoundedCornerShape(28.dp),
                extraLarge = RoundedCornerShape(32.dp)
            )
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
                firebaseUser = firebaseUser,
                authError = authError,
                onGoogleSignIn = {
                    authError = null
                    googleLauncher.launch(googleClient.signInIntent)
                },
                onGoogleSignOut = {
                    authError = null
                    runCatching { signOutFromGoogle(context) }
                        .onFailure { authError = it.message ?: "Sign-out failed" }
                }
            )
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
    onLanguageChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onGlassChange: (Boolean) -> Unit,
    onFontChange: (String) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    firebaseUser: FirebaseUser?,
    authError: String?,
    onGoogleSignIn: () -> Unit,
    onGoogleSignOut: () -> Unit
) {

    var selectedPage by remember { mutableStateOf(0) }

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
        "پیامک بانکی"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    AnimatedContent(
                        targetState = pages[selectedPage],
                        transitionSpec = {
                            val rtl = language != "en"
                            val direction = if (targetState != initialState) {
                                if (rtl) -1 else 1
                            } else {
                                1
                            }
                            (fadeIn(tween(180)) + slideInHorizontally(tween(260)) { direction * it / 5 }) togetherWith
                                (fadeOut(tween(140)) + slideOutHorizontally(tween(200)) { -direction * it / 6 })
                        },
                        label = "top_title"
                    ) { title ->
                        Column {
                            Text(
                                "VSOFT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        }
                    }
                },
                actions = {
                    TopActionIcon(Icons.Default.CreditCard, "کارت‌ها", selectedPage == 5) { selectedPage = 5 }
                    TopActionIcon(Icons.Default.Place, "محل‌های کار", selectedPage == 6) { selectedPage = 6 }
                    TopActionIcon(Icons.Default.Person, "افراد", selectedPage == 7) { selectedPage = 7 }
                    TopActionIcon(Icons.Default.Sms, "پیامک", selectedPage == 9) { selectedPage = 9 }
                },
                navigationIcon = {
                    IconButton(modifier = Modifier.pressScale(0.92f), onClick = { selectedPage = 4 }) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = strings.settings,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {

            NavigationBar(
                modifier = Modifier
                    .padding(8.dp)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .10f)),
                        RoundedCornerShape(28.dp)
                    ),
                containerColor = if (LocalVsoftGlass.current)
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                else MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {

                NavigationBarItem(
                    selected = selectedPage == 0,
                    onClick = { selectedPage = 0 },
                    icon = { AnimatedNavIcon(Icons.Default.Home, selectedPage == 0) },
                    label = {
                        Text(strings.dashboard)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 1,
                    onClick = { selectedPage = 1 },
                    icon = { AnimatedNavIcon(Icons.Default.AccountBalanceWallet, selectedPage == 1) },
                    label = {
                        Text(strings.finance)
                    },
                    alwaysShowLabel = true
                )

                NavigationBarItem(
                    selected = selectedPage == 2,
                    onClick = { selectedPage = 2 },
                    icon = { AnimatedNavIcon(Icons.Default.Work, selectedPage == 2) },
                    label = {
                        Text(strings.work)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 3,
                    onClick = { selectedPage = 3 },
                    icon = { AnimatedNavIcon(Icons.Default.BarChart, selectedPage == 3) },
                    label = {
                        Text(strings.reports)
                    }
                )

                NavigationBarItem(
                    selected = selectedPage == 8,
                    onClick = { selectedPage = 8 },
                    icon = { AnimatedNavIcon(Icons.Default.MoreHoriz, selectedPage == 8) },
                    label = {
                        Text(if (language == "en") "Tools" else if (language == "ar") "الأدوات" else "ابزارها")
                    }
                )

            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedPage) {
                    var dragDistance = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragDistance += dragAmount
                        },
                        onDragEnd = {
                            if (kotlin.math.abs(dragDistance) >= 80f) {
                                val swipePages = listOf(0, 1, 2, 3, 8, 9)
                                val currentIndex = swipePages.indexOf(selectedPage)
                                if (currentIndex >= 0) {
                                    val goNext = if (language == "en") dragDistance < 0f else dragDistance > 0f
                                    val nextIndex = if (goNext) {
                                        (currentIndex + 1).coerceAtMost(swipePages.lastIndex)
                                    } else {
                                        (currentIndex - 1).coerceAtLeast(0)
                                    }
                                    selectedPage = swipePages[nextIndex]
                                }
                            }
                            dragDistance = 0f
                        },
                        onDragCancel = { dragDistance = 0f }
                    )
                }
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
                        dampingRatio = 0.86f,
                        stiffness = 420f
                    ),
                    initialOffsetX = { direction * it / 2 }
                ) + fadeIn(tween(260)) + scaleIn(
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = 420f),
                    initialScale = 0.975f
                )) togetherWith
                (slideOutHorizontally(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    targetOffsetX = { -direction * it / 6 }
                ) + fadeOut(tween(180)) + scaleOut(
                    animationSpec = tween(240, easing = FastOutSlowInEasing),
                    targetScale = 0.985f
                ))
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
                    cards,
                    onWorkChange
                )

                3 -> ReportsPage(strings, transactions, workDays, cards)

                4 -> SettingsPage(
                    strings, language, theme, currency,
                    onLanguageChange, onCurrencyChange, onThemeChange,
                    glass, onGlassChange,
                    font, onFontChange,
                    onBackup = onBackup,
                    onRestore = onRestore,
                    firebaseUser = firebaseUser,
                    authError = authError,
                    onGoogleSignIn = onGoogleSignIn,
                    onGoogleSignOut = onGoogleSignOut
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
                    workplaces = workplaces
                )
            }
        }
        }
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
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 55L).coerceAtMost(330L))
        visible.targetState = true
    }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(320)) + slideInHorizontally(
            animationSpec = tween(420, easing = FastOutSlowInEasing)
        ) { direction * it / 14 } + scaleIn(
            animationSpec = spring(dampingRatio = 0.88f, stiffness = 380f),
            initialScale = 0.985f
        ),
        exit = fadeOut(tween(160)) + scaleOut(
            animationSpec = tween(160, easing = FastOutSlowInEasing),
            targetScale = 0.99f
        )
    ) { content() }
}

// ---------------- DASHBOARD ----------------

@Composable
fun DashboardPage(
    strings: AppStrings,
    transactions: List<Transaction>,
    workDays: List<WorkDay>,
    cards: List<BankCard>
) {
    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val workIncome = workDays.sumOf { it.income }
    val openingBalance = cards.sumOf { it.balance }
    val balance = openingBalance + income + workIncome - expense
    val totalHours = workDays.sumOf { calculateHours(it.start, it.end) }

    val currentJalali = today()
    val monthPrefix = currentJalali.substringBeforeLast("/") + "/"
    val monthWorkDays = workDays.filter { it.date.startsWith(monthPrefix) }
    val monthWorkIncome = monthWorkDays.sumOf { it.income }
    val monthExpenses = transactions
        .filter { it.type == "expense" && it.date.startsWith(monthPrefix) }
        .sumOf { it.amount }
    val monthIncome = transactions
        .filter { it.type == "income" && it.date.startsWith(monthPrefix) }
        .sumOf { it.amount }
    val monthNet = monthWorkIncome + monthIncome - monthExpenses
    val monthAverage = if (monthWorkDays.isEmpty()) 0L else monthWorkIncome / monthWorkDays.size

    val animatedBalance by animateFloatAsState(balance.toFloat(), tween(650), label = "balance")
    val animatedIncome by animateFloatAsState((income + workIncome).toFloat(), tween(750), label = "income")
    val animatedExpense by animateFloatAsState(expense.toFloat(), tween(800), label = "expense")

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            VsoftEntrance(0) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "VSOFT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            strings.dashboard,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            uiText("وضعیت مالی و کاری شما در یک نگاه"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Dashboard,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }
        }

        item {
            VsoftEntrance(1) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = .92f)
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = .16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        null,
                                        tint = Color.White
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        uiText("موجودی کل"),
                                        color = Color.White.copy(alpha = .76f),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        money(animatedBalance.toLong()),
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Icon(
                                    Icons.Default.TrendingUp,
                                    null,
                                    tint = Color.White.copy(alpha = .82f),
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            Spacer(Modifier.height(18.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DashboardPill(
                                    uiText("درآمد"),
                                    money(animatedIncome.toLong()),
                                    Icons.Default.AddCircleOutline,
                                    Modifier.weight(1f)
                                )
                                DashboardPill(
                                    uiText("هزینه"),
                                    money(animatedExpense.toLong()),
                                    Icons.Default.RemoveCircleOutline,
                                    Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardMetric(
                    uiText("ساعت کاری"),
                    String.format(Locale.US, "%.1f ساعت", totalHours),
                    Icons.Default.AccessTime,
                    MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f)
                )
                DashboardMetric(
                    uiText("کارت بانکی"),
                    cards.size.toString(),
                    Icons.Default.CreditCard,
                    MaterialTheme.colorScheme.secondary,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            VsoftEntrance(3) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = .08f)
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (LocalVsoftLanguage.current == "en") "This month"
                                    else if (LocalVsoftLanguage.current == "ar") "هذا الشهر"
                                    else "این ماه",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    uiText("خلاصه عملکرد این ماه"),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (monthNet >= 0) MaterialTheme.colorScheme.secondary.copy(alpha = .12f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = .12f)
                                    )
                                    .padding(horizontal = 11.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    money(monthNet),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthNet >= 0)
                                        MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            DashboardMiniStat(
                                if (LocalVsoftLanguage.current == "en") "Work income" else if (LocalVsoftLanguage.current == "ar") "دخل العمل" else "درآمد کاری",
                                money(monthWorkIncome),
                                Icons.Default.Work,
                                MaterialTheme.colorScheme.secondary,
                                Modifier.weight(1f)
                            )
                            DashboardMiniStat(
                                if (LocalVsoftLanguage.current == "en") "Expenses" else if (LocalVsoftLanguage.current == "ar") "المصروفات" else "هزینه‌ها",
                                money(monthExpenses),
                                Icons.Default.TrendingDown,
                                MaterialTheme.colorScheme.tertiary,
                                Modifier.weight(1f)
                            )
                            DashboardMiniStat(
                                if (LocalVsoftLanguage.current == "en") "Workdays" else if (LocalVsoftLanguage.current == "ar") "أيام العمل" else "روز کاری",
                                monthWorkDays.size.toString(),
                                Icons.Default.Event,
                                MaterialTheme.colorScheme.primary,
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            DashboardMetric(
                if (LocalVsoftLanguage.current == "en") "Average workday income"
                else if (LocalVsoftLanguage.current == "ar") "متوسط دخل يوم العمل"
                else "میانگین درآمد هر روز کاری",
                money(monthAverage),
                Icons.Default.AutoGraph,
                MaterialTheme.colorScheme.primary,
                Modifier.fillMaxWidth()
            )
        }

        item {
            VsoftEntrance(4) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        uiText("کارت‌های بانکی"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        cards.size.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            if (cards.isEmpty()) {
                EmptyState(uiText("هنوز کارتی ثبت نشده"), Icons.Default.CreditCard)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(cards, key = { _, it -> it.id }) { index, card ->
                        VsoftEntrance(index.coerceAtMost(5)) {
                            MiniBankCard(card, cardCurrentBalance(card, transactions, workDays))
                        }
                    }
                }
            }
        }

        item {
            VsoftEntrance(6) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        uiText("آخرین تراکنش‌ها"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        transactions.size.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                EmptyState(uiText("هنوز تراکنشی ثبت نشده"), Icons.Default.ReceiptLong)
            }
        } else {
            itemsIndexed(
                transactions.sortedByDescending { it.id }.take(5),
                key = { _, it -> it.id }
            ) { index, t ->
                VsoftEntrance(index.coerceAtMost(4)) {
                    TransactionCard(t, onDelete = {}, showActions = false)
                }
            }
        }
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
fun MiniBankCard(card: BankCard, balance: Long) {
    val shape = RoundedCornerShape(26.dp)
    val (startColor, endColor) = bankCardColors(card.bank)
    Card(Modifier.width(270.dp).vsoftGlass(shape), shape = shape,
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

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    
                    Text(transactions.size.toString() + " " + if (LocalVsoftLanguage.current == "en") "transactions" else "تراکنش", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 18.dp), singleLine = true, shape = RoundedCornerShape(18.dp),
            label = { Text(strings.search) }, leadingIcon = { Icon(Icons.Default.Search, null) })
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(filter == "all", { filter = "all" }, label = { Text(if (LocalVsoftLanguage.current == "en") "All" else "همه") })
            FilterChip(filter == "income", { filter = "income" }, label = { Text(strings.income) })
            FilterChip(filter == "expense", { filter = "expense" }, label = { Text(strings.expense) })
        }
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                EmptyState(uiText("تراکنشی با این فیلتر پیدا نشد"), Icons.Default.SearchOff)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(list, key = { _, it -> it.id }) { index, t ->
                    VsoftEntrance(index.coerceAtMost(7)) {
                        VsoftSwipeToDelete(
                            onDelete = {
                                val x = transactions.toMutableList()
                                x.removeAll { it.id == t.id }
                                onTransactionsChange(x)
                            }
                        ) {
                            TransactionCard(
                                t,
                                onDelete = {
                                    val x = transactions.toMutableList()
                                    x.removeAll { it.id == t.id }
                                    onTransactionsChange(x)
                                },
                                onEdit = { edit = t; show = true }
                            )
                        }
                    }
                }
            }
        }
        }
        FloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp).pressScale(),
            onClick = { edit = null; show = true }, containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(18.dp)) { Icon(Icons.Default.Add, "Add") }
    }
    if (show) AddTransactionDialog(strings, cards, people, edit, { show = false }) { t ->
        val x = transactions.toMutableList()
        val i = x.indexOfFirst { it.id == t.id }
        if (i >= 0) x[i] = t else x.add(t)
        onTransactionsChange(x)
        show = false
    }
}

@Composable
fun VsoftSwipeToDelete(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val maxReveal = 92f

    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = if (rtl) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DeleteOutline, uiText("حذف"), tint = MaterialTheme.colorScheme.onErrorContainer)
                Text(uiText("حذف"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Box(
            Modifier.fillMaxWidth()
                .graphicsLayer { translationX = offset.value }
                .pointerInput(rtl) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val next = if (rtl) {
                                    (offset.value + dragAmount).coerceIn(0f, maxReveal)
                                } else {
                                    (offset.value + dragAmount).coerceIn(-maxReveal, 0f)
                                }
                                offset.snapTo(next)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (kotlin.math.abs(offset.value) >= maxReveal * .72f) {
                                    onDelete()
                                } else {
                                    offset.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offset.animateTo(0f, tween(180)) }
                        }
                    )
                }
        ) { content() }
    }
}

// ---------------- TRANSACTION CARD ----------------

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    showActions: Boolean = true
) {
    val isIncome = transaction.type == "income"
    val accent = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val lift by animateFloatAsState(
        targetValue = if (isIncome) 0f else 0f,
        animationSpec = tween(220),
        label = "transaction_lift"
    )
    Card(Modifier.fillMaxWidth().animateContentSize().graphicsLayer { translationY = lift }, shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = .16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape)
                .background(accent.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                Icon(if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null, tint = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.category, fontWeight = FontWeight.Bold)
                if (transaction.description.isNotBlank())
                    Text(transaction.description, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(transaction.date, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (transaction.card.isNotBlank())
                        Text("• ${transaction.card}", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text((if (isIncome) "+" else "−") + money(transaction.amount),
                    color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                if (showActions) Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp))
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
    var personOpen by remember { mutableStateOf(false) }
    val financeCategories = listOf(
        "Salary — حقوق", "Freelance — فریلنسری", "Project income — درآمد پروژه", "Business income — درآمد کسب‌وکار",
        "Bonus — پاداش", "Gift — هدیه", "Investment return — سود سرمایه‌گذاری", "Interest — سود بانکی",
        "Refund — بازگشت وجه", "Transfer — انتقال وجه", "Other income — سایر درآمدها",
        "Food & groceries — غذا و خرید روزمره", "Restaurant & cafe — رستوران و کافه", "Transport — حمل‌ونقل",
        "Fuel — سوخت", "Taxi & ride-hailing — تاکسی و تاکسی اینترنتی", "Rent & housing — اجاره و مسکن",
        "Utilities — آب، برق و گاز", "Internet & mobile — اینترنت و موبایل", "Subscriptions — اشتراک‌ها",
        "Shopping — خرید", "Clothing — پوشاک", "Health & medicine — سلامت و دارو", "Education — آموزش",
        "Entertainment — سرگرمی", "Travel — سفر", "Insurance — بیمه", "Bank fees — کارمزد بانکی",
        "Loan payment — پرداخت وام", "Debt repayment — بازپرداخت بدهی", "Family — خانواده",
        "Home — خانه", "Electronics — لوازم الکترونیکی", "Personal care — مراقبت شخصی",
        "Charity — خیریه", "Taxes — مالیات", "Work expenses — هزینه‌های کاری",
        "Tools & equipment — ابزار و تجهیزات", "Maintenance — تعمیر و نگهداری", "Other expense — سایر هزینه‌ها"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, shadowElevation = 18.dp) {
            Column(Modifier.padding(22.dp).heightIn(max = 620.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (existing == null) uiText("ثبت تراکنش جدید") else uiText("ویرایش تراکنش"), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text(uiText("اطلاعات مالی را دقیق و سریع ثبت کنید"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, uiText("بستن")) }
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(type == "expense", { type = "expense" }, label = { Text(strings.expense) },
                        leadingIcon = { Icon(Icons.Default.TrendingDown, null, Modifier.size(17.dp)) }, modifier = Modifier.weight(1f))
                    FilterChip(type == "income", { type = "income" }, label = { Text(strings.income) },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, null, Modifier.size(17.dp)) }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(amount, { amount = normalizeAmountInput(it) }, label = { Text(strings.amount) },
                    leadingIcon = { Icon(Icons.Default.Payments, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = GroupedNumberVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ categoryOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.Label, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (category.isBlank()) "دسته‌بندی / Category" else category)
                    }
                    DropdownMenu(categoryOpen, { categoryOpen = false }) {
                        financeCategories.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryOpen = false })
                        }
                    }
                }
                OutlinedTextField(description, { description = it }, label = { Text(strings.description) },
                    leadingIcon = { Icon(Icons.Default.Notes, null) }, minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
                OutlinedButton({ dateOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                    Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text("${strings.date}: ${date}")
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ cardOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.CreditCard, null); Spacer(Modifier.width(8.dp)); Text(if (card.isBlank()) uiText("انتخاب کارت مبدا / مقصد") else "کارت: ${card}")
                    }
                    DropdownMenu(cardOpen, { cardOpen = false }) {
                        DropdownMenuItem({ Text(uiText("بدون کارت")) }, { card = ""; cardOpen = false })
                        cards.forEach { q -> DropdownMenuItem({ Text(q.name) }, { card = q.name; cardOpen = false }) }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ personOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text(if (person.isBlank()) uiText("انتخاب شخص") else "شخص: ${person}")
                    }
                    DropdownMenu(personOpen, { personOpen = false }) {
                        DropdownMenuItem({ Text(uiText("بدون شخص")) }, { person = ""; personOpen = false })
                        people.forEach { q -> DropdownMenuItem({ Text(q.name) }, { person = q.name; personOpen = false }) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onDismiss, Modifier.weight(1f), shape = RoundedCornerShape(17.dp)) { Text(strings.cancel) }
                    Button(onClick = {
                        val v = normalizeDigits(amount).toLongOrNull() ?: 0L
                        if (v > 0 && category.isNotBlank())
                            onSave(Transaction(existing?.id ?: System.currentTimeMillis(), type, v, category, description, date, card, person))
                    }, Modifier.weight(1f).pressScale(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text(strings.save)
                    }
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
    onWorkChange: (MutableList<WorkDay>) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<WorkDay?>(null) }
    val totalHours = workDays.sumOf { calculateHours(it.start, it.end) }
    val totalIncome = workDays.sumOf { it.income }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    
                    Text(
                        if (LocalVsoftLanguage.current == "en")
                            "${workDays.size} workdays • ${String.format(Locale.US, "%.1f", totalHours)} hours"
                        else
                            "${workDays.size} روز کاری • ${String.format(Locale.US, "%.1f", totalHours)} ساعت",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                    )
                }
            }

        Row(Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardMetric("درآمد کاری", money(totalIncome), Icons.Default.Payments,
                MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            DashboardMetric("محل‌های کار", workplaces.size.toString(), Icons.Default.Place,
                MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        if (workDays.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                EmptyState("هنوز روز کاری ثبت نشده", Icons.Default.WorkHistory)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(workDays.sortedByDescending { it.id }, key = { _, it -> it.id }) { index, w ->
                    VsoftEntrance(index.coerceAtMost(7)) {
                        VsoftSwipeToDelete(onDelete = {
                            val x = workDays.toMutableList()
                            x.removeAll { it.id == w.id }
                            onWorkChange(x)
                        }) {
                            WorkCard(w,
                                onDelete = {
                                    val x = workDays.toMutableList()
                                    x.removeAll { it.id == w.id }
                                    onWorkChange(x)
                                },
                                onEdit = { edit = w; show = true })
                        }
                    }
                }
            }
        }
        }
        FloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp).pressScale(0.90f),
            onClick = { edit = null; show = true }, containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(20.dp)) {
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
    Card(Modifier.fillMaxWidth().clickable { onEdit() }.animateContentSize().pressScale(0.985f),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = .14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Work, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(work.place.ifBlank { "محل کار ثبت نشده" },
                        fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(if (work.startDate.isNotBlank() && work.endDate.isNotBlank())
                        if (LocalLayoutDirection.current == LayoutDirection.Rtl) "${work.startDate} ← ${work.endDate}" else "${work.startDate} → ${work.endDate}"
                        else work.date,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, null) }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkMeta(Icons.Default.Schedule, if (LocalLayoutDirection.current == LayoutDirection.Rtl) "${work.start} ← ${work.end}" else "${work.start} → ${work.end}", Modifier.weight(1f))
                WorkMeta(Icons.Default.Timer, String.format(Locale.US, "%.1f ساعت", hours), Modifier.weight(1f))
            }
            Spacer(Modifier.height(9.dp))
            Text(money(work.income), fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary, fontSize = 17.sp)
            if (work.person.isNotBlank()) Text("با: ${work.person}", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (work.card.isNotBlank()) Text("واریز به: " + work.card, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            if (work.description.isNotBlank()) Text(work.description, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
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
    var dateOpen by remember { mutableStateOf(false) }; var endDateOpen by remember { mutableStateOf(false) }
    var placeOpen by remember { mutableStateOf(false) }; var personOpen by remember { mutableStateOf(false) }; var cardOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, shadowElevation = 18.dp) {
            Column(Modifier.padding(22.dp).heightIn(max = 650.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Work, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (existing == null) uiText("ثبت روز کاری") else uiText("ویرایش روز کاری"), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text(uiText("ساعت، درآمد و جزئیات کار را یکجا ثبت کنید"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, uiText("بستن")) }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ placeOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.Place, null); Spacer(Modifier.width(8.dp)); Text(if (place.isBlank()) uiText("انتخاب محل کار") else place)
                    }
                    DropdownMenu(placeOpen, { placeOpen = false }) { workplaces.forEach { q -> DropdownMenuItem({ Text(q.name) }, { place = q.name; placeOpen = false }) } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ dateOpen = true }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp)) { Text("شروع: ${startDate}") }
                    OutlinedButton({ endDateOpen = true }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp)) { Text("پایان: ${endDate}") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({
                        android.app.TimePickerDialog(ctx, { _, h, m -> start = "%02d:%02d".format(h, m) },
                            start.substringBefore(":").toIntOrNull() ?: 8, start.substringAfter(":").toIntOrNull() ?: 0, true).show()
                    }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp)) { Icon(Icons.Default.Login, null); Spacer(Modifier.width(5.dp)); Text("شروع ${start}") }
                    OutlinedButton({
                        android.app.TimePickerDialog(ctx, { _, h, m -> end = "%02d:%02d".format(h, m) },
                            end.substringBefore(":").toIntOrNull() ?: 16, end.substringAfter(":").toIntOrNull() ?: 0, true).show()
                    }, Modifier.weight(1f), shape = RoundedCornerShape(17.dp)) { Icon(Icons.Default.Logout, null); Spacer(Modifier.width(5.dp)); Text("پایان ${end}") }
                }
                OutlinedTextField(income, { income = normalizeAmountInput(it) }, label = { Text(uiText("درآمد کار")) },
                    leadingIcon = { Icon(Icons.Default.Payments, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = GroupedNumberVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ cardOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.CreditCard, null); Spacer(Modifier.width(8.dp)); Text(if (card.isBlank()) uiText("درآمد به کدام کارت برود؟") else "واریز به: ${card}")
                    }
                    DropdownMenu(cardOpen, { cardOpen = false }) {
                        DropdownMenuItem({ Text(uiText("بدون کارت")) }, { card = ""; cardOpen = false })
                        cards.forEach { q -> DropdownMenuItem({ Text(q.name) }, { card = q.name; cardOpen = false }) }
                    }
                }
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton({ personOpen = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text(if (person.isBlank()) uiText("انتخاب شخص / کارفرما") else person)
                    }
                    DropdownMenu(personOpen, { personOpen = false }) {
                        DropdownMenuItem({ Text(uiText("بدون شخص")) }, { person = ""; personOpen = false })
                        people.forEach { q -> DropdownMenuItem({ Text(q.name) }, { person = q.name; personOpen = false }) }
                    }
                }
                OutlinedTextField(description, { description = it }, label = { Text(uiText("شرح کار")) },
                    leadingIcon = { Icon(Icons.Default.Notes, null) }, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp), placeholder = { Text(uiText("مثلاً نصب تابلو، تعمیر موتور، سیم‌کشی...")) })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onDismiss, Modifier.weight(1f), shape = RoundedCornerShape(17.dp)) { Text(strings.cancel) }
                    Button(onClick = {
                        onSave(WorkDay(existing?.id ?: System.currentTimeMillis(), place, startDate, start, end,
                            normalizeDigits(income).toLongOrNull() ?: 0L, description, person, startDate, endDate, card))
                    }, Modifier.weight(1f).pressScale(), shape = RoundedCornerShape(17.dp)) {
                        Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text(strings.save)
                    }
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
    var selectedCardName by remember(cards) { mutableStateOf(cards.firstOrNull()?.name ?: "") }
    val selected = cards.firstOrNull { it.name == selectedCardName }
    val cardTransactions = transactions.filter { it.card == selectedCardName }
    val cardWork = workDays.filter { it.card == selectedCardName }
    val income = cardTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = cardTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val opening = selected?.balance ?: 0L
    val workIncome = cardWork.sumOf { it.income }
    val current = opening + income + workIncome - expense
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
            val tx = transactions.filter { it.card == selectedCardName && it.date.startsWith(prefix) }
            val wd = workDays.filter { it.card == selectedCardName && it.date.startsWith(prefix) }
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
            Text(strings.monthlyReport, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(uiText("گزارش مالی، روند ماهانه و عملکرد کارت را یکجا ببینید."),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    Text(uiText("انتخاب کارت"), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (cards.isEmpty()) {
                        Text(uiText("هنوز کارت بانکی ثبت نشده"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cards, key = { it.id }) { card ->
                                FilterChip(card.name == selectedCardName, { selectedCardName = card.name },
                                    label = { Text(card.name) }, leadingIcon = { Icon(Icons.Default.CreditCard, null, Modifier.size(16.dp)) })
                            }
                        }
                    }
                }
            }
        }
        if (selected == null) {
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
                    val targetIncome = (point.second.toFloat() / maxValue.toFloat()).coerceIn(0.04f, 1f)
                    val targetExpense = (point.third.toFloat() / maxValue.toFloat()).coerceIn(0.04f, 1f)
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
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1)
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

// ---------------- SETTINGS ----------------

@Composable
fun SettingsPage(
    strings: AppStrings, language: String, theme: String, currency: String,
    onLanguageChange: (String) -> Unit, onCurrencyChange: (String) -> Unit,
    onThemeChange: (String) -> Unit, glass: Boolean, onGlassChange: (Boolean) -> Unit,
    font: String, onFontChange: (String) -> Unit, onBackup: () -> Unit, onRestore: () -> Unit,
    firebaseUser: FirebaseUser?, authError: String?, onGoogleSignIn: () -> Unit, onGoogleSignOut: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { Text(strings.settings, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) }
        item { SettingsSection(uiText("زبان"), Icons.Default.Language) {
            LanguageOption("فارسی", "fa", language, onLanguageChange)
            LanguageOption("English", "en", language, onLanguageChange)
            LanguageOption("العربية", "ar", language, onLanguageChange)
        }}
        item { SettingsSection(if (language == "fa") "واحد پول" else "Currency", Icons.Default.Payments) {
            ThemeOption("تومان — IR Toman", "IRT", currency, onCurrencyChange)
            ThemeOption("ریال — Iranian Rial", "IRR", currency, onCurrencyChange)
            ThemeOption("دلار آمریکا — US Dollar", "USD", currency, onCurrencyChange)
            ThemeOption("یورو — Euro", "EUR", currency, onCurrencyChange)
            ThemeOption("پوند — British Pound", "GBP", currency, onCurrencyChange)
            ThemeOption("درهم — UAE Dirham", "AED", currency, onCurrencyChange)
            ThemeOption("لیر — Turkish Lira", "TRY", currency, onCurrencyChange)
        }}
        item { SettingsSection(strings.theme, Icons.Default.Palette) {
            ThemeOption(strings.light, "light", theme, onThemeChange)
            ThemeOption(strings.dark, "dark", theme, onThemeChange)
            ThemeOption(strings.system, "system", theme, onThemeChange)
        }}
        item { SettingsSection(uiText("حساب و همگام‌سازی"), Icons.Default.AccountCircle) {
            if (firebaseUser == null) {
                Text(uiText("با ورود به حساب Google، آماده اتصال امن اطلاعات Vsoft به حساب شما می‌شویم."), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGoogleSignIn, modifier = Modifier.fillMaxWidth().pressScale()) {
                    Icon(Icons.Default.AccountCircle, null); Spacer(Modifier.width(8.dp)); Text(uiText("ورود با Google"))
                }
            } else {
                Text(firebaseUser.displayName ?: uiText("حساب Google"), fontWeight = FontWeight.Bold)
                Text(firebaseUser.email ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onGoogleSignOut, modifier = Modifier.fillMaxWidth().pressScale()) {
                    Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text(uiText("خروج از حساب"))
                }
            }
            if (!authError.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp)); Text(authError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }}
        item { SettingsSection(uiText("پشتیبان‌گیری و بازیابی"), Icons.Default.Backup) {
            Text(uiText("یک نسخه کامل از اطلاعات Vsoft روی گوشی ذخیره می‌شود و می‌توانی آن را بعداً روی همین یا یک گوشی دیگر بازیابی کنی."), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onBackup, modifier = Modifier.weight(1f).pressScale()) {
                    Icon(Icons.Default.Upload, null); Spacer(Modifier.width(6.dp)); Text(uiText("ایجاد پشتیبان"))
                }
                OutlinedButton(onClick = onRestore, modifier = Modifier.weight(1f).pressScale()) {
                    Icon(Icons.Default.Download, null); Spacer(Modifier.width(6.dp)); Text(uiText("بازیابی"))
                }
            }
        }}
        item { SettingsSection(uiText("فونت برنامه"), Icons.Default.FontDownload) {
            FontOption(uiText("مدرن و خوانا"), "sans", font, onFontChange)
            FontOption(uiText("کلاسیک"), "serif", font, onFontChange)
            FontOption(uiText("فنی"), "mono", font, onFontChange)
            FontOption(uiText("دست‌نویس"), "cursive", font, onFontChange)
        }}
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
    var search by remember { mutableStateOf("") }
    val filteredCards = cards.filter {
        val q = normalizeVsoftSearch(search)
        q.isBlank() || normalizeVsoftSearch(it.bank).contains(q) || normalizeVsoftSearch(it.name).contains(q) || it.cardNumber.contains(q)
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                Column {
                    Text(uiText("کارت‌های بانکی"), fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(cards.size.toString() + " " + uiText("کارت"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            item {
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text(uiText("جستجوی کارت")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
            }
            itemsIndexed(filteredCards, key = { _, it -> it.id }) { index, card ->
                VsoftEntrance(index.coerceAtMost(5)) {
                    VsoftSwipeToDelete(onDelete = {
                        onCardsChange(cards.toMutableList().also { list -> list.removeAll { it.id == card.id } })
                    }) {
                        CardItem(card, cardCurrentBalance(card, transactions, workDays),
                            onEdit = { editing = card },
                            onDelete = { onCardsChange(cards.toMutableList().also { list -> list.removeAll { it.id == card.id } }) })
                    }
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp).pressScale(),
            onClick = { show = true }, shape = RoundedCornerShape(18.dp)
        ) { Icon(Icons.Default.Add, "افزودن کارت") }
    }
    if (show) AddCardDialog(onDismiss = { show = false }, onSave = { newCard ->
        onCardsChange(cards.toMutableList().also { it.add(newCard) }); show = false
    })
    if (editing != null) EditCardDialog(editing!!, onDismiss = { editing = null }, onSave = { updated ->
        onCardsChange(cards.toMutableList().also { list ->
            val i = list.indexOfFirst { it.id == updated.id }; if (i >= 0) list[i] = updated
        }); editing = null
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
                Column {
                    Text(if (LocalVsoftLanguage.current == "en") "Workplaces" else "محل‌های کار", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(workplaces.size.toString() + " " + if (LocalVsoftLanguage.current == "en") "workplaces" else "محل کار", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text(uiText("جستجوی محل کار")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
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
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)), contentAlignment=Alignment.Center) {
                        Icon(Icons.Default.Edit, null, tint=MaterialTheme.colorScheme.primary, modifier=Modifier.size(23.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(uiText("ویرایش محل کار"), fontSize=21.sp, fontWeight=FontWeight.ExtraBold)
                        Text(uiText("نام محل کار را به‌روزرسانی کنید"), fontSize=12.sp, color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick=onDismiss){Icon(Icons.Default.Close, uiText("بستن"))}
                }
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
                Column {
                    Text("افراد", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(people.size.toString() + " نفر", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text(uiText("جستجوی افراد")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
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
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            uiText("ویرایش شخص"),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            uiText("اطلاعات فرد را به‌روزرسانی کنید"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, uiText("بستن"))
                    }
                }

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
                    Text(
                        value,
                        fontSize = 16.sp,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
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
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(Icons.Default.CreditCard,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(23.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(uiText("کارت بانکی جدید"),fontSize=21.sp,fontWeight=FontWeight.ExtraBold);Text(uiText("مشخصات کارت را وارد کنید"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick=onDismiss){Icon(Icons.Default.Close,uiText("بستن"))}}
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
 var bank by remember(card.id){mutableStateOf(card.bank)};var name by remember(card.id){mutableStateOf(card.name)};var num by remember(card.id){mutableStateOf(card.cardNumber)};var balance by remember(card.id){mutableStateOf(card.balance.toString())}
 Dialog(onDismissRequest=onDismiss){Surface(Modifier.fillMaxWidth().padding(8.dp),shape=RoundedCornerShape(30.dp),color=if(LocalVsoftGlass.current)MaterialTheme.colorScheme.surface.copy(alpha=.94f)else MaterialTheme.colorScheme.surface,tonalElevation=8.dp,shadowElevation=18.dp){
  Column(Modifier.padding(22.dp).heightIn(max=650.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(Icons.Default.Edit,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(23.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(uiText("ویرایش کارت"),fontSize=21.sp,fontWeight=FontWeight.ExtraBold);Text(uiText("اطلاعات کارت را به‌روزرسانی کنید"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick=onDismiss){Icon(Icons.Default.Close,uiText("بستن"))}}
   val c=bankCardColors(bank);Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(c.first.copy(alpha=.92f),c.second.copy(alpha=.88f)))).padding(15.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CreditCard,null,tint=Color.White);Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(bank.ifBlank{uiText("کارت")},color=Color.White,fontWeight=FontWeight.ExtraBold);Text(name.ifBlank{"VSOFT"},color=Color.White.copy(alpha=.72f),fontSize=11.sp)};Text("•••• "+num.filter(Char::isDigit).takeLast(4),color=Color.White,fontSize=12.sp)}}
   OutlinedTextField(bank,{bank=it},label={Text(uiText("بانک"))},leadingIcon={Icon(Icons.Default.AccountBalance,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(name,{name=it},label={Text(uiText("نام کارت"))},leadingIcon={Icon(Icons.Default.CreditCard,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(num,{num=normalizeDigits(it).filter(Char::isDigit).take(16)},label={Text(uiText("شماره کامل کارت"))},leadingIcon={Icon(Icons.Default.Numbers,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,supportingText={Text(num.length.toString()+" / 16")},shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(balance,{balance=normalizeAmountInput(it)},label={Text(uiText("موجودی اولیه"))},leadingIcon={Icon(Icons.Default.AccountBalanceWallet,null)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),visualTransformation=GroupedNumberVisualTransformation(),singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onDismiss,Modifier.weight(1f),shape=RoundedCornerShape(16.dp)){Text(uiText("لغو"))};Button(onClick={val n=normalizeDigits(num).filter(Char::isDigit).take(16);onSave(card.copy(bank=bank.trim(),name=name.trim().ifBlank{bank.trim()},cardNumber=n,last4=n.takeLast(4),balance=normalizeDigits(balance).toLongOrNull()?:0L))},enabled=bank.isNotBlank(),modifier=Modifier.weight(1f).pressScale(),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.Check,null,modifier=Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text(uiText("ذخیره"))}}
  }
 }}
}

@Composable
fun AddWorkplaceDialog(onDismiss:()->Unit,onSave:(Workplace)->Unit){
 var name by remember{mutableStateOf("")}
 Dialog(onDismissRequest=onDismiss){Surface(Modifier.fillMaxWidth().padding(8.dp),shape=RoundedCornerShape(30.dp),color=if(LocalVsoftGlass.current)MaterialTheme.colorScheme.surface.copy(alpha=.94f)else MaterialTheme.colorScheme.surface,tonalElevation=8.dp,shadowElevation=18.dp){
  Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(Icons.Default.Place,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(23.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(uiText("محل کار جدید"),fontSize=21.sp,fontWeight=FontWeight.ExtraBold);Text(uiText("محل کار مورد استفاده را اضافه کنید"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick=onDismiss){Icon(Icons.Default.Close,uiText("بستن"))}}
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
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.secondary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Icon(Icons.Default.PersonAdd,null,tint=MaterialTheme.colorScheme.secondary,modifier=Modifier.size(23.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(uiText("شخص جدید"),fontSize=21.sp,fontWeight=FontWeight.ExtraBold);Text(uiText("اطلاعات فرد را برای کار و مالی ثبت کنید"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick=onDismiss){Icon(Icons.Default.Close,uiText("بستن"))}}
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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        month--
                        if (month < 1) { month = 12; year-- }
                    }) { Text("‹") }

                    AnimatedContent(
                        targetState = "${jalaliMonthName(month)} $year",
                        transitionSpec = {
                            fadeIn(tween(160)) + slideInHorizontally(tween(180)) { it / 4 } togetherWith
                                fadeOut(tween(100)) + slideOutHorizontally(tween(120)) { -it / 5 }
                        },
                        label = "jalali_month_title"
                    ) { title ->
                        Text(title, fontWeight = FontWeight.Bold)
                    }

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
                for (i in 1..jalaliMonthDays(year, month)) cells.add(i)
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