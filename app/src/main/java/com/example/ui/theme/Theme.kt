package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonarchColorScheme = darkColorScheme(
    primary = SystemNeonCyan,
    onPrimary = Color.Black,
    primaryContainer = SystemShadowPurple,
    onPrimaryContainer = Color.White,
    secondary = SystemVividViolet,
    onSecondary = Color.White,
    tertiary = SystemMonarchGold,
    background = SystemDeepBg,
    onBackground = SystemTextLight,
    surface = SystemObsidian,
    onSurface = SystemTextLight,
    surfaceVariant = Color(0xFF1E1E24),
    onSurfaceVariant = SystemTextMuted,
    error = SystemRedDread,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Monarch feeling
    dynamicColor: Boolean = false, // Disable dynamic colors to keep game's hand-crafted purple/blue identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonarchColorScheme,
        typography = Typography,
        content = content
    )
}
