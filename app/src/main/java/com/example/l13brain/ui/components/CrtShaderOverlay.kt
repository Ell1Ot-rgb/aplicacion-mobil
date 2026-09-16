package com.example.l13brain.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.l13brain.model.CrtProfile
import com.example.l13brain.model.ShaderConfig

@Composable
fun CrtShaderOverlay(
    config: ShaderConfig,
    profile: CrtProfile,
    modifier: Modifier = Modifier
) {
    if (!config.enabled || profile == CrtProfile.CLEAN_MODE) return

    val infiniteTransition = rememberInfiniteTransition(label = "crt_animation")
    val scanlineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_travel"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Subtle Scanline lines (Very light, non-destructive)
        val step = 6f
        var y = 0f
        val scanlineAlpha = (0.07f * config.scanlineDensity).coerceIn(0.01f, 0.15f)
        val scanlineColor = Color.Black.copy(alpha = scanlineAlpha)

        while (y < height) {
            drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += step
        }

        // 2. Very subtle high-voltage beam sweep
        val beamY = scanlineOffset * height
        val beamBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x08FFFFFF),
                Color.Transparent
            ),
            startY = beamY - 30f,
            endY = beamY + 30f
        )
        drawRect(
            brush = beamBrush,
            topLeft = Offset(0f, beamY - 30f),
            size = androidx.compose.ui.geometry.Size(width, 60f)
        )
    }
}

