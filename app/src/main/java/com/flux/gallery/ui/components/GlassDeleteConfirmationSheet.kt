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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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

    // Semi-transparent scrim to capture outside clicks and dismiss
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    // Popover hovering directly above the floating pill with matching width
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.88f,
            transformOrigin = TransformOrigin(0.5f, 1f),
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)
        ) + fadeIn(tween(180)),
        exit = scaleOut(
            targetScale = 0.88f,
            transformOrigin = TransformOrigin(0.5f, 1f),
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
        ) + fadeOut(tween(140)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 84.dp) // Sits neatly above the floating pill
                .padding(horizontal = 24.dp), // Matches floating pill width exactly
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(20.dp) },
                        specular = { PrismalSpecular.Default.copy(alpha = 0.75f) },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = false,
                                luminance = 0.5f,
                                blurRadiusPx = with(density) { 14.dp.toPx() },
                                refractionHeightPx = with(density) { 18.dp.toPx() },
                                refractionAmountPx = with(density) { 16.dp.toPx() }
                            )
                        },
                        onDrawSurface = { drawRect(surfaceColor) }
                    )
                    .clip(RoundedCornerShape(20.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Teks singkat di atasnya
                val titleText = if (itemCount <= 1) "Hapus foto ini?" else "Hapus $itemCount foto ini?"

                Text(
                    text = titleText,
                    color = FluxTheme.colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )

                // Divider horizontal tipis
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(dividerColor)
                )

                // Row berisi tombol Batal dan Hapus dalam satu baris berdampingan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Batal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Batal",
                            color = FluxTheme.colors.textSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Divider vertikal tipis pemisah Batal dan Hapus
                    Box(
                        modifier = Modifier
                            .width(0.6.dp)
                            .fillMaxHeight()
                            .background(dividerColor)
                    )

                    // Tombol Hapus (Destructive Red)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                onDismiss()
                                onConfirmDelete()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hapus",
                            color = AppleDestructiveRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
