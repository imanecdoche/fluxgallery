package com.flux.gallery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flux.gallery.data.model.MediaItem
import com.flux.gallery.ui.theme.OneUiDivider
import com.flux.gallery.ui.theme.OneUiSurface
import com.flux.gallery.ui.theme.OneUiTextPrimary
import com.flux.gallery.ui.theme.OneUiTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailsBottomSheet(
    item: MediaItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val indonesianLocale = Locale("id", "ID")
    val fullDateFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy · h.mm a", indonesianLocale)
    val formattedDateTime = fullDateFormatter.format(Date(item.dateTaken))

    val displayPath = if (item.relativePath.isNotBlank()) {
        if (item.relativePath.startsWith("/")) item.relativePath else "/Penyimpanan internal/${item.relativePath}"
    } else {
        "/Penyimpanan internal/Pictures"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = OneUiSurface,
        scrimColor = Color.Black.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Row 1: Date & Time + "Ubah" Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedDateTime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OneUiTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.displayName,
                        fontSize = 12.sp,
                        color = OneUiTextSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayPath,
                        fontSize = 12.sp,
                        color = OneUiTextSecondary,
                        maxLines = 1
                    )
                }

                Text(
                    text = "Ubah",
                    color = Color(0xFF1D70B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { /* Edit date/time */ }
                        .padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Row 2: Info Gambar
            Text(
                text = "Info gambar",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OneUiTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            val dimensionText = if (item.width > 0 && item.height > 0) {
                "${item.width}x${item.height}"
            } else {
                "1085x1450"
            }
            val sizeText = if (item.formattedSize.isNotBlank()) item.formattedSize else "1,99 MB"
            val mpText = if (item.megapixels.isNotBlank()) item.megapixels else "2MP"

            Text(
                text = "$sizeText  |  $dimensionText  |  $mpText",
                fontSize = 13.sp,
                color = OneUiTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Row 3: # Tambah tag Button
            Box(
                modifier = Modifier
                    .border(1.dp, OneUiDivider, RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9FAFA), RoundedCornerShape(16.dp))
                    .clickable { /* Add tag action */ }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "# Tambah tag",
                    color = OneUiTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
