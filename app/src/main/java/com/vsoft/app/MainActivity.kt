package com.vsoft.app

import android.os.Bundle
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
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
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.math.BigInteger

private val android.content.Context.dataStore by preferencesDataStore("vsoft_data")

private val TRANSACTIONS_KEY = stringPreferencesKey("transactions")
private val WORK_KEY = stringPreferencesKey("work_days")
private val CARDS_KEY = stringPreferencesKey("cards")
private val PEOPLE_KEY = stringPreferencesKey("people")
private val WORKPLACES_KEY = stringPreferencesKey("workplaces")
private val LANGUAGE_KEY = stringPreferencesKey("language")
private val THEME_KEY = stringPreferencesKey("theme")
private val GLASS_KEY = stringPreferencesKey("glass")
private val FONT_KEY = stringPreferencesKey("font")

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

@Composable
fun Modifier.vsoftGlass(shape: RoundedCornerShape = RoundedCornerShape(22.dp)): Modifier {
    if (!LocalVsoftGlass.current) return this
    val dark = isSystemInDarkTheme()
    val matte = if (dark) Color(0xD91A2028) else Color(0xD9FFFFFF)
    val topSheen = if (dark) Color.White.copy(alpha = .055f) else Color.White.copy(alpha = .60f)
    return this
        .clip(shape)
        .shadow(
            elevation = 8.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (dark) .22f else .08f),
            spotColor = Color.Black.copy(alpha = if (dark) .16f else .06f)
        )
        .background(matte, shape)
        .border(
            BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        topSheen,
                        Color.White.copy(alpha = if (dark) .10f else .24f),
                        Color.Transparent
                    )
                )
            ),
            shape
        )
}
fun money(value: Long): String {
    return NumberFormat.getNumberInstance(Locale("fa", "IR")).format(value) + " تومان"
}

fun cardCurrentBalance(card: BankCard, transactions: List<Transaction>, workDays: List<WorkDay>): Long {
    val movement = transactions.filter { it.card == card.name }.sumOf {
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
        p[THEME_KEY] = data.theme
        p[GLASS_KEY] = data.glass.toString()
        p[FONT_KEY] = data.font
    }
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

        val preferences = context.dataStore.data.first()

        language = preferences[LANGUAGE_KEY] ?: "fa"
        theme = preferences[THEME_KEY] ?: "system"
        glass = preferences[GLASS_KEY] == "true"
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

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val snapshot = VsoftBackup(
                transactions, workDays, cards, people, workplaces,
                language, theme, glass, font
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
        theme = data.theme
        glass = data.glass
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
        LocalVsoftLanguage provides language
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
                        context.dataStore.edit { prefs ->
                            prefs[LANGUAGE_KEY] = it
                        }
                    }
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
    onThemeChange: (String) -> Unit,
    onGlassChange: (Boolean) -> Unit,
    onFontChange: (String) -> Unit
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
        strings.people
    )

    Box(
        Modifier.fillMaxSize().background(
            if (glass) Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = .30f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = .16f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = .075f),
                    MaterialTheme.colorScheme.background.copy(alpha = .92f),
                    Color.Transparent
                ),
                radius = 1250f
            ) else Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
            )
        )
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (glass) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = .98f)
                ),
                title = {
                    AnimatedContent(
                        targetState = pages[selectedPage],
                        transitionSpec = {
                            (fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 5 }) togetherWith
                                (fadeOut(tween(120)) + slideOutHorizontally(tween(160)) { -it / 6 })
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
                modifier = if (glass) Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .shadow(18.dp, RoundedCornerShape(28.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = .10f))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isSystemInDarkTheme()) .06f else .34f),
                                MaterialTheme.colorScheme.surface.copy(alpha = .66f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = .26f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = .08f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = .13f)
                                )
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                else Modifier,
                containerColor = if (glass) Color.Transparent else MaterialTheme.colorScheme.surface,
                tonalElevation = if (glass) 0.dp else 10.dp
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
                    }
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

            }
        }
    ) { padding ->

        AnimatedContent(
            targetState = selectedPage,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    initialOffsetX = { direction * it / 2 }
                ) + fadeIn(tween(300)) + scaleIn(tween(420), initialScale = 0.985f)) togetherWith
                (slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -direction * it / 5 }
                ) + fadeOut(tween(220)) + scaleOut(tween(300), targetScale = 0.985f))
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
                    strings, language, theme,
                    onLanguageChange, onThemeChange,
                    glass, onGlassChange,
                    font, onFontChange,
                    onBackup = { backupLauncher.launch("Vsoft-Backup.json") },
                    onRestore = { restoreLauncher.launch(arrayOf("application/json", "text/plain")) }
                )
                5 -> CardsPage(cards, transactions, workDays, onCardsChange)
                6 -> WorkplacesPage(workplaces, onWorkplacesChange)
                7 -> PeoplePage(people, onPeopleChange)
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
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(pressedScale) {
            detectTapGestures(
                onPress = {
                    scaleState.floatValue = pressedScale
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
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 55L).coerceAtMost(330L))
        visible.targetState = true
    }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(360)) + slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { it / 14 }
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
    val animatedBalance by animateFloatAsState(balance.toFloat(), animationSpec = tween(650), label = "balance")
    val animatedIncome by animateFloatAsState((income + workIncome).toFloat(), animationSpec = tween(750), label = "income")
    val animatedExpense by animateFloatAsState(expense.toFloat(), animationSpec = tween(800), label = "expense")

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("VSOFT", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text(strings.dashboard, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                VsoftEntrance(0) { Text(uiText("وضعیت مالی و کاری شما در یک نگاه"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = .16f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(uiText("موجودی کل"), color = Color.White.copy(alpha = .82f), fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(money(animatedBalance.toLong()), color = Color.White,
                        fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { 1f },
                        Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)),
                        color = Color.White.copy(alpha = .72f),
                        trackColor = Color.White.copy(alpha = .16f)
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("درآمد", money(animatedIncome.toLong()), Icons.Default.TrendingUp,
                    MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                DashboardMetric("هزینه", money(animatedExpense.toLong()), Icons.Default.TrendingDown,
                    MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric(uiText("ساعت کاری"), String.format(Locale.US, "%.1f ساعت", totalHours),
                    Icons.Default.AccessTime, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                DashboardMetric("کارت بانکی", cards.size.toString(),
                    Icons.Default.CreditCard, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
        }
        item { VsoftEntrance(4) { Text(uiText("کارت‌های بانکی"), fontSize = 21.sp, fontWeight = FontWeight.Bold) } }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(cards, key = { _, it -> it.id }) { index, card ->
                    VsoftEntrance(index.coerceAtMost(5)) {
                        MiniBankCard(card, cardCurrentBalance(card, transactions, workDays))
                    }
                }
            }
        }
        item { VsoftEntrance(6) { Text(uiText("آخرین تراکنش‌ها"), fontSize = 21.sp, fontWeight = FontWeight.Bold) } }
        if (transactions.isEmpty()) {
            item { EmptyState(uiText("هنوز تراکنشی ثبت نشده"), Icons.Default.ReceiptLong) }
        } else {
            itemsIndexed(transactions.sortedByDescending { it.id }.take(5), key = { _, it -> it.id }) { index, t ->
                VsoftEntrance(index.coerceAtMost(4)) {
                    TransactionCard(t, onDelete = {})
                }
            }
        }
    }
}

@Composable
fun DashboardMetric(
    title: String, value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color, modifier: Modifier
) {
    Card(modifier.vsoftGlass(RoundedCornerShape(22.dp)), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = .12f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun MiniBankCard(card: BankCard, balance: Long) {
    Card(Modifier.width(235.dp), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(card.bank, fontWeight = FontWeight.Bold)
                    Text(card.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(if (card.cardNumber.isNotBlank())
                "••••  ••••  ••••  " + card.cardNumber.filter(Char::isDigit).takeLast(4)
            else "••••  ••••  ••••  ••••", letterSpacing = 1.5.sp, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Text(money(balance), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
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
    val list = transactions.filter {
        (filter == "all" || it.type == filter) &&
        (search.isBlank() || it.description.contains(search, true) ||
         it.category.contains(search, true) || it.person.contains(search, true) ||
         it.card.contains(search, true))
    }.sortedByDescending { it.id }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.finance, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(transactions.size.toString() + " تراکنش ثبت شده", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 18.dp), singleLine = true, shape = RoundedCornerShape(18.dp),
            label = { Text(strings.search) }, leadingIcon = { Icon(Icons.Default.Search, null) })
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(filter == "all", { filter = "all" }, label = { Text("همه") })
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
                        TransactionCard(t,
                            onDelete = {
                                val x = transactions.toMutableList()
                                x.removeAll { it.id == t.id }
                                onTransactionsChange(x)
                            },
                            onEdit = { edit = t; show = true })
                    }
                }
            }
        }
        }
        FloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp).pressScale(),
            onClick = { edit = null; show = true }, containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(18.dp)) { Icon(Icons.Default.Add, "افزودن") }
    }
    if (show) AddTransactionDialog(strings, cards, people, edit, { show = false }) { t ->
        val x = transactions.toMutableList()
        val i = x.indexOfFirst { it.id == t.id }
        if (i >= 0) x[i] = t else x.add(t)
        onTransactionsChange(x)
        show = false
    }
}

// ---------------- TRANSACTION CARD ----------------

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val isIncome = transaction.type == "income"
    val accent = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Row {
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
    var personOpen by remember { mutableStateOf(false) }

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
                OutlinedTextField(category, { category = it }, label = { Text(strings.category) },
                    leadingIcon = { Icon(Icons.Default.Label, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp))
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
                    Text(strings.work, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(workDays.size.toString() + " روز کاری • " + String.format(Locale.US, "%.1f", totalHours) + " ساعت",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
        FloatingActionButton(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp).pressScale(),
            onClick = { edit = null; show = true }, containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(18.dp)) { Icon(Icons.Default.Add, "افزودن") }
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
    Card(Modifier.fillMaxWidth().clickable { onEdit() }.animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
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
                        "${work.startDate} → ${work.endDate}" else work.date,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, null) }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkMeta(Icons.Default.Schedule, "${work.start} → ${work.end}", Modifier.weight(1f))
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

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Text(strings.monthlyReport, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(uiText("هر کارت را جداگانه بررسی کنید؛ گزارش‌ها شلوغ نمی‌شوند."),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    Text(uiText("انتخاب کارت"), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cards, key = { it.id }) { card ->
                            FilterChip(card.name == selectedCardName, { selectedCardName = card.name },
                                label = { Text(card.name) }, leadingIcon = { Icon(Icons.Default.CreditCard, null, Modifier.size(16.dp)) })
                        }
                    }
                }
            }
        }
        if (selected == null) {
            item { EmptyState(uiText("هنوز کارت بانکی ثبت نشده"), Icons.Default.CreditCard) }
        } else {
            item { InfoCard(uiText("موجودی فعلی کارت"), money(current), Icons.Default.AccountBalance) }
            item { InfoCard(uiText("درآمد کارت"), money(income), Icons.Default.TrendingUp) }
            item { InfoCard(uiText("هزینه کارت"), money(expense), Icons.Default.TrendingDown) }
            item { InfoCard(uiText("موجودی اولیه"), money(opening), Icons.Default.CreditCard) }
            item { InfoCard(uiText("درآمد کاری واریزشده"), money(workIncome), Icons.Default.Work) }
            item { InfoCard(uiText("ساعات کاری مرتبط"), String.format(Locale.US, "%.1f ساعت", hours), Icons.Default.AccessTime) }
        }
    }
}

// ---------------- SETTINGS ----------------

@Composable
fun SettingsPage(
    strings: AppStrings,
    language: String,
    theme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    glass: Boolean,
    onGlassChange: (Boolean) -> Unit,
    font: String,
    onFontChange: (String) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text(strings.settings, fontSize = 30.sp, fontWeight = FontWeight.Bold) }
        item { SettingsSection(strings.language) {
            LanguageOption("فارسی", "fa", language, onLanguageChange)
            LanguageOption("English", "en", language, onLanguageChange)
            LanguageOption("العربية", "ar", language, onLanguageChange)
        }}
        item { SettingsSection(strings.theme) {
            ThemeOption(strings.light, "light", theme, onThemeChange)
            ThemeOption(strings.dark, "dark", theme, onThemeChange)
            ThemeOption(strings.system, "system", theme, onThemeChange)
        }}
        item { SettingsSection(uiText("تم شیشه‌ای / Liquid Glass")) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(uiText("ظاهر شفاف و چندلایه"))
                    Text(uiText("شفافیت کنترل‌شده با حرکت و عمق بیشتر"),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = glass, onCheckedChange = onGlassChange)
            }
        }}
        item {
            SettingsSection(uiText("پشتیبان‌گیری و بازیابی")) {
                Text(
                    uiText("یک نسخه کامل از اطلاعات Vsoft روی گوشی ذخیره می‌شود و می‌توانی آن را بعداً روی همین یا یک گوشی دیگر بازیابی کنی."),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onBackup,
                        modifier = Modifier.weight(1f).pressScale()
                    ) {
                        Icon(Icons.Default.Upload, null)
                        Spacer(Modifier.width(6.dp))
                        Text(uiText("ایجاد پشتیبان"))
                    }
                    OutlinedButton(
                        onClick = onRestore,
                        modifier = Modifier.weight(1f).pressScale()
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(Modifier.width(6.dp))
                        Text(uiText("بازیابی"))
                    }
                }
            }
        }
        item { SettingsSection(uiText("فونت برنامه")) {
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
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                Column {
                    Text(uiText("کارت‌های بانکی"), fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(cards.size.toString() + " کارت", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            itemsIndexed(cards, key = { _, it -> it.id }) { index, card ->
                VsoftEntrance(index.coerceAtMost(5)) {
                    CardItem(card, cardCurrentBalance(card, transactions, workDays),
                        onEdit = { editing = card },
                        onDelete = { onCardsChange(cards.toMutableList().also { list -> list.removeAll { it.id == card.id } }) })
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
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                Column {
                    Text("محل‌های کار", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(workplaces.size.toString() + " محل", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            itemsIndexed(workplaces, key = { _, it -> it.id }) { index, workplace ->
                VsoftEntrance(index.coerceAtMost(7)) {
                    Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(22.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(46.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(workplace.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                onWorkplacesChange(workplaces.toMutableList().also { list -> list.removeAll { w -> w.id == workplace.id } })
                            }) { Icon(Icons.Default.DeleteOutline, null) }
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
}

@Composable
fun PeoplePage(people: List<Person>, onPeopleChange: (MutableList<Person>) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                Column {
                    Text("افراد", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(people.size.toString() + " نفر", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            itemsIndexed(people, key = { _, it -> it.id }) { index, person ->
                VsoftEntrance(index.coerceAtMost(7)) {
                    PersonItem(person) {
                        onPeopleChange(people.toMutableList().also { list -> list.removeAll { p -> p.id == person.id } })
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
}

// ---------------- SETTINGS COMPONENTS ----------------

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().vsoftGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
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
    val name = bank.trim().lowercase()
    return when {
        "ملی" in name -> Color(0xFF0B3D91) to Color(0xFF4D8DFF)
        "مسکن" in name -> Color(0xFF00695C) to Color(0xFF26A69A)
        "بلو" in name -> Color(0xFF1565C0) to Color(0xFF42A5F5)
        "رد" in name -> Color(0xFFB71C1C) to Color(0xFFFF5252)
        "مهر" in name -> Color(0xFF0B6E4F) to Color(0xFFFFB300)
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.secondary
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
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (BankCard) -> Unit
) {
    var bank by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalized = normalizeDigits(cardNumber).filter(Char::isDigit).take(16)
                onSave(
                    BankCard(
                        id = System.currentTimeMillis(),
                        bank = bank,
                        name = name,
                        cardNumber = normalized,
                        last4 = normalized.takeLast(4),
                        balance = normalizeDigits(balance).toLongOrNull() ?: 0L
                    )
                )
            }) { Text(uiText("ذخیره")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(uiText("لغو")) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .82f) else MaterialTheme.colorScheme.surface,
        title = { Text(uiText("کارت بانکی جدید"), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(bank, { bank = it }, label = { Text(uiText("نام بانک")) }, leadingIcon = { Icon(Icons.Default.AccountBalance, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(name, { name = it }, label = { Text(uiText("عنوان کارت")) }, leadingIcon = { Icon(Icons.Default.CreditCard, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    cardNumber,
                    { cardNumber = normalizeDigits(it).filter(Char::isDigit).take(16) },
                    label = { Text(uiText("شماره کامل کارت")) },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = { Text(uiText("۱۶ رقم")) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = normalizeAmountInput(it) },
                    label = { Text(uiText("موجودی اولیه")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = GroupedNumberVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun EditCardDialog(
    card: BankCard,
    onDismiss: () -> Unit,
    onSave: (BankCard) -> Unit
) {
    var bank by remember { mutableStateOf(card.bank) }
    var name by remember { mutableStateOf(card.name) }
    var cardNumber by remember { mutableStateOf(card.cardNumber) }
    var balance by remember { mutableStateOf(card.balance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalized = normalizeDigits(cardNumber).filter(Char::isDigit).take(16)
                onSave(
                    card.copy(
                        bank = bank,
                        name = name,
                        cardNumber = normalized,
                        last4 = normalized.takeLast(4),
                        balance = normalizeDigits(balance).toLongOrNull() ?: 0L
                    )
                )
            }) { Text(uiText("ذخیره")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(uiText("لغو")) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .82f) else MaterialTheme.colorScheme.surface,
        title = { Text(uiText("ویرایش کارت"), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(bank, { bank = it }, label = { Text(uiText("بانک")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(name, { name = it }, label = { Text(uiText("نام کارت")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    cardNumber,
                    { cardNumber = normalizeDigits(it).filter(Char::isDigit).take(16) },
                    label = { Text(uiText("شماره کامل کارت")) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    balance,
                    { balance = normalizeAmountInput(it) },
                    label = { Text(uiText("موجودی اولیه")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = GroupedNumberVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
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
            }) { Text(uiText("ذخیره")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(uiText("لغو")) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .82f) else MaterialTheme.colorScheme.surface,
        title = { Text(uiText("محل کار جدید"), fontWeight = FontWeight.ExtraBold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(uiText("نام محل کار")) },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                placeholder = { Text(uiText("مثلاً پروژه، شرکت یا کارگاه")) },
                singleLine = true,
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
        Modifier
            .fillMaxWidth()
            .vsoftGlass(RoundedCornerShape(22.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalVsoftGlass.current) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .07f))
    ) {

        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

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
                Text(uiText("ذخیره"))
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text(uiText("لغو"))
            }
        },

        shape = RoundedCornerShape(28.dp),
        containerColor = if (LocalVsoftGlass.current) MaterialTheme.colorScheme.surface.copy(alpha = .82f) else MaterialTheme.colorScheme.surface,
        title = {
            Text(uiText("شخص جدید"), fontWeight = FontWeight.ExtraBold)
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(uiText("نام و نام خانوادگی")) },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(uiText("شماره تماس")) },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text(uiText("شغل / نقش")) },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(uiText("یادداشت")) },
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
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
