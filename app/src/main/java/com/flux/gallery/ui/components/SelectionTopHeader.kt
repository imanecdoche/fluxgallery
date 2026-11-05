package com.flux.gallery.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.ui.theme.FluxTheme

@Composable
fun SelectionTopHeader(
    visible: Boolean,
    selectedCount: Int,
    isAllSelected: Boolean,
    onSelectAllToggle: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(240)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200)
        ) + fadeOut(tween(160)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Cancel Button
            Text(
                text = "Batal",
                color = FluxTheme.colors.activeTeal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onCancel)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )

            // Center: Selected Count Typography
            Text(
                text = if (selectedCount == 0) "Pilih item" else "$selectedCount Dipilih",
                color = FluxTheme.colors.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            // Right: Select All / Deselect All Toggle
            Text(
                text = if (isAllSelected) "Batal pilih" else "Pilih semua",
                color = FluxTheme.colors.activeTeal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onSelectAllToggle)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )
        }
    }
}
