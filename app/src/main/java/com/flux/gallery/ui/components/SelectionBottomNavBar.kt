package com.flux.gallery.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
fun SelectionBottomNavBar(
    visible: Boolean,
    selectedCount: Int,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit,
    onMore: () -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val isEnabled = selectedCount > 0
    val containerColor = FluxTheme.colors.glassContainer

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(280)
        ) + fadeIn(tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(220)
        ) + fadeOut(tween(180)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxWidth()
                .height(64.dp)
                .drawPrismalGlass(
                    backdrop = backdrop,
                    shape = { PrismalCapsule() },
                    specular = { PrismalSpecular.Default },
                    effects = {
                        applyPrismalGlassEffects(
                            density = density,
                            adaptiveLuminance = false,
                            luminance = 0.5f,
                            blurRadiusPx = with(density) { 12.dp.toPx() },
                            refractionHeightPx = with(density) { 22.dp.toPx() },
                            refractionAmountPx = with(density) { 20.dp.toPx() }
                        )
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .clip(androidx.compose.foundation.shape.CircleShape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectionActionItem(
                    title = "Bagikan",
                    iconRes = R.drawable.ic_share,
                    enabled = isEnabled,
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                )
                SelectionActionItem(
                    title = "Hapus",
                    iconRes = R.drawable.ic_trash,
                    enabled = isEnabled,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
                SelectionActionItem(
                    title = "Favorit",
                    iconRes = R.drawable.ic_favorite,
                    enabled = isEnabled,
                    onClick = onFavorite,
                    modifier = Modifier.weight(1f)
                )
                SelectionActionItem(
                    title = "Lainnya",
                    iconRes = R.drawable.ic_more_horiz,
                    enabled = isEnabled,
                    onClick = onMore,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun SelectionActionItem(
    title: String,
    iconRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = FluxTheme.colors.textPrimary
    val itemAlpha = if (enabled) 1f else 0.35f

    Column(
        modifier = modifier
            .fillMaxHeight()
            .alpha(itemAlpha)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = activeColor,
            modifier = Modifier.size(23.dp)
        )
        Text(
            text = title,
            color = activeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
