package com.flux.gallery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.R
import com.flux.gallery.ui.theme.FluxTheme
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.specular.PrismalSpecular
import kotlinx.coroutines.launch

enum class GalleryTab(
    val title: String,
    @DrawableRes val normalIconRes: Int,
    @DrawableRes val filledIconRes: Int
) {
    GAMBAR("Gambar", R.drawable.ic_recent, R.drawable.ic_recent_filled),
    ALBUM("Album", R.drawable.ic_album, R.drawable.ic_album_filled),
    CARI("Cari", R.drawable.ic_search, R.drawable.ic_search),
    MENU("Menu", R.drawable.ic_menu, R.drawable.ic_menu)
}

@Composable
fun FloatingGlassBottomNavBar(
    selectedTab: GalleryTab,
    onTabSelected: (GalleryTab) -> Unit,
    backdrop: PrismalBackdrop,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    isDeleteConfirming: Boolean = false,
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissDeleteConfirm: () -> Unit = {},
    onFavorite: () -> Unit = {},
    onMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    AnimatedContent(
        targetState = isSelectionMode,
        transitionSpec = {
            (scaleIn(
                initialScale = 0.88f,
                animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f)
            ) + fadeIn(tween(180)))
            .togetherWith(
                scaleOut(
                    targetScale = 0.88f,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f)
                ) + fadeOut(tween(140))
            )
        },
        label = "BottomNavModeMorph",
        modifier = modifier
    ) { inSelection ->
        if (!inSelection) {
            PrismalGlassBottomTabs(
                selectedTabIndex = { selectedTab.ordinal },
                onTabSelected = { index ->
                    if (index in GalleryTab.entries.indices) {
                        onTabSelected(GalleryTab.entries[index])
                    }
                },
                backdrop = backdrop,
                tabsCount = GalleryTab.entries.size,
                tintDropletContent = false,
                specular = { PrismalSpecular.Default },
                blurRadius = 8.dp,
                refractionHeight = 22.dp,
                refractionAmount = 20.dp,
                dropletBlurRadius = 6.dp,
                dropletChromaticAberration = 0.8f,
                containerColor = FluxTheme.colors.glassContainer,
                showDroplet = true,
                enableTabGestures = true,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLayerActive = com.styropyr0.prismal.components.LocalPrismalTabIsActive.current
                    val currentHighlight = com.styropyr0.prismal.components.LocalPrismalBottomTabHighlightedIndex.current()
                    val inactiveTabColor = if (FluxTheme.colors.isDark) Color.White.copy(alpha = 0.90f) else FluxTheme.colors.inactive
                    GalleryTab.entries.forEach { tab ->
                        val isSelected = isLayerActive || tab.ordinal == currentHighlight
                        val itemColor = if (isLayerActive) FluxTheme.colors.activeTeal else inactiveTabColor
                        val iconRes = tab.normalIconRes

                        PrismalGlassBottomTab(
                            onClick = { onTabSelected(tab) }
                        ) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = tab.title,
                                tint = itemColor,
                                modifier = Modifier.size(23.dp)
                            )
                            Text(
                                text = tab.title,
                                color = itemColor,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        } else {
            // Selection Bar with In-Place Morphing to Confirmation Dialog (Zero Dividers!)
            val containerHeight by animateDpAsState(
                targetValue = if (isDeleteConfirming) 106.dp else 64.dp,
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 380f),
                label = "SelectionBarHeight"
            )
            val cornerRadius by animateDpAsState(
                targetValue = if (isDeleteConfirming) 22.dp else 32.dp,
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 380f),
                label = "SelectionBarCornerRadius"
            )

            val glassContainerColor = FluxTheme.colors.glassContainer

            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .height(containerHeight)
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(cornerRadius) },
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
                        onDrawSurface = { drawRect(glassContainerColor) }
                    )
                    .clip(RoundedCornerShape(cornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isDeleteConfirming,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.92f, animationSpec = spring(0.72f, 400f)) + fadeIn(tween(160)))
                        .togetherWith(scaleOut(targetScale = 0.92f, animationSpec = spring(0.85f, 420f)) + fadeOut(tween(120)))
                    },
                    label = "SelectionPillToConfirmDialog",
                    modifier = Modifier.fillMaxSize()
                ) { confirming ->
                    if (!confirming) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isEnabled = selectedCount > 0
                            // 1. Bagikan
                            SelectionActionItem(
                                title = "Bagikan",
                                iconRes = R.drawable.ic_share,
                                enabled = isEnabled,
                                onClick = onShare,
                                modifier = Modifier.weight(1f)
                            )
                            // 2. Favorit (SWAPPED to position 2!)
                            SelectionActionItem(
                                title = "Favorit",
                                iconRes = R.drawable.ic_favorite,
                                enabled = isEnabled,
                                onClick = onFavorite,
                                modifier = Modifier.weight(1f)
                            )
                            // 3. Hapus (SWAPPED to position 3!)
                            SelectionActionItem(
                                title = "Hapus",
                                iconRes = R.drawable.ic_trash,
                                enabled = isEnabled,
                                onClick = onDelete,
                                modifier = Modifier.weight(1f)
                            )
                            // 4. Lainnya
                            SelectionActionItem(
                                title = "Lainnya",
                                iconRes = R.drawable.ic_more_horiz,
                                enabled = isEnabled,
                                onClick = onMore,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Morphed Confirmation Dialog (Zero Dividers!)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            val titleText = if (selectedCount <= 1) "Hapus foto ini?" else "Hapus $selectedCount foto ini?"
                            Text(
                                text = titleText,
                                color = FluxTheme.colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            // ZERO DIVIDERS - clean pure liquid glass

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(onClick = onDismissDeleteConfirm),
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

                                // ZERO DIVIDERS - no divider between buttons

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(onClick = onConfirmDelete),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Hapus",
                                        color = Color(0xFFFF3B30),
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
        }
    }
}
