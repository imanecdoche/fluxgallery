package com.flux.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.flux.gallery.data.model.MediaDateGroup
import com.flux.gallery.data.model.MediaItem
import com.flux.gallery.data.repository.MediaRepository
import com.flux.gallery.ui.components.FloatingGlassBottomNavBar
import com.flux.gallery.ui.components.GalleryTab
import com.flux.gallery.ui.screens.PhotoViewerScreen
import com.flux.gallery.ui.screens.RecentScreen
import com.flux.gallery.ui.screens.SplashScreenOverlay
import com.flux.gallery.ui.theme.FluxGalleryTheme
import com.flux.gallery.ui.theme.OneUiBackground
import com.flux.gallery.ui.theme.OneUiTextSecondary
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
                    .background(OneUiBackground)
                    .prismalGlassLayer(backdropLayer)
            ) {
                when (selectedTab) {
                    GalleryTab.GAMBAR -> {
                        RecentScreen(
                            groups = mediaGroups,
                            isLoading = isLoading,
                            onMediaClick = { clickedItem ->
                                val index = flattenedMediaList.indexOfFirst { it.id == clickedItem.id }
                                if (index != -1) {
                                    viewingMediaIndex = index
                                }
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

            // Floating Pill Liquid Glass Navbar (iOS Refractive Glass Style via Prismal AGSL)
            FloatingGlassBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                backdrop = backdropLayer,
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
            color = OneUiTextSecondary,
            fontSize = 16.sp
        )
    }
}
