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

    // Morphing Dimension Animations for 3-Dot Circle -> Contextual Menu Popover Card
    val menuWidth by animateDpAsState(
        targetValue = if (isMenuOpen) 190.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = if (isMenuOpen) 0.62f else 0.72f,
            stiffness = 380f
        ),
        label = "HeaderMenuWidth"
    )
    val menuHeight by animateDpAsState(
        targetValue = if (isMenuOpen) 144.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = if (isMenuOpen) 0.62f else 0.72f,
            stiffness = 380f
        ),
        label = "HeaderMenuHeight"
    )
    val menuCorner by animateDpAsState(
        targetValue = if (isMenuOpen) 18.dp else 20.dp,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "HeaderMenuCorner"
    )

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

        // 2. Default Mode: 3-Dot Button & Morphing Contextual Popover
        AnimatedVisibility(
            visible = !isSelectionMode,
            enter = fadeIn(tween(180)) + scaleIn(
                initialScale = 0.82f,
                animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f)
            ),
            exit = fadeOut(tween(140)) + scaleOut(
                targetScale = 0.82f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f)
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp) // Anchored so 40dp circle sits centered in 52dp header row
                    .size(width = menuWidth, height = menuHeight)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(1f, 0f)
                    }
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(menuCorner) },
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
                    .clip(RoundedCornerShape(menuCorner)),
                contentAlignment = Alignment.TopEnd
            ) {
                if (!isMenuOpen) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                } else {
                    Column(
                        modifier = Modifier
                            .width(190.dp)
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
