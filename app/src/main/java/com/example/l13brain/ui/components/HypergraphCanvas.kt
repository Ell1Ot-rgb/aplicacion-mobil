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
import androidx.compose.ui.graphics.toArgb
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
        // 1. Frame Header: "┌─ TOPOSCOPIO L13 // DOMINIO MIXTO" + MERKLE Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "┌─ TOPOSCOPIO L13 // DOMINIO MIXTO",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color(0xFF00FF66),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF071A12), RoundedCornerShape(4.dp))
                    .border(0.8.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "MERKLE: [${telemetry.stateHash.take(8)}..]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    color = Color(0xFFFFB300),
                    maxLines = 1
                )
            }
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

                    BadgePill(text = "CH1: s_optico", color = Color(0xFFFFB300), bg = Color(0xFF241505))
                    BadgePill(text = "CH2: s_optico", color = Color(0xFF00E5FF), bg = Color(0xFF051C24))
                    BadgePill(text = "DC", color = Color(0xFF00FF66), bg = Color(0xFF061A12))
                }

                BadgePill(text = "TRIG: AUTO ●", color = Color(0xFF00FF66), bg = Color(0xFF061A12))
            }

            // Filter Subtabs: 1. MORFOLOGÍA | 2. DIRAC S¹ | 3. GFT WATERFALL | 4. TRIGGER CAUSAL | 5. MEMORIA VSA | 6. DENSIDAD
            val subtabs = listOf(
                "1. MORFOLOGÍA",
                "2. DIRAC S¹",
                "3. GFT WATERFALL",
                "4. TRIGGER CAUSAL",
                "5. MEMORIA VSA",
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
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[TOPOLOGÍA: k=3..4]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = Color(0xFF00FF66)
            )
            Text(
                text = "[ARISTAS: ${hyperedges.size}]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = Color(0xFF00FF66)
            )
            Text(
                text = "[CONV: ${String.format(java.util.Locale.US, "%.1f", telemetry.convergencePct)}%]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = Color(0xFF00FF66)
            )
            Text(
                text = "[VIEW: EULER HULL]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = Color(0xFF00FF66)
            )
            Text(
                text = "[ZOOM: 100%]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
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
    val nodePositions = listOf(
        Offset(width * 0.12f, height * 0.27f),
        Offset(width * 0.11f, height * 0.70f),
        Offset(width * 0.48f, height * 0.27f),
        Offset(width * 0.88f, height * 0.27f),
        Offset(width * 0.50f, height * 0.71f),
        Offset(width * 0.88f, height * 0.71f)
    )
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

    if (hyperedges.isEmpty() || nodes.isEmpty()) {
        // Fallback default visualization if lists are empty
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
        drawTagBox(
            text = "e1_sensorial (k=3) • DPO L=89%",
            center = Offset(width * 0.24f, height * 0.40f),
            textColor = Color(0xFF00FF66),
            borderColor = Color(0xFF00FF66),
            bgColor = Color(0xFF04180E)
        )
        return
    }

    // Dynamic rendering of all active hyperedges
    for (edge in hyperedges) {
        val memberNodes = edge.nodeIds.mapNotNull { nodeMap[it] }
        if (memberNodes.isEmpty()) continue

        val memberPoints = memberNodes.map { node ->
            Offset(
                node.x.coerceIn(0.08f, 0.92f) * width,
                node.y.coerceIn(0.12f, 0.88f) * height
            )
        }

        val edgeColor = Color(edge.colorHex.toInt())
        val avgX = memberPoints.map { it.x }.average().toFloat()
        val avgY = memberPoints.map { it.y }.average().toFloat()
        val centroid = Offset(avgX, avgY)

        val minX = memberPoints.minOf { it.x }
        val maxX = memberPoints.maxOf { it.x }
        val minY = memberPoints.minOf { it.y }
        val maxY = memberPoints.maxOf { it.y }

        val padX = 42f
        val padY = 34f
        val boxLeft = (minX - padX).coerceAtLeast(8f)
        val boxTop = (minY - padY).coerceAtLeast(8f)
        val boxWidth = ((maxX - minX) + padX * 2f).coerceAtMost(width - boxLeft - 8f)
        val boxHeight = ((maxY - minY) + padY * 2f).coerceAtMost(height - boxTop - 8f)

        // Draw bounding polyadic envelope / ellipse
        val glowAlpha = (0.07f * edge.phosphorLuminance * (0.8f + pulseAlpha * 0.4f)).coerceIn(0.04f, 0.22f)
        drawOval(
            color = edgeColor.copy(alpha = glowAlpha),
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight)
        )
        drawOval(
            color = edgeColor.copy(alpha = (0.75f * edge.phosphorLuminance).coerceIn(0.4f, 0.95f)),
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            style = Stroke(width = (2.0f * edge.afterglowTrailRadius).coerceIn(1.5f, 3.5f))
        )

        // Laser lines from centroid to all member vertices
        for (pt in memberPoints) {
            drawLine(
                color = edgeColor.copy(alpha = 0.45f * edge.phosphorLuminance),
                start = centroid,
                end = pt,
                strokeWidth = 1.2f
            )
        }

        // Draw Tag Box at centroid
        val dpoPct = (edge.phosphorLuminance * 100).toInt()
        val tagText = "${edge.label} (k=${edge.nodeIds.size}) • W=${String.format(java.util.Locale.US, "%.2f", edge.weight)}"
        val tagCenter = Offset(
            centroid.x.coerceIn(80f, width - 80f),
            (centroid.y - 18f).coerceIn(24f, height - 24f)
        )
        drawTagBox(
            text = tagText,
            center = tagCenter,
            textColor = edgeColor,
            borderColor = edgeColor,
            bgColor = Color(0xFF030A12)
        )
    }
}

private fun DrawScope.drawTagBox(
    text: String,
    center: Offset,
    textColor: Color,
    borderColor: Color,
    bgColor: Color
) {
    val paint = android.graphics.Paint().apply {
        color = textColor.toArgb()
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
    val displayNodes = if (nodes.isNotEmpty()) {
        nodes.map { node ->
            val cx = node.x.coerceIn(0.08f, 0.92f) * width
            val cy = node.y.coerceIn(0.12f, 0.88f) * height
            val isHub = node.id.contains("hub", ignoreCase = true) || node.label.contains("hub", ignoreCase = true) || node.label.contains("monje", ignoreCase = true)
            val isSensory = node.modalState.contains("Sensorial", ignoreCase = true) || node.id.startsWith("s_")
            val isS4 = node.modalState.contains("S4", ignoreCase = true) || node.id.contains("s4", ignoreCase = true)
            val isMemory = node.id.contains("memoria", ignoreCase = true)

            val color = when {
                isSensory -> Color(0xFF00FF66)
                isS4 -> Color(0xFFFF4081)
                isMemory -> Color(0xFF00E5FF)
                isHub -> Color(0xFFFFB300)
                else -> Color(0xFF00E5FF)
            }

            val star = if (isHub) " ★" else ""
            val label = "${node.label} [${String.format(java.util.Locale.US, "%.2f", node.energy)} J]$star"
            NodeVisual(
                id = node.id,
                label = label,
                pos = Offset(cx, cy),
                color = color,
                isStar = isHub,
                isSelected = (node.id == selectedNodeId)
            )
        }
    } else {
        listOf(
            NodeVisual("v1", "s_optico [0.19 J]", Offset(width * 0.12f, height * 0.27f), Color(0xFF00FF66), false, false),
            NodeVisual("v2", "s_acustico [0.19 J]", Offset(width * 0.11f, height * 0.70f), Color(0xFF00FF66), false, false),
            NodeVisual("v3", "HUB_CENTRAL [0.19 J] ★", Offset(width * 0.48f, height * 0.27f), Color(0xFFFFB300), true, selectedNodeId == "v3" || selectedNodeId == "hub_central"),
            NodeVisual("v4", "COG_MONJE [0.19 J] ★", Offset(width * 0.88f, height * 0.27f), Color(0xFFFFB300), true, false),
            NodeVisual("v5", "cog_memoria [0.19 J]", Offset(width * 0.50f, height * 0.71f), Color(0xFF00E5FF), false, false),
            NodeVisual("v6", "cog_s4 [0.19 J]", Offset(width * 0.88f, height * 0.71f), Color(0xFFFF4081), false, false)
        )
    }

    val labelPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 17f
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    for (node in displayNodes) {
        val cx = node.pos.x
        val cy = node.pos.y

        // Reticle for selected node
        if (node.isSelected) {
            val reticleRadius = 24f + pulseAlpha * 6f
            drawCircle(
                color = Color(0xFFFFB300).copy(alpha = 0.8f),
                radius = reticleRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
            // Crosshairs
            drawLine(Color(0xFFFFB300), Offset(cx - reticleRadius - 4f, cy), Offset(cx - reticleRadius + 4f, cy), strokeWidth = 1.5f)
            drawLine(Color(0xFFFFB300), Offset(cx + reticleRadius - 4f, cy), Offset(cx + reticleRadius + 4f, cy), strokeWidth = 1.5f)
            drawLine(Color(0xFFFFB300), Offset(cx, cy - reticleRadius - 4f), Offset(cx, cy - reticleRadius + 4f), strokeWidth = 1.5f)
            drawLine(Color(0xFFFFB300), Offset(cx, cy + reticleRadius - 4f), Offset(cx, cy + reticleRadius + 4f), strokeWidth = 1.5f)
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

        // Label box beneath or above node
        val isBottomNode = cy > height * 0.72f
        val tagY = if (isBottomNode) cy - 22f else cy + 20f

        val textWidth = labelPaint.measureText(node.label)
        val boxWidth = textWidth + 12f
        val boxHeight = 20f

        drawRect(
            color = Color(0xFF050E17),
            topLeft = Offset(cx - boxWidth / 2f, tagY - boxHeight / 2f),
            size = Size(boxWidth, boxHeight)
        )
        drawRect(
            color = node.color.copy(alpha = 0.6f),
            topLeft = Offset(cx - boxWidth / 2f, tagY - boxHeight / 2f),
            size = Size(boxWidth, boxHeight),
            style = Stroke(width = 0.8f)
        )

        labelPaint.color = node.color.toArgb()
        drawContext.canvas.nativeCanvas.drawText(
            node.label,
            cx,
            tagY + 5f,
            labelPaint
        )
    }
}

private data class NodeVisual(
    val id: String,
    val label: String,
    val pos: Offset,
    val color: Color,
    val isStar: Boolean,
    val isSelected: Boolean
)

@Composable
fun DiracPolarRadarCanvas(
    nodes: List<HyperNode>,
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

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

        // Rotating radar sweep line
        val sweepX = cx + cos(sweepAngle) * maxRadius
        val sweepY = cy + sin(sweepAngle) * maxRadius
        drawLine(
            color = Color(0xFF00FF66).copy(alpha = 0.7f),
            start = Offset(cx, cy),
            end = Offset(sweepX, sweepY),
            strokeWidth = 1.8f
        )

        // Phase vectors for actual nodes
        val activeNodes = if (nodes.isNotEmpty()) nodes.take(8) else listOf(
            HyperNode("v1", "s_optico", 1.1f, 0f, 0f, phase = 0.35f),
            HyperNode("v2", "s_acustico", 0.9f, 0f, 0f, phase = 1.15f),
            HyperNode("v3", "hub", 1.2f, 0f, 0f, phase = 2.10f),
            HyperNode("v4", "monje", 1.4f, 0f, 0f, phase = 3.80f),
            HyperNode("v5", "s4", 0.8f, 0f, 0f, phase = 4.70f)
        )

        for (node in activeNodes) {
            val nodeAngle = node.phase
            val rLen = maxRadius * (0.50f + (node.energy / 3.0f).coerceIn(0.1f, 0.45f))
            val ex = cx + cos(nodeAngle) * rLen
            val ey = cy + sin(nodeAngle) * rLen

            val vecColor = when {
                node.modalState.contains("Sensorial") || node.label.startsWith("s_") -> Color(0xFF00FF66)
                node.modalState.contains("S4") -> Color(0xFFFF4081)
                node.label.contains("hub") || node.label.contains("monje") -> Color(0xFFFFB300)
                else -> Color(0xFF00E5FF)
            }

            drawLine(
                color = vecColor.copy(alpha = 0.85f),
                start = Offset(cx, cy),
                end = Offset(ex, ey),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = vecColor,
                radius = 3.5f,
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
    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscope_anim")
    val phaseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnim"
    )

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

        // CH1: Amber Wave & CH2: Cyan Wave
        val pathCh1 = Path()
        val pathCh2 = Path()
        val points = 80
        val omega = 4.0 * Math.PI
        val rFactor = telemetry.kuramotoOrderR.coerceIn(0.2f, 1.2f)
        val phase1 = phaseAnim
        val phase2 = phaseAnim + (0.93 * (2.0 - rFactor))

        for (i in 0..points) {
            val x = (width / points) * i
            val normX = i.toFloat() / points
            val y1 = cy + (sin(normX * omega + phase1) * (height * 0.32f * rFactor)).toFloat()
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
    val infiniteTransition = rememberInfiniteTransition(label = "lissajous_anim")
    val deltaAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "deltaAnim"
    )

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
        val delta = deltaAnim.toDouble()
        for (i in 0..steps) {
            val t = (i.toDouble() / steps) * 2.0 * Math.PI
            val x = cx + (sin(2.0 * t + delta) * rx).toFloat()
            val y = cy + (sin(3.0 * t) * ry).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF00FF66), style = Stroke(width = 2.2f))

        // Trajectory active point
        val activeT = (delta * 1.5) % (2.0 * Math.PI)
        val px = cx + (sin(2.0 * activeT + delta) * rx).toFloat()
        val py = cy + (sin(3.0 * activeT) * ry).toFloat()
        drawCircle(Color(0xFF00E5FF), radius = 4.5f, center = Offset(px, py))
        drawCircle(Color.White, radius = 2f, center = Offset(px, py))
    }
}

@Composable
fun GftWaterfallCanvas(
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waterfall_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

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

        for (row in 0..4) {
            val baseY = (height * 0.22f) + (row * height * 0.16f)
            val path = Path()
            val points = 40
            for (i in 0..points) {
                val x = (width / points) * i
                val normX = i.toFloat() / points
                val peak1 = Math.exp(-Math.pow((normX - 0.25).toDouble(), 2.0) * 80.0) * 18.0
                val peak2 = Math.exp(-Math.pow((normX - 0.65).toDouble(), 2.0) * 60.0) * (14.0 + sin(waveOffset + row) * 4.0)
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
    val infiniteTransition = rememberInfiniteTransition(label = "holographic_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

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
            val peak = Math.exp(-Math.pow(normX.toDouble() * 6.0, 2.0)) * (height * (0.38 + sin(pulse) * 0.04))
            val noise = (sin(i * 1.5 + pulse) * 3.5)
            val y = cy - (peak + noise).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 2f))
        // Center binding tag
        drawCircle(Color(0xFFFFB300), radius = 3.5f, center = Offset(width / 2f, cy - (height * 0.40f)))
    }
}

@Composable
fun DensityDistributionCanvas(
    nodes: List<HyperNode>,
    modifier: Modifier = Modifier
) {
    val displayNodes = if (nodes.isNotEmpty()) nodes.take(6) else listOf(
        HyperNode("v1", "s_optico", 1.1f, 0f, 0f),
        HyperNode("v2", "s_acustico", 0.9f, 0f, 0f),
        HyperNode("v3", "hub", 1.25f, 0f, 0f),
        HyperNode("v4", "monje", 1.4f, 0f, 0f),
        HyperNode("v5", "s4", 0.85f, 0f, 0f),
        HyperNode("v6", "memoria", 0.5f, 0f, 0f)
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val count = displayNodes.size.coerceAtLeast(1)
        val spacing = width / count
        val barWidth = spacing * 0.65f

        val textPaint = android.graphics.Paint().apply {
            textSize = 15f
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }

        for ((idx, node) in displayNodes.withIndex()) {
            val cx = (idx * spacing) + spacing / 2f
            val normEnergy = (node.energy / 2.5f).coerceIn(0.12f, 1.0f)
            val barHeight = (height * 0.62f) * normEnergy
            val topY = height - barHeight - 18f

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
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(cx - barWidth / 2f, topY),
                size = Size(barWidth, barHeight),
                style = Stroke(width = 0.8f)
            )

            // Value text above bar
            textPaint.color = Color.White.toArgb()
            val valText = String.format(java.util.Locale.US, "%.2f", node.energy)
            drawContext.canvas.nativeCanvas.drawText(valText, cx, (topY - 4f).coerceAtLeast(12f), textPaint)

            // Short label below bar
            textPaint.color = nodeColor.toArgb()
            val shortLabel = node.label.replace("s_", "").replace("cog_", "").take(7)
            drawContext.canvas.nativeCanvas.drawText(shortLabel, cx, height - 2f, textPaint)
        }

        // Mean threshold line
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
