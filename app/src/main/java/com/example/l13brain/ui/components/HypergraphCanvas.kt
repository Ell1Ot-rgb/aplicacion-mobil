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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.TelemetryState
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ToposcopioGrafoTab(
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    telemetry: TelemetryState,
    selectedNodeId: String?,
    onNodeSelected: (String) -> Unit,
    onNodeDragged: (String, Float, Float) -> Unit,
    onAmalgamatedSum: () -> Unit = {},
    onBergeDual: () -> Unit = {},
    onAutopoieticStep: () -> Unit = {},
    onInjectEnergy: () -> Unit = {},
    onWolframMutation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilterTab by remember { mutableIntStateOf(1) } // 2. DIRAC & ( by default
    var showDensityOverlay by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Frame Header: "┌─ TOPOSCOPIO DINÁMICO L13 // DOMINIO MIXTO TOPOLÓGICO"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "┌─ TOPOSCOPIO DINÁMICO L13 // DOMINIO MIXTO TOPOLÓGICO",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF00FF66),
                modifier = Modifier.weight(1f, fill = false)
            )

            Text(
                text = "MERKLE:\n[12a901a3..]",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color(0xFFFFB300)
            )
        }

        // 2. Main Hypergraph Canvas Card (Convex Euler Ellipses & Bridge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(370.dp)
                .background(Color(0xFF03070E), RoundedCornerShape(8.dp))
                .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
        ) {
            HypergraphCanvas(
                nodes = nodes,
                hyperedges = hyperedges,
                selectedNodeId = selectedNodeId,
                showDensityOverlay = showDensityOverlay,
                onNodeSelected = onNodeSelected,
                onNodeDragged = onNodeDragged,
                modifier = Modifier.fillMaxSize()
            )

            // Top-right floating button: "📊 DENSIDAD DE OCUPACIÓN"
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .border(1.dp, if (showDensityOverlay) Color(0xFF00E5FF) else Color(0xFF00FF66), RoundedCornerShape(4.dp))
                    .background(if (showDensityOverlay) Color(0xFF072430) else Color(0xFF061A12), RoundedCornerShape(4.dp))
                    .clickable { showDensityOverlay = !showDensityOverlay }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (showDensityOverlay) "📊 DENSIDAD [ACTIVA]" else "📊 DENSIDAD DE OCUPACIÓN",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showDensityOverlay) Color(0xFF00E5FF) else Color(0xFF00FF66)
                )
            }
        }

        // 3. Quick Topology Action Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TopGraphActionPill("⚡ SUMA AMALGAMADA", Color(0xFF00FF66), onAmalgamatedSum)
            TopGraphActionPill("🔄 DUAL DE BERGE", Color(0xFF00E5FF), onBergeDual)
            TopGraphActionPill("🌀 PASO AUTOPOIÉTICO", Color(0xFF86EFAC), onAutopoieticStep)
            TopGraphActionPill("⚡ +0.50J INYECTAR", Color(0xFFFFB300), onInjectEnergy)
            TopGraphActionPill("W REGLA WOLFRAM", Color(0xFFFF4081), onWolframMutation)
        }

        // 4. Bottom Card: "┌─ TOPOSCOPIO: [CH1: s_optico] [CH2: s_optico] [DC]      [TRIG: AUTO ●]"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040810), RoundedCornerShape(8.dp))
                .border(1.2.dp, Color(0xFF00FF66), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Oscilloscope Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "┌─ TOPOSCOPIO:",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF00FF66)
                    )

                    BadgePill(text = "CH1: ${selectedNodeId ?: "s_optico"}", color = Color(0xFFFFB300), bg = Color(0xFF241505))
                    BadgePill(text = "CH2: hub_central", color = Color(0xFF00E5FF), bg = Color(0xFF051C24))
                    BadgePill(text = "DC", color = Color(0xFF00FF66), bg = Color(0xFF061A12))
                }

                BadgePill(text = "TRIG: AUTO ●", color = Color(0xFF00FF66), bg = Color(0xFF061A12))
            }

            // Filter Subtabs: 1. MORFOLOGÍ | 2. DIRAC & ( | 3. GFT WATER | 4. TRIGGER C | 5. MEMORIA M | 6. DENSIDAD
            val subtabs = listOf(
                "1. MORFOLOGÍ",
                "2. DIRAC & (",
                "3. GFT WATER",
                "4. TRIGGER C",
                "5. MEMORIA M",
                "6. DENSIDAD"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                subtabs.forEachIndexed { index, title ->
                    val isActive = selectedFilterTab == index
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (isActive) Color(0xFF00FF66) else Color(0xFF16382B), RoundedCornerShape(3.dp))
                            .background(if (isActive) Color(0xFF072618) else Color(0xFF070E17), RoundedCornerShape(3.dp))
                            .clickable { selectedFilterTab = index }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color(0xFF00FF66) else Color(0xFF5B786D)
                        )
                    }
                }
            }

            // Dynamic Oscilloscope Screen based on Subtab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Color(0xFF02060B), RoundedCornerShape(4.dp))
                    .border(0.8.dp, Color(0xFF0F3022), RoundedCornerShape(4.dp))
            ) {
                when (selectedFilterTab) {
                    0 -> OscilloscopeLissajousCanvas(telemetry, Modifier.fillMaxSize())
                    1 -> DiracPolarRadarCanvas(nodes, telemetry, Modifier.fillMaxSize())
                    2 -> GftWaterfallCanvas(telemetry, Modifier.fillMaxSize())
                    3 -> DualChannelOscilloscopeCanvas(telemetry, Modifier.fillMaxSize())
                    4 -> HolographicVsaMemoryCanvas(Modifier.fillMaxSize())
                    else -> DensityDistributionCanvas(nodes, Modifier.fillMaxSize())
                }
            }

            // Dynamic Telemetry line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ΔE: 0.00J | Δθ: 0.00rad | TAYLOR +100ms: 0.19J | R(t): ${String.format("%.1f", telemetry.kuramotoOrderR * 100)}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = Color(0xFF00FF66)
                )

                Text(
                    text = "GKF: S1 SYNC",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
        }

        // 5. Zoom / Topology Footer Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[TOPOLOGÍA: k=3..4] [ARISTAS: 3] [CONV: 38.9%] [VIEW: EULER HULL]",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color(0xFF00FF66)
            )

            Text(
                text = "[ZOOM: 100%]",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF66)
            )
        }
    }
}

@Composable
fun TopGraphActionPill(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .background(Color(0xFF06141D), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
fun HypergraphCanvas(
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    selectedNodeId: String?,
    showDensityOverlay: Boolean = false,
    onNodeSelected: (String) -> Unit,
    onNodeDragged: (String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(nodes) {
                detectTapGestures { tapOffset ->
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    for (node in nodes) {
                        val nx = node.x * width
                        val ny = node.y * height
                        val dist = sqrt((tapOffset.x - nx) * (tapOffset.x - nx) + (tapOffset.y - ny) * (tapOffset.y - ny))
                        if (dist <= 64f) {
                            onNodeSelected(node.id)
                            break
                        }
                    }
                }
            }
            .pointerInput(nodes) {
                var draggedNodeId: String? = null
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        for (node in nodes) {
                            val nx = node.x * width
                            val ny = node.y * height
                            val dist = sqrt((startOffset.x - nx) * (startOffset.x - nx) + (startOffset.y - ny) * (startOffset.y - ny))
                            if (dist <= 64f) {
                                draggedNodeId = node.id
                                onNodeSelected(node.id)
                                break
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        draggedNodeId?.let { id ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val node = nodes.find { it.id == id }
                            if (node != null && width > 0 && height > 0) {
                                val newX = (node.x + dragAmount.x / width).coerceIn(0.06f, 0.94f)
                                val newY = (node.y + dragAmount.y / height).coerceIn(0.08f, 0.92f)
                                onNodeDragged(id, newX, newY)
                            }
                        }
                        change.consume()
                    },
                    onDragEnd = { draggedNodeId = null },
                    onDragCancel = { draggedNodeId = null }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        // 1. Grid Background
        drawCyberGrid(width, height)

        if (showDensityOverlay) {
            drawDensityHeatmap(width, height, nodes)
        }

        // 2. Draw Hypergraph Ellipses and Laser Lines
        drawEulerHypergraph(width, height, nodes, hyperedges, pulseAlpha)

        // 3. Draw Nodes with halos and labels
        drawTopNodes(width, height, nodes, selectedNodeId, pulseAlpha)
    }
}

private fun DrawScope.drawDensityHeatmap(width: Float, height: Float, nodes: List<HyperNode>) {
    val nodePositions = if (nodes.isNotEmpty()) {
        nodes.map { Offset(it.x * width, it.y * height) }
    } else {
        listOf(
            Offset(width * 0.12f, height * 0.27f),
            Offset(width * 0.11f, height * 0.70f),
            Offset(width * 0.48f, height * 0.27f),
            Offset(width * 0.88f, height * 0.27f),
            Offset(width * 0.50f, height * 0.71f),
            Offset(width * 0.88f, height * 0.71f)
        )
    }
    for (pt in nodePositions) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00FF66).copy(alpha = 0.22f), Color(0xFF00E5FF).copy(alpha = 0.08f), Color.Transparent),
                center = pt,
                radius = 65f
            ),
            radius = 65f,
            center = pt
        )
    }
}

private fun DrawScope.drawCyberGrid(width: Float, height: Float) {
    val gridSpacing = 32f
    val gridColor = Color(0xFF0A1F16)
    var x = 0f
    while (x < width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 0.7f)
        x += gridSpacing
    }
    var y = 0f
    while (y < height) {
        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 0.7f)
        y += gridSpacing
    }
}

private fun DrawScope.drawEulerHypergraph(
    width: Float,
    height: Float,
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    pulseAlpha: Float
) {
    val nodeMap = nodes.associateBy { it.id }

    // Positions for member nodes
    val pOptico = nodeMap["v1"]?.let { Offset(it.x * width, it.y * height) } ?: Offset(width * 0.12f, height * 0.27f)
    val pAcustico = nodeMap["v2"]?.let { Offset(it.x * width, it.y * height) } ?: Offset(width * 0.11f, height * 0.70f)
    val pHub = nodeMap["v3"]?.let { Offset(it.x * width, it.y * height) } ?: Offset(width * 0.48f, height * 0.27f)
    val pMonje = nodeMap["v4"]?.let { Offset(it.x * width, it.y * height) } ?: Offset(width * 0.88f, height * 0.27f)
    val pMemoria = nodeMap["v5"]?.let { Offset(it.x * width, it.y * height) } ?: Offset(width * 0.50f, height * 0.71f)
    val pS4 = nodeMap["v6"]?.let { Offset(it.x * width, it.y * height) } ?: Offset(width * 0.88f, height * 0.71f)

    // Ellipse 1: Green Sensorial (e1_sensorial, k=3, DPO L=89%)
    drawOval(
        color = Color(0xFF00FF66).copy(alpha = 0.08f),
        topLeft = Offset(width * 0.06f, height * 0.15f),
        size = Size(width * 0.38f, height * 0.68f)
    )
    drawOval(
        color = Color(0xFF00FF66).copy(alpha = 0.85f),
        topLeft = Offset(width * 0.06f, height * 0.15f),
        size = Size(width * 0.38f, height * 0.68f),
        style = Stroke(width = 2.4f)
    )

    // Tag e1_sensorial
    drawTagBox(
        text = "e1_sensorial (k=3) • DPO L=89%",
        center = Offset(width * 0.24f, height * 0.40f),
        textColor = Color(0xFF00FF66),
        borderColor = Color(0xFF00FF66),
        bgColor = Color(0xFF04180E)
    )

    // Ellipse 2: Magenta Cognitiva (e2_cognitiva_s4, k=4, DPO L=89%)
    drawOval(
        color = Color(0xFFFF4081).copy(alpha = 0.08f),
        topLeft = Offset(width * 0.48f, height * 0.14f),
        size = Size(width * 0.44f, height * 0.72f)
    )
    drawOval(
        color = Color(0xFFFF4081).copy(alpha = 0.85f),
        topLeft = Offset(width * 0.48f, height * 0.14f),
        size = Size(width * 0.44f, height * 0.72f),
        style = Stroke(width = 2.4f)
    )

    // Tag e2_cognitiva_s4
    drawTagBox(
        text = "e2_cognitiva_s4 (k=4) • DPO L=89%",
        center = Offset(width * 0.70f, height * 0.48f),
        textColor = Color(0xFFFF4081),
        borderColor = Color(0xFFFF4081),
        bgColor = Color(0xFF1E0510)
    )

    // Laser lines from ellipse center to member nodes
    // For e1 (Green)
    val e1Nodes = listOf(pOptico, pAcustico, pHub)
    for (pt in e1Nodes) {
        drawLine(
            color = Color(0xFF00FF66).copy(alpha = 0.5f),
            start = Offset(width * 0.24f, height * 0.40f),
            end = pt,
            strokeWidth = 1.2f
        )
    }

    // For e2 (Pink)
    val e2Nodes = listOf(pHub, pMonje, pMemoria, pS4)
    for (pt in e2Nodes) {
        drawLine(
            color = Color(0xFFFF4081).copy(alpha = 0.5f),
            start = Offset(width * 0.70f, height * 0.48f),
            end = pt,
            strokeWidth = 1.2f
        )
    }

    // Bridge Hyperedge e3_puente (Cyan Line across top nodes)
    drawLine(
        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
        start = pOptico,
        end = pMonje,
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF00E5FF),
        start = pOptico,
        end = pMonje,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Tag e3_puente
    drawTagBox(
        text = "e3_puente (k=3) • DPO L=89%",
        center = Offset(width * 0.48f, height * 0.26f),
        textColor = Color(0xFF00E5FF),
        borderColor = Color(0xFF00E5FF),
        bgColor = Color(0xFF051C24)
    )
}

private fun DrawScope.drawTagBox(
    text: String,
    center: Offset,
    textColor: Color,
    borderColor: Color,
    bgColor: Color
) {
    val paint = android.graphics.Paint().apply {
        color = textColor.hashCode()
        textSize = 18f
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val textWidth = paint.measureText(text)
    val boxWidth = textWidth + 16f
    val boxHeight = 24f

    drawRect(
        color = bgColor,
        topLeft = Offset(center.x - boxWidth / 2f, center.y - boxHeight / 2f),
        size = Size(boxWidth, boxHeight)
    )
    drawRect(
        color = borderColor,
        topLeft = Offset(center.x - boxWidth / 2f, center.y - boxHeight / 2f),
        size = Size(boxWidth, boxHeight),
        style = Stroke(width = 1f)
    )

    drawContext.canvas.nativeCanvas.drawText(
        text,
        center.x,
        center.y + 6f,
        paint
    )
}

private fun DrawScope.drawTopNodes(
    width: Float,
    height: Float,
    nodes: List<HyperNode>,
    selectedNodeId: String?,
    pulseAlpha: Float
) {
    val labelPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 17f
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val displayNodes = if (nodes.isNotEmpty()) {
        nodes.map { node ->
            val color = when {
                node.modalState.contains("Sensorial", ignoreCase = true) -> Color(0xFF00FF66)
                node.modalState.contains("Cognitivo", ignoreCase = true) -> Color(0xFFFFB300)
                node.modalState.contains("Motor", ignoreCase = true) -> Color(0xFF00E5FF)
                node.modalState.contains("Memoria", ignoreCase = true) -> Color(0xFFFF4081)
                else -> Color(0xFF00FF66)
            }
            val isStar = node.id == "v3" || node.id == "v4" || node.label.contains("hub", ignoreCase = true)
            val starSuffix = if (isStar) " ★" else ""
            val energyStr = String.format(java.util.Locale.US, "%.2f", node.energy)
            val label = "${node.label} [${energyStr} J]$starSuffix"
            NodeVisual(
                id = node.id,
                label = label,
                pos = Offset(node.x * width, node.y * height),
                color = color,
                isStar = isStar,
                isSelected = node.id == selectedNodeId
            )
        }
    } else {
        listOf(
            NodeVisual("v1", "s_optico [0.19 J]", Offset(width * 0.12f, height * 0.27f), Color(0xFF00FF66), false, false),
            NodeVisual("v2", "s_acustico [0.19 J]", Offset(width * 0.11f, height * 0.70f), Color(0xFF00FF66), false, false),
            NodeVisual("v3", "HUB_CENTRAL [0.19 J] ★", Offset(width * 0.48f, height * 0.27f), Color(0xFFFFB300), true, selectedNodeId == "v3"),
            NodeVisual("v4", "COG_MONJE [0.19 J] ★", Offset(width * 0.88f, height * 0.27f), Color(0xFFFFB300), true, selectedNodeId == "v4"),
            NodeVisual("v5", "cog_memoria [0.19 J]", Offset(width * 0.50f, height * 0.71f), Color(0xFF00E5FF), false, selectedNodeId == "v5"),
            NodeVisual("v6", "cog_s4 [0.19 J]", Offset(width * 0.88f, height * 0.71f), Color(0xFFFF4081), false, selectedNodeId == "v6")
        )
    }

    for (node in displayNodes) {
        val cx = node.pos.x
        val cy = node.pos.y

        // Selection highlight ring & radar halo
        if (node.isSelected) {
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.35f + pulseAlpha * 0.25f),
                radius = 24f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 20f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }

        // Halo
        drawCircle(
            color = node.color.copy(alpha = 0.25f),
            radius = 16f,
            center = Offset(cx, cy)
        )
        // Outline ring
        drawCircle(
            color = node.color,
            radius = 10f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )
        // Center core
        drawCircle(
            color = Color(0xFF040A10),
            radius = 7f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = if (node.isStar) Color(0xFFFFB300) else node.color,
            radius = 3.5f,
            center = Offset(cx, cy)
        )

        // Label box beneath node
        val tagY = cy + 18f

        val textWidth = labelPaint.measureText(node.label)
        val boxWidth = textWidth + 12f
        val boxHeight = 20f

        drawRect(
            color = Color(0xFF050E17),
            topLeft = Offset(cx - boxWidth / 2f, tagY - boxHeight / 2f),
            size = Size(boxWidth, boxHeight)
        )
        drawRect(
            color = if (node.isSelected) Color(0xFF00E5FF) else node.color.copy(alpha = 0.6f),
            topLeft = Offset(cx - boxWidth / 2f, tagY - boxHeight / 2f),
            size = Size(boxWidth, boxHeight),
            style = Stroke(width = if (node.isSelected) 1.5f else 0.8f)
        )

        labelPaint.color = if (node.isSelected) android.graphics.Color.WHITE else node.color.hashCode()
        drawContext.canvas.nativeCanvas.drawText(
            node.label,
            cx,
            tagY + 5f,
            labelPaint
        )
    }
}

private data class NodeVisual(
    val id: String = "",
    val label: String,
    val pos: Offset,
    val color: Color,
    val isStar: Boolean,
    val isSelected: Boolean = false
)

@Composable
fun DiracPolarRadarCanvas(
    nodes: List<HyperNode>,
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = (height / 2f) - 6f

        // Grid lines
        val gridColor = Color(0xFF0F3022)
        drawLine(gridColor, Offset(0f, cy), Offset(width, cy), strokeWidth = 0.8f)
        drawLine(gridColor, Offset(cx, 0f), Offset(cx, height), strokeWidth = 0.8f)

        // Concentric circles
        drawCircle(gridColor, radius = maxRadius * 0.4f, center = Offset(cx, cy), style = Stroke(width = 0.8f))
        drawCircle(gridColor, radius = maxRadius * 0.75f, center = Offset(cx, cy), style = Stroke(width = 0.8f))
        drawCircle(gridColor, radius = maxRadius, center = Offset(cx, cy), style = Stroke(width = 1f))

        // Phase vectors
        val rays = listOf(
            Pair(Color(0xFFFFB300), 0.35f), // Yellow
            Pair(Color(0xFF00FF66), 1.15f), // Green
            Pair(Color(0xFF00E5FF), 2.10f), // Cyan
            Pair(Color(0xFFFF4081), 3.80f), // Magenta
            Pair(Color(0xFF00E5FF), 4.70f)  // Cyan 2
        )

        for ((color, angle) in rays) {
            val ex = cx + cos(angle) * maxRadius * 0.85f
            val ey = cy + sin(angle) * maxRadius * 0.85f

            drawLine(
                color = color.copy(alpha = 0.85f),
                start = Offset(cx, cy),
                end = Offset(ex, ey),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color,
                radius = 3f,
                center = Offset(ex, ey)
            )
        }

        // Center dot
        drawCircle(Color.White, radius = 3f, center = Offset(cx, cy))
    }
}

@Composable
fun DualChannelOscilloscopeCanvas(
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cy = height / 2f
        val gridColor = Color(0xFF092016)

        // CRT Graticule 8 x 6
        for (i in 0..8) {
            val gx = (width / 8f) * i
            drawLine(gridColor, Offset(gx, 0f), Offset(gx, height), strokeWidth = 0.6f)
        }
        for (i in 0..6) {
            val gy = (height / 6f) * i
            drawLine(gridColor, Offset(0f, gy), Offset(width, gy), strokeWidth = 0.6f)
        }
        // Center Axes
        drawLine(Color(0xFF0F3A27), Offset(0f, cy), Offset(width, cy), strokeWidth = 1f)
        drawLine(Color(0xFF0F3A27), Offset(width / 2f, 0f), Offset(width / 2f, height), strokeWidth = 1f)

        // CH1: Amber Wave (s_optico)
        val pathCh1 = Path()
        val pathCh2 = Path()
        val points = 80
        val omega = 4.0 * Math.PI
        val phase1 = (System.currentTimeMillis() % 3000) / 3000.0 * 2.0 * Math.PI
        val phase2 = phase1 + 0.93 // ~53.3 degrees shift

        for (i in 0..points) {
            val x = (width / points) * i
            val normX = i.toFloat() / points
            val y1 = cy + (sin(normX * omega + phase1) * (height * 0.32f)).toFloat()
            val y2 = cy + (sin(normX * omega + phase2) * (height * 0.28f)).toFloat()

            if (i == 0) {
                pathCh1.moveTo(x, y1)
                pathCh2.moveTo(x, y2)
            } else {
                pathCh1.lineTo(x, y1)
                pathCh2.lineTo(x, y2)
            }
        }

        // Draw CH2 Cyan first
        drawPath(pathCh2, Color(0xFF00E5FF).copy(alpha = 0.85f), style = Stroke(width = 1.8f))
        // Draw CH1 Amber
        drawPath(pathCh1, Color(0xFFFFB300), style = Stroke(width = 2.0f))

        // Trigger Marker cursor
        drawLine(
            Color(0xFF00FF66),
            Offset(width - 8f, cy - 14f),
            Offset(width - 2f, cy - 14f),
            strokeWidth = 2f
        )
    }
}

@Composable
fun OscilloscopeLissajousCanvas(
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val rx = width * 0.35f
        val ry = height * 0.38f

        // Grid
        drawLine(Color(0xFF0F3022), Offset(0f, cy), Offset(width, cy), strokeWidth = 0.8f)
        drawLine(Color(0xFF0F3022), Offset(cx, 0f), Offset(cx, height), strokeWidth = 0.8f)
        drawCircle(Color(0xFF0F3022), radius = ry, center = Offset(cx, cy), style = Stroke(width = 0.8f))

        // Lissajous curve
        val path = Path()
        val steps = 120
        val delta = (System.currentTimeMillis() % 4000) / 4000.0 * 2.0 * Math.PI
        for (i in 0..steps) {
            val t = (i.toDouble() / steps) * 2.0 * Math.PI
            val x = cx + (sin(2.0 * t + delta) * rx).toFloat()
            val y = cy + (sin(3.0 * t) * ry).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF00FF66), style = Stroke(width = 2.2f))

        // Trajectory active point
        val activeT = (System.currentTimeMillis() % 2000) / 2000.0 * 2.0 * Math.PI
        val px = cx + (sin(2.0 * activeT + delta) * rx).toFloat()
        val py = cy + (sin(3.0 * activeT) * ry).toFloat()
        drawCircle(Color(0xFF00E5FF), radius = 4f, center = Offset(px, py))
        drawCircle(Color.White, radius = 2f, center = Offset(px, py))
    }
}

@Composable
fun GftWaterfallCanvas(
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 5 spectral waterfall lines
        val lineColors = listOf(
            Color(0xFF00FF66),
            Color(0xFF00E5FF),
            Color(0xFFFFB300),
            Color(0xFFFF4081),
            Color(0xFF86EFAC)
        )
        val t = System.currentTimeMillis() / 250.0

        for (row in 0..4) {
            val baseY = (height * 0.22f) + (row * height * 0.16f)
            val path = Path()
            val points = 40
            for (i in 0..points) {
                val x = (width / points) * i
                val normX = i.toFloat() / points
                val peak1 = Math.exp(-Math.pow((normX - 0.25).toDouble(), 2.0) * 80.0) * 18.0
                val peak2 = Math.exp(-Math.pow((normX - 0.65).toDouble(), 2.0) * 60.0) * (14.0 + sin(t + row) * 4.0)
                val y = baseY - (peak1 + peak2).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColors[row].copy(alpha = 0.85f - row * 0.1f), style = Stroke(width = 1.5f))
        }
    }
}

@Composable
fun HolographicVsaMemoryCanvas(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cy = height / 2f

        drawLine(Color(0xFF0F3022), Offset(0f, cy), Offset(width, cy), strokeWidth = 0.8f)

        val path = Path()
        val points = 70
        for (i in 0..points) {
            val x = (width / points) * i
            val normX = (i.toFloat() / points) - 0.5f
            // Central sinc/correlation peak
            val peak = Math.exp(-Math.pow(normX.toDouble() * 6.0, 2.0)) * (height * 0.42)
            val noise = (sin(i * 1.5) * 4.0)
            val y = cy - (peak + noise).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 2f))
        // Center binding tag
        drawCircle(Color(0xFFFFB300), radius = 3.5f, center = Offset(width / 2f, cy - (height * 0.42f)))
    }
}

@Composable
fun DensityDistributionCanvas(
    nodes: List<HyperNode>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val count = nodes.size.coerceAtLeast(1)
        val barWidth = (width / count) * 0.65f
        val spacing = width / count

        for ((idx, node) in nodes.take(6).withIndex()) {
            val cx = (idx * spacing) + spacing / 2f
            val normEnergy = (node.energy / 3.0f).coerceIn(0.1f, 1.0f)
            val barHeight = height * 0.75f * normEnergy
            val topY = height - barHeight - 8f

            val nodeColor = when {
                node.label.startsWith("s_") -> Color(0xFF00FF66)
                node.label.contains("hub") || node.label.contains("monje") -> Color(0xFFFFB300)
                node.label.contains("memoria") -> Color(0xFF00E5FF)
                else -> Color(0xFFFF4081)
            }

            drawRect(
                color = nodeColor.copy(alpha = 0.8f),
                topLeft = Offset(cx - barWidth / 2f, topY),
                size = Size(barWidth, barHeight)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(cx - barWidth / 2f, topY),
                size = Size(barWidth, barHeight),
                style = Stroke(width = 0.8f)
            )
        }

        // Mean line
        drawLine(
            Color(0xFFFFB300),
            Offset(0f, height * 0.45f),
            Offset(width, height * 0.45f),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
    }
}

@Composable
fun BadgePill(text: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
