package com.example.l13brain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.l13brain.model.CrtProfile

val CrtPhosphorGreen = Color(0xFF00FF66)
val CrtGreenGlow = Color(0x3300FF66)
val CrtGreenDim = Color(0xFF0D5C3A)
val CrtGreenDark = Color(0xFF08261A)

val CrtNeonCyan = Color(0xFF00E5FF)
val CrtCyanGlow = Color(0x3300E5FF)

val CrtAmber = Color(0xFFFFB300)
val CrtAmberGlow = Color(0x33FFB300)

val CrtPink = Color(0xFFFF4081)
val CrtPinkGlow = Color(0x33FF4081)

val CrtBackground = Color(0xFF05080E)
val CrtSurface = Color(0xFF0A0F1A)
val CrtSurfaceBorder = Color(0xFF103328)
val CrtTextMuted = Color(0xFF5B786D)

fun getThemeColors(profile: CrtProfile) = when (profile) {
    CrtProfile.CRT_P31_GREEN -> darkColorScheme(
        primary = CrtPhosphorGreen,
        secondary = CrtNeonCyan,
        tertiary = CrtAmber,
        background = CrtBackground,
        surface = CrtSurface,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = CrtPhosphorGreen,
        onSurface = CrtPhosphorGreen,
        outline = CrtSurfaceBorder
    )
    CrtProfile.AMBER_P4 -> darkColorScheme(
        primary = CrtAmber,
        secondary = CrtPhosphorGreen,
        tertiary = CrtNeonCyan,
        background = Color(0xFF0A0804),
        surface = Color(0xFF140F08),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = CrtAmber,
        onSurface = CrtAmber,
        outline = Color(0xFF38260D)
    )
    CrtProfile.CYBER_CYAN -> darkColorScheme(
        primary = CrtNeonCyan,
        secondary = CrtPink,
        tertiary = CrtPhosphorGreen,
        background = Color(0xFF050B12),
        surface = Color(0xFF0B1624),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = CrtNeonCyan,
        onSurface = CrtNeonCyan,
        outline = Color(0xFF123447)
    )
    CrtProfile.CLEAN_MODE -> darkColorScheme(
        primary = Color(0xFF4ADE80),
        secondary = Color(0xFF38BDF8),
        tertiary = Color(0xFFFBBF24),
        background = Color(0xFF0F172A),
        surface = Color(0xFF1E293B),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
        outline = Color(0xFF334155)
    )
}

val TopTuiTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.3.sp
    )
)

@Composable
fun TopTuiTheme(
    profile: CrtProfile = CrtProfile.CRT_P31_GREEN,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getThemeColors(profile),
        typography = TopTuiTypography,
        content = content
    )
}
