package com.flux.gallery.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Light Mode Tokens (One UI & Apple Light)
// ==========================================
val LightSystemBackground = Color(0xFFF8FAFB)
val LightSecondaryBackground = Color(0xFFFFFFFF)
val LightTertiaryBackground = Color(0xFFE8ECEE)
val LightTextPrimary = Color(0xFF1A2024)
val LightTextSecondary = Color(0xFF7B8C95)
val LightDivider = Color(0xFFE8ECEE)
val LightTealActive = Color(0xFF0B5C68)
val LightTealHeader = Color(0xFF0B3B46)
val LightInactive = Color(0xFF7B8C95)

// ==========================================
// Dark Mode Tokens (Official Apple HIG Dark)
// ==========================================
// System Background: Pure Black #000000 (OLED battery efficiency & maximum photo contrast)
val AppleDarkBackground = Color(0xFF000000)
// Secondary System Background: #1C1C1E (Floating cards, bottom sheets, elevated containers)
val AppleDarkSecondaryBackground = Color(0xFF1C1C1E)
// Tertiary System Background: #2C2C2E (Nested cards & insets)
val AppleDarkTertiaryBackground = Color(0xFF2C2C2E)
// Primary Text / Label: #FFFFFF (100% white)
val AppleDarkTextPrimary = Color(0xFFFFFFFF)
// Secondary Text / Label: #8E8E93 (Apple HIG standard secondary label)
val AppleDarkTextSecondary = Color(0xFF8E8E93)
// Separator / Divider: #38383A (Apple HIG dark separator)
val AppleDarkDivider = Color(0xFF38383A)
// System Accent Teal: Vibrant Cyan-Teal for maximum contrast on glass
val AppleDarkTeal = Color(0xFF00C7BE)
// Inactive Tab / Icon: #8E8E93
val AppleDarkInactive = Color(0xFF8E8E93)

// ==========================================
// Brand / Splash
// ==========================================
val SplashBlue = Color(0xFF143C70)

// Backward-compatibility aliases
val OneUiTealHeader = LightTealHeader
val OneUiTealActive = LightTealActive
val OneUiInactive = LightInactive
val OneUiBackground = LightSystemBackground
val OneUiSurface = LightSecondaryBackground
val OneUiTextPrimary = LightTextPrimary
val OneUiTextSecondary = LightTextSecondary
val OneUiDivider = LightDivider
