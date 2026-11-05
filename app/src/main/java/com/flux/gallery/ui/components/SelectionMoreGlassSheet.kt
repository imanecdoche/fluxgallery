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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

private val SelectionMoreOptions = listOf(
    "Salin ke clipboard",
    "Salin ke album",
    "Pindahkan ke album",
    "Tambahkan ke favorit",
    "Tambahkan tag",
    "Ubah tanggal dan waktu",
    "Ubah lokasi",
    "Mulai tayangan slide",
    "Atur sebagai wallpaper"
)

@Composable
fun SelectionMoreGlassSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedCount: Int,
    backdrop: PrismalBackdrop,
    onActionSelected: (String) -> Unit,
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
                .padding(bottom = 88.dp) // Floats gracefully 88dp above the floating pill navbar
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // Card 1: 9 Text-Only Options (Liquid Glass)
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
                // Header Title
                Text(
                    text = "$selectedCount item dipilih",
                    color = FluxTheme.colors.textSecondary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Divider below header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(dividerColor)
                )

                // 9 Text-Only Actions (Scrollable if screen height is constrained)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionMoreOptions.forEachIndexed { index, actionText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onActionSelected(actionText)
                                    onDismiss()
                                }
                                .padding(vertical = 13.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = actionText,
                                color = FluxTheme.colors.textPrimary, // Pure White #FFFFFF in Dark Mode
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Divider between items (except after the last item)
                        if (index < SelectionMoreOptions.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.6.dp)
                                    .background(dividerColor)
                            )
                        }
                    }
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
                    color = FluxTheme.colors.activeTeal, // Apple Teal #40C8E0
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
