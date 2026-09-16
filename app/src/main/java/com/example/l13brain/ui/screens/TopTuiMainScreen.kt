package com.example.l13brain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.l13brain.ui.L13ViewModel
import com.example.l13brain.ui.TopTuiViewModel
import com.example.l13brain.ui.components.AjustesTab
import com.example.l13brain.ui.components.CrtShaderOverlay
import com.example.l13brain.ui.components.HypergraphSpectralCalculatorDialog
import com.example.l13brain.ui.components.ReplCalculatorTab
import com.example.l13brain.ui.components.TelemetriaTensoresTab
import com.example.l13brain.ui.components.TopHeader
import com.example.l13brain.ui.components.ToposcopioGrafoTab

@Composable
fun TopTuiMainScreen(
    topTuiViewModel: TopTuiViewModel,
    l13ViewModel: L13ViewModel,
    modifier: Modifier = Modifier
) {
    val state by topTuiViewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03070E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Top Header with Title, Badges (Artemis, Calc, Guide) and 4 Navigation Tabs
            TopHeader(
                selectedTab = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                onOpenArtemis = { topTuiViewModel.toggleArtemisDialog(true) },
                onOpenGuide = { topTuiViewModel.toggleGuideDialog(true) },
                onOpenCalculator = { topTuiViewModel.toggleCalculatorDialog(true) },
                statusMessage = state.statusMessage,
                tick = state.telemetry.totalTicks,
                isPaused = state.isSimulationPaused
            )

            // 2. Tab Content switching
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: 🌐 GRAFO (Screenshots 3 & 7)
                    ToposcopioGrafoTab(
                        nodes = state.nodes,
                        hyperedges = state.hyperedges,
                        telemetry = state.telemetry,
                        selectedNodeId = state.selectedNodeId,
                        onNodeSelected = { id -> topTuiViewModel.selectNode(id) },
                        onNodeDragged = { id, x, y -> topTuiViewModel.dragNode(id, x, y) },
                        onAmalgamatedSum = { topTuiViewModel.executeAmalgamatedSum() },
                        onBergeDual = { topTuiViewModel.executeBergeDual() },
                        onAutopoieticStep = { topTuiViewModel.executeAutopoieticStep() },
                        onInjectEnergy = { topTuiViewModel.injectEnergyToSelected(0.5f) },
                        onWolframMutation = { topTuiViewModel.executeWolframMutation() },
                        temporalRegime = state.temporalRegime,
                        filtrationEpsilon = state.filtrationEpsilon,
                        isSweepActive = state.isSweepActive,
                        betti0 = state.betti0,
                        betti1 = state.betti1,
                        betti2 = state.betti2,
                        persistenceIntervals = state.persistenceIntervals,
                        onRegimeChanged = { regime -> topTuiViewModel.setTemporalRegime(regime) },
                        onEpsilonChanged = { eps -> topTuiViewModel.setFiltrationEpsilon(eps) },
                        onToggleSweep = { topTuiViewModel.toggleSweepActive() }
                    )
                }
                1 -> {
                    // TAB 1: 💻 REPL (Screenshots 2, 4, 5, 6)
                    ReplCalculatorTab(
                        logs = state.replLogs,
                        nodes = state.nodes,
                        hyperedges = state.hyperedges,
                        telemetry = state.telemetry,
                        selectedNodeId = state.selectedNodeId,
                        onExecuteCommand = { cmd -> topTuiViewModel.executeReplCommand(cmd) },
                        onNodeSelected = { id -> topTuiViewModel.selectNode(id) },
                        onInjectEnergy = { id, amount -> topTuiViewModel.injectEnergyToNode(id, amount) },
                        onResetPhase = { id -> topTuiViewModel.resetNodePhase(id) }
                    )
                }
                2 -> {
                    // TAB 2: 📊 TELEMETRÍA & TENSORES (Screenshot 1)
                    TelemetriaTensoresTab(
                        telemetry = state.telemetry,
                        onHaltEngine = { topTuiViewModel.toggleSimulationPause() },
                        onPurgeSwap = { topTuiViewModel.executeReplCommand("sum --batch 50k") },
                        onTakeSnapshot = { topTuiViewModel.executeReplCommand("whos") }
                    )
                }
                3 -> {
                    // TAB 3: ⚙️ AJUSTES (Screenshot 8)
                    AjustesTab(
                        onExportPng = { topTuiViewModel.executeReplCommand("export --png") },
                        onSyncApi = { topTuiViewModel.toggleEndpointDialog(true) },
                        onDumpJson = { topTuiViewModel.executeReplCommand("dump --json") },
                        onGeminiDiagnosis = { topTuiViewModel.runAiDiagnosis() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // CRT Shader Overlay (subtle phosphor look)
        CrtShaderOverlay(
            config = state.shaderConfig,
            profile = state.crtProfile
        )

        // Artemis MCP Dialog (Screenshot 7)
        if (state.showArtemisDialog) {
            ArtemisConsoleDialog(
                onDismiss = { topTuiViewModel.toggleArtemisDialog(false) },
                onExecuteAction = { instruction ->
                    topTuiViewModel.executeArtemisInstruction(instruction)
                }
            )
        }

        // Hypergraph & Spectral Calculator Dialog (Screenshots 1, 2, 3, 5)
        if (state.showCalculatorDialog) {
            HypergraphSpectralCalculatorDialog(
                telemetry = state.telemetry,
                nodes = state.nodes,
                hyperedges = state.hyperedges,
                onDismiss = { topTuiViewModel.toggleCalculatorDialog(false) },
                onExecuteCommand = { cmd ->
                    topTuiViewModel.executeReplCommand(cmd)
                }
            )
        }

        // Guide Dialog
        if (state.showGuideDialog) {
            AnalyticalGuideDialog(
                onDismiss = { topTuiViewModel.toggleGuideDialog(false) }
            )
        }

        // Endpoint Dialog
        if (state.showEndpointDialog) {
            EndpointsConfigDialog(
                currentRest = state.restApiUrl,
                currentWss = state.wssApiUrl,
                onDismiss = { topTuiViewModel.toggleEndpointDialog(false) },
                onSave = { rest, wss -> topTuiViewModel.saveEndpointsAndConnect(rest, wss) }
            )
        }
    }
}

@Composable
fun EndpointsConfigDialog(
    currentRest: String,
    currentWss: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var rest by remember { mutableStateOf(currentRest) }
    var wss by remember { mutableStateOf(currentWss) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("CONFIGURACIÓN DE CONEXIÓN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ajusta los endpoints REST y WebSocket del VPS o Heroku Dyno:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White)

                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("URL REST (HTTPS / HTTP)", fontSize = 10.sp) },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wss,
                    onValueChange = { wss = it },
                    label = { Text("URL WebSocket (WSS / WS)", fontSize = 10.sp) },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { onSave(rest, wss) },
                colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF0A3A28), contentColor = Color(0xFF00FF66))
            ) {
                Text("Guardar y Conectar", fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = FontFamily.Monospace, color = Color.Gray)
            }
        },
        containerColor = Color(0xFF0B131C)
    )
}

@Composable
fun AnalyticalGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("GUÍA ANALÍTICA Y TEÓRICA L13", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("1. SUMA AMALGAMADA (H_A ⊕ H_B):", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00E5FF))
                Text("Unificación de hipergrafos persistentes sobre intersecciones no vacías (Pushouts categóricos). Los potenciales energéticos se fusionan aditivamente preservando la invariancia modal.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)

                Text("2. TRANSFORMACIÓN DUAL DE BERGE H*:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00E5FF))
                Text("Intercambia vértices y aristas (V* = E, E* = V). Permite mapear relaciones poliádicas como flujos de información disipativa.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)

                Text("3. SINCRONIZACIÓN DE KURAMOTO EN S¹:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00E5FF))
                Text("Dinámica de acoplamiento de orden superior (arXiv:2512.14729). R(t) mide el parámetro de coherencia del atractor autopoiético.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)

                Text("4. COMPLEJOS DE VIETORIS-RIPS Y VSA:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00E5FF))
                Text("Filtraciones topológicas computan β₀ y β₁ (números de Betti) con convolución circular FFT de vectores de 2048 dimensiones.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
            }
        },
        confirmButton = {
            ElevatedButton(onClick = onDismiss) {
                Text("Entendido", fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = Color(0xFF0B131C)
    )
}
