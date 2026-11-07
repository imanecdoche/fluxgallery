package com.flux.gallery.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
fun AlbumsTopHeader(
    onCreateAlbumClick: () -> Unit,
    onSelectAlbumsClick: () -> Unit = {},
    onSortAlbumsClick: () -> Unit = {},
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

    // Fullscreen transparent scrim to dismiss popover when tapping outside
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

    // Zero black background: pure floating container over scrolling album grid
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left: Floating Liquid Glass Pill with "Album" label
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { PrismalCapsule() },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.55f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 16.dp.toPx() },
                                refractionHeightPx = with(density) { 16.dp.toPx() },
                                refractionAmountPx = with(density) { 14.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .clip(CircleShape)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Album",
                    color = FluxTheme.colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                )
            }

            // 2. Right: 40dp Circular Liquid Glass 3-Dot Menu Button (Visible when menu is closed)
            AnimatedVisibility(
                visible = !isMenuOpen,
                enter = fadeIn(tween(160, delayMillis = 60)) + scaleIn(
                    initialScale = 0.5f,
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = spring(dampingRatio = 0.76f, stiffness = 280f)
                ),
                exit = fadeOut(tween(80)) + scaleOut(
                    targetScale = 0.5f,
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = spring(dampingRatio = 0.76f, stiffness = 320f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .drawPrismalGlass(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            specular = { PrismalSpecular.Default.copy(alpha = 0.55f) },
                            effects = {
                                applyPrismalGlassEffects(
                                    density = density,
                                    adaptiveLuminance = false,
                                    luminance = 0.5f,
                                    blurRadiusPx = with(density) { 16.dp.toPx() },
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
                        contentDescription = "Menu Album",
                        tint = FluxTheme.colors.headerText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. Right: Morphing Liquid Glass Menu Popover Card (Anchored to top-end)
        AnimatedVisibility(
            visible = isMenuOpen,
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
                    .width(200.dp)
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(18.dp) },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.65f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 16.dp.toPx() },
                                refractionHeightPx = with(density) { 18.dp.toPx() },
                                refractionAmountPx = with(density) { 16.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AlbumContextMenuItem(
                        text = "Buat Album Baru",
                        onClick = {
                            isMenuOpen = false
                            onCreateAlbumClick()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.6.dp)
                            .background(dividerColor)
                    )
                    AlbumContextMenuItem(
                        text = "Pilih Album",
                        onClick = {
                            isMenuOpen = false
                            onSelectAlbumsClick()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.6.dp)
                            .background(dividerColor)
                    )
                    AlbumContextMenuItem(
                        text = "Urutkan",
                        onClick = {
                            isMenuOpen = false
                            onSortAlbumsClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumContextMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = FluxTheme.colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
