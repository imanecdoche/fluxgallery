package com.flux.gallery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
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
        blurRadius = 12.dp,
        refractionHeight = 22.dp,
        refractionAmount = 20.dp,
        dropletChromaticAberration = 0.8f,
        containerColor = FluxTheme.colors.glassContainer,
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        val currentHighlight = com.styropyr0.prismal.components.LocalPrismalBottomTabHighlightedIndex.current()
        GalleryTab.entries.forEach { tab ->
            val isSelected = tab.ordinal == currentHighlight
            val itemColor = if (isSelected) FluxTheme.colors.activeTeal else FluxTheme.colors.inactive
            val iconRes = if (isSelected) tab.filledIconRes else tab.normalIconRes

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
