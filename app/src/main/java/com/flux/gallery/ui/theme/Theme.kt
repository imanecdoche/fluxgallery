package com.flux.gallery.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme
import io.github.alexzhirkevich.cupertino.theme.darkColorScheme as cupertinoDarkColorScheme
import io.github.alexzhirkevich.cupertino.theme.lightColorScheme as cupertinoLightColorScheme

data class FluxThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val activeTeal: Color,
    val headerText: Color,
    val inactive: Color,
    val glassContainer: Color,
    val glassSurface: Color,
    val glassDivider: Color,
    val dragHandle: Color,
    val isDark: Boolean
)

val LightFluxColors = FluxThemeColors(
    background = LightSystemBackground,
    surface = LightSecondaryBackground,
    surfaceVariant = LightTertiaryBackground,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    divider = LightDivider,
    activeTeal = LightTealActive,
    headerText = LightTealHeader,
    inactive = LightInactive,
    glassContainer = Color(0xFFFAFAFA).copy(alpha = 0.28f),
    glassSurface = Color(0xFFFCFCFD).copy(alpha = 0.68f),
    glassDivider = Color.Black.copy(alpha = 0.08f),
    dragHandle = Color.Black.copy(alpha = 0.18f),
    isDark = false
)

val DarkFluxColors = FluxThemeColors(
    background = AppleDarkBackground,
    surface = AppleDarkSecondaryBackground,
    surfaceVariant = AppleDarkTertiaryBackground,
    textPrimary = AppleDarkTextPrimary,
    textSecondary = AppleDarkTextSecondary,
    divider = AppleDarkDivider,
    activeTeal = AppleDarkTeal,
    headerText = AppleDarkTextPrimary,
    inactive = AppleDarkInactive,
    glassContainer = AppleDarkSecondaryBackground.copy(alpha = 0.38f),
    glassSurface = AppleDarkSecondaryBackground.copy(alpha = 0.75f),
    glassDivider = AppleDarkDivider.copy(alpha = 0.70f),
    dragHandle = Color.White.copy(alpha = 0.28f),
    isDark = true
)

val LocalFluxColors = staticCompositionLocalOf { LightFluxColors }

object FluxTheme {
    val colors: FluxThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFluxColors.current
}

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = LightTealActive,
    onPrimary = Color.White,
    background = LightSystemBackground,
    onBackground = LightTextPrimary,
    surface = LightSecondaryBackground,
    onSurface = LightTextPrimary,
    surfaceVariant = LightTertiaryBackground,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = AppleDarkTeal,
    onPrimary = Color.Black,
    background = AppleDarkBackground,
    onBackground = AppleDarkTextPrimary,
    surface = AppleDarkSecondaryBackground,
    onSurface = AppleDarkTextPrimary,
    surfaceVariant = AppleDarkTertiaryBackground,
    onSurfaceVariant = AppleDarkTextSecondary,
    outline = AppleDarkDivider
)

@Composable
fun FluxGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val fluxColors = if (darkTheme) DarkFluxColors else LightFluxColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            window.statusBarColor = fluxColors.background.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val cupertinoColors = if (darkTheme) {
        cupertinoDarkColorScheme(accent = AppleDarkTeal)
    } else {
        cupertinoLightColorScheme(accent = LightTealActive)
    }

    CupertinoTheme(colorScheme = cupertinoColors) {
        CompositionLocalProvider(LocalFluxColors provides fluxColors) {
            MaterialTheme(
                colorScheme = colorScheme,
                content = content
            )
        }
    }
}
