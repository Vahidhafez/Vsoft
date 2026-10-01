package com.vsoft.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import kotlin.math.roundToInt

/** Persian/Arabic friendly search normalization used across Vsoft's offline search. */
fun normalizeVsoftSearch(value: String): String {
    return value
        .replace('ي', 'ی')
        .replace('ى', 'ی')
        .replace('ك', 'ک')
        .replace('ة', 'ه')
        .replace('ۀ', 'ه')
        .replace('ؤ', 'و')
        .replace('إ', 'ا')
        .replace('أ', 'ا')
        .replace('ٱ', 'ا')
        .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3')
        .replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7')
        .replace('۸', '8').replace('۹', '9')
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()
}

/** Swipe-to-delete with threshold, spring motion, haptic feedback and undo snackbar hook. */
@Composable
fun VsoftSwipeToDelete(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var deleting by remember { mutableStateOf(false) }
    var hapticSent by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val targetOffset = if (deleting) -1000f else offsetX
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "swipe_delete_offset"
    )
    val progress = (kotlin.math.abs(animatedOffset) / 180f).coerceIn(0f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = progress))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, drag ->
                        offsetX = (offsetX + drag).coerceIn(-220f, 24f)
                        if (kotlin.math.abs(offsetX) > 170f && !hapticSent) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hapticSent = true
                        }
                    },
                    onDragEnd = {
                        if (offsetX < -170f) {
                            deleting = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        } else {
                            offsetX = 0f
                            hapticSent = false
                        }
                    },
                    onDragCancel = {
                        offsetX = 0f
                        hapticSent = false
                    }
                )
            }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
        }
        androidx.compose.foundation.layout.Box(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
        ) {
            AnimatedVisibility(
                visible = !deleting,
                exit = fadeOut() + shrinkVertically()
            ) { content() }
        }
    }
}

/** Lightweight skeleton placeholder for lists and dashboards. */
@Composable
fun VsoftSkeleton(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 72.dp
) {
    val infinite = rememberInfiniteTransition(label = "skeleton")
    val alpha by infinite.animateFloat(
        initialValue = .42f,
        targetValue = .78f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(700),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
    )
}

/** Scroll-to-top helper for long native Compose lists. */
fun Modifier.vsoftScrollToTopOnDoubleTap(
    state: LazyListState,
    scope: kotlinx.coroutines.CoroutineScope
): Modifier = pointerInput(state) {
    var lastTap = 0L
    detectTapGestures {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastTap < 280L) {
            scope.launch { state.animateScrollToItem(0) }
        }
        lastTap = now
    }
}


/** Floating action button for returning a LazyColumn to its first item. */
@Composable
fun VsoftScrollToTopFab(
    state: LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier
) {
    val show by remember {
        derivedStateOf { state.firstVisibleItemIndex > 1 || state.firstVisibleItemScrollOffset > 500 }
    }
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(180)) + androidx.compose.animation.scaleIn(tween(180), initialScale = 0.8f),
        exit = fadeOut(tween(120)) + androidx.compose.animation.scaleOut(tween(120), targetScale = 0.8f),
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = { scope.launch { state.animateScrollToItem(0) } },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                contentDescription = uiText("بازگشت به بالا")
            )
        }
    }
}

/** Native Material pull-to-refresh container used by Vsoft's local-first screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VsoftPullToRefresh(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            if (isRefreshing) return@PullToRefreshBox
            scope.launch {
                isRefreshing = true
                runCatching { onRefresh() }
                isRefreshing = false
            }
        },
        state = state,
        modifier = modifier
    ) { content() }
}
