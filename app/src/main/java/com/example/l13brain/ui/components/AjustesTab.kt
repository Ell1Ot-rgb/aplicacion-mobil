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
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.PhysicsConfig
import com.example.l13brain.model.ShaderConfig
import com.example.l13brain.ui.theme.CalcBevelBorder
import com.example.l13brain.ui.theme.CalcBezel
import com.example.l13brain.ui.theme.CalcBorderSubtle
import com.example.l13brain.ui.theme.CalcChassis
import com.example.l13brain.ui.theme.CalcKeyEnterBg
import com.example.l13brain.ui.theme.CalcKeyEnterBorder
import com.example.l13brain.ui.theme.CalcKeyEnterText
import com.example.l13brain.ui.theme.CalcLcdBackground
import com.example.l13brain.ui.theme.CalcLcdGrid
import com.example.l13brain.ui.theme.CalcLcdText
import com.example.l13brain.ui.theme.CalcLcdTextMuted
import com.example.l13brain.ui.theme.CalcPlotY1
import com.example.l13brain.ui.theme.CalcPlotY2
import com.example.l13brain.ui.theme.CalcPlotY3
import com.example.l13brain.ui.theme.CalcPlotY4
import com.example.l13brain.ui.theme.CalcPlotY5
import com.example.l13brain.ui.theme.CalcPlotY6
import com.example.l13brain.ui.theme.CalcSoftkeyBg
import com.example.l13brain.ui.theme.CalcSoftkeyBorder

@Composable
fun AjustesTab(
    shaderConfig: ShaderConfig = ShaderConfig(),
    physicsConfig: PhysicsConfig = PhysicsConfig(),
    selectedNode: HyperNode? = null,
    onShaderConfigChange: (ShaderConfig) -> Unit = {},
    onPhysicsConfigChange: (PhysicsConfig) -> Unit = {},
    onInjectEnergyToNode: (String, Float) -> Unit = { _, _ -> },
    onResetNodePhase: (String) -> Unit = {},
    onExportPng: () -> Unit,
    onSyncApi: () -> Unit,
    onDumpJson: () -> Unit,
    onGeminiDiagnosis: () -> Unit,
    latestDiagnosis: String? = null,
    modifier: Modifier = Modifier
) {
    var scanlines by remember(shaderConfig.scanlineDensity) { mutableFloatStateOf(shaderConfig.scanlineDensity) }
    var curvature by remember(shaderConfig.barrelCurvature) { mutableFloatStateOf(shaderConfig.barrelCurvature) }
    var coulombKr by remember(physicsConfig.coulombRepulsionKr) { mutableFloatStateOf(physicsConfig.coulombRepulsionKr) }
    var hookeKa by remember(physicsConfig.hookeSpringKa) { mutableFloatStateOf(physicsConfig.hookeSpringKa) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CalcChassis, RoundedCornerShape(8.dp))
            .border(1.2.dp, CalcBevelBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title
        Text(
            text = "┌── ELEMENT INSPECTOR ──────────────────────────────────────",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = CalcPlotY1
        )

        if (selectedNode != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalcLcdBackground, RoundedCornerShape(6.dp))
                    .border(1.dp, CalcPlotY1, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● NODO SELECCIONADO: ${selectedNode.label}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = CalcPlotY1
                    )
                    Text(
                        text = "ID: ${selectedNode.id}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CalcPlotY2
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ENERGÍA: ${String.format(java.util.Locale.US, "%.2f", selectedNode.energy)} J",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CalcPlotY3
                    )
                    Text(
                        text = "FASE θ: ${String.format(java.util.Locale.US, "%.2f", selectedNode.phase)} rad",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CalcLcdText
                    )
                }

                Text(
                    text = "ESTADO S4: ${selectedNode.modalState} | POS: (${String.format(java.util.Locale.US, "%.2f", selectedNode.x)}, ${String.format(java.util.Locale.US, "%.2f", selectedNode.y)})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = CalcLcdTextMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InspectorButton(
                        label = "⚡ +0.50J",
                        color = CalcPlotY3,
                        modifier = Modifier.weight(1f)
                    ) {
                        onInjectEnergyToNode(selectedNode.id, 0.50f)
                    }
                    InspectorButton(
                        label = "🔄 Reset Fase",
                        color = CalcPlotY1,
                        modifier = Modifier.weight(1f)
                    ) {
                        onResetNodePhase(selectedNode.id)
                    }
                }
            }
        } else {
            Text(
                text = "Toca cualquier nodo en el Toposcopio (CH1) para calibrar sus parámetros.",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = CalcLcdTextMuted
            )
        }

        // Section: SHADER CONTROLS (CRT)
        Text(
            text = "├── SHADER CONTROLS (CRT) ──────────────────────────────────┤",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = CalcPlotY1
        )

        // Scanlines slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Scanlines", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CalcLcdText)
                Text("${(scanlines * 100).toInt()}%", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CalcPlotY1)
            }
            Slider(
                value = scanlines,
                onValueChange = {
                    scanlines = it
                    onShaderConfigChange(shaderConfig.copy(scanlineDensity = it))
                },
                colors = SliderDefaults.colors(
                    thumbColor = CalcPlotY1,
                    activeTrackColor = CalcPlotY1,
                    inactiveTrackColor = CalcLcdGrid
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
                Text("Curvatura CRT", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CalcLcdText)
                Text("${(curvature * 100).toInt()}%", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CalcPlotY1)
            }
            Slider(
                value = curvature,
                onValueChange = {
                    curvature = it
                    onShaderConfigChange(shaderConfig.copy(barrelCurvature = it))
                },
                colors = SliderDefaults.colors(
                    thumbColor = CalcPlotY1,
                    activeTrackColor = CalcPlotY1,
                    inactiveTrackColor = CalcLcdGrid
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        // Section: PHYSICS PARAMETERS
        Text(
            text = "├── PHYSICS PARAMETERS ─────────────────────────────────────┤",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = CalcPlotY1
        )

        // Coulomb Repulsion
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Coulomb Repuls. kr: ${coulombKr.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CalcLcdText)
            }
            Slider(
                value = coulombKr,
                onValueChange = {
                    coulombKr = it
                    onPhysicsConfigChange(physicsConfig.copy(coulombRepulsionKr = it))
                },
                valueRange = 20f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = CalcPlotY2,
                    activeTrackColor = CalcPlotY2,
                    inactiveTrackColor = CalcLcdGrid
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
                Text("Hooke Spring ka: ${String.format(java.util.Locale.US, "%.3f", hookeKa)}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CalcLcdText)
            }
            Slider(
                value = hookeKa,
                onValueChange = {
                    hookeKa = it
                    onPhysicsConfigChange(physicsConfig.copy(hookeSpringKa = it))
                },
                valueRange = 0.01f..0.20f,
                colors = SliderDefaults.colors(
                    thumbColor = CalcPlotY2,
                    activeTrackColor = CalcPlotY2,
                    inactiveTrackColor = CalcLcdGrid
                ),
                modifier = Modifier.height(24.dp)
            )
        }

        // Section: EXPORT & SNAPSHOT
        Text(
            text = "├── EXPORT & SNAPSHOT ──────────────────────────────────────┤",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = CalcPlotY1
        )

        // Outlined Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorButton("[P] Snapshot PNG", CalcPlotY2, Modifier.weight(1f), onExportPng)
            InspectorButton("[S] Sync Remote", CalcPlotY1, Modifier.weight(1f), onSyncApi)
            InspectorButton("[D] Dump JSON", CalcPlotY3, Modifier.weight(1f), onDumpJson)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Big Gemini Diagnosis Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalcKeyEnterBg, RoundedCornerShape(6.dp))
                .border(1.dp, CalcKeyEnterBorder, RoundedCornerShape(6.dp))
                .clickable { onGeminiDiagnosis() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ Gemini AI Diagnosis",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CalcKeyEnterText
            )
        }

        if (latestDiagnosis != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalcLcdBackground, RoundedCornerShape(6.dp))
                    .border(1.dp, CalcPlotY1, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🤖 INFORME DE DIAGNÓSTICO GEMINI AI:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    color = CalcPlotY1
                )
                Text(
                    text = latestDiagnosis,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = CalcLcdText
                )
            }
        }
    }
}

@Composable
private fun InspectorButton(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(CalcSoftkeyBg, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
