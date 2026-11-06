package com.flux.gallery

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.flux.gallery.data.model.MediaDateGroup
import com.flux.gallery.data.model.MediaItem
import com.flux.gallery.data.repository.FavoritesManager
import com.flux.gallery.data.repository.MediaRepository
import com.flux.gallery.ui.components.FloatingGlassBottomNavBar
import com.flux.gallery.ui.components.FloatingGlassMenuSheet
import com.flux.gallery.ui.components.GalleryTab
import com.flux.gallery.ui.components.GlassDeleteConfirmationSheet
import com.flux.gallery.ui.components.RecentTopHeader
import com.flux.gallery.ui.components.SelectionBottomNavBar
import com.flux.gallery.ui.components.SelectionMoreGlassSheet
import com.flux.gallery.ui.screens.PhotoViewerScreen
import com.flux.gallery.ui.screens.RecentScreen
import com.flux.gallery.ui.screens.SplashScreenOverlay
import com.flux.gallery.ui.theme.FluxGalleryTheme
import com.flux.gallery.ui.theme.FluxTheme
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FluxGalleryTheme {
                MainGalleryScreen()
            }
        }
    }
}

@Composable
fun MainGalleryScreen() {
    val context = LocalContext.current
    val repository = remember { MediaRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val backdropLayer = rememberPrismalGlassLayer()

    var selectedTab by remember { mutableStateOf(GalleryTab.GAMBAR) }
    var isMenuSheetOpen by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isSelectionMoreSheetOpen by remember { mutableStateOf(false) }
    var mediaGroups by remember { mutableStateOf<List<MediaDateGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSplashVisible by remember { mutableStateOf(true) }
    var viewingMediaIndex by remember { mutableStateOf<Int?>(null) }

    val favoritesManager = remember { FavoritesManager(context) }
    var favoriteIds by remember { mutableStateOf(favoritesManager.getFavoriteIds()) }

    var isDeleteConfirmationOpen by remember { mutableStateOf(false) }
    var isDeleteConfirming by remember { mutableStateOf(false) }
    var pendingDeleteItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val deletedIds = pendingDeleteItems.map { it.id }.toSet()
            if (deletedIds.isNotEmpty()) {
                mediaGroups = mediaGroups.map { group ->
                    group.copy(items = group.items.filter { it.id !in deletedIds })
                }.filter { it.items.isNotEmpty() }

                selectedItemIds = selectedItemIds - deletedIds
                if (selectedItemIds.isEmpty()) {
                    isSelectionMode = false
                }
                Toast.makeText(context, "${deletedIds.size} foto berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Penghapusan dibatalkan", Toast.LENGTH_SHORT).show()
        }
        pendingDeleteItems = emptyList()
    }

    fun executeDelete(items: List<MediaItem>) {
        if (items.isEmpty()) return
        val uris = items.map { it.uri }
        val ids = items.map { it.id }.toSet()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal membuat permintaan hapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            var count = 0
            uris.forEach { uri ->
                try {
                    count += context.contentResolver.delete(uri, null, null)
                } catch (e: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                        deleteLauncher.launch(IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build())
                        return
                    }
                }
            }
            if (count > 0) {
                mediaGroups = mediaGroups.map { group ->
                    group.copy(items = group.items.filter { it.id !in ids })
                }.filter { it.items.isNotEmpty() }
                selectedItemIds = selectedItemIds - ids
                if (selectedItemIds.isEmpty()) isSelectionMode = false
                Toast.makeText(context, "$count foto berhasil dihapus", Toast.LENGTH_SHORT).show()
                pendingDeleteItems = emptyList()
            }
        }
    }

    val galleryPrefs = remember { context.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE) }
    var isBlurThumbnails by remember {
        mutableStateOf(galleryPrefs.getBoolean("blur_thumbnails", false))
    }

    val splashStartTime = remember { System.currentTimeMillis() }

    val flattenedMediaList = remember(mediaGroups) {
        mediaGroups.flatMap { it.items }
    }

    fun loadMedia() {
        coroutineScope.launch {
            isLoading = true
            mediaGroups = repository.loadRecentMedia()
            isLoading = false
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            val elapsed = System.currentTimeMillis() - splashStartTime
            val remaining = (1200L - elapsed).coerceAtLeast(0L)
            delay(remaining)
            isSplashVisible = false
        }
    }

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var hasPermission by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        hasPermission = granted
        if (granted) {
            loadMedia()
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            loadMedia()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    BackHandler(enabled = isMenuSheetOpen) {
        isMenuSheetOpen = false
    }

    BackHandler(enabled = isDeleteConfirming) {
        isDeleteConfirming = false
    }

    BackHandler(enabled = isSelectionMode && !isDeleteConfirming) {
        isSelectionMode = false
        selectedItemIds = emptySet()
        isDeleteConfirming = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isDeleteConfirming) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { isDeleteConfirming = false }
                    )
            )
        }

        if (viewingMediaIndex != null) {
            BackHandler {
                viewingMediaIndex = null
            }
            PhotoViewerScreen(
                mediaList = flattenedMediaList,
                initialIndex = viewingMediaIndex ?: 0,
                favoriteIds = favoriteIds,
                onToggleFavorite = { id ->
                    val nowFav = favoritesManager.toggleFavorite(id)
                    favoriteIds = favoritesManager.getFavoriteIds()
                    Toast.makeText(
                        context,
                        if (nowFav) "Ditambahkan ke Favorit" else "Dihapus dari Favorit",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDeleteRequest = { item ->
                    pendingDeleteItems = listOf(item)
                    isDeleteConfirmationOpen = true
                },
                onBack = { viewingMediaIndex = null },
                modifier = Modifier.prismalGlassLayer(backdropLayer)
            )
        } else {
            // Live Content Layer that is refracted and sampled by the floating glass pill
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FluxTheme.colors.background)
                    .prismalGlassLayer(backdropLayer)
            ) {
                when (selectedTab) {
                    GalleryTab.GAMBAR -> {
                        RecentScreen(
                            groups = mediaGroups,
                            isLoading = isLoading,
                            isSelectionMode = isSelectionMode,
                            selectedItemIds = selectedItemIds,
                            onMediaClick = { clickedItem ->
                                val index = flattenedMediaList.indexOfFirst { it.id == clickedItem.id }
                                if (index != -1) {
                                    viewingMediaIndex = index
                                }
                            },
                            onEnterSelectionMode = { item ->
                                isMenuSheetOpen = false
                                isSelectionMode = true
                                selectedItemIds = setOf(item.id)
                            },
                            onUpdateSelectionRange = { rangeIds ->
                                selectedItemIds = rangeIds
                            },
                            onToggleSelection = { item ->
                                selectedItemIds = if (selectedItemIds.contains(item.id)) {
                                    selectedItemIds - item.id
                                } else {
                                    selectedItemIds + item.id
                                }
                            },
                            onSelectAll = {
                                selectedItemIds = flattenedMediaList.map { it.id }.toSet()
                            },
                            onClearSelection = {
                                selectedItemIds = emptySet()
                            },
                            onExitSelectionMode = {
                                isSelectionMode = false
                                selectedItemIds = emptySet()
                            },
                            onMenuClick = { /* Three-dot options menu */ },
                            isBlurThumbnails = isBlurThumbnails,
                            backdrop = backdropLayer
                        )
                    }
                    GalleryTab.ALBUM -> {
                        PlaceholderTabContent(title = "Album")
                    }
                    GalleryTab.CARI -> {
                        PlaceholderTabContent(title = "Cari")
                    }
                    GalleryTab.MENU -> {
                        PlaceholderTabContent(title = "Menu")
                    }
                }
            }

            // Floating Liquid Glass Top Header (Locked 52dp DOM height with zero layout snapping)
            if (selectedTab == GalleryTab.GAMBAR) {
                val allMediaIds = remember(mediaGroups) { mediaGroups.flatMap { it.items }.map { it.id }.toSet() }
                val isAllSelected = allMediaIds.isNotEmpty() && selectedItemIds.size == allMediaIds.size
                RecentTopHeader(
                    isSelectionMode = isSelectionMode,
                    selectedCount = selectedItemIds.size,
                    isAllSelected = isAllSelected,
                    onSelectAllToggle = {
                        if (isAllSelected) {
                            selectedItemIds = emptySet()
                        } else {
                            selectedItemIds = flattenedMediaList.map { it.id }.toSet()
                        }
                    },
                    onCancel = {
                        isSelectionMode = false
                        selectedItemIds = emptySet()
                    },
                    onEnterSelectionMode = {
                        isSelectionMode = true
                    },
                    onCreateClick = {
                        Toast.makeText(context, "Buat", Toast.LENGTH_SHORT).show()
                    },
                    onStartSlideshow = {
                        Toast.makeText(context, "Mulai Slideshow", Toast.LENGTH_SHORT).show()
                    },
                    isBlurThumbnails = isBlurThumbnails,
                    onBlurThumbnailsChange = { isBlurred ->
                        isBlurThumbnails = isBlurred
                        galleryPrefs.edit().putBoolean("blur_thumbnails", isBlurred).apply()
                    },
                    backdrop = backdropLayer,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                )
            }

            // Floating Glass Menu Hamburger Sheet (One UI Bottom Sheet Hub with Prismal Liquid Glass)
            FloatingGlassMenuSheet(
                visible = isMenuSheetOpen,
                onDismiss = { isMenuSheetOpen = false },
                backdrop = backdropLayer,
                onItemClick = { itemTitle ->
                    Toast.makeText(context, itemTitle, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Floating Pill Liquid Glass Navbar with Seamless Morphing Single-Pill Pop Transition
            FloatingGlassBottomNavBar(
                selectedTab = if (isMenuSheetOpen) GalleryTab.MENU else selectedTab,
                onTabSelected = { newTab ->
                    if (newTab == GalleryTab.MENU) {
                        isMenuSheetOpen = !isMenuSheetOpen
                    } else {
                        isMenuSheetOpen = false
                        selectedTab = newTab
                    }
                },
                isSelectionMode = isSelectionMode,
                selectedCount = selectedItemIds.size,
                isDeleteConfirming = isDeleteConfirming,
                onShare = {
                    val selectedItems = flattenedMediaList.filter { it.id in selectedItemIds }
                    if (selectedItems.isNotEmpty()) {
                        if (selectedItems.size == 1) {
                            val item = selectedItems.first()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = item.mimeType
                                putExtra(Intent.EXTRA_STREAM, item.uri)
                                clipData = ClipData.newUri(context.contentResolver, item.displayName, item.uri)
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            try {
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan ke").apply {
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                })
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membagikan foto", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val uris = ArrayList(selectedItems.map { it.uri })
                            val isAllImages = selectedItems.all { !it.isVideo }
                            val isAllVideos = selectedItems.all { it.isVideo }
                            val mimeType = when {
                                isAllImages -> "image/*"
                                isAllVideos -> "video/*"
                                else -> "*/*"
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = mimeType
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                val clip = ClipData.newUri(context.contentResolver, "media", uris.first())
                                for (i in 1 until uris.size) {
                                    clip.addItem(ClipData.Item(uris[i]))
                                }
                                clipData = clip
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            try {
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan ${selectedItems.size} item ke").apply {
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                })
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membagikan item", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onDelete = {
                    val itemsToDelete = flattenedMediaList.filter { it.id in selectedItemIds }
                    if (itemsToDelete.isNotEmpty()) {
                        pendingDeleteItems = itemsToDelete
                        isDeleteConfirming = true
                    }
                },
                onConfirmDelete = {
                    isDeleteConfirming = false
                    executeDelete(pendingDeleteItems)
                },
                onDismissDeleteConfirm = {
                    isDeleteConfirming = false
                },
                onFavorite = {
                    val isAdded = favoritesManager.toggleFavorites(selectedItemIds)
                    favoriteIds = favoritesManager.getFavoriteIds()
                    Toast.makeText(
                        context,
                        if (isAdded) "${selectedItemIds.size} item ditambahkan ke Favorit" else "${selectedItemIds.size} item dihapus dari Favorit",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onMore = {
                    isSelectionMoreSheetOpen = true
                },
                backdrop = backdropLayer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Floating Liquid Glass Sheet for More Options (Dual-Card Text-Only Design)
            SelectionMoreGlassSheet(
                visible = isSelectionMoreSheetOpen,
                onDismiss = { isSelectionMoreSheetOpen = false },
                selectedCount = selectedItemIds.size,
                backdrop = backdropLayer,
                modifier = Modifier.align(Alignment.BottomCenter),
                onActionSelected = { actionTitle ->
                    when (actionTitle) {
                        "Tambahkan ke favorit" -> {
                            val isAdded = favoritesManager.toggleFavorites(selectedItemIds)
                            favoriteIds = favoritesManager.getFavoriteIds()
                            Toast.makeText(
                                context,
                                if (isAdded) "${selectedItemIds.size} item ditambahkan ke Favorit" else "${selectedItemIds.size} item dihapus dari Favorit",
                                Toast.LENGTH_SHORT
                            ).show()
                            isSelectionMode = false
                            selectedItemIds = emptySet()
                        }
                        "Salin ke clipboard" -> {
                            val firstItem = flattenedMediaList.firstOrNull { it.id in selectedItemIds }
                            if (firstItem != null) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newUri(context.contentResolver, "URI", firstItem.uri)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }
                        "Atur sebagai wallpaper" -> {
                            val firstItem = flattenedMediaList.firstOrNull { it.id in selectedItemIds }
                            if (firstItem != null) {
                                val wallpaperIntent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                                    setDataAndType(firstItem.uri, firstItem.mimeType)
                                    putExtra("mimeType", firstItem.mimeType)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                try {
                                    context.startActivity(Intent.createChooser(wallpaperIntent, "Atur sebagai"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membuka pengatur wallpaper", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(context, "$actionTitle untuk ${selectedItemIds.size} item", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // iOS Liquid Glass Delete Confirmation Action Sheet (For Fullscreen Photo Viewer)
        if (viewingMediaIndex != null) {
            GlassDeleteConfirmationSheet(
                visible = isDeleteConfirmationOpen,
                itemCount = pendingDeleteItems.size,
                backdrop = backdropLayer,
                onConfirmDelete = {
                    executeDelete(pendingDeleteItems)
                },
                onDismiss = {
                    isDeleteConfirmationOpen = false
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Custom Splash Screen Overlay (Deep Blue + Pure White Flower Glyph)
        SplashScreenOverlay(isVisible = isSplashVisible)
    }
}

@Composable
private fun PlaceholderTabContent(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = FluxTheme.colors.textSecondary,
            fontSize = 16.sp
        )
    }
}
