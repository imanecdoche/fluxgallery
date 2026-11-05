package com.flux.gallery.ui.screens

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flux.gallery.R
import com.flux.gallery.data.model.MediaDateGroup
import com.flux.gallery.data.model.MediaItem
import com.flux.gallery.ui.components.RecentTopHeader
import com.flux.gallery.ui.theme.FluxTheme
import com.styropyr0.prismal.PrismalBackdrop
import io.github.alexzhirkevich.cupertino.CupertinoActivityIndicator
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.hypot

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentScreen(
    groups: List<MediaDateGroup>,
    isLoading: Boolean,
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onMediaClick: (MediaItem) -> Unit,
    onEnterSelectionMode: (MediaItem) -> Unit = {},
    onToggleSelection: (MediaItem) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onMenuClick: () -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE) }

    // Persisted column count (Default 3, Min 2 [Zoom In max], Max 5 [Zoom Out max])
    var columnCount by remember {
        mutableIntStateOf(prefs.getInt("grid_columns", 3).coerceIn(2, 5))
    }

    // Pinch-to-zoom multi-touch scroll lock state
    var isPinchZooming by remember { mutableStateOf(false) }

    val spacing = 1.5.dp
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FluxTheme.colors.background)
            // Discrete Pinch-to-Zoom Gesture Detector with Immediate Scroll Interception
            .pointerInput(columnCount) {
                awaitEachGesture {
                    var hasTriggered = false
                    var initialSpan = 0f

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pressedPointers = event.changes.filter { it.pressed }

                        if (pressedPointers.size >= 2) {
                            isPinchZooming = true
                            // Consume pointer deltas in Initial pass so child LazyColumn cannot scroll
                            pressedPointers.forEach { it.consume() }

                            val p1 = pressedPointers[0].position
                            val p2 = pressedPointers[1].position
                            val currentSpan = hypot(p1.x - p2.x, p1.y - p2.y)

                            if (initialSpan == 0f) {
                                initialSpan = currentSpan
                            } else if (!hasTriggered && initialSpan > 15f) {
                                val ratio = currentSpan / initialSpan
                                // Pinch OUT (fingers spreading apart -> Zoom In -> fewer columns, min 2)
                                if (ratio > 1.25f && columnCount > 2) {
                                    columnCount--
                                    prefs.edit().putInt("grid_columns", columnCount).apply()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hasTriggered = true
                                }
                                // Pinch IN (fingers pinching together -> Zoom Out -> more columns, max 5)
                                else if (ratio < 0.80f && columnCount < 5) {
                                    columnCount++
                                    prefs.edit().putInt("grid_columns", columnCount).apply()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hasTriggered = true
                                }
                            }
                        } else if (isPinchZooming && pressedPointers.size == 1) {
                            // Maintain scroll lock for remaining trailing finger until full release
                            pressedPointers.forEach { it.consume() }
                        } else if (pressedPointers.isEmpty()) {
                            // All fingers released, end of gesture cycle
                            isPinchZooming = false
                            break
                        }
                    }
                }
            }
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CupertinoActivityIndicator(
                    color = FluxTheme.colors.activeTeal,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada foto atau video",
                    color = FluxTheme.colors.textSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isPinchZooming,
                contentPadding = PaddingValues(
                    top = statusBarTop + 54.dp,
                    bottom = 100.dp
                )
            ) {
                items(groups, key = { it.title }) { group ->
                    MediaGroupSection(
                        group = group,
                        columns = columnCount,
                        spacing = spacing,
                        isSelectionMode = isSelectionMode,
                        selectedItemIds = selectedItemIds,
                        onMediaClick = onMediaClick,
                        onEnterSelectionMode = onEnterSelectionMode,
                        onToggleSelection = onToggleSelection
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaGroupSection(
    group: MediaDateGroup,
    columns: Int,
    spacing: Dp,
    isSelectionMode: Boolean,
    selectedItemIds: Set<Long>,
    onMediaClick: (MediaItem) -> Unit,
    onEnterSelectionMode: (MediaItem) -> Unit,
    onToggleSelection: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            .padding(bottom = 16.dp)
    ) {
        // Section Date Header (With 16dp horizontal breathing room)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FluxTheme.colors.headerText
            )

            if (!group.locationSubtext.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = group.locationSubtext,
                    fontSize = 13.sp,
                    color = FluxTheme.colors.textSecondary
                )
            }
        }

        // 100% Full Edge-to-Edge Grid (0px outer margin, exact equal weight per column)
        val chunkedItems = remember(group.items, columns) {
            group.items.chunked(columns)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            chunkedItems.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    rowItems.forEach { item ->
                        val itemHaptic = LocalHapticFeedback.current
                        val isSelected = selectedItemIds.contains(item.id)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            itemHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleSelection(item)
                                        } else {
                                            onMediaClick(item)
                                        }
                                    },
                                    onLongClick = {
                                        itemHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!isSelectionMode) {
                                            onEnterSelectionMode(item)
                                        } else {
                                            onToggleSelection(item)
                                        }
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Animated Top-Right Selection Checkmark Badge
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isSelectionMode,
                                enter = scaleIn(spring(dampingRatio = 0.8f, stiffness = 420f)) + fadeIn(),
                                exit = scaleOut() + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(FluxTheme.colors.activeTeal, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.size(24.dp)) {
                                            val path = Path().apply {
                                                moveTo(size.width * 0.29f, size.height * 0.54f)
                                                lineTo(size.width * 0.44f, size.height * 0.70f)
                                                lineTo(size.width * 0.71f, size.height * 0.32f)
                                            }
                                            drawPath(
                                                path = path,
                                                color = Color.White,
                                                style = Stroke(
                                                    width = 2.4.dp.toPx(),
                                                    cap = StrokeCap.Round,
                                                    join = StrokeJoin.Round
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                                            .border(1.8.dp, Color.White, CircleShape)
                                    )
                                }
                            }

                            // Video duration overlay at bottom right
                            if (item.isVideo) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = formatDuration(item.durationMs),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Incomplete last row: fill remaining slots with empty weighted Spacers
                    // so the existing items lock their exact column width without expanding
                    val emptySlots = columns - rowItems.size
                    repeat(emptySlots) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
