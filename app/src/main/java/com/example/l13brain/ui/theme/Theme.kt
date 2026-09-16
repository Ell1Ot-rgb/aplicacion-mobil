package com.example.l13brain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberViolet,
    onPrimary = TextPrimary,
    primaryContainer = CyberVioletDark,
    onPrimaryContainer = TextPrimary,
    secondary = NeonCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = CyberViolet,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFFECE6FF),
    onPrimaryContainer = CyberVioletDark,
    secondary = Color(0xFF0097A7),
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = NeonPink,
    onTertiary = TextPrimary,
    background = Color(0xFFF8F9FD),
    onBackground = Color(0xFF191C24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C24),
    surfaceVariant = Color(0xFFEEF2F8),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFD1D5DB)
)

@Composable
fun L13BrainTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
