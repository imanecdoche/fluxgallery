package com.flux.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import android.content.Context
import android.content.Intent
import com.flux.gallery.data.model.MediaDateGroup
import com.flux.gallery.data.model.MediaItem
import com.flux.gallery.data.repository.MediaRepository
import com.flux.gallery.ui.components.FloatingGlassBottomNavBar
import com.flux.gallery.ui.components.FloatingGlassMenuSheet
import com.flux.gallery.ui.components.GalleryTab
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

    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedItemIds = emptySet()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewingMediaIndex != null) {
            BackHandler {
                viewingMediaIndex = null
            }
            PhotoViewerScreen(
                mediaList = flattenedMediaList,
                initialIndex = viewingMediaIndex ?: 0,
                onBack = { viewingMediaIndex = null }
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
                            onMenuClick = { /* Three-dot options menu */ }
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
                onShare = {
                    val selectedItems = flattenedMediaList.filter { it.id in selectedItemIds }
                    if (selectedItems.isNotEmpty()) {
                        val uris = ArrayList(selectedItems.map { it.uri })
                        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "image/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan ${selectedItems.size} foto"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membagikan foto", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDelete = {
                    Toast.makeText(context, "Pindahkan ${selectedItemIds.size} item ke Sampah", Toast.LENGTH_SHORT).show()
                    isSelectionMode = false
                    selectedItemIds = emptySet()
                },
                onFavorite = {
                    Toast.makeText(context, "${selectedItemIds.size} item ditambahkan ke Favorit", Toast.LENGTH_SHORT).show()
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
