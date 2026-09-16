package com.example.l13brain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AjustesTab(
    onExportPng: () -> Unit,
    onSyncApi: () -> Unit,
    onDumpJson: () -> Unit,
    onGeminiDiagnosis: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scanlines by remember { mutableFloatStateOf(0.80f) }
    var curvature by remember { mutableFloatStateOf(0.40f) }
    var coulombKr by remember { mutableFloatStateOf(120f) }
    var hookeKa by remember { mutableFloatStateOf(0.045f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF03070E), RoundedCornerShape(8.dp))
            .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title
        Text(
            text = "┌── ELEMENT INSPECTOR ──────────────────────────────────────",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF00FF66)
        )

        Text(
            text = "Toca un nodo en el canvas para inspeccionar.",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = Color(0xFF5B786D)
        )

        // Section: SHADER CONTROLS (CRT)
        Text(
            text = "├── SHADER CONTROLS (CRT) ──────────────────────────────────┤",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = Color(0xFF00FF66)
        )

        // Scanlines slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Scanlines", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFCBD5E1))
                Text("${(scanlines * 100).toInt()}%", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFF00FF66))
            }
            Slider(
                value = scanlines,
                onValueChange = { scanlines = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00FF66),
                    activeTrackColor = Color(0xFF00FF66),
                    inactiveTrackColor = Color(0xFF0C2418)
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        // Curvatura CRT slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Curvatura CRT", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFCBD5E1))
                Text("${(curvature * 100).toInt()}%", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFF00FF66))
            }
            Slider(
                value = curvature,
                onValueChange = { curvature = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00FF66),
                    activeTrackColor = Color(0xFF00FF66),
                    inactiveTrackColor = Color(0xFF0C2418)
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        // Section: PHYSICS PARAMETERS
        Text(
            text = "├── PHYSICS PARAMETERS ─────────────────────────────────────┤",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = Color(0xFF00FF66)
        )

        // Coulomb Repulsion
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Coulomb Repuls. kr: ${coulombKr.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFCBD5E1))
            }
            Slider(
                value = coulombKr,
                onValueChange = { coulombKr = it },
                valueRange = 20f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00FF66),
                    activeTrackColor = Color(0xFF00FF66),
                    inactiveTrackColor = Color(0xFF0C2418)
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        // Hooke Spring
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hooke Spring ka: ${String.format(java.util.Locale.US, "%.3f", hookeKa)}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFCBD5E1))
            }
            Slider(
                value = hookeKa,
                onValueChange = { hookeKa = it },
                valueRange = 0.01f..0.20f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00FF66),
                    activeTrackColor = Color(0xFF00FF66),
                    inactiveTrackColor = Color(0xFF0C2418)
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        // Section: EXPORT & SNAPSHOT
        Text(
            text = "├── EXPORT & SNAPSHOT ──────────────────────────────────────┤",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = Color(0xFF00FF66)
        )

        // Outlined Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorButton("[P] Export Snapshot PNG", Color(0xFFFFB300), Modifier.weight(1f), onExportPng)
            InspectorButton("[S] Sync to Remote API", Color(0xFF00E5FF), Modifier.weight(1f), onSyncApi)
            InspectorButton("[D] Dump State JSON", Color(0xFF00E5FF), Modifier.weight(1f), onDumpJson)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Big Gemini Diagnosis Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00FF66), RoundedCornerShape(6.dp))
                .clickable { onGeminiDiagnosis() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ Gemini AI Diagnosis",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun InspectorButton(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(Color(0xFF07121A), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
