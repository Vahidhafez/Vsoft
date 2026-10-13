package com.vsoft.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────── تعامل ─────────────

/** کلیک به سبک iOS: بدون ripple، فقط کم‌رنگ شدن هنگام لمس. */
@Composable
fun Modifier.vsoftTap(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val animatedAlpha by animateFloatAsState(if (pressed) 0.55f else 1f, tween(90), label = "vsoft_tap")
    return this
        .graphicsLayer { this.alpha = animatedAlpha }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

@Composable
fun rememberVsoftHaptic(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } }
}

// ───────────── گروه‌بندی (Inset Grouped List) ─────────────

@Composable
fun VsoftSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = VsoftTheme.colors.labelSecondary,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

/** لینک متنی کوچک کنار عنوان بخش (مثل «همه»). */
@Composable
fun VsoftHeaderAction(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        color = VsoftTheme.colors.accent,
        modifier = Modifier.vsoftTap(onClick = onClick)
    )
}

@Composable
fun VsoftGroupedSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    headerTrailing: (@Composable () -> Unit)? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = VsoftTheme.colors
    Column(modifier.fillMaxWidth().padding(top = if (header == null) 16.dp else 0.dp)) {
        if (header != null) VsoftSectionHeader(header, trailing = headerTrailing)
        Surface(shape = VsoftShapes.card, color = c.surface, modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
        if (footer != null) {
            Text(
                footer,
                style = MaterialTheme.typography.bodySmall,
                color = c.labelSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** بین ردیف‌ها بگذار (نه بعد از آخرین ردیف). */
@Composable
fun VsoftGroupedDivider(startInset: Dp = 16.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startInset),
        thickness = 0.5.dp,
        color = VsoftTheme.colors.separator
    )
}

/** کارت ساده‌ی سفید (بدون سایه و بوردر). */
@Composable
fun VsoftCardSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = VsoftShapes.card, color = VsoftTheme.colors.surface, modifier = modifier.fillMaxWidth()) {
        Column(content = content)
    }
}

@Composable
fun VsoftIconTile(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 30.dp) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(size * 0.24f)).background(tint),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.6f))
    }
}

/** آیکن دایره‌ای کم‌رنگ (برای ردیف تراکنش و ...). */
@Composable
fun VsoftSoftIcon(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    Box(
        modifier.size(size).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun VsoftGroupedRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = VsoftTheme.colors.accent,
    showChevron: Boolean = false,
    titleColor: Color = VsoftTheme.colors.label,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = VsoftTheme.colors
    val rowModifier = if (onClick != null) modifier.vsoftTap(onClick = onClick) else modifier
    Row(
        rowModifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            VsoftIconTile(icon, iconTint)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.labelSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (value != null) {
            Spacer(Modifier.width(8.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = c.labelSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = c.labelTertiary, modifier = Modifier.size(20.dp))
        }
    }
}

// ───────────── ورودی‌ها ─────────────

@Composable
fun VsoftSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = VsoftTheme.colors
    val haptic = rememberVsoftHaptic()
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.fill).padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(if (selected) c.segmentSelected else Color.Transparent, tween(160), label = "seg_bg")
            Box(
                Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(8.dp)).background(bg)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        if (!selected) { haptic(); onSelect(index) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = c.label,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VsoftSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val c = VsoftTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF34C759),
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = if (c.isDark) Color(0xFF39393D) else Color(0xFFE9E9EA),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

/** فیلد مینیمال با لیبل بالای فیلد (به‌جای floating label). */
@Composable
fun VsoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    isError: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = VsoftTheme.colors
    val placeholderContent: (@Composable () -> Unit)? =
        if (placeholder != null) { { Text(placeholder, color = c.labelTertiary) } } else null
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) c.expense else c.labelSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            isError = isError,
            textStyle = textStyle,
            placeholder = placeholderContent,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            leadingIcon = leading,
            trailingIcon = trailing,
            shape = VsoftShapes.control,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = c.fieldBackground,
                unfocusedContainerColor = c.fieldBackground,
                disabledContainerColor = c.fieldBackground,
                errorContainerColor = c.fieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = c.accent,
                focusedTextColor = c.label,
                unfocusedTextColor = c.label
            )
        )
    }
}

/** سرچ‌بار به سبک iOS (ارتفاع ۴۰). */
@Composable
fun VsoftSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val c = VsoftTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.label),
        cursorBrush = SolidColor(c.accent),
        modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(c.fill),
        decorationBox = { inner ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, null, tint = c.labelTertiary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = c.labelTertiary, maxLines = 1)
                    }
                    inner()
                }
                if (value.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Cancel, null, tint = c.labelTertiary,
                        modifier = Modifier.size(18.dp).vsoftTap { onValueChange("") }
                    )
                }
            }
        }
    )
}

// ───────────── دکمه‌ها ─────────────

@Composable
fun VsoftPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    icon: ImageVector? = null
) {
    val c = VsoftTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        enabled = enabled,
        shape = VsoftShapes.control,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destructive) c.expense else c.accent,
            contentColor = Color.White,
            disabledContainerColor = c.fill,
            disabledContentColor = c.labelTertiary
        ),
        elevation = null
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** دکمه‌ی tinted (ثانویه). */
@Composable
fun VsoftSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    icon: ImageVector? = null
) {
    val c = VsoftTheme.colors
    val tint = if (destructive) c.expense else c.accent
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = VsoftShapes.control,
        colors = ButtonDefaults.buttonColors(containerColor = tint.copy(alpha = 0.12f), contentColor = tint),
        elevation = null
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun VsoftTextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, destructive: Boolean = false) {
    val c = VsoftTheme.colors
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = if (destructive) c.expense else c.accent, style = MaterialTheme.typography.bodyLarge)
    }
}

// ───────────── شیت فرم ─────────────

/**
 * جایگزین Dialog+Surface برای فرم‌ها. محتوای داخلی خودش اسکرول داشته باشد
 * (Column با verticalScroll و heightIn(max = 640.dp)). اسکرول تودرتو نساز.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VsoftFormSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = VsoftTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surface,
        shape = VsoftShapes.sheet,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = c.labelTertiary.copy(alpha = 0.5f)) }
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding()) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            content()
        }
    }
}

// ───────────── نمایش داده ─────────────

@Composable
fun VsoftEmptyState(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = VsoftTheme.colors
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = c.labelTertiary, modifier = Modifier.size(44.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.label, textAlign = TextAlign.Center)
        if (message != null) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = c.labelSecondary, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            VsoftTextAction(actionLabel, onAction)
        }
    }
}

/** مبلغ با ارقام هم‌عرض (tabular). */
@Composable
fun VsoftAmountText(
    amount: Long,
    modifier: Modifier = Modifier,
    color: Color = VsoftTheme.colors.label,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    prefix: String = "",
    fontWeight: FontWeight? = null
) {
    Text(
        prefix + money(amount),
        modifier = modifier,
        color = color,
        style = style.merge(TextStyle(fontFeatureSettings = "tnum")),
        fontWeight = fontWeight ?: style.fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun VsoftProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = VsoftTheme.colors.accent) {
    val c = VsoftTheme.colors
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(300), label = "vsoft_progress")
    Box(modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(c.fill)) {
        Box(Modifier.fillMaxWidth(animated).height(4.dp).clip(CircleShape).background(color))
    }
}
