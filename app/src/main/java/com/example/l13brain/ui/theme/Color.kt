package com.example.l13brain.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// HIGH-CONTRAST MONOCHROME GRAPHING CALCULATOR (BLANCO Y NEGRO)
// ==========================================

// Casing & Chassis (True Pitch Black & Crisp Metallic Contours)
val CalcChassis = Color(0xFF000000)          // Pure pitch black body
val CalcBezel = Color(0xFF0D0D0D)            // Precision deep black bezel
val CalcBevelBorder = Color(0xFFFFFFFF)      // High-contrast pure white border
val CalcBorderSubtle = Color(0xFF3F3F46)     // Crisp visible zinc outline

// LCD Display Screen (High-Contrast Monochrome LCD Display)
val CalcLcdBackground = Color(0xFF000000)    // Pure black LCD background
val CalcLcdGrid = Color(0xFF27272A)          // Sharp coordinate grid
val CalcLcdAxis = Color(0xFFD4D4D8)          // Crisp white/light-gray axes
val CalcLcdText = Color(0xFFFFFFFF)          // Pure brilliant white readout text
val CalcLcdTextMuted = Color(0xFFA1A1AA)     // High-readability silver readout
val CalcLcdGlow = Color(0x22FFFFFF)          // Subtle backlight

// Mathematical Function Traces (Crisp High-Contrast White Traces)
val CalcPlotY1 = Color(0xFFFFFFFF)           // Y1: Pure Stark White
val CalcPlotY2 = Color(0xFFE4E4E7)           // Y2: Bright Chalk Silver
val CalcPlotY3 = Color(0xFFD4D4D8)           // Y3: Light Silver
val CalcPlotY4 = Color(0xFFFFFFFF)           // Y4: Solid White
val CalcPlotY5 = Color(0xFFA1A1AA)           // Y5: Cool Slate
val CalcPlotY6 = Color(0xFF71717A)           // Y6: Medium Gray

// Graphing Calculator Keypad Categories (Black & White High-Contrast)
val CalcKeyNumBg = Color(0xFF18181B)         // Deep keycap
val CalcKeyNumText = Color(0xFFFFFFFF)       // Crisp white numerals
val CalcKeyNumBorder = Color(0xFF71717A)

val CalcKeyOpBg = Color(0xFF27272A)          // Operator keycap
val CalcKeyOpText = Color(0xFFFFFFFF)        // Stark white text
val CalcKeyOpBorder = Color(0xFFA1A1AA)

val CalcKeyVarBg = Color(0xFF18181B)         // Variables keycap
val CalcKeyVarText = Color(0xFFFFFFFF)
val CalcKeyVarBorder = Color(0xFF71717A)

val CalcKeyEnterBg = Color(0xFFFFFFFF)       // [ENTER] / [EXE] - INVERTED PURE WHITE BUTTON
val CalcKeyEnterText = Color(0xFF000000)     // Bold black text (Maximum contrast)
val CalcKeyEnterBorder = Color(0xFFFFFFFF)

val CalcKeyClearBg = Color(0xFF27272A)       // [DEL] / [CLEAR]
val CalcKeyClearText = Color(0xFFFFFFFF)
val CalcKeyClearBorder = Color(0xFF71717A)

val CalcKey2ndBg = Color(0xFFFFFFFF)         // [2nd] Inverted White Keycap
val CalcKey2ndText = Color(0xFF000000)       // Pure Black Text
val CalcKey2ndBorder = Color(0xFFFFFFFF)

val CalcKeyAlphaBg = Color(0xFF27272A)       // [ALPHA] Key
val CalcKeyAlphaText = Color(0xFFFFFFFF)     // Bright White
val CalcKeyAlphaBorder = Color(0xFFD4D4D8)

// Softkey Top Row (F1-F5)
val CalcSoftkeyBg = Color(0xFF18181B)        // Unselected keycap
val CalcSoftkeyActiveBg = Color(0xFFFFFFFF)  // Selected: Inverted pure white!
val CalcSoftkeyActiveText = Color(0xFF000000)// Selected: Pure black text!
val CalcSoftkeyBorder = Color(0xFF52525B)
val CalcSoftkeyActiveBorder = Color(0xFFFFFFFF)

// Legacy alias compatibility
val CyberViolet = CalcPlotY5
val CyberVioletDark = Color(0xFF3F3F46)
val NeonCyan = CalcPlotY1
val NeonPink = Color(0xFFFFFFFF)
val EmeraldGreen = Color(0xFFE4E4E7)
val AmberWarn = Color(0xFFD4D4D8)

val DarkBackground = CalcChassis
val DarkSurface = CalcBezel
val DarkSurfaceVariant = Color(0xFF18181B)
val DarkSurfaceElevated = Color(0xFF27272A)

val TextPrimary = CalcLcdText
val TextSecondary = CalcLcdTextMuted
val TextMuted = Color(0xFF71717A)

val BorderSubtle = CalcBorderSubtle
val GlowCyan = Color(0x33FFFFFF)
val GlowViolet = Color(0x33FFFFFF)
val GlowPink = Color(0x33FFFFFF)
