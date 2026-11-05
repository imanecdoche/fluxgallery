package com.flux.gallery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.R
import com.flux.gallery.ui.theme.OneUiDivider
import com.flux.gallery.ui.theme.OneUiInactive
import com.flux.gallery.ui.theme.OneUiSurface
import com.flux.gallery.ui.theme.OneUiTealActive

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
fun BottomNavBar(
    selectedTab: GalleryTab,
    onTabSelected: (GalleryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OneUiSurface)
            .navigationBarsPadding()
    ) {
        // Hairline Top Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(OneUiDivider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GalleryTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val itemColor = if (isSelected) OneUiTealActive else OneUiInactive
                val iconRes = if (isSelected) tab.filledIconRes else tab.normalIconRes

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = tab.title,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.title,
                        color = itemColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
