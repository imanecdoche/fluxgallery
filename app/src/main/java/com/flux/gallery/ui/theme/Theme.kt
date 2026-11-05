package com.flux.gallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = OneUiTealActive,
    onPrimary = OneUiSurface,
    background = OneUiBackground,
    onBackground = OneUiTextPrimary,
    surface = OneUiSurface,
    onSurface = OneUiTextPrimary,
    surfaceVariant = OneUiBackground,
    onSurfaceVariant = OneUiTextSecondary,
    outline = OneUiDivider
)

@Composable
fun FluxGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
