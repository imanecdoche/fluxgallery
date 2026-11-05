package com.flux.gallery.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.ui.theme.FluxTheme
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.specular.PrismalSpecular

private val AppleDestructiveRed = Color(0xFFFF3B30)

@Composable
fun GlassDeleteConfirmationSheet(
    visible: Boolean,
    itemCount: Int,
    backdrop: PrismalBackdrop,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val surfaceColor = FluxTheme.colors.glassSurface
    val dividerColor = FluxTheme.colors.glassDivider

    BackHandler(enabled = visible) {
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(240)),
        exit = fadeOut(tween(180))
    ) {
        // Scrim backdrop (tap to dismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(tween(240)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(220)
        ) + fadeOut(tween(180)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // Card 1: Warning Message & Destructive Action
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(22.dp) },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 16.dp.toPx() },
                                refractionHeightPx = with(density) { 18.dp.toPx() },
                                refractionAmountPx = with(density) { 14.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(surfaceColor) }
                    )
                    .clip(RoundedCornerShape(22.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header / Warning Subtext
                val warningText = if (itemCount <= 1) {
                    "Foto ini akan dihapus dari penyimpanan perangkat Anda."
                } else {
                    "$itemCount foto akan dihapus dari penyimpanan perangkat Anda."
                }

                Text(
                    text = warningText,
                    color = FluxTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(dividerColor)
                )

                // Destructive Delete Button
                val deleteButtonText = if (itemCount <= 1) "Hapus Foto" else "Hapus $itemCount Foto"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onConfirmDelete()
                        }
                        .padding(vertical = 15.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deleteButtonText,
                        color = AppleDestructiveRed,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // Card 2: Separated Cancel Button (Liquid Glass)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(20.dp) },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 16.dp.toPx() },
                                refractionHeightPx = with(density) { 18.dp.toPx() },
                                refractionAmountPx = with(density) { 14.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(surfaceColor) }
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Batal",
                    color = FluxTheme.colors.activeTeal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
