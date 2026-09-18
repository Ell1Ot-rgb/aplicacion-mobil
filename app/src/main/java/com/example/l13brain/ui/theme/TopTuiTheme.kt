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

// Primary Graphing Calculator Theme Colors
val CrtPhosphorGreen = CalcPlotY3
val CrtGreenGlow = Color(0x3310B981)
val CrtGreenDim = Color(0xFF064E3B)
val CrtGreenDark = Color(0xFF022C22)

val CrtNeonCyan = CalcPlotY1
val CrtCyanGlow = Color(0x3338BDF8)

val CrtAmber = CalcPlotY2
val CrtAmberGlow = Color(0x33F59E0B)

val CrtPink = CalcPlotY4
val CrtPinkGlow = Color(0x33F43F5E)

val CrtBackground = CalcChassis
val CrtSurface = CalcBezel
val CrtSurfaceBorder = CalcBevelBorder
val CrtTextMuted = CalcLcdTextMuted

fun getThemeColors(profile: CrtProfile) = when (profile) {
    CrtProfile.CRT_P31_GREEN, CrtProfile.CLEAN_MODE -> darkColorScheme(
        primary = CalcPlotY1,
        secondary = CalcPlotY2,
        tertiary = CalcPlotY3,
        background = CalcChassis,
        surface = CalcBezel,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = CalcLcdText,
        onSurface = CalcLcdText,
        outline = CalcBevelBorder
    )
    CrtProfile.AMBER_P4 -> darkColorScheme(
        primary = CalcPlotY2,
        secondary = CalcPlotY1,
        tertiary = CalcPlotY3,
        background = Color(0xFF100E08),
        surface = Color(0xFF1A160F),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = CalcPlotY2,
        onSurface = CalcPlotY2,
        outline = Color(0xFF382A16)
    )
    CrtProfile.CYBER_CYAN -> darkColorScheme(
        primary = CalcPlotY1,
        secondary = CalcPlotY4,
        tertiary = CalcPlotY3,
        background = CalcLcdBackground,
        surface = CalcBezel,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = CalcPlotY1,
        onSurface = CalcPlotY1,
        outline = CalcBevelBorder
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
