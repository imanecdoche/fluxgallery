package com.flux.gallery.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flux.gallery.R
import com.flux.gallery.data.model.MediaItem
import com.flux.gallery.ui.theme.FluxTheme

@Composable
fun PhotoViewerScreen(
    mediaList: List<MediaItem>,
    initialIndex: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (mediaList.size - 1).coerceAtLeast(0)),
        pageCount = { mediaList.size }
    )

    var isUiVisible by remember { mutableStateOf(true) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    val currentItem = mediaList.getOrNull(pagerState.currentPage)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Horizontal Pager for Fullscreen Media
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = mediaList[page]
            ZoomableMediaImage(
                item = item,
                onSingleTap = {
                    isUiVisible = !isUiVisible
                    showContextMenu = false
                }
            )
        }

        // Top Action Bar
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = {
                        Toast.makeText(context, "Menganalisis detail foto...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rotate),
                        contentDescription = "Remaster / Info",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Bottom Action Bar (5 Naked Icons)
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Favorit
                    IconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            Toast.makeText(
                                context,
                                if (isFavorite) "Ditambahkan ke Favorit" else "Dihapus dari Favorit",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        val favIconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
                        Icon(
                            painter = painterResource(id = favIconRes),
                            contentDescription = "Favorit",
                            tint = if (isFavorite) Color(0xFFE53935) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 2. Edit
                    IconButton(
                        onClick = {
                            currentItem?.let { item ->
                                val editIntent = Intent(Intent.ACTION_EDIT).apply {
                                    setDataAndType(item.uri, item.mimeType)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                try {
                                    context.startActivity(Intent.createChooser(editIntent, "Edit foto"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Tidak ada aplikasi editor", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 3. Bagikan / Share
                    IconButton(
                        onClick = {
                            currentItem?.let { item ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = item.mimeType
                                    putExtra(Intent.EXTRA_STREAM, item.uri)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan ke"))
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Bagikan",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 4. Hapus / Delete
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Pindahkan ke Sampah", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash),
                            contentDescription = "Hapus",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 5. Opsi lainnya (Titik Tiga Vertikal)
                    Box {
                        IconButton(onClick = { showContextMenu = !showContextMenu }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_more_vert),
                                contentDescription = "Lainnya",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Context Menu Popup (Exact One UI layout from Screenshot 8)
                        DropdownMenu(
                            expanded = showContextMenu,
                            onDismissRequest = { showContextMenu = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = FluxTheme.colors.surface,
                            modifier = Modifier.width(220.dp)
                        ) {
                            // Group 1: Rincian
                            DropdownMenuItem(
                                text = { Text("Rincian", color = FluxTheme.colors.textPrimary, fontSize = 15.sp) },
                                onClick = {
                                    showContextMenu = false
                                    showDetailsSheet = true
                                }
                            )

                            HorizontalDivider(
                                color = FluxTheme.colors.divider,
                                thickness = 0.6.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Group 2: Clipboard
                            DropdownMenuItem(
                                text = { Text("Salin ke clipboard", color = FluxTheme.colors.textPrimary, fontSize = 15.sp) },
                                onClick = {
                                    showContextMenu = false
                                    currentItem?.let { item ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newUri(context.contentResolver, "URI", item.uri)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Tempel dari clipboard", color = FluxTheme.colors.textPrimary, fontSize = 15.sp) },
                                onClick = {
                                    showContextMenu = false
                                    Toast.makeText(context, "Tidak ada data clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )

                            HorizontalDivider(
                                color = FluxTheme.colors.divider,
                                thickness = 0.6.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Group 3: Wallpaper & Cetak
                            DropdownMenuItem(
                                text = { Text("Atur sebagai wallpaper", color = FluxTheme.colors.textPrimary, fontSize = 15.sp) },
                                onClick = {
                                    showContextMenu = false
                                    currentItem?.let { item ->
                                        val wallpaperIntent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                                            setDataAndType(item.uri, item.mimeType)
                                            putExtra("mimeType", item.mimeType)
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        try {
                                            context.startActivity(Intent.createChooser(wallpaperIntent, "Atur sebagai"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal membuka pengatur wallpaper", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cetak", color = FluxTheme.colors.textPrimary, fontSize = 15.sp) },
                                onClick = {
                                    showContextMenu = false
                                    Toast.makeText(context, "Mempersiapkan dokumen cetak...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Details View Bottom Sheet
        if (showDetailsSheet && currentItem != null) {
            PhotoDetailsBottomSheet(
                item = currentItem,
                onDismiss = { showDetailsSheet = false }
            )
        }
    }
}

@Composable
private fun ZoomableMediaImage(
    item: MediaItem,
    onSingleTap: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}
