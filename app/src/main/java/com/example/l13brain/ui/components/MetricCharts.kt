package com.example.l13brain.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.l13brain.model.TelemetryState
import com.example.l13brain.ui.theme.CalcBevelBorder
import com.example.l13brain.ui.theme.CalcBezel
import com.example.l13brain.ui.theme.CalcBorderSubtle
import com.example.l13brain.ui.theme.CalcChassis
import com.example.l13brain.ui.theme.CalcLcdAxis
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
import com.example.l13brain.ui.theme.CalcSoftkeyActiveBg
import com.example.l13brain.ui.theme.CalcSoftkeyActiveBorder
import com.example.l13brain.ui.theme.CalcSoftkeyBg
import com.example.l13brain.ui.theme.CalcSoftkeyBorder
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun TelemetriaTensoresTab(
    telemetry: TelemetryState,
    onHaltEngine: () -> Unit,
    onPurgeSwap: () -> Unit,
    onTakeSnapshot: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 4-Grid Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricCard(
                title = "KURAMOTO R(t)",
                value = String.format("%.3f", telemetry.kuramotoOrderR),
                badge = "+0.02% STB",
                badgeColor = CalcPlotY3,
                badgeBg = CalcSoftkeyBg,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "BETTI NOS",
                value = "β₀=1, β₁=2",
                badge = "TORUS S¹×S¹",
                badgeColor = CalcPlotY1,
                badgeBg = CalcSoftkeyBg,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "ENTROPÍA SH.",
                value = String.format("%.3fb", telemetry.entropyShannon),
                badge = "-0.1% LOW",
                badgeColor = CalcPlotY4,
                badgeBg = CalcSoftkeyBg,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "SPECTRAL GAP",
                value = "0.4281",
                badge = "λ₁ ÓPTIMO",
                badgeColor = CalcPlotY2,
                badgeBg = CalcSoftkeyBg,
                modifier = Modifier.weight(1f)
            )
        }

        // 2. FASE KURAMOTO S¹ & RESONANCIA DIFERENCIAL Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalcBezel, RoundedCornerShape(8.dp))
                .border(1.2.dp, CalcBorderSubtle, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(CalcPlotY1, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FASE KURAMOTO S¹ & RESONANCIA DIFERENCIAL",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = CalcLcdText
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[60s TRACE]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = CalcLcdTextMuted
                    )
                    OutlinedBadge("∠ 53.3°", CalcPlotY1)
                    OutlinedBadge("JITTER ±0.02 Hz", CalcPlotY4)
                }
            }

            // Sine Wave Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(CalcLcdBackground, RoundedCornerShape(4.dp))
                    .border(1.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
            ) {
                KuramotoSineWaveCanvas(telemetry = telemetry, modifier = Modifier.fillMaxSize())
            }

            // Sync line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "SYNC: φ_ext = ${String.format(java.util.Locale.US, "%.3f", telemetry.kuramotoOrderR)} [R(t)]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = CalcPlotY1
                )
            }

            // RAM Allocation Bar
            val ramPct = ((telemetry.ramAllocatedGb / telemetry.ramTotalGb) * 100).toInt().coerceIn(1, 100)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RAM ALLOC Q-POOL", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcLcdTextMuted)
                    Text(
                        "${String.format(java.util.Locale.US, "%.1f", telemetry.ramAllocatedGb)}/${String.format(java.util.Locale.US, "%.0f", telemetry.ramTotalGb)} GB [$ramPct%]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = CalcPlotY1
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(CalcLcdBackground, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((telemetry.ramAllocatedGb / telemetry.ramTotalGb).toFloat().coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .background(CalcPlotY1, RoundedCornerShape(3.dp))
                    )
                }
            }

            // CPU Core Density Bar
            val cpuLoadVal = telemetry.cpuLoad.coerceIn(5.0f, 100.0f)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CPU CORE DENSITY", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcLcdTextMuted)
                    Text(
                        "${String.format(java.util.Locale.US, "%.1f", cpuLoadVal)}% LOAD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = CalcPlotY2
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(CalcLcdBackground, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((cpuLoadVal / 100f).coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .background(CalcPlotY2, RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // 3. MATRIZ DE INCIDENCIA & TENSOR ADYACENTE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalcBezel, RoundedCornerShape(8.dp))
                .border(1.2.dp, CalcBorderSubtle, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "▦ MATRIZ DE INCIDENCIA & TENSOR ADYACENTE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    color = CalcLcdText
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("[42.8% SPARSE]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY1)
                    Text("[RANK: [3..4]]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY2)
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalcLcdBackground, RoundedCornerShape(2.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("VÉRTICE", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY1, modifier = Modifier.weight(1.2f))
                Text("H_INC [e₁ e₂ e₃]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY1, modifier = Modifier.weight(1.8f))
                Text("GRADO", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY1, modifier = Modifier.weight(1f))
                Text("ESTADO", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY1, modifier = Modifier.weight(1f))
            }

            // Rows
            val matrixRows = listOf(
                MatrixItem("v1_ir", "[ 1,  0,  0 ]", "deg:1", "PERCEP", CalcPlotY1, CalcSoftkeyBg),
                MatrixItem("v2_lidar", "[ 1,  0,  0 ]", "deg:1", "PERCEP", CalcPlotY1, CalcSoftkeyBg),
                MatrixItem("v3_hub", "[ 1,  1,  1 ]", "deg:3", "DUAL", CalcPlotY4, CalcSoftkeyBg),
                MatrixItem("v4_monk", "[ 0,  1,  0 ]", "deg:1", "COG", CalcPlotY5, CalcSoftkeyBg),
                MatrixItem("v5_modal", "[ 1,  0,  1 ]", "deg:2", "BRIDGE", CalcPlotY6, CalcSoftkeyBg),
                MatrixItem("v6_emerg", "[ 0,  0,  1 ]", "deg:1", "RESON", CalcPlotY3, CalcSoftkeyBg)
            )

            matrixRows.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.name, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = CalcLcdText, modifier = Modifier.weight(1.2f))
                    Text(item.matrix, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = CalcPlotY1, modifier = Modifier.weight(1.8f))
                    Text(
                        item.degree,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = if (item.degree == "deg:3") CalcPlotY4 else CalcLcdTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, item.stateColor, RoundedCornerShape(3.dp))
                            .background(item.bgColor, RoundedCornerShape(3.dp))
                            .padding(vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.state, fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = item.stateColor)
                    }
                }
            }
        }

        // 4. L13 RUNTIME DAEMONS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalcBezel, RoundedCornerShape(8.dp))
                .border(1.2.dp, CalcBorderSubtle, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙ L13 RUNTIME DAEMONS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    color = CalcLcdText
                )
                Text(
                    text = "5 ACTIVOS • TICK RT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = CalcLcdTextMuted
                )
            }

            // Processes
            val processes = listOf(
                DaemonItem("1024", "l13_brain_engine", "18.5% CPU", "[RUNNING]", CalcPlotY3),
                DaemonItem("1042", "ws_stream_daemon", "2.1% CPU", "[IDLE]", CalcLcdTextMuted),
                DaemonItem("1088", "ccipca_eigen_4d", "8.4% CPU", "[CALC]", CalcPlotY6),
                DaemonItem("1105", "vietoris_rips", "5.2% CPU", "[CONV]", CalcPlotY4)
            )

            processes.forEach { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${p.pid}  ${p.name}", fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = CalcLcdText, modifier = Modifier.weight(2f))
                    Text(p.cpu, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = CalcPlotY1, modifier = Modifier.weight(1f))
                    Text(p.status, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = p.statusColor, modifier = Modifier.weight(1f))
                }
            }
        }

        // 5. Action Buttons (HALT ENGINE, PURGE SWAP, SNAPSHOT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BottomActionPill("⏹ HALT ENGINE", CalcPlotY4, Modifier.weight(1f), onHaltEngine)
            BottomActionPill("🧹 PURGE SWAP", CalcPlotY2, Modifier.weight(1f), onPurgeSwap)
            BottomActionPill("💾 SNAPSHOT", CalcPlotY1, Modifier.weight(1f), onTakeSnapshot)
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CalcBezel, RoundedCornerShape(6.dp))
            .border(1.dp, CalcBorderSubtle, RoundedCornerShape(6.dp))
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 7.sp,
            color = CalcLcdTextMuted,
            maxLines = 1
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            color = CalcLcdText,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .border(0.8.dp, badgeColor, RoundedCornerShape(3.dp))
                .background(badgeBg, RoundedCornerShape(3.dp))
                .padding(horizontal = 3.dp, vertical = 1.dp)
        ) {
            Text(
                text = badge,
                fontFamily = FontFamily.Monospace,
                fontSize = 6.5.sp,
                color = badgeColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OutlinedBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(3.dp))
            .background(CalcSoftkeyBg, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 7.5.sp,
            color = color
        )
    }
}

@Composable
private fun BottomActionPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(CalcSoftkeyBg, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun KuramotoSineWaveCanvas(
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sine_anim")
    val phaseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnim"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Grid lines
        drawLine(CalcLcdGrid, Offset(0f, midY), Offset(w, midY), strokeWidth = 0.8f)

        // Solid Y1 Blue Sine Wave & Dashed Y2 Red Shifted Wave
        val pathCyan = Path()
        val pathMagenta = Path()

        val r = telemetry.kuramotoOrderR.coerceIn(0.1f, 1.2f)
        val amp1 = h * 0.36f * r
        val amp2 = h * 0.30f

        var first = true
        for (i in 0..100) {
            val x = (i / 100f) * w
            val normX = i / 100f
            val angle1 = (normX * 4f * PI.toFloat()) - phaseAnim
            val y1 = midY + sin(angle1) * amp1

            val angle2 = angle1 + 0.93f * (1.5f - r)
            val y2 = midY + sin(angle2) * amp2

            if (first) {
                pathCyan.moveTo(x, y1)
                pathMagenta.moveTo(x, y2)
                first = false
            } else {
                pathCyan.lineTo(x, y1)
                pathMagenta.lineTo(x, y2)
            }
        }

        drawPath(pathCyan, CalcPlotY1, style = Stroke(width = 1.8f))
        drawPath(
            pathMagenta,
            CalcPlotY2,
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
        )

        // Phase tracker yellow dot moving along wave
        val trackerNormX = ((phaseAnim / (2 * PI.toFloat())) * 0.8f + 0.1f) % 1.0f
        val trackerX = trackerNormX * w
        val trackerY = midY + sin((trackerNormX * 4f * PI.toFloat()) - phaseAnim) * amp1
        drawCircle(CalcPlotY3, radius = 4f, center = Offset(trackerX, trackerY))
        drawCircle(CalcLcdText, radius = 2f, center = Offset(trackerX, trackerY))
    }
}

private data class MatrixItem(
    val name: String,
    val matrix: String,
    val degree: String,
    val state: String,
    val stateColor: Color,
    val bgColor: Color
)

private data class DaemonItem(
    val pid: String,
    val name: String,
    val cpu: String,
    val status: String,
    val statusColor: Color
)
