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
import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.ReplLogEntry
import com.example.l13brain.model.TelemetryState
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReplCalculatorTab(
    logs: List<ReplLogEntry>,
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge> = emptyList(),
    telemetry: TelemetryState = TelemetryState(),
    selectedNodeId: String?,
    onExecuteCommand: (String) -> Unit,
    onNodeSelected: (String) -> Unit,
    onInjectEnergy: (String, Float) -> Unit,
    onResetPhase: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    var isAnalyzerMinimized by remember { mutableStateOf(false) }
    var activeSubtabIndex by remember { mutableIntStateOf(2) } // 2: Dinámica & Stem Plot por defecto (Screenshot 7)
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

        // 3.5. Interactive Cyber-Calculator Keypad & Quick Sum Toolbar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040A12), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF133829), RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Quick Hypergraph & Vector Sum Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CalcSpecialPill("⚡ H_A ⊕ H_B", Color(0xFF00FF66)) {
                    onExecuteCommand("sum H_A + H_B")
                }
                CalcSpecialPill("⊕ A ⊕ B", Color(0xFF00E5FF)) {
                    onExecuteCommand("sum --direct")
                }
                CalcSpecialPill("🔥 HotSum V3", Color(0xFFFFB300)) {
                    onExecuteCommand("hotsum")
                }
                CalcSpecialPill("Σ E(v)", Color(0xFFFF4081)) {
                    onExecuteCommand("sum(E)")
                }
                CalcSpecialPill("Σ W(e)", Color(0xFF86EFAC)) {
                    onExecuteCommand("sum(W)")
                }
                CalcSpecialPill("Σ d(v)", Color(0xFFB388FF)) {
                    onExecuteCommand("sum(d)")
                }
            }

            // Numeric & Operator Keypad Grid
            val keyRows = listOf(
                listOf("7", "8", "9", "/", "C"),
                listOf("4", "5", "6", "*", "("),
                listOf("1", "2", "3", "-", ")"),
                listOf("0", ".", "+", "^", "=")
            )

            keyRows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .background(
                                    when (key) {
                                        "=" -> Color(0xFF00FF66)
                                        "C" -> Color(0xFF261014)
                                        "+", "-", "*", "/", "^" -> Color(0xFF0A2218)
                                        "(", ")" -> Color(0xFF081B26)
                                        else -> Color(0xFF061019)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    when (key) {
                                        "=" -> Color(0xFF00FF66)
                                        "C" -> Color(0xFFFF4081)
                                        "+", "-", "*", "/", "^" -> Color(0xFF00E5FF)
                                        else -> Color(0xFF133829)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    when (key) {
                                        "=" -> {
                                            if (commandInput.isNotBlank()) {
                                                onExecuteCommand(commandInput)
                                                commandInput = ""
                                            }
                                        }
                                        "C" -> {
                                            commandInput = ""
                                        }
                                        else -> {
                                            commandInput += key
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (key) {
                                    "=" -> Color.Black
                                    "C" -> Color(0xFFFF4081)
                                    "+", "-", "*", "/", "^" -> Color(0xFF00E5FF)
                                    else -> Color(0xFFE2E8F0)
                                }
                            )
                        }
                    }
                }
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
                    Pair("🌀 TOPOLOGÍA & MATRICES", Color(0xFF00E5FF)),
                    Pair("🔄 KURAMOTO S¹", Color(0xFF86EFAC)),
                    Pair("📈 DINÁMICA & STEM PLOT", Color(0xFF00E5FF)),
                    Pair("⚡ BENCHMARK & CONVERGENCIA", Color(0xFFFFB300))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    analyzerSubtabs.forEachIndexed { index, (label, color) ->
                        val isActive = activeSubtabIndex == index
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .border(1.dp, if (isActive) Color(0xFF00E5FF) else Color(0xFF16382B), RoundedCornerShape(4.dp))
                                .background(if (isActive) Color(0xFF072430) else Color(0xFF060D15), RoundedCornerShape(4.dp))
                                .clickable { activeSubtabIndex = index }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF00E5FF) else Color(0xFF4F7363),
                                maxLines = 1
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
                                    0 -> MiniForceGraphCanvas(
                                        nodes = nodes,
                                        hyperedges = hyperedges,
                                        selectedNodeId = selectedNodeId,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    1 -> MiniKonigBipartiteCanvas(
                                        nodes = nodes,
                                        hyperedges = hyperedges,
                                        modifier = Modifier.fillMaxSize()
                                    )
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
                                            0 -> "H_A ⊕ H_B (Layout Force 2D) [${nodes.size} Nodos, ${hyperedges.size} Hiperaristas]"
                                            1 -> "Grafo Bipartito König G_bi(V, E)"
                                            else -> "Indexación MATLAB M(1:3, [1, 3])"
                                        },
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF00E5FF)
                                    )
                                    Text(
                                        "⚡ R(t)=${String.format(java.util.Locale.US, "%.3f", telemetry.kuramotoOrderR)} | Foco: ${selectedNodeId ?: nodes.firstOrNull()?.id ?: "none"}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Subtab 2: KURAMOTO S¹ (Screenshot 6 + Enlace directo a Dinámica)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DualKuramotoCirclesCanvas(
                                nodes = nodes,
                                telemetry = telemetry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF03070E), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF071C14), RoundedCornerShape(4.dp))
                                    .border(0.8.dp, Color(0xFF00FF66), RoundedCornerShape(4.dp))
                                    .clickable { activeSubtabIndex = 2 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📈 VER DINÁMICA CONTINUA & STEM PLOT",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF66)
                                )
                                Text(
                                    text = "CAMBIAR VISTA ▶",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                    2 -> {
                        // Subtab 3: DINÁMICA & STEM PLOT (Screenshot 7)
                        DinamicaStemPlotView(
                            nodes = nodes,
                            telemetry = telemetry,
                            hyperedges = hyperedges,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        // Subtab 4: BENCHMARK & CONVERGENCIA (Screenshot 4)
                        BenchmarkMerkleBox(
                            nodes = nodes,
                            hyperedges = hyperedges,
                            telemetry = telemetry,
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
                        "📜 REGLAS WOLFRAM"
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
private fun CalcSpecialPill(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp)
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
private fun MiniForceGraphCanvas(
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    selectedNodeId: String?,
    modifier: Modifier = Modifier
) {
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

        // Map node IDs to screen offsets
        val nodePosMap = mutableMapOf<String, Offset>()
        val n = nodes.size.coerceAtLeast(1)
        nodes.forEachIndexed { i, node ->
            val nx = if (node.x > 0.01f && node.x < 0.99f) {
                node.x * width
            } else {
                val angle = (2.0 * Math.PI * i / n) - Math.PI / 2.0
                (width * 0.5f + kotlin.math.cos(angle).toFloat() * width * 0.38f)
            }
            val ny = if (node.y > 0.01f && node.y < 0.99f) {
                node.y * height
            } else {
                val angle = (2.0 * Math.PI * i / n) - Math.PI / 2.0
                (height * 0.5f + kotlin.math.sin(angle).toFloat() * height * 0.36f)
            }
            nodePosMap[node.id] = Offset(nx.coerceIn(16f, width - 16f), ny.coerceIn(16f, height - 16f))
        }

        // Draw Hyperedges
        hyperedges.forEach { edge ->
            val edgeColor = Color(edge.colorHex)
            val memberPoints = edge.nodeIds.mapNotNull { nodePosMap[it] }
            if (memberPoints.size >= 2) {
                val centroidX = memberPoints.map { it.x }.average().toFloat()
                val centroidY = memberPoints.map { it.y }.average().toFloat()
                val centroid = Offset(centroidX, centroidY)

                if (memberPoints.size >= 3) {
                    val path = Path().apply {
                        moveTo(memberPoints[0].x, memberPoints[0].y)
                        for (idx in 1 until memberPoints.size) {
                            lineTo(memberPoints[idx].x, memberPoints[idx].y)
                        }
                        close()
                    }
                    drawPath(path, edgeColor.copy(alpha = 0.12f))
                    drawPath(path, edgeColor.copy(alpha = 0.7f), style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
                }

                // Connect nodes to centroid
                memberPoints.forEach { pt ->
                    drawLine(
                        color = edgeColor.copy(alpha = 0.45f),
                        start = pt,
                        end = centroid,
                        strokeWidth = 1f
                    )
                }

                // Centroid badge
                drawCircle(edgeColor, radius = 3.5f, center = centroid)
                drawCircle(Color.Black, radius = 1.5f, center = centroid)
            }
        }

        // Draw Nodes
        nodes.forEach { node ->
            val pos = nodePosMap[node.id] ?: return@forEach
            val isSelected = node.id == selectedNodeId
            val nodeColor = when {
                node.energy > 0.8f -> Color(0xFFFF4081)
                node.energy > 0.4f -> Color(0xFFFFB300)
                node.energy > 0.15f -> Color(0xFF00FF66)
                else -> Color(0xFF00E5FF)
            }
            val radius = 4f + (node.energy * 4f).coerceIn(1f, 8f)

            // Outer glow
            drawCircle(nodeColor.copy(alpha = 0.25f), radius = radius + 6f, center = pos)

            // Selected reticle
            if (isSelected) {
                drawCircle(
                    Color(0xFF00E5FF),
                    radius = radius + 9f,
                    center = pos,
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)))
                )
            }

            // Core
            drawCircle(nodeColor, radius = radius, center = pos)
            drawCircle(Color.White, radius = (radius * 0.4f).coerceAtLeast(1.5f), center = pos)
        }
    }
}

@Composable
private fun DualKuramotoCirclesCanvas(
    nodes: List<HyperNode>,
    telemetry: TelemetryState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val c1x = width * 0.28f
        val c2x = width * 0.72f
        val cy = height * 0.48f
        val radius = height * 0.32f

        // Circle 1: Fases Nodales en S¹
        drawCircle(Color(0xFF0A2218), radius = radius, center = Offset(c1x, cy), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        drawCircle(Color(0xFF00FF66), radius = 2f, center = Offset(c1x, cy))

        nodes.forEach { node ->
            val ex = c1x + kotlin.math.cos(node.phase.toDouble()).toFloat() * radius
            val ey = cy + kotlin.math.sin(node.phase.toDouble()).toFloat() * radius
            val rayColor = when {
                node.energy > 0.6f -> Color(0xFFFF4081)
                node.energy > 0.3f -> Color(0xFFFFB300)
                else -> Color(0xFF00E5FF)
            }
            drawLine(rayColor.copy(alpha = 0.75f), Offset(c1x, cy), Offset(ex, ey), strokeWidth = 1.4f)
            drawCircle(rayColor, radius = 3.5f, center = Offset(ex, ey))
        }

        // Circle 2: Parámetro de Orden Kuramoto R(t)
        drawCircle(Color(0xFF0A2218), radius = radius, center = Offset(c2x, cy), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
        drawCircle(Color(0xFF00FF66), radius = 2f, center = Offset(c2x, cy))

        val r = telemetry.kuramotoOrderR.coerceIn(0.05f, 1.0f)
        val meanPhase = (nodes.map { it.phase }.average().toFloat()).takeIf { !it.isNaN() } ?: 0.5f
        val ox = c2x + kotlin.math.cos(meanPhase.toDouble()).toFloat() * (radius * r)
        val oy = cy + kotlin.math.sin(meanPhase.toDouble()).toFloat() * (radius * r)

        drawCircle(Color(0xFF00FF66).copy(alpha = (r * 0.3f).coerceIn(0.05f, 0.4f)), radius = radius * r, center = Offset(c2x, cy))
        drawLine(Color(0xFF00FF66), Offset(c2x, cy), Offset(ox, oy), strokeWidth = 2.2f)
        drawCircle(Color(0xFF00FF66), radius = 4.5f, center = Offset(ox, oy))
        drawCircle(Color.White, radius = 2f, center = Offset(ox, oy))
    }
}

@Composable
private fun DinamicaStemPlotView(
    nodes: List<HyperNode>,
    telemetry: TelemetryState,
    hyperedges: List<HyperEdge>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Chart 1: DINÁMICA CONTINUA DE ACTIVACIÓN E_i(t) [TICK 20: SUMA EN CALIENTE]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF103328), RoundedCornerShape(4.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "📈 DINÁMICA CONTINUA DE ACTIVACIÓN E_i(t) [TICK 20: SUMA EN CALIENTE]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f, fill = false)
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "— hub_central (E=1.20J)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 6.5.sp,
                        color = Color(0xFFFFB300)
                    )
                    Text(
                        text = "— cog_monje (E=1.38J)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 6.5.sp,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "— s_optico (E=0.74J)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 6.5.sp,
                        color = Color(0xFF00FF66)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid
                    val gridColor = Color(0xFF081E15)
                    for (i in 1..4) {
                        val gy = h * (i * 0.2f)
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 0.6f)
                    }
                    var gx = 25f
                    while (gx < w) {
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 0.5f)
                        gx += 35f
                    }

                    // Tick 20 vertical line (Suma en Caliente boundary)
                    val xTick = w * 0.42f
                    drawLine(
                        color = Color(0xFFFF3366),
                        start = Offset(xTick, 0f),
                        end = Offset(xTick, h),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )

                    // Curve 1: hub_central (Amber)
                    val pHub = Path().apply {
                        moveTo(0f, h * 0.84f)
                        // Smooth rise up to tick
                        cubicTo(
                            xTick * 0.35f, h * 0.83f,
                            xTick * 0.75f, h * 0.79f,
                            xTick, h * 0.76f
                        )
                        // Vertical jump at tick
                        lineTo(xTick, h * 0.28f)
                        // Exponential decay
                        cubicTo(
                            xTick + (w - xTick) * 0.25f, h * 0.35f,
                            xTick + (w - xTick) * 0.65f, h * 0.40f,
                            w, h * 0.42f
                        )
                    }
                    drawPath(pHub, Color(0xFFFFB300), style = Stroke(width = 1.8f, cap = StrokeCap.Round))

                    // Curve 2: cog_monje (Cyan)
                    val pCog = Path().apply {
                        moveTo(0f, h * 0.62f)
                        cubicTo(
                            xTick * 0.35f, h * 0.63f,
                            xTick * 0.75f, h * 0.65f,
                            xTick, h * 0.66f
                        )
                        // Spike jump at tick
                        lineTo(xTick, h * 0.18f)
                        // Gentle decay
                        cubicTo(
                            xTick + (w - xTick) * 0.28f, h * 0.26f,
                            xTick + (w - xTick) * 0.70f, h * 0.30f,
                            w, h * 0.32f
                        )
                    }
                    drawPath(pCog, Color(0xFF00E5FF), style = Stroke(width = 1.8f, cap = StrokeCap.Round))

                    // Curve 3: s_optico (Green)
                    val pOpt = Path().apply {
                        moveTo(0f, h * 0.48f)
                        cubicTo(
                            xTick * 0.4f, h * 0.51f,
                            xTick * 0.8f, h * 0.53f,
                            xTick, h * 0.54f
                        )
                        // Subtle step
                        lineTo(xTick, h * 0.62f)
                        cubicTo(
                            xTick + (w - xTick) * 0.4f, h * 0.63f,
                            xTick + (w - xTick) * 0.8f, h * 0.64f,
                            w, h * 0.65f
                        )
                    }
                    drawPath(pOpt, Color(0xFF00FF66), style = Stroke(width = 1.8f, cap = StrokeCap.Round))
                }
            }
        }

        // 2. Chart 2: ENTROPÍA H(t) [CIAN] vs PESO HEBBIANO W(e,) [ÁMBAR]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF103328), RoundedCornerShape(4.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 ENTROPÍA H(t) [CIAN] vs PESO HEBBIANO W(e,) [ÁMBAR]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = "Salto: 1.50 -> 2.78 bits",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF86EFAC)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val xTick = w * 0.42f

                    // Subtle grid
                    val gridColor = Color(0xFF081E15)
                    for (i in 1..3) {
                        val gy = h * (i * 0.25f)
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 0.5f)
                    }

                    // Cyan Solid Step Curve (Entropía Shannon)
                    val pEntropy = Path().apply {
                        moveTo(0f, h * 0.76f)
                        lineTo(xTick, h * 0.76f)
                        lineTo(xTick, h * 0.24f)
                        lineTo(w, h * 0.25f)
                    }
                    drawPath(pEntropy, Color(0xFF00E5FF), style = Stroke(width = 1.8f, cap = StrokeCap.Square))

                    // Amber Dashed Curve (Peso Hebbiano W(e))
                    val pHebb = Path().apply {
                        moveTo(0f, h * 0.90f)
                        cubicTo(
                            w * 0.35f, h * 0.80f,
                            w * 0.70f, h * 0.62f,
                            w, h * 0.55f
                        )
                    }
                    drawPath(
                        pHebb,
                        Color(0xFFFFB300),
                        style = Stroke(
                            width = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    )
                }
            }
        }

        // 3. Chart 3: ESPECTRO LAPLACIANO (STEM PLOT: stem(eig(L_H)))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF03070E), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF103328), RoundedCornerShape(4.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "▦ ESPECTRO LAPLACIANO (STEM PLOT: stem(eig(L_H)))",
                fontFamily = FontFamily.Monospace,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid
                    val gridColor = Color(0xFF081E15)
                    for (i in 1..3) {
                        val gy = h * (i * 0.25f)
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 0.5f)
                    }

                    // Baseline
                    val baseY = h * 0.92f
                    drawLine(Color(0xFF0C2B1D), Offset(0f, baseY), Offset(w, baseY), strokeWidth = 1f)

                    // 6 Eigenvalue Stems sorted ascending
                    val stemFractions = floatArrayOf(0.15f, 0.30f, 0.45f, 0.60f, 0.75f, 0.90f)
                    val stemHeights = floatArrayOf(0.14f, 0.22f, 0.36f, 0.54f, 0.72f, 0.90f)

                    for (i in stemFractions.indices) {
                        val sx = w * stemFractions[i]
                        val sh = stemHeights[i] * (baseY - 8f)
                        val sy = baseY - sh

                        // Stem line
                        drawLine(
                            color = Color(0xFF00FF66).copy(alpha = 0.85f),
                            start = Offset(sx, baseY),
                            end = Offset(sx, sy),
                            strokeWidth = 1.4f
                        )

                        // Glowing marker on top
                        drawCircle(Color(0xFF00FF66).copy(alpha = 0.30f), radius = 5f, center = Offset(sx, sy))
                        drawCircle(Color(0xFF00FF66), radius = 3.2f, center = Offset(sx, sy))
                        drawCircle(Color.White, radius = 1.2f, center = Offset(sx, sy))
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkMerkleBox(
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    telemetry: TelemetryState,
    modifier: Modifier = Modifier,
    onRunTest: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
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

            BenchmarkComparisonRow(
                label = "3M OPS (H_A ⊕ H_B):",
                boxedMs = 115,
                zeroBoxMs = 24,
                speedup = "4.8x"
            )

            BenchmarkComparisonRow(
                label = "10M OPS (MASSIVE):",
                boxedMs = 384,
                zeroBoxMs = 78,
                speedup = "4.9x"
            )

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
            MerkleRow("• Nodos Unificados:", "${nodes.size} Nodos (${nodes.firstOrNull()?.label ?: "s_optico"} E=${String.format(java.util.Locale.US, "%.2f", nodes.firstOrNull()?.energy ?: 0.05f)}J)")
            MerkleRow("• Hiperaristas Totales:", "${hyperedges.size} Aristas de orden superior")
            MerkleRow("• Sincronización Kuramoto:", "R(t) = ${String.format(java.util.Locale.US, "%.3f", telemetry.kuramotoOrderR)} (${telemetry.autopoieticStage})")
            MerkleRow("• Entropía de Shannon H(t):", "${String.format(java.util.Locale.US, "%.3f", telemetry.entropyShannon)} bits")
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
private fun MiniKonigBipartiteCanvas(
    nodes: List<HyperNode>,
    hyperedges: List<HyperEdge>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftX = width * 0.20f
        val rightX = width * 0.80f

        val displayNodes = nodes.take(8)
        val displayEdges = hyperedges.take(6)

        val nodeYs = displayNodes.mapIndexed { i, _ ->
            (height * 0.15f) + i * ((height * 0.70f) / (displayNodes.size - 1).coerceAtLeast(1))
        }
        val edgeYs = displayEdges.mapIndexed { j, _ ->
            (height * 0.20f) + j * ((height * 0.60f) / (displayEdges.size - 1).coerceAtLeast(1))
        }

        displayNodes.forEachIndexed { vIdx, vNode ->
            displayEdges.forEachIndexed { eIdx, eEdge ->
                if (eEdge.nodeIds.contains(vNode.id)) {
                    val edgeColor = Color(eEdge.colorHex)
                    drawLine(
                        edgeColor.copy(alpha = 0.5f),
                        Offset(leftX, nodeYs[vIdx]),
                        Offset(rightX, edgeYs[eIdx]),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }
            }
        }

        displayNodes.forEachIndexed { i, node ->
            val nCol = when {
                node.energy > 0.6f -> Color(0xFFFF4081)
                node.energy > 0.3f -> Color(0xFFFFB300)
                else -> Color(0xFF00FF66)
            }
            drawCircle(nCol, radius = 5.5f, center = Offset(leftX, nodeYs[i]))
            drawCircle(Color.Black, radius = 2f, center = Offset(leftX, nodeYs[i]))
        }

        displayEdges.forEachIndexed { j, edge ->
            val eCol = Color(edge.colorHex)
            drawRect(
                eCol,
                topLeft = Offset(rightX - 6f, edgeYs[j] - 6f),
                size = Size(12f, 12f)
            )
            drawRect(
                Color.Black,
                topLeft = Offset(rightX - 2f, edgeYs[j] - 2f),
                size = Size(4f, 4f)
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
