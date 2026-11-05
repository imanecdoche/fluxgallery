package com.flux.gallery.ui.screens

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.flux.gallery.ui.theme.FluxTheme
import io.github.alexzhirkevich.cupertino.CupertinoActivityIndicator
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.hypot

@Composable
fun RecentScreen(
    groups: List<MediaDateGroup>,
    isLoading: Boolean,
    onMediaClick: (MediaItem) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE) }

    // Persisted column count (Default 3, Min 2 [Zoom In max], Max 5 [Zoom Out max])
    var columnCount by remember {
        mutableIntStateOf(prefs.getInt("grid_columns", 3).coerceIn(2, 5))
    }

    val spacing = 1.5.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FluxTheme.colors.background)
            .statusBarsPadding()
            // Discrete Pinch-to-Zoom Gesture Detector
            .pointerInput(columnCount) {
                awaitEachGesture {
                    var hasTriggered = false
                    var initialSpan = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.filter { it.pressed }

                        if (pressedPointers.size >= 2) {
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
                                    pressedPointers.forEach { it.consume() }
                                }
                                // Pinch IN (fingers pinching together -> Zoom Out -> more columns, max 5)
                                else if (ratio < 0.80f && columnCount < 5) {
                                    columnCount++
                                    prefs.edit().putInt("grid_columns", columnCount).apply()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hasTriggered = true
                                    pressedPointers.forEach { it.consume() }
                                }
                            }
                        } else if (pressedPointers.isEmpty()) {
                            // All fingers released, end of gesture cycle
                            break
                        }
                    }
                }
            }
    ) {
        // Top Action Bar with 3-dot Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "Opsi lainnya",
                    tint = FluxTheme.colors.headerText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CupertinoActivityIndicator(
                    color = FluxTheme.colors.activeTeal,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(groups, key = { it.title }) { group ->
                    MediaGroupSection(
                        group = group,
                        columns = columnCount,
                        spacing = spacing,
                        onMediaClick = onMediaClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun MediaGroupSection(
    group: MediaDateGroup,
    columns: Int,
    spacing: Dp,
    onMediaClick: (MediaItem) -> Unit,
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onMediaClick(item) }
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
