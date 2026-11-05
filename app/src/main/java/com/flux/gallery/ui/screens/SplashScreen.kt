package com.flux.gallery.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.flux.gallery.R
import com.flux.gallery.ui.theme.OneUiBackground
import com.flux.gallery.ui.theme.OneUiSurface
import com.flux.gallery.ui.theme.SplashBlue

@Composable
fun SplashScreenOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)

            if (isVisible) {
                // Immersive Unibody Splash Theme
                window.statusBarColor = SplashBlue.toArgb()
                window.navigationBarColor = SplashBlue.toArgb()
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            } else {
                // Restored Gallery Theme
                window.statusBarColor = OneUiBackground.toArgb()
                window.navigationBarColor = OneUiSurface.toArgb()
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(400)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SplashBlue),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_flower),
                contentDescription = "Logo",
                modifier = Modifier.size(84.dp)
            )
        }
    }
}
