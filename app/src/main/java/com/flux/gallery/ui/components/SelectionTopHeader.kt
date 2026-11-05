package com.flux.gallery.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.R
import com.flux.gallery.ui.theme.FluxTheme
import com.styropyr0.prismal.components.PrismalGlassToggle
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.specular.PrismalSpecular

@Composable
fun RecentTopHeader(
    isSelectionMode: Boolean,
    selectedCount: Int,
    isAllSelected: Boolean,
    onSelectAllToggle: () -> Unit,
    onCancel: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onCreateClick: () -> Unit = {},
    onStartSlideshow: () -> Unit = {},
    isBlurThumbnails: Boolean = false,
    onBlurThumbnailsChange: (Boolean) -> Unit = {},
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val containerColor = FluxTheme.colors.glassContainer
    val dividerColor = FluxTheme.colors.glassDivider

    var isMenuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = isMenuOpen) {
        isMenuOpen = false
    }

    // Tap-on-blur dismiss scrim (full-screen transparent barrier)
    if (isMenuOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isMenuOpen = false }
                )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Selection Mode Header (3 Liquid Glass Pills: Batal, n Dipilih, Pilih semua)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn(tween(180)) + scaleIn(
                initialScale = 0.82f,
                animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f)
            ),
            exit = fadeOut(tween(140)) + scaleOut(
                targetScale = 0.82f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pill: "Batal"
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .drawPrismalGlass(
                            backdrop = backdrop,
                            shape = { PrismalCapsule() },
                            specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                            effects = {
                                applyPrismalGlassEffects(
                                    density = density,
                                    adaptiveLuminance = false,
                                    luminance = 0.5f,
                                    blurRadiusPx = with(density) { 8.dp.toPx() },
                                    refractionHeightPx = with(density) { 16.dp.toPx() },
                                    refractionAmountPx = with(density) { 14.dp.toPx() }
                                )
                            },
                            onDrawSurface = { drawRect(containerColor) }
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Batal",
                        color = FluxTheme.colors.activeTeal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Center Pill: "n Dipilih"
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .drawPrismalGlass(
                            backdrop = backdrop,
                            shape = { PrismalCapsule() },
                            specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                            effects = {
                                applyPrismalGlassEffects(
                                    density = density,
                                    adaptiveLuminance = false,
                                    luminance = 0.5f,
                                    blurRadiusPx = with(density) { 8.dp.toPx() },
                                    refractionHeightPx = with(density) { 16.dp.toPx() },
                                    refractionAmountPx = with(density) { 14.dp.toPx() }
                                )
                            },
                            onDrawSurface = { drawRect(containerColor) }
                        )
                        .clip(CircleShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedCount == 0) "Pilih item" else "$selectedCount Dipilih",
                        color = FluxTheme.colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Right Pill: "Pilih semua" / "Batal pilih"
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .drawPrismalGlass(
                            backdrop = backdrop,
                            shape = { PrismalCapsule() },
                            specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                            effects = {
                                applyPrismalGlassEffects(
                                    density = density,
                                    adaptiveLuminance = false,
                                    luminance = 0.5f,
                                    blurRadiusPx = with(density) { 8.dp.toPx() },
                                    refractionHeightPx = with(density) { 16.dp.toPx() },
                                    refractionAmountPx = with(density) { 14.dp.toPx() }
                                )
                            },
                            onDrawSurface = { drawRect(containerColor) }
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onSelectAllToggle)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAllSelected) "Batal pilih" else "Pilih semua",
                        color = FluxTheme.colors.activeTeal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 2. Default Mode: 3-Dot Circle Button & High-Performance 60FPS Morphing Popover Card
        // 2a. 40dp 3-Dot Circle Button (Visible when menu is closed)
        AnimatedVisibility(
            visible = !isSelectionMode && !isMenuOpen,
            enter = fadeIn(tween(180, delayMillis = 60)) + scaleIn(
                initialScale = 0.5f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 260f)
            ),
            exit = fadeOut(tween(80)) + scaleOut(
                targetScale = 0.5f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 320f)
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(40.dp)
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 8.dp.toPx() },
                                refractionHeightPx = with(density) { 16.dp.toPx() },
                                refractionAmountPx = with(density) { 14.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .clip(CircleShape)
                    .clickable { isMenuOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "Opsi lainnya",
                    tint = FluxTheme.colors.headerText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2b. High-Performance 60FPS Popover Menu Card (Anchored to top-end, hardware matrix scale)
        AnimatedVisibility(
            visible = !isSelectionMode && isMenuOpen,
            enter = fadeIn(tween(140)) + scaleIn(
                initialScale = 0.18f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 320f)
            ),
            exit = fadeOut(tween(180)) + scaleOut(
                targetScale = 0.18f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 260f)
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(width = 220.dp, height = 192.dp)
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(18.dp) },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 8.dp.toPx() },
                                refractionHeightPx = with(density) { 18.dp.toPx() },
                                refractionAmountPx = with(density) { 16.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .clip(RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.TopEnd
            ) {
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    HeaderContextMenuItem(
                        text = "Pilih",
                        onClick = {
                            isMenuOpen = false
                            onEnterSelectionMode()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.6.dp)
                            .background(dividerColor)
                    )
                    HeaderContextMenuItem(
                        text = "Buat",
                        onClick = {
                            isMenuOpen = false
                            onCreateClick()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.6.dp)
                            .background(dividerColor)
                    )
                    HeaderContextMenuItem(
                        text = "Mulai Slideshow",
                        onClick = {
                            isMenuOpen = false
                            onStartSlideshow()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.6.dp)
                            .background(dividerColor)
                    )
                    // 4. Blur Thumbnail Privacy Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blur Thumbnail",
                            color = FluxTheme.colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        PrismalGlassToggle(
                            selected = { isBlurThumbnails },
                            onSelect = onBlurThumbnailsChange,
                            backdrop = backdrop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderContextMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = FluxTheme.colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
