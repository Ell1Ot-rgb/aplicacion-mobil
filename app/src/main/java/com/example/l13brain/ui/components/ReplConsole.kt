package com.example.l13brain.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.ReplLogEntry
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReplCalculatorTab(
    logs: List<ReplLogEntry>,
    nodes: List<HyperNode>,
    selectedNodeId: String?,
    onExecuteCommand: (String) -> Unit,
    onNodeSelected: (String) -> Unit,
    onInjectEnergy: (String, Float) -> Unit,
    onResetPhase: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    var isAnalyzerMinimized by remember { mutableStateOf(false) }
    var activeSubtabIndex by remember { mutableIntStateOf(0) } // 0: Topología, 1: Kuramoto, 2: Dinámica, 3: Benchmark
    var activeMathBottomTab by remember { mutableIntStateOf(0) }
    var selectedNodeName by remember { mutableStateOf(selectedNodeId ?: "s_optico") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Title Frame
        Text(
            text = "┌─ L13 BRAIN INTERACTIVE REPL CALCULATOR",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF00FF66)
        )

        // 2. Terminal Log Output Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070D), RoundedCornerShape(6.dp))
                .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (logs.isNotEmpty()) {
                logs.takeLast(6).forEach { entry ->
                    Text(
                        text = if (entry.isCommand) "l13> ${entry.text}" else entry.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = if (entry.isCommand) FontWeight.Bold else FontWeight.Normal,
                        color = if (entry.isCommand) Color(0xFF00FF66) else Color(0xFFFFB300)
                    )
                }
            } else {
                Text(
                    text = "--weight=0.95 --anneal=0.88",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF66)
                )
                Text(
                    text = ">> REESCRITURA APLICADA: 3 hiperaristas generadas | Delta Entropía: -0.042 | Estado S4: Necesario (Box-Phi)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFFFFB300)
                )
            }
        }

        // 3. Command Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03080F), RoundedCornerShape(6.dp))
                .border(1.2.dp, Color(0xFF00FF66), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "l13> ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF00FF66)
                )
                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF00FF66)
                    ),
                    cursorBrush = SolidColor(Color(0xFF00FF66)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF00FF66), RoundedCornerShape(4.dp))
                    .clickable {
                        if (commandInput.isNotBlank()) {
                            onExecuteCommand(commandInput)
                            commandInput = ""
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "▶ ENTER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // 4. Quick Command Macro Pills
        val quickMacros = listOf(
            "sum H_A + H_B (Amalgamada)",
            "M(1:3, [1, 3]) (Submatriz)",
            "eig(L_H) (Espectro)",
            "sum --batch 50k (Zero-Boxing)",
            "bipartite (König A_bi)",
            "sum --direct (Disjunta A⊕B)",
            "diffuse 0.20 (Laplaciano)",
            "dual (Berge Dual H*)",
            "mutate (Wolfram)",
            "whos (Workspace)",
            "inject +0.95J"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickMacros.forEach { macro ->
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF00FF66).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .background(Color(0xFF06150E), RoundedCornerShape(4.dp))
                        .clickable { onExecuteCommand(macro) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = macro,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF66)
                    )
                }
            }
        }

        // 5. Mathematical Hypergraph Analyzer Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040810), RoundedCornerShape(8.dp))
                .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card Title + Minimize Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).background(Color(0xFFFF4081), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🌌 ANALIZADOR MATEMÁTICO DE HIPERGRAFOS L13",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = Color(0xFF00E5FF)
                    )
                }

                Text(
                    text = if (isAnalyzerMinimized) "[EXPANDIR ▼]" else "[MINIMIZAR ▲]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = Color(0xFFFFB300),
                    modifier = Modifier.clickable { isAnalyzerMinimized = !isAnalyzerMinimized }
                )
            }

            if (!isAnalyzerMinimized) {
                val activeNode = nodes.find { it.id == selectedNodeId || it.label == selectedNodeName } ?: nodes.firstOrNull()
                val activeId = activeNode?.id ?: selectedNodeId ?: "v1"
                val activeLabel = activeNode?.label ?: selectedNodeName

                // Node chips selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 UBICAR NODO EN HIPERGRAFO:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color(0xFF00FF66)
                    )
                    Text(
                        text = "${nodes.size} Vértices Activos",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = Color(0xFF86EFAC)
                    )
                }

                // Node chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val displayList = if (nodes.isNotEmpty()) nodes else listOf(
                        HyperNode("v1", "s_optico", 0.19f, 0.12f, 0.27f),
                        HyperNode("v2", "s_acustico", 0.19f, 0.11f, 0.70f),
                        HyperNode("v3", "hub_central", 0.19f, 0.48f, 0.27f),
                        HyperNode("v4", "cog_monje", 0.19f, 0.88f, 0.27f),
                        HyperNode("v5", "cog_memoria", 0.19f, 0.50f, 0.71f),
                        HyperNode("v6", "cog_s4", 0.19f, 0.88f, 0.71f)
                    )

                    displayList.forEach { node ->
                        val isSelected = (node.id == activeId)
                        val color = when {
                            node.modalState.contains("Sensorial", ignoreCase = true) -> Color(0xFF00FF66)
                            node.modalState.contains("Cognitivo", ignoreCase = true) -> Color(0xFFFFB300)
                            node.modalState.contains("Motor", ignoreCase = true) -> Color(0xFF00E5FF)
                            node.modalState.contains("Memoria", ignoreCase = true) -> Color(0xFFFF4081)
                            else -> Color(0xFF00FF66)
                        }
                        val energyStr = String.format(java.util.Locale.US, "%.2f", node.energy)
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSelected) color else Color(0xFF16382B), RoundedCornerShape(4.dp))
                                .background(if (isSelected) color.copy(alpha = 0.20f) else Color(0xFF070E17), RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedNodeName = node.label
                                    onNodeSelected(node.id)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(5.dp).background(color, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${node.label} [E=${energyStr}J]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    color = color
                                )
                            }
                        }
                    }
                }

                // Node Analysis Details Box
                val deg = activeNode?.degree ?: 2
                val energyFmt = String.format(java.util.Locale.US, "%.3f", activeNode?.energy ?: 0.19f)
                val phaseFmt = String.format(java.util.Locale.US, "%.3f", activeNode?.phase ?: 0f)
                val angleDeg = String.format(java.util.Locale.US, "%.1f", (((activeNode?.phase ?: 0f) * 180f / Math.PI.toFloat()) % 360f + 360f) % 360f)
                val modalStr = activeNode?.modalState ?: "Sensorial"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF03070E), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔎 ANÁLISIS NODAL: [$activeLabel ($activeId)] • ESTADO: $modalStr",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "Lógica S4: [OK]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color(0xFF86EFAC)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Grado d(v): $deg aristas", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFCBD5E1))
                        Text("• Energía E(v): $energyFmt J", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66))
                        Text("• Fase θ(v): $phaseFmt rad", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00E5FF))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Posición: (${String.format(java.util.Locale.US, "%.2f", activeNode?.x ?: 0f)}, ${String.format(java.util.Locale.US, "%.2f", activeNode?.y ?: 0f)})", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFCBD5E1))
                        Text("• Umbral Wolfram: 1.350 J", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFFFB300))
                        Text("• Ángulo: $angleDeg°", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00E5FF))
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReplActionButton("⚡ +0.50J Inyectar", Color(0xFFFFB300), Modifier.weight(1f)) {
                            onInjectEnergy(activeId, 0.5f)
                        }
                        ReplActionButton("🔄 Reset Fase", Color(0xFF00E5FF), Modifier.weight(1f)) {
                            onResetPhase(activeId)
                        }
                        ReplActionButton("🎯 Centrar Foco", Color(0xFFFF4081), Modifier.weight(1f)) {
                            onNodeSelected(activeId)
                        }
                    }
                }

                // 4 Sub-Tabs for Analyzer:
                // 1. TOPOLOGÍA & MATRICES | 2. KURAMOTO S¹ | 3. DINÁMICA & STEM PLOT | 4. BENCHMARK & CONVERGENCIA
                val analyzerSubtabs = listOf(
                    Pair("🌀 TOPOLOGÍA &\nMATRICES", Color(0xFF00E5FF)),
                    Pair("🔄 KURAMOTO S¹", Color(0xFF86EFAC)),
                    Pair("📈 DINÁMICA &\nSTEM PLOT", Color(0xFF00FF66)),
                    Pair("⚡ BENCHMARK &\nCONVERGENCIA", Color(0xFFFFB300))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    analyzerSubtabs.forEachIndexed { index, (label, color) ->
                        val isActive = activeSubtabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .border(1.dp, if (isActive) Color(0xFF00E5FF) else Color(0xFF16382B), RoundedCornerShape(4.dp))
                                .background(if (isActive) Color(0xFF072430) else Color(0xFF060D15), RoundedCornerShape(4.dp))
                                .clickable { activeSubtabIndex = index }
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF00E5FF) else Color(0xFF4F7363)
                            )
                        }
                    }
                }

                var topologyMode by remember { mutableIntStateOf(0) }

                // Content of selected subtab:
                when (activeSubtabIndex) {
                    0 -> {
                        // Subtab 1: TOPOLOGÍA & MATRICES (Screenshot 2)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SmallViewButton("🌀 Envolventes 2D", topologyMode == 0) { topologyMode = 0 }
                                SmallViewButton("⚖️ König Bipartito A_bi", topologyMode == 1) { topologyMode = 1 }
                                SmallViewButton("▦ Matriz M(1:3, [1, 3])", topologyMode == 2) { topologyMode = 2 }
                            }

                            // Dynamic Topology Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF03070E), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                            ) {
                                when (topologyMode) {
                                    0 -> MiniForceGraphCanvas(modifier = Modifier.fillMaxSize())
                                    1 -> MiniKonigBipartiteCanvas(modifier = Modifier.fillMaxSize())
                                    else -> MiniMatlabMatrixView(modifier = Modifier.fillMaxSize())
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        when (topologyMode) {
                                            0 -> "H_A ⊕ H_B (Layout Force 2D)"
                                            1 -> "Grafo Bipartito König G_bi(V, E)"
                                            else -> "Indexación MATLAB M(1:3, [1, 3])"
                                        },
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF00E5FF)
                                    )
                                    Text(
                                        "⚡ R(t)=0.356 | Foco: v3",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Subtab 2: KURAMOTO S¹ (Screenshot 6)
                        DualKuramotoCirclesCanvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color(0xFF03070E), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                        )
                    }
                    2 -> {
                        // Subtab 3: DINÁMICA & STEM PLOT (Screenshot 5)
                        DinamicaStemPlotView(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        // Subtab 4: BENCHMARK & CONVERGENCIA (Screenshot 4)
                        BenchmarkMerkleBox(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Math Bottom Tabs & Action Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF03080F), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val bottomTabs = listOf(
                        "🧮 ÁLGEBRA &\nMATRICES",
                        "⏱️ TIEMPO & MERKLE",
                        "📊 ESPECTRO &\nATRACTOR",
                        "🕸️ REGLAS WOLFRAM"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        bottomTabs.forEachIndexed { idx, title ->
                            val isSel = activeMathBottomTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, if (isSel) Color(0xFF00E5FF) else Color.Transparent, RoundedCornerShape(3.dp))
                                    .background(if (isSel) Color(0xFF08222E) else Color.Transparent)
                                    .clickable { activeMathBottomTab = idx }
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.sp,
                                    color = if (isSel) Color(0xFF00E5FF) else Color(0xFF5B786D)
                                )
                            }
                        }
                    }

                    // Action buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallActionPill("▦ M(1:3,[1,3])") { onExecuteCommand("M(1:3, [1, 3])") }
                        SmallActionPill("📐 Laplaciano") { onExecuteCommand("diffuse 0.20") }
                        SmallActionPill("🔄 Dual Berge") { onExecuteCommand("dual") }
                        SmallActionPill("⚖️ König A_bi") { onExecuteCommand("bipartite") }
                        SmallActionPill("📋 Whos") { onExecuteCommand("whos") }
                    }
                }
            }
        }

        // 6. Persistent Hotkey Footer
        Text(
            text = "[Q] Salir | [Space] Pausar Sim | [P] Exportar PNG | [F2] Toggle CRT | [F3] Modo Incidencia",
            fontFamily = FontFamily.Monospace,
            fontSize = 8.5.sp,
            color = Color(0xFF00FF66)
        )
    }
}

@Composable
private fun SmallViewButton(label: String, isActive: Boolean, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .border(1.dp, if (isActive) Color(0xFF00FF66) else Color(0xFF16382B), RoundedCornerShape(4.dp))
            .background(if (isActive) Color(0xFF062215) else Color(0xFF060D15), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFF00FF66) else Color(0xFF5B786D)
        )
    }
}

@Composable
private fun ReplActionButton(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .background(Color(0xFF07121A), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun SmallActionPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFF16382B), RoundedCornerShape(3.dp))
            .background(Color(0xFF060D15), RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = Color(0xFF86EFAC)
        )
    }
}

@Composable
private fun MiniForceGraphCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Grid
        val gridColor = Color(0xFF0A1F16)
        var x = 0f
        while (x < width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 0.5f)
            x += 24f
        }
        var y = 0f
        while (y < height) {
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 0.5f)
            y += 24f
        }

        // Green Polygon region
        val path1 = Path().apply {
            moveTo(width * 0.15f, height * 0.80f)
            lineTo(width * 0.48f, height * 0.20f)
            lineTo(width * 0.48f, height * 0.80f)
            close()
        }
        drawPath(path1, Color(0xFF00FF66).copy(alpha = 0.15f))
        drawPath(path1, Color(0xFF00FF66), style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))

        // Magenta Polygon region
        val path2 = Path().apply {
            moveTo(width * 0.48f, height * 0.20f)
            lineTo(width * 0.85f, height * 0.20f)
            lineTo(width * 0.85f, height * 0.80f)
            lineTo(width * 0.48f, height * 0.80f)
            close()
        }
        drawPath(path2, Color(0xFFFF4081).copy(alpha = 0.15f))
        drawPath(path2, Color(0xFFFF4081), style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))

        // Nodes
        val pts = listOf(
            Pair(Offset(width * 0.15f, height * 0.80f), Color(0xFF00FF66)),
            Pair(Offset(width * 0.48f, height * 0.20f), Color(0xFFFFB300)),
            Pair(Offset(width * 0.48f, height * 0.80f), Color(0xFF00E5FF)),
            Pair(Offset(width * 0.85f, height * 0.20f), Color(0xFFFF4081)),
            Pair(Offset(width * 0.85f, height * 0.80f), Color(0xFF00E5FF))
        )
        for ((pt, col) in pts) {
            drawCircle(col, radius = 5f, center = pt)
            drawCircle(Color.White, radius = 2f, center = pt)
        }
    }
}

@Composable
private fun DualKuramotoCirclesCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val c1x = width * 0.28f
        val c2x = width * 0.72f
        val cy = height * 0.45f
        val radius = height * 0.32f

        // Circle 1: Choque Post-Suma
        drawCircle(Color(0xFF0A2218), radius = radius, center = Offset(c1x, cy), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        drawCircle(Color(0xFF00FF66), radius = 2f, center = Offset(c1x, cy))

        val rays1 = listOf(0.4f, 1.8f, 3.2f, 4.5f)
        for (r in rays1) {
            val ex = c1x + cos(r) * radius
            val ey = cy + sin(r) * radius
            drawLine(Color(0xFFFF4081), Offset(c1x, cy), Offset(ex, ey), strokeWidth = 1.5f)
            drawCircle(Color(0xFFFF4081), radius = 3f, center = Offset(ex, ey))
        }

        // Circle 2: Coherencia Dinámica
        drawCircle(Color(0xFF0A2218), radius = radius, center = Offset(c2x, cy), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        drawCircle(Color(0xFF00FF66), radius = 2f, center = Offset(c2x, cy))

        val rays2 = listOf(0.8f, 1.2f, 2.7f, 5.1f)
        for (r in rays2) {
            val ex = c2x + cos(r) * radius
            val ey = cy + sin(r) * radius
            drawLine(Color(0xFF00FF66), Offset(c2x, cy), Offset(ex, ey), strokeWidth = 1.5f)
            drawCircle(Color(0xFF00FF66), radius = 3f, center = Offset(ex, ey))
        }
    }
}

@Composable
private fun DinamicaStemPlotView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Chart 1: Dinámica Continua
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF103328), RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📈 DINÁMICA CONTINUA DE ACTIVACIÓN E_i(t) [TICK 20: SUMA EN CALIENTE]", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF00E5FF))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Yellow line
                    val pY = Path().apply {
                        moveTo(0f, h * 0.7f)
                        lineTo(w * 0.45f, h * 0.68f)
                        lineTo(w * 0.45f, h * 0.30f)
                        lineTo(w, h * 0.28f)
                    }
                    drawPath(pY, Color(0xFFFFB300), style = Stroke(width = 1.8f))

                    // Cyan line
                    val pC = Path().apply {
                        moveTo(0f, h * 0.45f)
                        lineTo(w * 0.45f, h * 0.43f)
                        lineTo(w * 0.45f, h * 0.20f)
                        lineTo(w, h * 0.35f)
                    }
                    drawPath(pC, Color(0xFF00E5FF), style = Stroke(width = 1.8f))

                    // Green line
                    val pG = Path().apply {
                        moveTo(0f, h * 0.35f)
                        lineTo(w * 0.45f, h * 0.48f)
                        lineTo(w, h * 0.55f)
                    }
                    drawPath(pG, Color(0xFF00FF66), style = Stroke(width = 1.8f))

                    // Dashed red event line
                    drawLine(Color(0xFFFF4081), Offset(w * 0.45f, 0f), Offset(w * 0.45f, h), strokeWidth = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                }
            }
        }

        // Chart 2: Entropía vs Peso Hebbiano
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF103328), RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📊 ENTROPÍA H(t) [CIAN] vs PESO HEBBIANO W(e_i) [ÁMBAR]", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF00E5FF))
                Text("Salto: 1.50 -> 2.78 bits", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = Color(0xFF86EFAC))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Cyan jump line
                    val p1 = Path().apply {
                        moveTo(0f, h * 0.75f)
                        lineTo(w * 0.45f, h * 0.75f)
                        lineTo(w * 0.45f, h * 0.15f)
                        lineTo(w, h * 0.25f)
                    }
                    drawPath(p1, Color(0xFF00E5FF), style = Stroke(width = 1.8f))

                    // Amber dashed line
                    val p2 = Path().apply {
                        moveTo(0f, h * 0.90f)
                        lineTo(w * 0.45f, h * 0.85f)
                        lineTo(w, h * 0.50f)
                    }
                    drawPath(p2, Color(0xFFFFB300), style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
                }
            }
        }

        // Chart 3: Espectro Laplaciano (Stem Plot)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF103328), RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            Text("▦ ESPECTRO LAPLACIANO (STEM PLOT: stem(eig(L_H)))", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF00E5FF))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stemHeights = listOf(0.12f, 0.22f, 0.40f, 0.65f, 0.78f, 0.92f)
                    for (i in stemHeights.indices) {
                        val sx = w * (0.15f + i * 0.14f)
                        val sy = h * (1f - stemHeights[i])
                        drawLine(Color(0xFF00FF66).copy(alpha = 0.7f), Offset(sx, h), Offset(sx, sy), strokeWidth = 1.5f)
                        drawCircle(Color(0xFF00FF66), radius = 3.5f, center = Offset(sx, sy))
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkMerkleBox(
    modifier: Modifier = Modifier,
    onRunTest: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Visual Benchmark Bar Comparison (from Benchmark_Sumas_Masivas_3M_10M.png)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚡ BENCHMARK: SUMAS MASIVAS (3M - 10M)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB300)
                )
                Text(
                    "26.04 M ops/s",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF66)
                )
            }

            // Benchmark Row 1: 3M Operaciones
            BenchmarkComparisonRow(
                label = "3M OPS (H_A ⊕ H_B):",
                boxedMs = 115,
                zeroBoxMs = 24,
                speedup = "4.8x"
            )

            // Benchmark Row 2: 10M Operaciones
            BenchmarkComparisonRow(
                label = "10M OPS (MASSIVE):",
                boxedMs = 384,
                zeroBoxMs = 78,
                speedup = "4.9x"
            )

            // GC pauses comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• GC Pauses (Boxed vs Zero-Box):", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF94A3B8))
                Text("142ms vs 0ms (Zero-GC)", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
            }
        }

        // Merkle DAG Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03080E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF00FF66), RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("╔══════ RESUMEN CRIPTOGRÁFICO MERKLE DAG V3.0 ══════╗", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66))
            MerkleRow("• SHA-256 Época Actual:", "bba89ef74c83d8b5 (Inmutable DAG)")
            MerkleRow("• Parent Hash (Génesis):", "fdb942fa4d760c5c (Criptográfico)")
            MerkleRow("• Álgebra de Hipergrafos:", "Suma Amalgamada H_A ⊕ H_B (Monoide)")
            MerkleRow("• Nodos Unificados:", "6 Nodos (s_optico E=0.05J)")
            MerkleRow("• Hiperaristas Totales:", "3 Aristas de orden superior")
            MerkleRow("• Sincronización Kuramoto:", "R(t) = 0.128 (Transición)")
            MerkleRow("• Entropía de Shannon H(t):", "2.585 bits (Estado Estacionario)")
            MerkleRow("• Optimización Android:", "Zero-Boxing (26.04 M ops/s | 0 ms GC)")
            Text("╚═════════════════════════════════════════════════════╝", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66))
        }
    }
}

@Composable
private fun BenchmarkComparisonRow(
    label: String,
    boxedMs: Int,
    zeroBoxMs: Int,
    speedup: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFFE2E8F0))
            Text("Speedup: $speedup", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
        }
        // Dual Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Boxed Bar (Amber)
            Box(
                modifier = Modifier
                    .weight(boxedMs.toFloat())
                    .height(10.dp)
                    .background(Color(0xFFFFB300).copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Std: ${boxedMs}ms", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, color = Color.White)
            }
            // Zero-Boxing Bar (Phosphor Green)
            Box(
                modifier = Modifier
                    .weight(zeroBoxMs.toFloat().coerceAtLeast(15f))
                    .height(10.dp)
                    .background(Color(0xFF00FF66), RoundedCornerShape(2.dp))
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("ZeroBox: ${zeroBoxMs}ms", fontFamily = FontFamily.Monospace, fontSize = 6.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MiniKonigBipartiteCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftX = width * 0.20f
        val rightX = width * 0.80f

        val nodeYs = (0..5).map { (height * 0.15f) + it * (height * 0.14f) }
        val edgeYs = listOf(height * 0.25f, height * 0.50f, height * 0.75f)

        // Bipartite lines
        val connections = listOf(
            Pair(0, 0), Pair(1, 0), Pair(2, 0), // e1 connects v1, v2, v3
            Pair(2, 2), Pair(3, 2), Pair(4, 2), // e3 connects v3, v4, v5
            Pair(3, 1), Pair(4, 1), Pair(5, 1)  // e2 connects v4, v5, v6
        )

        for ((vIdx, eIdx) in connections) {
            drawLine(
                Color(0xFF00E5FF).copy(alpha = 0.4f),
                Offset(leftX, nodeYs[vIdx]),
                Offset(rightX, edgeYs[eIdx]),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
        }

        // Draw left nodes (V)
        for (i in 0..5) {
            drawCircle(Color(0xFF00FF66), radius = 6f, center = Offset(leftX, nodeYs[i]))
            drawCircle(Color.Black, radius = 2f, center = Offset(leftX, nodeYs[i]))
        }

        // Draw right nodes (E)
        for (j in 0..2) {
            drawRect(
                Color(0xFFFF4081),
                topLeft = Offset(rightX - 7f, edgeYs[j] - 7f),
                size = Size(14f, 14f)
            )
        }
    }
}

@Composable
private fun MiniMatlabMatrixView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF02050A))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            ">> M(1:3, [1, 3])  % Submatriz de Incidencia",
            fontFamily = FontFamily.Monospace,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF)
        )
        Text(
            "ans = ",
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = Color(0xFF86EFAC)
        )

        // Matrix Table layout matching MATLAB
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF0F3022), RoundedCornerShape(4.dp))
                .background(Color(0xFF05101A))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Vértice", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFFFFB300))
                Text("v1_ir", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66))
                Text("v2_lidar", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF00FF66))
                Text("v3_hub", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFFFB300))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("e1_sens", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF00E5FF))
                Text("1", fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                Text("1", fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                Text("1", fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("e3_puente", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF00E5FF))
                Text("0", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF475569))
                Text("0", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF475569))
                Text("1", fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
            }
        }

        Text(
            "whos: Class 'double' | Size 3x2 | Bytes 48 | Attr: sparse",
            fontFamily = FontFamily.Monospace,
            fontSize = 7.sp,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun MerkleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFFFFB300))
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF00FF66))
    }
}
