package com.flux.gallery.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.styropyr0.prismal.specular.PrismalSpecular

@Composable
fun FloatingGlassMenuSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    backdrop: PrismalBackdrop,
    onItemClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val surfaceColor = FluxTheme.colors.glassSurface
    val dividerColor = FluxTheme.colors.glassDivider
    val dragHandleColor = FluxTheme.colors.dragHandle

    if (visible) {
        // Scrim backdrop (tap to dismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
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
            animationSpec = tween(320)
        ) + fadeIn(tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(240)
        ) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 88.dp) // Sits floating just above the floating bottom navbar
                .padding(horizontal = 16.dp)
                .drawPrismalGlass(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(26.dp) },
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
                .clip(RoundedCornerShape(26.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = dragHandleColor,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Upper Category Grid (4 Columns: Video, Favorit, Terbaru, Lokasi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuCategoryItem(
                    title = "Video",
                    iconRes = R.drawable.ic_video,
                    onClick = { onItemClick("Video"); onDismiss() }
                )
                MenuCategoryItem(
                    title = "Favorit",
                    iconRes = R.drawable.ic_favorite,
                    onClick = { onItemClick("Favorit"); onDismiss() }
                )
                MenuCategoryItem(
                    title = "Terbaru",
                    iconRes = R.drawable.ic_recent_menu,
                    onClick = { onItemClick("Terbaru"); onDismiss() }
                )
                MenuCategoryItem(
                    title = "Lokasi",
                    iconRes = R.drawable.ic_location,
                    onClick = { onItemClick("Lokasi"); onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glass Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Lower Action Items (Sampah & Pengaturan)
            MenuActionRow(
                title = "Sampah",
                iconRes = R.drawable.ic_trash,
                onClick = { onItemClick("Sampah"); onDismiss() }
            )
            MenuActionRow(
                title = "Pengaturan",
                iconRes = R.drawable.ic_setting,
                onClick = { onItemClick("Pengaturan"); onDismiss() }
            )

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun MenuCategoryItem(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = FluxTheme.colors.textPrimary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = FluxTheme.colors.textPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MenuActionRow(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = FluxTheme.colors.textPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = FluxTheme.colors.textPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
