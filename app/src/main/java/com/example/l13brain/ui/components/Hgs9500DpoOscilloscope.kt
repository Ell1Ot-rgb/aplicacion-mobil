package com.example.l13brain.ui.components

import com.example.l13brain.model.HypergraphTemporalRegime
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.coroutines.delay

private data class DpoTrail(
    val time: Long,
    val edges: List<Pair<Int, Int>>,
    val positions: List<Triple<Float, Float, Float>>
)

private data class ProjectedSimplexFace(
    val indices: List<Int>,
    val points: List<Offset>,
    val avgZ: Float,
    val color: Color,
    val borderColor: Color
)

/**
 * HGS-9500DPO: Real-Time Persistent Hypergraph Oscilloscope (Industrial Metrology Grade).
 * Full-width, ultra-detailed 3D Volumetric Hypergraph Viewport matching the user's reference image:
 * - Direct in-screen header with 10 GSa/s FastAcq & TRIG'D OSD
 * - Large, prominent translucent simplicial facets (2-simplices & 3-simplices) with depth-sorted transparency
 * - Digital Phosphor (DPO) Persistence Trails with realistic decay
 * - Luminous 3D vertex spheres with specular highlights and glowing modal coronas
 * - Free 3D orbital touch drag rotation + zoom + auto-spin
 * - Tactical CNC Aluminum Deck with 5 Rotary Encoders & glowing LED halos
 * - Expandable high-resolution analytical bay (TDA Barcode & YT / FFT spectrum)
 * - Triaxial Front-End BNC connectors with IEEE 1451.4 TEDS calibration
 */
@Composable
fun Hgs9500DpoOscilloscope(
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    telemetry: TelemetryState,
    onAmalgamatedSum: () -> Unit,
    modifier: Modifier = Modifier,
    temporalRegime: HypergraphTemporalRegime = HypergraphTemporalRegime.PERSISTENTE,
    externalFiltrationEps: Float? = null
) {
    var isRunning by remember { mutableStateOf(true) }
    var isSingleArmed by remember { mutableStateOf(false) }
    var triggerMode by remember { mutableStateOf("AUTO") }
    var filtrationEps by remember { mutableFloatStateOf(0.85f) }

    LaunchedEffect(externalFiltrationEps) {
        if (externalFiltrationEps != null) {
            filtrationEps = externalFiltrationEps
        }
    }
    var persistenceTau by remember { mutableFloatStateOf(1.2f) }
    var bettiTriggerLevel by remember { mutableIntStateOf(2) }
    var hyperedgeKMax by remember { mutableIntStateOf(4) }
    var timebaseMs by remember { mutableStateOf("200 ms/div") }
    var showCrtShader by remember { mutableStateOf(true) }
    var autoOrbit by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.15f) }
    var rotX by remember { mutableFloatStateOf(24f) }
    var rotY by remember { mutableFloatStateOf(35f) }
    var activeAnalyticalTab by remember { mutableIntStateOf(-1) } // -1 = minimized, 0 = TDA Barcode, 1 = YT, 2 = FFT
    var toastText by remember { mutableStateOf<String?>(null) }

    val dpoTrails = remember { mutableStateListOf<DpoTrail>() }

    // Orbital slow rotation when active
    val infiniteTransition = rememberInfiniteTransition(label = "scope_anim")
    val animOrbit by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    // Clear old DPO trails periodically
    LaunchedEffect(isRunning, persistenceTau) {
        while (true) {
            delay(200)
            if (isRunning && nodes.isNotEmpty()) {
                val currentTime = System.currentTimeMillis()
                val currentNodes = nodes.toList()
                val currentEdges = hyperedges.toList()
                val nTotal = currentNodes.size.coerceAtLeast(1)

                val edgePairs = currentEdges.take(20).mapNotNull { edge ->
                    if (edge.nodeIds.size >= 2) {
                        val firstId = edge.nodeIds.getOrNull(0)
                        val secondId = edge.nodeIds.getOrNull(1)
                        val idx1 = currentNodes.indexOfFirst { it.id == firstId }.takeIf { it >= 0 } ?: 0
                        val idx2 = currentNodes.indexOfFirst { it.id == secondId }.takeIf { it >= 0 } ?: 0
                        idx1 to idx2
                    } else null
                }

                val posList = currentNodes.take(20).mapIndexed { i, n ->
                    val angle = (i.toFloat() / nTotal) * 6.283f + (if (autoOrbit) animOrbit * 0.017f else 0f)
                    val r = 2.6f + n.energy * 0.8f
                    Triple(cos(angle) * r, sin(angle) * r * 0.7f, sin(angle * 2.2f) * 1.3f)
                }

                if (posList.isNotEmpty()) {
                    dpoTrails.add(DpoTrail(currentTime, edgePairs, posList))
                }

                val maxAge = (persistenceTau * 1000).toLong().coerceAtLeast(200L)
                while (dpoTrails.size > 20) {
                    dpoTrails.removeAt(0)
                }
                dpoTrails.removeAll { currentTime - it.time > maxAge }
            }
        }
    }

    // Dismiss toast after delay
    LaunchedEffect(toastText) {
        if (toastText != null) {
            delay(2200)
            toastText = null
        }
    }

    // Outer Industrial Instrument Enclosure (CNC Metal Chassis)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0C1118), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFF28384C), RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. CNC Top Brand & Metrology Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141C27), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .border(1.dp, Color(0xFF1E2C3D), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Dual LED Status
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isRunning) Color(0xFF00FF66) else Color(0xFFFF3366), CircleShape)
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HGS-9500DPO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.5.sp,
                            color = Color(0xFFF1F5F9)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF062328), RoundedCornerShape(2.dp))
                                .border(0.6.dp, Color(0xFF00E5FF), RoundedCornerShape(2.dp))
                                .padding(horizontal = 3.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = "DPO HYPERGRAPHOSCOPE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                    Text(
                        text = "10 GSa/s • FastAcq 2.5M wfms/s • ISO/IEC 17025 Calibrated",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 6.5.sp,
                        color = Color(0xFF7E93A8)
                    )
                }
            }

            // Quick Hardware Header Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(if (showCrtShader) Color(0xFF09221C) else Color(0xFF111822), RoundedCornerShape(3.dp))
                        .border(0.8.dp, if (showCrtShader) Color(0xFF00FF66) else Color(0xFF28394E), RoundedCornerShape(3.dp))
                        .clickable {
                            showCrtShader = !showCrtShader
                            toastText = if (showCrtShader) "CRT SHADERS: ACTIVADO" else "F2 CLEAN: CERO ARTEFACTOS"
                        }
                        .padding(horizontal = 5.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = if (showCrtShader) "CRT ON" else "CRT OFF",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showCrtShader) Color(0xFF00FF66) else Color(0xFF7E93A8)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                        .clickable {
                            toastText = "REGISTRO METROLÓGICO HGS-9500 EXPORTADO"
                        }
                        .padding(horizontal = 6.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "SNAPSHOT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF040810)
                    )
                }
            }
        }

        // 2. Main CRT Screen Bezel (Anti-glare dark CRT with Graticule)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070B), RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF192534), RoundedCornerShape(8.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // OSD Telemetry Bar (Top of Screen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050B12), RoundedCornerShape(4.dp))
                    .border(0.8.dp, Color(0xFF0C1B2A), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isRunning) Color(0xFF00FF66) else Color(0xFFFFB300), CircleShape)
                        )
                        Text(
                            text = if (isRunning) "● TRIG'D" else "○ READY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color(0xFF00FF66) else Color(0xFFFFB300)
                        )
                    }
                    Text("10.0 GSa/s", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = Color(0xFF00E5FF))
                    Text("24.0 Mpts", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = Color(0xFF7E93A8))
                    Text("BW: 1.0 GHz", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = Color(0xFFCBD5E1))
                }

                // Topological Betti Numbers Readout (Homology Invariants)
                val b0 = (nodes.size / 5).coerceIn(1, 4)
                val b1 = hyperedges.count { it.nodeIds.size >= 3 }.coerceIn(1, 5)
                val b2 = (hyperedges.size / 4).coerceIn(0, 3)
                val euler = b0 - b1 + b2
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("β₀:$b0", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                    Text("β₁:$b1", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                    Text("β₂:$b2", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB55FE6))
                    Text("χ:$euler", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                }
            }

            // PRIMARY 3D VOLUMETRIC HYPERGRAPH VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFF020509), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF0E2230), RoundedCornerShape(6.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotY = (rotY + dragAmount.x * 0.45f) % 360f
                            rotX = (rotX - dragAmount.y * 0.45f).coerceIn(-65f, 65f)
                        }
                    }
            ) {
                // 3D Canvas Rendering Engine
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w * 0.5f
                    val cy = h * 0.52f
                    val baseScale = h * 2.5f * zoomScale

                    val currentRotY = rotY + (if (isRunning && autoOrbit) animOrbit * 0.08f else 0f)

                    // 1. Perspective Coordinate Ground Floor Grid (Y = -2.4f)
                    val gridCol = Color(0xFF0A1B28)
                    for (i in -5..5) {
                        val p1 = project3D(i * 0.9f, -2.4f, -3.2f, rotX, currentRotY, cx, cy, baseScale)
                        val p2 = project3D(i * 0.9f, -2.4f, 3.2f, rotX, currentRotY, cx, cy, baseScale)
                        drawLine(gridCol, p1.first, p2.first, strokeWidth = 0.6f)

                        val p3 = project3D(-4.5f, -2.4f, i * 0.64f, rotX, currentRotY, cx, cy, baseScale)
                        val p4 = project3D(4.5f, -2.4f, i * 0.64f, rotX, currentRotY, cx, cy, baseScale)
                        drawLine(gridCol, p3.first, p4.first, strokeWidth = 0.6f)
                    }

                    // 2. Compute 3D Coordinates of Nodes
                    val nTotal = nodes.size.coerceAtLeast(6)
                    val node3DPositions = mutableListOf<Triple<Float, Float, Float>>()
                    val projectedNodes = mutableListOf<Pair<Offset, Float>>() // Offset + Z for depth sorting

                    for (i in 0 until nTotal) {
                        val node = nodes.getOrNull(i)
                        val angle = (i.toFloat() / nTotal) * 6.283f
                        val energy = node?.energy ?: 0.5f
                        val r = 2.5f + energy * 0.7f
                        val nx = cos(angle) * r
                        val ny = sin(angle) * r * 0.62f + (if (i % 2 == 0) 0.4f else -0.4f)
                        val nz = sin(angle * 2.2f) * 1.4f

                        node3DPositions.add(Triple(nx, ny, nz))
                        val proj = project3D(nx, ny, nz, rotX, currentRotY, cx, cy, baseScale)
                        projectedNodes.add(proj)
                    }

                    // 3. Draw DPO Phosphor Persistence Trails (Magenta #FF007F)
                    val now = System.currentTimeMillis()
                    val maxAge = (persistenceTau * 1000).toLong().coerceAtLeast(200L)
                    val trailsSnapshot = try { dpoTrails.toList() } catch (e: Exception) { emptyList() }
                    trailsSnapshot.forEach { trail ->
                        val age = (now - trail.time).toFloat() / maxAge
                        val alpha = (0.42f * (1f - age)).coerceIn(0f, 0.42f)
                        if (alpha > 0.02f && trail.positions.isNotEmpty()) {
                            val trailProj = trail.positions.map {
                                project3D(it.first, it.second, it.third, rotX, currentRotY, cx, cy, baseScale)
                            }
                            trail.edges.forEach { (i1, i2) ->
                                if (i1 in trailProj.indices && i2 in trailProj.indices) {
                                    drawLine(
                                        color = Color(0xFFFF007F).copy(alpha = alpha),
                                        start = trailProj[i1].first,
                                        end = trailProj[i2].first,
                                        strokeWidth = 1.0f
                                    )
                                }
                            }
                        }
                    }

                    // 4. Generate & Render 2-Simplices & 3-Simplices (Large Translucent Colored Polygons as in image.png)
                    val facesToRender = mutableListOf<ProjectedSimplexFace>()

                    if (hyperedgeKMax >= 3 && projectedNodes.size >= 4) {
                        // Triads (2-simplices)
                        val maxIdx = projectedNodes.size - 1
                        val triadTuples = listOf(
                            Triple(0, 1.coerceAtMost(maxIdx), 3.coerceAtMost(maxIdx)),
                            Triple(1.coerceAtMost(maxIdx), 2.coerceAtMost(maxIdx), 4.coerceAtMost(maxIdx)),
                            Triple(2.coerceAtMost(maxIdx), 3.coerceAtMost(maxIdx), 5.coerceAtMost(maxIdx)),
                            Triple(0, 4.coerceAtMost(maxIdx), 2.coerceAtMost(maxIdx)),
                            Triple(3.coerceAtMost(maxIdx), 4.coerceAtMost(maxIdx), 1.coerceAtMost(maxIdx))
                        )

                        val colors = listOf(
                            Color(0x48FFAA00) to Color(0xFFFFAA00), // Amber Gold (Facet 1 - Hero Facet)
                            Color(0x3800F0FF) to Color(0xFF00F0FF), // Neon Cyan (Facet 2)
                            Color(0x3239D353) to Color(0xFF39D353), // Phosphor Emerald
                            Color(0x32B55FE6) to Color(0xFFB55FE6), // Electric Violet
                            Color(0x30FF007F) to Color(0xFFFF007F)  // Deep Magenta
                        )

                        triadTuples.forEachIndexed { fIdx, (i1, i2, i3) ->
                            if (i1 in projectedNodes.indices && i2 in projectedNodes.indices && i3 in projectedNodes.indices && i1 != i2 && i2 != i3 && i1 != i3) {
                                val p1 = projectedNodes[i1]
                                val p2 = projectedNodes[i2]
                                val p3 = projectedNodes[i3]
                                val avgZ = (p1.second + p2.second + p3.second) / 3f
                                val (fillCol, borderCol) = colors[fIdx % colors.size]
                                facesToRender.add(
                                    ProjectedSimplexFace(
                                        indices = listOf(i1, i2, i3),
                                        points = listOf(p1.first, p2.first, p3.first),
                                        avgZ = avgZ,
                                        color = fillCol,
                                        borderColor = borderCol
                                    )
                                )
                            }
                        }
                    }

                    // Depth-sort faces from back to front (Painter's Algorithm) for realistic volumetric transparency
                    facesToRender.sortByDescending { it.avgZ }

                    facesToRender.forEach { face ->
                        val path = Path().apply {
                            moveTo(face.points[0].x, face.points[0].y)
                            for (p in 1 until face.points.size) {
                                lineTo(face.points[p].x, face.points[p].y)
                            }
                            close()
                        }
                        // Volumetric Translucent Face
                        drawPath(path, face.color)
                        // Glowing Simplicial Border Line
                        drawPath(path, face.borderColor.copy(alpha = 0.75f), style = Stroke(width = 1.3f))
                    }

                    // 5. Render 1-Simplices (Connecting Edges)
                    for (i in 0 until projectedNodes.size) {
                        val next = (i + 1) % projectedNodes.size
                        val p1 = projectedNodes[i]
                        val p2 = projectedNodes[next]
                        val avgZ = (p1.second + p2.second) / 2f
                        val edgeAlpha = (0.85f - (avgZ * 0.04f)).coerceIn(0.35f, 0.95f)

                        // Primary Ring Edges (Cyan)
                        drawLine(
                            color = Color(0xFF00F0FF).copy(alpha = edgeAlpha),
                            start = p1.first,
                            end = p2.first,
                            strokeWidth = 1.4f
                        )

                        // Internal Cross-Diagonal Hyperedges (Amber/Gold)
                        if (i % 2 == 0) {
                            val cross = (i + 2) % projectedNodes.size
                            val pc = projectedNodes[cross]
                            drawLine(
                                color = Color(0xFFFFB300).copy(alpha = 0.50f),
                                start = p1.first,
                                end = pc.first,
                                strokeWidth = 1.0f
                            )
                        }
                    }

                    // 6. Render 0-Simplices (3D Luminous Vertex Spheres with Halos)
                    projectedNodes.forEachIndexed { idx, (pos, zDepth) ->
                        val node = nodes.getOrNull(idx)
                        val nEnergy = node?.energy ?: 0.5f
                        val depthRatio = (1f - (zDepth * 0.05f)).coerceIn(0.6f, 1.4f)
                        val baseR = 3.6f * depthRatio

                        val dotCol = when {
                            nEnergy > 0.75f -> Color(0xFFFF007F) // High energy: Neon Pink
                            idx % 3 == 0 -> Color(0xFFFFB300)    // Amber
                            else -> Color(0xFF00F0FF)            // Cyan
                        }

                        // Layer 1: Outer soft glow aura
                        drawCircle(dotCol.copy(alpha = 0.22f), radius = baseR * 2.8f, center = pos)
                        // Layer 2: Radiant corona
                        drawCircle(dotCol, radius = baseR, center = pos)
                        // Layer 3: Concentric white hot-spot specular highlight
                        drawCircle(Color.White, radius = baseR * 0.45f, center = Offset(pos.x - baseR * 0.25f, pos.y - baseR * 0.25f))
                    }

                    // 7. CRT Phosphor Scanline Shader Overlay
                    if (showCrtShader) {
                        var sy = 0f
                        while (sy < h) {
                            drawLine(Color(0xFF000000).copy(alpha = 0.28f), Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
                            sy += 3.5f
                        }
                    }
                }

                // Top-Left In-Screen Instrument Title & Legend (EXACTLY MATCHES image.png)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Title with status dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF00FF66), CircleShape)
                        )
                        Text(
                            text = "HGS-9500DPO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp,
                            color = Color(0xFFF1F5F9)
                        )
                        Text(
                            text = "[DPO HYPERGRAPHOSCOPE]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.5.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    // Instrument subtitle line
                    Text(
                        text = "10 GSa/s • FastAcq 2.5M wfms/s • ISO/IEC 17025",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 6.5.sp,
                        color = Color(0xFF7E93A8)
                    )

                    // Live trigger status line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(if (isRunning) Color(0xFF00FF66) else Color(0xFFFFB300), CircleShape)
                        )
                        Text(
                            text = if (isRunning) "TRIG'D 10 GSa/s | 24Mpts BW: 1.0 GHz" else "READY (STANDBY)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color(0xFF00FF66) else Color(0xFFFFB300)
                        )
                    }

                    // Legend: Live (cyan) • DPO (magenta) • Simplices (amber)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Box(modifier = Modifier.size(4.5.dp).background(Color(0xFF00F0FF), CircleShape))
                        Text("Live", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, color = Color(0xFF00F0FF))

                        Box(modifier = Modifier.size(4.5.dp).background(Color(0xFFFF007F), CircleShape))
                        Text("DPO", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, color = Color(0xFFFF007F))

                        Box(modifier = Modifier.size(4.5.dp).background(Color(0xFFFFB300), CircleShape))
                        Text("Simplices", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, color = Color(0xFFFFB300))
                    }
                }

                // Top-Right Floating 3D Control Buttons (Auto-Orbit, Zoom, Reset)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (autoOrbit) Color(0xFF082218) else Color(0xFF101722), RoundedCornerShape(3.dp))
                            .border(0.6.dp, if (autoOrbit) Color(0xFF00FF66) else Color(0xFF283A4E), RoundedCornerShape(3.dp))
                            .clickable { autoOrbit = !autoOrbit }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (autoOrbit) "⟳ AUTO" else "⏸ FIJO",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (autoOrbit) Color(0xFF00FF66) else Color(0xFF7E93A8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0C1622), RoundedCornerShape(3.dp))
                            .border(0.6.dp, Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                            .clickable { zoomScale = (zoomScale + 0.15f).coerceAtMost(2.0f) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("+", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0C1622), RoundedCornerShape(3.dp))
                            .border(0.6.dp, Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                            .clickable { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.6f) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("-", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF161026), RoundedCornerShape(3.dp))
                            .border(0.6.dp, Color(0xFFB55FE6), RoundedCornerShape(3.dp))
                            .clickable {
                                rotX = 24f
                                rotY = 35f
                                zoomScale = 1.15f
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("⌖ RESET", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2B6FF))
                    }
                }

                // Bottom-Left Floating Scale & Calibration HUD (from HTML reference)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color(0xD9020509), RoundedCornerShape(4.dp))
                        .border(0.8.dp, Color(0xFF0E2D22), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.size(4.dp).background(Color(0xFF00F0FF), CircleShape))
                        Text(
                            text = "CH1: 3D VOLUMETRIC HYPERGRAPH DPO",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF)
                        )
                    }
                    Text("FILTRATION SCALE: ${String.format(java.util.Locale.US, "%.2f", filtrationEps)} ε / DIV", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFFFFB300))
                    Text("PERSISTENCE DECAY: τ = ${String.format(java.util.Locale.US, "%.2f", persistenceTau)} s", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFFFF007F))
                    Text("CAPACITY: k-MAX = $hyperedgeKMax (${if (hyperedgeKMax >= 4) "3-Simplices" else "2-Simplices"})", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFF39D353))
                    Text("KURAMOTO COHERENCE: R = ${String.format(java.util.Locale.US, "%.3f", telemetry.kuramotoOrderR)}", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFF86EFAC))
                }

                // Bottom-Right Touch Drag Hint
                Text(
                    text = "ROT: Arrastrar 3D • ZOOM: Botones",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 6.sp,
                    color = Color(0xFF4A6275),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }

            // In-Screen Diagnostics Footer Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF04070C), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PROBE: OPTIMIZED (10X ACTIVE)", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFF39D353))
                Text("HOLDOFF: 16 ns", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFFFFB300))
                Text("MERKLE: [${telemetry.stateHash.take(8)}..]", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFFFF007F))
                Text("TEMP: 37.2°C", fontFamily = FontFamily.Monospace, fontSize = 6.sp, color = Color(0xFF7E93A8))
            }
        }

        // 3. Tactical Hardware Control Deck (CNC Aluminum Panel with Rotary Knobs & Actions)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF080D14), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1B2838), RoundedCornerShape(8.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row of 5 Rotary Encoders with Glowing Halos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScopeRotaryKnob(
                    title = "FILTRATION ε",
                    valueText = "${String.format(java.util.Locale.US, "%.2f", filtrationEps)} ε",
                    haloColor = Color(0xFF00E5FF),
                    fraction = (filtrationEps - 0.2f) / 2.0f,
                    centerSymbol = "ε",
                    onTurn = { delta ->
                        filtrationEps = (filtrationEps + delta * 0.15f).coerceIn(0.2f, 2.2f)
                    }
                )

                ScopeRotaryKnob(
                    title = "PERSISTENCE τ",
                    valueText = "${String.format(java.util.Locale.US, "%.1f", persistenceTau)} s",
                    haloColor = Color(0xFFFF007F),
                    fraction = persistenceTau / 4.0f,
                    centerSymbol = "τ",
                    onTurn = { delta ->
                        persistenceTau = (persistenceTau + delta * 0.3f).coerceIn(0.2f, 4.0f)
                    }
                )

                ScopeRotaryKnob(
                    title = "BETTI TRIG",
                    valueText = "β₁ ≥ $bettiTriggerLevel",
                    haloColor = Color(0xFF00FF66),
                    fraction = bettiTriggerLevel / 5.0f,
                    centerSymbol = "Δβ",
                    onTurn = { delta ->
                        bettiTriggerLevel = (bettiTriggerLevel + (if (delta > 0) 1 else -1)).coerceIn(1, 5)
                    }
                )

                ScopeRotaryKnob(
                    title = "k-MAX CAPACITY",
                    valueText = "k = $hyperedgeKMax",
                    haloColor = Color(0xFFFFB300),
                    fraction = (hyperedgeKMax - 2) / 2.0f,
                    centerSymbol = "k",
                    onTurn = { delta ->
                        hyperedgeKMax = if (hyperedgeKMax == 4) 2 else hyperedgeKMax + 1
                    }
                )

                ScopeRotaryKnob(
                    title = "TIMEBASE",
                    valueText = timebaseMs,
                    haloColor = Color(0xFF818CF8),
                    fraction = 0.5f,
                    centerSymbol = "ms",
                    onTurn = { delta ->
                        val options = listOf("50 ms/div", "100 ms/div", "200 ms/div", "500 ms/div", "1.0 s/div")
                        val currentIdx = options.indexOf(timebaseMs).coerceAtLeast(0)
                        val nextIdx = (currentIdx + (if (delta > 0) 1 else -1) + options.size) % options.size
                        timebaseMs = options[nextIdx]
                    }
                )
            }

            // Push-Action Buttons & Autopoietic Hot-Sum Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RUN / STOP button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isRunning) Color(0xFF0E3825) else Color(0xFF38101E), RoundedCornerShape(4.dp))
                        .border(1.dp, if (isRunning) Color(0xFF00FF66) else Color(0xFFFF3366), RoundedCornerShape(4.dp))
                        .clickable {
                            isRunning = !isRunning
                            toastText = if (isRunning) "ACQUISITION: RUN MODE" else "ACQUISITION: FROZEN [STOP]"
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRunning) "● RUN" else "■ STOP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isRunning) Color(0xFF00FF66) else Color(0xFFFF3366)
                    )
                }

                // SINGLE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2B1D08), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                        .clickable {
                            isSingleArmed = true
                            isRunning = true
                            toastText = "TRIGGER: SINGLE ARMED"
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SINGLE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                }

                // AUTO / NORM
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0A1D2B), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                        .clickable {
                            triggerMode = if (triggerMode == "AUTO") "NORM" else "AUTO"
                            toastText = "TRIGGER: $triggerMode"
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = triggerMode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                // DPO CLR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF280B1C), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFF007F), RoundedCornerShape(4.dp))
                        .clickable {
                            dpoTrails.clear()
                            toastText = "DPO MEMORY PURGED"
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DPO CLR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF007F)
                    )
                }

                // Hot-Sum Injector Button (Gradient)
                Box(
                    modifier = Modifier
                        .weight(1.6f)
                        .background(Color(0xFF221144), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFB55FE6), RoundedCornerShape(4.dp))
                        .clickable {
                            onAmalgamatedSum()
                            toastText = "⚡ HOT-SUM INYECTADA (H_Σ = H_A + H_B)"
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ INJECT HOT-SUM",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE2B6FF)
                    )
                }
            }
        }

        // 4. Metrological Analytical Bay Selector (Barcode, YT, FFT)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnalyticalTabButton("📊 TDA BARCODE", activeAnalyticalTab == 0, Color(0xFF00E5FF)) {
                activeAnalyticalTab = if (activeAnalyticalTab == 0) -1 else 0
            }
            AnalyticalTabButton("📈 YT TIMELINE", activeAnalyticalTab == 1, Color(0xFF00FF66)) {
                activeAnalyticalTab = if (activeAnalyticalTab == 1) -1 else 1
            }
            AnalyticalTabButton("🎛️ FFT SPECTRUM", activeAnalyticalTab == 2, Color(0xFFFF007F)) {
                activeAnalyticalTab = if (activeAnalyticalTab == 2) -1 else 2
            }
            if (activeAnalyticalTab >= 0) {
                Box(
                    modifier = Modifier
                        .border(0.8.dp, Color(0xFF2D3E50), RoundedCornerShape(4.dp))
                        .background(Color(0xFF0B111A), RoundedCornerShape(4.dp))
                        .clickable { activeAnalyticalTab = -1 }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text("✕ MINIMIZAR", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF7E93A8))
                }
            }
        }

        // Expandable Analytical Canvas
        if (activeAnalyticalTab >= 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF03070E), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF132A20), RoundedCornerShape(6.dp))
                    .padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (activeAnalyticalTab) {
                    0 -> {
                        // TDA Homology Persistence Barcode Canvas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TDA HOMOLOGY PERSISTENCE BARCODE (VIETORIS-RIPS)", fontFamily = FontFamily.Monospace, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            Text("ε = ${String.format(java.util.Locale.US, "%.2f", filtrationEps)}", fontFamily = FontFamily.Monospace, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                        }
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val bw = size.width
                                val bh = size.height
                                val maxEps = 2.5f

                                for (gx in 1..5) {
                                    val px = bw * (gx * 0.166f)
                                    drawLine(Color(0xFF0A1D16), Offset(px, 0f), Offset(px, bh), strokeWidth = 0.5f)
                                }

                                val h0Bars = listOf(0.0f to 0.50f, 0.0f to 0.75f, 0.0f to 1.10f, 0.0f to 1.85f, 0.0f to 2.45f)
                                val h1Bars = listOf(0.35f to 0.85f, 0.58f to 1.40f, 0.75f to 1.95f, 1.15f to 2.15f)
                                val h2Bars = listOf(0.80f to 1.55f, 1.05f to 1.80f)

                                var curY = 4f
                                val barH = 4.0f
                                val gap = 3.5f

                                h0Bars.forEach { (b, d) ->
                                    val x1 = (b / maxEps) * bw
                                    val x2 = (d / maxEps) * bw
                                    drawRect(Color(0xFFFFB300), Offset(x1, curY), Size(x2 - x1, barH))
                                    curY += barH + gap
                                }
                                curY += 2f
                                h1Bars.forEach { (b, d) ->
                                    val x1 = (b / maxEps) * bw
                                    val x2 = (d / maxEps) * bw
                                    drawRect(Color(0xFF00E5FF), Offset(x1, curY), Size(x2 - x1, barH))
                                    curY += barH + gap
                                }
                                curY += 2f
                                h2Bars.forEach { (b, d) ->
                                    val x1 = (b / maxEps) * bw
                                    val x2 = (d / maxEps) * bw
                                    drawRect(Color(0xFFB55FE6), Offset(x1, curY), Size(x2 - x1, barH))
                                    curY += barH + gap
                                }

                                val cursorX = (filtrationEps / maxEps) * bw
                                drawLine(Color(0xFFFFB300), Offset(cursorX, 0f), Offset(cursorX, bh), strokeWidth = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 2f)))
                                drawCircle(Color(0xFFFFB300), radius = 2.5f, center = Offset(cursorX, 3f))
                            }
                        }
                    }
                    1 -> {
                        // YT Topological Time Series Timeline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOPOLOGICAL TIME SERIES (YT MODE)", fontFamily = FontFamily.Monospace, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                            Text(timebaseMs, fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = Color(0xFFCBD5E1))
                        }
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sw = size.width
                                val sh = size.height
                                for (i in 1..3) {
                                    val gy = sh * (i * 0.25f)
                                    drawLine(Color(0xFF0A1D16), Offset(0f, gy), Offset(sw, gy), strokeWidth = 0.5f)
                                }
                                val pB0 = Path().apply {
                                    moveTo(0f, sh * 0.72f); lineTo(sw * 0.35f, sh * 0.72f); lineTo(sw * 0.35f, sh * 0.55f)
                                    lineTo(sw * 0.65f, sh * 0.55f); lineTo(sw * 0.65f, sh * 0.72f); lineTo(sw, sh * 0.72f)
                                }
                                drawPath(pB0, Color(0xFFFFB300), style = Stroke(1.4f))

                                val pB1 = Path().apply {
                                    moveTo(0f, sh * 0.45f); lineTo(sw * 0.45f, sh * 0.45f); lineTo(sw * 0.45f, sh * 0.25f)
                                    lineTo(sw * 0.85f, sh * 0.25f); lineTo(sw, sh * 0.40f)
                                }
                                drawPath(pB1, Color(0xFF00E5FF), style = Stroke(1.4f))

                                val pEuler = Path().apply {
                                    moveTo(0f, sh * 0.88f); lineTo(sw * 0.5f, sh * 0.88f); lineTo(sw * 0.5f, sh * 0.78f); lineTo(sw, sh * 0.78f)
                                }
                                drawPath(pEuler, Color(0xFF00FF66), style = Stroke(1.2f))
                            }
                        }
                    }
                    2 -> {
                        // MATH FFT Spectrum Canvas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MATH FFT MAG (dBV) - HANNING WINDOWED [χ FLUX]", fontFamily = FontFamily.Monospace, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF007F))
                            Text("ENOB: 11.4b | SINAD: 70.4dB", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, color = Color(0xFF7E93A8))
                        }
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sw = size.width
                                val sh = size.height
                                val bins = 32
                                val bw = sw / bins
                                for (b in 0 until bins) {
                                    val f = b.toFloat() / bins
                                    val peak = kotlin.math.exp(-((f - 0.28f) * (f - 0.28f)) / 0.018f)
                                    val noise = ((b * 17) % 7) * 0.025f
                                    val mag = (peak * 0.88f + noise).coerceIn(0.05f, 0.95f)
                                    val bHeight = mag * (sh - 4f)
                                    val bCol = if (b in 8..10) Color(0xFFFF007F) else Color(0xFF00E5FF).copy(alpha = 0.75f)
                                    drawRect(bCol, Offset(b * bw + 1f, sh - bHeight), Size(bw - 1.5f, bHeight))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Front-End Triaxial BNC Connectors & Active Sockets Bay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF06090E), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .border(1.dp, Color(0xFF141E2B), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CH 1 BNC Jack
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .border(1.2.dp, Color(0xFF00E5FF), CircleShape)
                            .background(Color(0xFF071B24), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(3.dp).background(Color(0xFFFFB300), CircleShape))
                    }
                    Text("CH1 TDA", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                }

                // CH 2 BNC Jack
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .border(1.2.dp, Color(0xFFFF007F), CircleShape)
                            .background(Color(0xFF220817), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(3.dp).background(Color(0xFFFFB300), CircleShape))
                    }
                    Text("CH2 DPO", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF007F))
                }

                // EXT TRIG Jack
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .border(1.2.dp, Color(0xFF00FF66), CircleShape)
                            .background(Color(0xFF081C12), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(3.dp).background(Color(0xFF7E93A8), CircleShape))
                    }
                    Text("EXT TRIG", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                }
            }

            // Metrology Accreditation Badge
            Text(
                text = "ISO/IEC 17025 • v5.12-AUTOPOIETIC",
                fontFamily = FontFamily.Monospace,
                fontSize = 6.sp,
                color = Color(0xFF5B786D)
            )
        }

        // Real-Time Action Notification Toast HUD
        toastText?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF071C14), RoundedCornerShape(4.dp))
                    .border(0.8.dp, Color(0xFF00FF66), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = msg,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF66)
                )
            }
        }
    }
}

@Composable
private fun AnalyticalTabButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(0.8.dp, if (isSelected) color else Color(0xFF1B2B3C), RoundedCornerShape(4.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF080E16), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 7.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else Color(0xFF88A0B2)
        )
    }
}

/**
 * Tactical Rotary Encoder with Knurled Metallic Finish and Glowing LED Halo Ring.
 */
@Composable
private fun ScopeRotaryKnob(
    title: String,
    valueText: String,
    haloColor: Color,
    fraction: Float,
    centerSymbol: String,
    onTurn: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { onTurn(0.5f) }
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Bold,
            color = haloColor
        )

        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            // Halo Ring Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 2f
                val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
                // Track
                drawArc(
                    color = Color(0xFF141F2C),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                )
                // Active Halo
                val sweep = (fraction.coerceIn(0.05f, 1.0f)) * 270f
                drawArc(
                    color = haloColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                )
            }

            // Dial Face
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFF101722), CircleShape)
                    .border(0.8.dp, Color(0xFF283A4E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = centerSymbol,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = haloColor
                )
            }
        }

        // Value Badge
        Box(
            modifier = Modifier
                .background(Color(0xFF05080E), RoundedCornerShape(2.dp))
                .border(0.5.dp, Color(0xFF152230), RoundedCornerShape(2.dp))
                .padding(horizontal = 3.dp, vertical = 0.5.dp)
        ) {
            Text(
                text = valueText,
                fontFamily = FontFamily.Monospace,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Bold,
                color = haloColor
            )
        }
    }
}

/**
 * 3D-to-2D Perspective Projection Helper
 * Returns Pair(Offset, Z_camera) where Z_camera can be used for depth sorting.
 */
private fun project3D(
    x: Float,
    y: Float,
    z: Float,
    rotX: Float,
    rotY: Float,
    cx: Float,
    cy: Float,
    scale: Float
): Pair<Offset, Float> {
    val radX = Math.toRadians(rotX.toDouble()).toFloat()
    val radY = Math.toRadians(rotY.toDouble()).toFloat()

    // Rotation around Y
    val x1 = x * cos(radY) + z * sin(radY)
    val z1 = -x * sin(radY) + z * cos(radY)

    // Rotation around X
    val y2 = y * cos(radX) - z1 * sin(radX)
    val z2 = y * sin(radX) + z1 * cos(radX)

    val distance = 26f
    val fov = scale / (distance + z2).coerceAtLeast(6f)
    return Pair(Offset(cx + x1 * fov, cy - y2 * fov), z2)
}
