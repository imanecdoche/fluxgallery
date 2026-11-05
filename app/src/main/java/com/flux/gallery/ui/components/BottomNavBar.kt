package com.flux.gallery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.R
import com.flux.gallery.ui.theme.FluxTheme
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
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
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {},
    onFavorite: () -> Unit = {},
    onMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pillScaleX = remember { Animatable(1f) }
    val pillScaleY = remember { Animatable(1f) }
    var isFirstRender by remember { mutableStateOf(true) }

    LaunchedEffect(isSelectionMode) {
        if (isFirstRender) {
            isFirstRender = false
            return@LaunchedEffect
        }
        // Apple Squash & Stretch: Spring bounce on pill width during mode transition
        pillScaleX.snapTo(0.95f)
        pillScaleY.snapTo(0.97f)
        launch {
            pillScaleX.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.54f, // Organic elastic bounce/overshoot
                    stiffness = 380f
                )
            )
        }
        launch {
            pillScaleY.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 420f
                )
            )
        }
    }

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
        showDroplet = !isSelectionMode,
        enableTabGestures = !isSelectionMode,
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .graphicsLayer {
                scaleX = pillScaleX.value
                scaleY = pillScaleY.value
            }
    ) {
        AnimatedContent(
            targetState = isSelectionMode,
            transitionSpec = {
                (scaleIn(
                    initialScale = 0.82f,
                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f)
                ) + fadeIn(tween(180)))
                .togetherWith(
                    scaleOut(
                        targetScale = 0.82f,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f)
                    ) + fadeOut(tween(140))
                )
            },
            label = "BottomNavMorphTransition",
            modifier = Modifier.fillMaxSize()
        ) { inSelection ->
            if (!inSelection) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLayerActive = com.styropyr0.prismal.components.LocalPrismalTabIsActive.current
                    val currentHighlight = com.styropyr0.prismal.components.LocalPrismalBottomTabHighlightedIndex.current()
                    GalleryTab.entries.forEach { tab ->
                        val isSelected = isLayerActive || tab.ordinal == currentHighlight
                        val itemColor = if (isLayerActive) FluxTheme.colors.activeTeal else FluxTheme.colors.inactive
                        val iconRes = if (isLayerActive) tab.filledIconRes else tab.normalIconRes

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
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isEnabled = selectedCount > 0
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
}
