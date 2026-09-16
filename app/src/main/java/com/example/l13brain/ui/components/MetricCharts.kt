package com.example.l13brain.ui.components

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
                badgeColor = Color(0xFF00FF66),
                badgeBg = Color(0xFF062215),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "BETTI NOS",
                value = "β₀=1, β₁=2",
                badge = "TORUS S¹×S¹",
                badgeColor = Color(0xFF00FF66),
                badgeBg = Color(0xFF062215),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "ENTROPÍA SH.",
                value = String.format("%.3fb", telemetry.entropyShannon),
                badge = "-0.1% LOW",
                badgeColor = Color(0xFFFF4081),
                badgeBg = Color(0xFF220614),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "SPECTRAL GAP",
                value = "0.4281",
                badge = "λ₁ ÓPTIMO",
                badgeColor = Color(0xFF00FF66),
                badgeBg = Color(0xFF062215),
                modifier = Modifier.weight(1f)
            )
        }

        // 2. FASE KURAMOTO S¹ & RESONANCIA DIFERENCIAL Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(8.dp))
                .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF00FF66), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FASE KURAMOTO S¹ & RESONANCIA DIFERENCIAL",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = Color(0xFF00FF66)
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
                        color = Color(0xFF5B786D)
                    )
                    OutlinedBadge("∠ 53.3°", Color(0xFF00E5FF))
                    OutlinedBadge("JITTER ±0.02 Hz", Color(0xFFFF4081))
                }
            }

            // Sine Wave Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                KuramotoSineWaveCanvas(modifier = Modifier.fillMaxSize())
            }

            // Sync line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SYNC: φ_ext = 0.992", fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = Color(0xFF00E5FF))
            }

            // RAM Allocation Bar
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RAM ALLOC Q-POOL", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF5B786D))
                    Text("8.0/16 GB [50%]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00E5FF))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF081822), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.50f)
                            .fillMaxHeight()
                            .background(Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                    )
                }
            }

            // CPU Core Density Bar
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CPU CORE DENSITY", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF5B786D))
                    Text("59.1% LOAD", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00E5FF))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF081822), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.591f)
                            .fillMaxHeight()
                            .background(Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // 3. MATRIZ DE INCIDENCIA & TENSOR ADYACENTE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(8.dp))
                .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
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
                    color = Color(0xFF00FF66)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("[42.8% SPARSE]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00E5FF))
                    Text("[RANK: [3..4]]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFFFB300))
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF081A12), RoundedCornerShape(2.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("VÉRTICE", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66), modifier = Modifier.weight(1.2f))
                Text("H_INC [e₁ e₂ e₃]", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66), modifier = Modifier.weight(1.8f))
                Text("GRADO", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66), modifier = Modifier.weight(1f))
                Text("ESTADO", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66), modifier = Modifier.weight(1f))
            }

            // Rows
            val matrixRows = listOf(
                MatrixItem("v1_ir", "[ 1,  0,  0 ]", "deg:1", "PERCEP", Color(0xFF00E5FF), Color(0xFF051C24)),
                MatrixItem("v2_lidar", "[ 1,  0,  0 ]", "deg:1", "PERCEP", Color(0xFF00E5FF), Color(0xFF051C24)),
                MatrixItem("v3_hub", "[ 1,  1,  1 ]", "deg:3", "DUAL", Color(0xFFFF4081), Color(0xFF240516)),
                MatrixItem("v4_monk", "[ 0,  1,  0 ]", "deg:1", "COG", Color(0xFFFF80AB), Color(0xFF240516)),
                MatrixItem("v5_modal", "[ 1,  0,  1 ]", "deg:2", "BRIDGE", Color(0xFFB388FF), Color(0xFF160524)),
                MatrixItem("v6_emerg", "[ 0,  0,  1 ]", "deg:1", "RESON", Color(0xFF00E5FF), Color(0xFF051C24))
            )

            matrixRows.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.name, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = Color(0xFFCBD5E1), modifier = Modifier.weight(1.2f))
                    Text(item.matrix, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = Color(0xFF00FF66), modifier = Modifier.weight(1.8f))
                    Text(
                        item.degree,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = if (item.degree == "deg:3") Color(0xFFFF4081) else Color(0xFF94A3B8),
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
                .background(Color(0xFF03070E), RoundedCornerShape(8.dp))
                .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
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
                    color = Color(0xFF00FF66)
                )
                Text(
                    text = "5 ACTIVOS • TICK RT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = Color(0xFF5B786D)
                )
            }

            // Processes
            val processes = listOf(
                DaemonItem("1024", "l13_brain_engine", "18.5% CPU", "[RUNNING]", Color(0xFF00E5FF)),
                DaemonItem("1042", "ws_stream_daemon", "2.1% CPU", "[IDLE]", Color(0xFF94A3B8)),
                DaemonItem("1088", "ccipca_eigen_4d", "8.4% CPU", "[CALC]", Color(0xFFB388FF)),
                DaemonItem("1105", "vietoris_rips", "5.2% CPU", "[CONV]", Color(0xFFFF4081))
            )

            processes.forEach { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${p.pid}  ${p.name}", fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = Color(0xFFCBD5E1), modifier = Modifier.weight(2f))
                    Text(p.cpu, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = Color(0xFF00FF66), modifier = Modifier.weight(1f))
                    Text(p.status, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = p.statusColor, modifier = Modifier.weight(1f))
                }
            }
        }

        // 5. Action Buttons (HALT ENGINE, PURGE SWAP, SNAPSHOT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BottomActionPill("⏹ HALT ENGINE", Color(0xFFFF4081), Modifier.weight(1f), onHaltEngine)
            BottomActionPill("🧹 PURGE SWAP", Color(0xFFFFB300), Modifier.weight(1f), onPurgeSwap)
            BottomActionPill("💾 SNAPSHOT", Color(0xFF00E5FF), Modifier.weight(1f), onTakeSnapshot)
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
            .background(Color(0xFF03070E), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 7.5.sp,
            color = Color(0xFF5B786D)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF00E5FF)
        )
        Box(
            modifier = Modifier
                .border(0.8.dp, badgeColor, RoundedCornerShape(3.dp))
                .background(badgeBg, RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = badge,
                fontFamily = FontFamily.Monospace,
                fontSize = 6.5.sp,
                color = badgeColor
            )
        }
    }
}

@Composable
private fun OutlinedBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(3.dp))
            .background(Color(0xFF040F18), RoundedCornerShape(3.dp))
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
            .background(Color(0xFF07121A), RoundedCornerShape(4.dp))
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
private fun KuramotoSineWaveCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Grid lines
        drawLine(Color(0xFF0A1F16), Offset(0f, midY), Offset(w, midY), strokeWidth = 0.8f)

        // Solid Cyan Sine Wave
        val pathCyan = Path()
        val pathMagenta = Path()

        var first = true
        for (i in 0..100) {
            val x = (i / 100f) * w
            val angle1 = (i / 100f) * (4 * PI.toFloat())
            val y1 = midY + sin(angle1) * (h * 0.38f)

            val angle2 = angle1 + 0.8f
            val y2 = midY + sin(angle2) * (h * 0.35f)

            if (first) {
                pathCyan.moveTo(x, y1)
                pathMagenta.moveTo(x, y2)
                first = false
            } else {
                pathCyan.lineTo(x, y1)
                pathMagenta.lineTo(x, y2)
            }
        }

        drawPath(pathCyan, Color(0xFF00E5FF), style = Stroke(width = 1.8f))
        drawPath(
            pathMagenta,
            Color(0xFFFF4081),
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
        )

        // Phase tracker yellow dot
        val peakX = w * 0.38f
        val peakY = midY - (h * 0.38f)
        drawCircle(Color(0xFFFFB300), radius = 4f, center = Offset(peakX, peakY))
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
