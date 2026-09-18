package com.example.l13brain.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.l13brain.model.HypergraphTemporalRegime
import com.example.l13brain.model.PersistenceBarcodeInterval
import com.example.l13brain.model.TelemetryState
import com.example.l13brain.ui.theme.CalcBezel
import com.example.l13brain.ui.theme.CalcBevelBorder
import com.example.l13brain.ui.theme.CalcBorderSubtle
import com.example.l13brain.ui.theme.CalcChassis
import com.example.l13brain.ui.theme.CalcKey2ndBg
import com.example.l13brain.ui.theme.CalcKeyAlphaBg
import com.example.l13brain.ui.theme.CalcKeyEnterBg
import com.example.l13brain.ui.theme.CalcKeyNumBg
import com.example.l13brain.ui.theme.CalcKeyNumBorder
import com.example.l13brain.ui.theme.CalcKeyVarBg
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
import com.example.l13brain.ui.theme.CalcSoftkeyActiveText
import com.example.l13brain.ui.theme.CalcSoftkeyBg
import com.example.l13brain.ui.theme.CalcSoftkeyBorder
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
    temporalRegime: HypergraphTemporalRegime = HypergraphTemporalRegime.PERSISTENTE,
    filtrationEpsilon: Float = 0.85f,
    isSweepActive: Boolean = false,
    betti0: Int = 2,
    betti1: Int = 1,
    betti2: Int = 1,
    persistenceIntervals: List<PersistenceBarcodeInterval> = emptyList(),
    onRegimeChanged: (HypergraphTemporalRegime) -> Unit = {},
    onEpsilonChanged: (Float) -> Unit = {},
    onToggleSweep: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableIntStateOf(2) } // 0: 2D Euler, 1: 3D DPO, 2: Ambos (Split)
    var showDensityOverlay by remember { mutableStateOf(false) }
    var showBarcodePanel by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Frame Header: "┌─ PLOT 1: TOPOSCOPIO // Y₁(x, y)" + View Mode Switcher + MERKLE Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "┌─ PLOT 1: TOPOSCOPIO // Y₁(x, y)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = CalcPlotY1,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // View Mode Selector: 2D / 3D / AMBOS (Graphing Calculator View Softkeys)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0 to "2D", 1 to "3D", 2 to "AMBOS").forEach { (modeIdx, modeLabel) ->
                    val isModeSelected = (viewMode == modeIdx)
                    Box(
                        modifier = Modifier
                            .background(
                                if (isModeSelected) CalcSoftkeyActiveBg else CalcSoftkeyBg,
                                RoundedCornerShape(3.dp)
                            )
                            .border(
                                0.8.dp,
                                if (isModeSelected) CalcSoftkeyActiveBorder else CalcSoftkeyBorder,
                                RoundedCornerShape(3.dp)
                            )
                            .clickable { viewMode = modeIdx }
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = modeLabel,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = if (isModeSelected) CalcSoftkeyActiveText else CalcLcdTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .background(CalcKey2ndBg, RoundedCornerShape(4.dp))
                    .border(0.8.dp, CalcPlotY2, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "MERKLE: [${telemetry.stateHash.take(8)}..]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    color = CalcPlotY2,
                    maxLines = 1
                )
            }
        }

        // 2. Temporal Regime Selector Bar (ESTÁTICO | CONTINUO | PERSISTENTE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalcBezel, RoundedCornerShape(6.dp))
                .border(1.dp, CalcBorderSubtle, RoundedCornerShape(6.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RÉGIMEN:",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = CalcLcdTextMuted,
                modifier = Modifier.padding(start = 4.dp, end = 6.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HypergraphTemporalRegime.values().forEach { regime ->
                    val isSel = (regime == temporalRegime)
                    val activeColor = when (regime) {
                        HypergraphTemporalRegime.ESTATICO -> CalcPlotY6
                        HypergraphTemporalRegime.CONTINUO -> CalcPlotY3
                        HypergraphTemporalRegime.PERSISTENTE -> CalcPlotY1
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSel) activeColor.copy(alpha = 0.18f) else CalcLcdBackground,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (isSel) activeColor else CalcBorderSubtle,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onRegimeChanged(regime) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = regime.shortName,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.5.sp,
                            color = if (isSel) activeColor else CalcLcdTextMuted
                        )
                    }
                }
            }
        }

        // 3. Dynamic TDA Filtration & Persistent Homology Bar (when in PERSISTENTE regime)
        if (temporalRegime == HypergraphTemporalRegime.PERSISTENTE) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalcLcdBackground, RoundedCornerShape(6.dp))
                    .border(1.dp, CalcBorderSubtle, RoundedCornerShape(6.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Sweep
                    Box(
                        modifier = Modifier
                            .background(if (isSweepActive) CalcKey2ndBg else CalcSoftkeyBg, RoundedCornerShape(4.dp))
                            .border(1.dp, if (isSweepActive) CalcPlotY2 else CalcPlotY1, RoundedCornerShape(4.dp))
                            .clickable { onToggleSweep() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isSweepActive) "⏸ PAUSAR ε" else "▶ BARRIDO ε",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            color = if (isSweepActive) CalcPlotY2 else CalcPlotY1
                        )
                    }

                    Text(
                        text = "FILTRACIÓN: ε=${String.format(java.util.Locale.US, "%.2f", filtrationEpsilon)}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = CalcPlotY1
                    )

                    // Betti Numbers Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(
                            modifier = Modifier
                                .background(CalcKeyVarBg, RoundedCornerShape(3.dp))
                                .border(0.8.dp, CalcPlotY1, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("β₀=$betti0", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY1)
                        }
                        Box(
                            modifier = Modifier
                                .background(CalcKey2ndBg, RoundedCornerShape(3.dp))
                                .border(0.8.dp, CalcPlotY2, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("β₁=$betti1", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY2)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF381418), RoundedCornerShape(3.dp))
                                .border(0.8.dp, CalcPlotY4, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("β₂=$betti2", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = CalcPlotY4)
                        }
                    }

                    // Barcode Drawer Toggle
                    Box(
                        modifier = Modifier
                            .background(if (showBarcodePanel) CalcSoftkeyActiveBg else CalcSoftkeyBg, RoundedCornerShape(4.dp))
                            .border(1.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { showBarcodePanel = !showBarcodePanel }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showBarcodePanel) "BARCODE ▲" else "BARCODE ▼",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = if (showBarcodePanel) CalcSoftkeyActiveText else CalcLcdTextMuted
                        )
                    }
                }

                // Epsilon Slider
                Slider(
                    value = filtrationEpsilon,
                    onValueChange = onEpsilonChanged,
                    valueRange = 0.0f..2.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = CalcPlotY1,
                        activeTrackColor = CalcPlotY1,
                        inactiveTrackColor = CalcLcdGrid
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                )

                // Expandable Barcode Interval Viewer
                AnimatedVisibility(visible = showBarcodePanel) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CalcLcdBackground, RoundedCornerShape(4.dp))
                            .border(0.8.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HOMOLOGÍA PERSISTENTE // INTERVALOS [NACIMIENTO -> MUERTE]:",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = CalcPlotY1
                        )
                        persistenceIntervals.forEach { interval ->
                            val isAlive = (filtrationEpsilon >= interval.birth && filtrationEpsilon <= interval.death)
                            val barColor = when (interval.dimension) {
                                0 -> CalcPlotY1
                                1 -> CalcPlotY2
                                else -> CalcPlotY4
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${interval.label} [${interval.birth}..${interval.death}]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.5.sp,
                                    color = if (isAlive) barColor else CalcLcdTextMuted,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (isAlive) "● ACTIVO" else "○ INACTIVO",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAlive) barColor else CalcLcdTextMuted
                                )
                            }
                        }
                    }
                }
            }
        } else if (temporalRegime == HypergraphTemporalRegime.ESTATICO) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalcLcdBackground, RoundedCornerShape(4.dp))
                    .border(0.8.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📸 CORTE t₀ // INVARIANTE TOPOLÓGICO CONGELADO (SIN OSCILACIÓN TEMPORAL)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = CalcPlotY6
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalcLcdBackground, RoundedCornerShape(4.dp))
                    .border(0.8.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⚡ DINÁMICA CONTINUA // KURAMOTO FLOW [R = ${String.format(java.util.Locale.US, "%.3f", telemetry.kuramotoOrderR)}] & DIFUSIÓN LAPLACIANA EN VIVO",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = CalcPlotY3
                )
            }
        }

        // 4. Main Hypergraph Canvas Card (Convex Euler Ellipses & Bridge)
        if (viewMode == 0 || viewMode == 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(CalcLcdBackground, RoundedCornerShape(8.dp))
                    .border(1.2.dp, CalcBevelBorder, RoundedCornerShape(8.dp))
            ) {
                HypergraphCanvas(
                    nodes = nodes,
                    hyperedges = hyperedges,
                    selectedNodeId = selectedNodeId,
                    showDensityOverlay = showDensityOverlay,
                    temporalRegime = temporalRegime,
                    filtrationEpsilon = filtrationEpsilon,
                    onNodeSelected = onNodeSelected,
                    onNodeDragged = onNodeDragged,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-right floating button: "📊 DENSIDAD DE OCUPACIÓN"
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .border(1.dp, if (showDensityOverlay) CalcPlotY1 else CalcBorderSubtle, RoundedCornerShape(4.dp))
                        .background(if (showDensityOverlay) CalcKeyVarBg else CalcSoftkeyBg, RoundedCornerShape(4.dp))
                        .clickable { showDensityOverlay = !showDensityOverlay }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (showDensityOverlay) "📊 DENSIDAD [ON]" else "📊 DENSIDAD DE OCUPACIÓN",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showDensityOverlay) CalcPlotY1 else CalcLcdTextMuted
                    )
                }
            }
        }

        // 5. Quick Topology Action Pills (Styled as Graphing Calculator Function Keys)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TopGraphActionPill("⚡ SUMA AMALGAMADA", CalcPlotY1, onAmalgamatedSum)
            TopGraphActionPill("🔄 DUAL DE BERGE", CalcPlotY2, onBergeDual)
            TopGraphActionPill("🌀 PASO AUTOPOIÉTICO", CalcPlotY3, onAutopoieticStep)
            TopGraphActionPill("⚡ +0.50J INYECTAR", CalcPlotY4, onInjectEnergy)
            TopGraphActionPill("🔬 REGLA WOLFRAM", CalcPlotY5, onWolframMutation)
        }

        // 6. HGS-9500DPO Real-Time Persistent Hypergraph Oscilloscope (Industrial Metrology Grade)
        if (viewMode == 1 || viewMode == 2) {
            Hgs9500DpoOscilloscope(
                nodes = nodes,
                hyperedges = hyperedges,
                telemetry = telemetry,
                onAmalgamatedSum = onAmalgamatedSum,
                temporalRegime = temporalRegime,
                externalFiltrationEps = filtrationEpsilon,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 7. Zoom / Topology Footer Line
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
                color = CalcPlotY1
            )
            Text(
                text = "[ARISTAS: ${hyperedges.size}]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = CalcPlotY2
            )
            Text(
                text = "[CONV: ${String.format(java.util.Locale.US, "%.1f", telemetry.convergencePct)}%]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = CalcPlotY3
            )
            Text(
                text = "[VIEW: EULER HULL]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = CalcLcdTextMuted
            )
            Text(
                text = "[ZOOM: 100%]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = CalcPlotY1
            )
        }
    }
}

@Composable
fun TopGraphActionPill(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .background(CalcBezel, RoundedCornerShape(4.dp))
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
    temporalRegime: HypergraphTemporalRegime = HypergraphTemporalRegime.PERSISTENTE,
    filtrationEpsilon: Float = 0.85f,
    onNodeSelected: (String) -> Unit,
    onNodeDragged: (String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val currentNodesState = androidx.compose.runtime.rememberUpdatedState(nodes)
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
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    val activeNodes = currentNodesState.value
                    for (node in activeNodes) {
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
            .pointerInput(Unit) {
                var draggedNodeId: String? = null
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val activeNodes = currentNodesState.value
                        for (node in activeNodes) {
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
                            val activeNodes = currentNodesState.value
                            val node = activeNodes.find { it.id == id }
                            if (node != null && width > 0 && height > 0) {
                                val newX = (node.x + dragAmount.x / width).coerceIn(0.06f, 0.94f)
                                val newY = (node.y + dragAmount.y / height).coerceIn(0.08f, 0.92f)
                                onNodeDragged(id, newX, newY)
                            }
                            change.consume()
                        }
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

        // 2. Draw Hypergraph Ellipses and Laser Lines with TDA Persistence Filtering
        drawEulerHypergraph(
            width = width,
            height = height,
            nodes = nodes,
            hyperedges = hyperedges,
            pulseAlpha = if (temporalRegime == HypergraphTemporalRegime.ESTATICO) 0.8f else pulseAlpha,
            temporalRegime = temporalRegime,
            filtrationEpsilon = filtrationEpsilon
        )

        // 3. Draw Nodes with halos and labels
        drawTopNodes(width, height, nodes, selectedNodeId, if (temporalRegime == HypergraphTemporalRegime.ESTATICO) 0.8f else pulseAlpha)
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
                colors = listOf(CalcPlotY1.copy(alpha = 0.20f), CalcPlotY3.copy(alpha = 0.08f), Color.Transparent),
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
    val gridColor = CalcLcdGrid
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

    // Graphing Calculator Cartesian Coordinate Axes (X and Y centered)
    val midX = width / 2f
    val midY = height / 2f
    val axisColor = CalcLcdAxis
    drawLine(axisColor, Offset(midX, 0f), Offset(midX, height), strokeWidth = 1.2f)
    drawLine(axisColor, Offset(0f, midY), Offset(width, midY), strokeWidth = 1.2f)

    // Tick marks along axes
    var tickX = 0f
    while (tickX < width) {
        drawLine(axisColor, Offset(tickX, midY - 3f), Offset(tickX, midY + 3f), strokeWidth = 1f)
        tickX += gridSpacing * 2
    }
    var tickY = 0f
    while (tickY < height) {
        drawLine(axisColor, Offset(midX - 3f, tickY), Offset(midX + 3f, tickY), strokeWidth = 1f)
        tickY += gridSpacing * 2
    }
}

private fun DrawScope.drawEulerHypergraph(
    width: Float,
    height: Float,
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    pulseAlpha: Float,
    temporalRegime: HypergraphTemporalRegime = HypergraphTemporalRegime.PERSISTENTE,
    filtrationEpsilon: Float = 0.85f
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

        // Check TDA Persistence filtration activity
        val isAliveInTda = if (temporalRegime == HypergraphTemporalRegime.PERSISTENTE) {
            val edgeBirth = when (edge.id) {
                "e1_sensorial" -> 0.00f
                "e2_cognitiva_s4" -> 0.60f
                "e3_puente" -> 0.45f
                else -> (edge.weight * 0.35f).coerceIn(0.0f, 1.5f)
            }
            val edgeDeath = when (edge.id) {
                "e1_sensorial" -> 1.40f
                "e2_cognitiva_s4" -> 1.85f
                "e3_puente" -> 2.10f
                else -> edgeBirth + 1.2f
            }
            filtrationEpsilon in edgeBirth..edgeDeath
        } else true

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

        val padX = 22f
        val padY = 16f
        val boxLeft = (minX - padX).coerceAtLeast(6f)
        val boxTop = (minY - padY).coerceAtLeast(6f)
        val boxWidth = ((maxX - minX) + padX * 2f).coerceAtMost(width - boxLeft - 6f)
        val boxHeight = ((maxY - minY) + padY * 2f).coerceAtMost(height - boxTop - 6f)

        if (isAliveInTda) {
            // Draw full vibrant bounding polyadic envelope / ellipse
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
        } else {
            // Ghost filtered mode: subtle dashed outline showing inactive homological component
            drawOval(
                color = edgeColor.copy(alpha = 0.03f),
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight)
            )
            drawOval(
                color = edgeColor.copy(alpha = 0.18f),
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight),
                style = Stroke(width = 1.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
            )
            // Ghost tag box
            val tagCenter = Offset(
                centroid.x.coerceIn(80f, width - 80f),
                (centroid.y - 18f).coerceIn(24f, height - 24f)
            )
            drawTagBox(
                text = "${edge.id} [TDA FILTRADO: fuera de ε]",
                center = tagCenter,
                textColor = Color(0xFF5A7280),
                borderColor = Color(0xFF2E404C),
                bgColor = Color(0xFF03080E)
            )
        }
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
                isSensory -> CalcPlotY3
                isS4 -> CalcPlotY4
                isMemory -> CalcPlotY1
                isHub -> CalcPlotY2
                else -> CalcPlotY1
            }

            val star = if (isHub) " ★" else ""
            val label = "${node.label} [${String.format(java.util.Locale.US, "%.2f", node.energy)} J]$star"
            NodeVisual(
                id = node.id,
                label = label,
                pos = Offset(cx, cy),
                color = color,
                isStar = isHub,
                isSelected = (node.id == selectedNodeId || (selectedNodeId?.contains("v3") == true && node.id.contains("v3")) || (selectedNodeId?.contains("hub") == true && node.id.contains("hub")))
            )
        }
    } else {
        listOf(
            NodeVisual("v1", "s_optico [0.19 J]", Offset(width * 0.12f, height * 0.27f), CalcPlotY3, false, false),
            NodeVisual("v2", "s_acustico [0.19 J]", Offset(width * 0.11f, height * 0.70f), CalcPlotY3, false, false),
            NodeVisual("v3", "HUB_CENTRAL [0.19 J] ★", Offset(width * 0.48f, height * 0.27f), CalcPlotY2, true, selectedNodeId == "v3" || selectedNodeId == "hub_central"),
            NodeVisual("v4", "COG_MONJE [0.19 J] ★", Offset(width * 0.88f, height * 0.27f), CalcPlotY2, true, false),
            NodeVisual("v5", "cog_memoria [0.19 J]", Offset(width * 0.50f, height * 0.71f), CalcPlotY1, false, false),
            NodeVisual("v6", "cog_s4 [0.19 J]", Offset(width * 0.88f, height * 0.71f), CalcPlotY4, false, false)
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
                color = CalcPlotY2.copy(alpha = 0.85f),
                radius = reticleRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
            // Crosshairs
            drawLine(CalcPlotY2, Offset(cx - reticleRadius - 4f, cy), Offset(cx - reticleRadius + 4f, cy), strokeWidth = 1.5f)
            drawLine(CalcPlotY2, Offset(cx + reticleRadius - 4f, cy), Offset(cx + reticleRadius + 4f, cy), strokeWidth = 1.5f)
            drawLine(CalcPlotY2, Offset(cx, cy - reticleRadius - 4f), Offset(cx, cy - reticleRadius + 4f), strokeWidth = 1.5f)
            drawLine(CalcPlotY2, Offset(cx, cy + reticleRadius - 4f), Offset(cx, cy + reticleRadius + 4f), strokeWidth = 1.5f)
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
            color = CalcLcdBackground,
            radius = 7f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = if (node.isStar) CalcPlotY2 else node.color,
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
            color = CalcBezel,
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
