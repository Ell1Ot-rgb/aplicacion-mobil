package com.ell1ot.l13monitor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = L13Primary,
    secondary = L13Secondary,
    background = L13Background,
    surface = L13Surface,
    onSurface = L13OnSurface,
    error = L13Error,
    outline = L13Bevel,
)

@Composable
fun L13Theme(content: @Composable () -> Unit) {
    // Dark-first per spec
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else DarkScheme,
        typography = L13Typography,
        content = content,
    )
}
