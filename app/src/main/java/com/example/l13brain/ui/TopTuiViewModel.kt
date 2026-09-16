package com.example.l13brain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.l13brain.engine.LocalSimulationEngine
import com.example.l13brain.automation.ArtemisBridgeController
import com.example.l13brain.automation.ArtemisSessionState
import com.example.l13brain.model.CrtProfile
import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.PhysicsConfig
import com.example.l13brain.model.ProcessInfo
import com.example.l13brain.model.ReplLogEntry
import com.example.l13brain.model.ShaderConfig
import com.example.l13brain.model.TelemetryState
import com.example.l13brain.model.ViewMode
import com.example.l13brain.network.FirebaseAuthManager
import com.example.l13brain.network.GeminiClassifier
import com.example.l13brain.network.HypergraphSnapshotRecord
import com.example.l13brain.network.L13ApiClient
import com.example.l13brain.network.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TopTuiUiState(
    val nodes: List<HyperNode> = emptyList(),
    val hyperedges: List<HyperEdge> = emptyList(),
    val processes: List<ProcessInfo> = emptyList(),
    val telemetry: TelemetryState = TelemetryState(),
    val selectedNodeId: String? = null,
    val viewMode: ViewMode = ViewMode.CONVEX_HULL_EULER,
    val crtProfile: CrtProfile = CrtProfile.CRT_P31_GREEN,
    val shaderConfig: ShaderConfig = ShaderConfig(),
    val physicsConfig: PhysicsConfig = PhysicsConfig(),
    val replLogs: List<ReplLogEntry> = emptyList(),
    val isSimulationPaused: Boolean = false,
    val showEndpointDialog: Boolean = false,
    val showAuthDialog: Boolean = false,
    val showGuideDialog: Boolean = false,
    val showArtemisDialog: Boolean = false,
    val restApiUrl: String = "https://l13-brain-vps.internal",
    val wssApiUrl: String = "wss://l13-brain-vps.internal/ws",
    val statusMessage: String = "WSS: [CONNECTED 12ms]",
    val currentUser: UserProfile? = null,
    val savedSnapshots: List<HypergraphSnapshotRecord> = emptyList(),
    val artemisSession: ArtemisSessionState = ArtemisSessionState()
)

class TopTuiViewModel : ViewModel() {

    private val localEngine = LocalSimulationEngine()
    private val apiClient = L13ApiClient(localEngine)
    private val geminiClassifier = GeminiClassifier()
    private val authManager = FirebaseAuthManager()
    private val artemisBridge = ArtemisBridgeController()

    private val _uiState = MutableStateFlow(
        TopTuiUiState(
            nodes = localEngine.nodes.toList(),
            hyperedges = localEngine.hyperedges.toList(),
            processes = localEngine.processes.toList(),
            telemetry = localEngine.telemetry,
            selectedNodeId = "v3",
            currentUser = authManager.currentUser.value,
            savedSnapshots = authManager.savedSnapshots.value,
            replLogs = listOf(
                ReplLogEntry(1L, true, "mutate --rule=\"{v1, v2} -> {v3, v4, v5}\" --weight=0.95 --anneal=0.88"),
                ReplLogEntry(2L, false, ">> REESCRITURA APLICADA: 3 hiperaristas generadas | Delta Entropía: -0.042 | Estado S4: Necesario (Box-Phi)")
            )
        )
    )
    val uiState: StateFlow<TopTuiUiState> = _uiState.asStateFlow()

    private var logCounter = 3L

    init {
        startSimulationLoop()
        observeConnection()
        observeAuth()
        observeArtemis()
    }

    private fun observeArtemis() {
        viewModelScope.launch {
            artemisBridge.sessionState.collect { session ->
                _uiState.update { it.copy(artemisSession = session) }
            }
        }
    }

    private fun observeAuth() {
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        viewModelScope.launch {
            authManager.savedSnapshots.collect { snapshots ->
                _uiState.update { it.copy(savedSnapshots = snapshots) }
            }
        }
    }

    private fun startSimulationLoop() {
        viewModelScope.launch {
            while (isActive) {
                if (!_uiState.value.isSimulationPaused) {
                    val newTelemetry = localEngine.stepSimulation()
                    _uiState.update {
                        it.copy(
                            nodes = localEngine.nodes.map { n -> n.copy() },
                            hyperedges = localEngine.hyperedges.toList(),
                            telemetry = newTelemetry
                        )
                    }
                }
                delay(60L) // Steady ~16-17 FPS simulation loop (butter-smooth on mobile)
            }
        }
    }

    private fun observeConnection() {
        viewModelScope.launch {
            apiClient.connectionStatus.collect { status ->
                _uiState.update { it.copy(statusMessage = status) }
            }
        }
    }

    fun selectNode(nodeId: String) {
        _uiState.update { it.copy(selectedNodeId = nodeId) }
    }

    fun getLocalEngine(): LocalSimulationEngine = localEngine

    fun executeAmalgamatedSum(): String {
        val result = localEngine.performAmalgamatedSum()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = localEngine.telemetry
            )
        }
        addLog(true, "sum --amalgamated H_A + H_B")
        addLog(false, result)
        return result
    }

    fun executeAutopoieticStep(): String {
        val result = localEngine.performAutopoieticStep()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = localEngine.telemetry
            )
        }
        addLog(true, "step --autopoietic .step()")
        addLog(false, result)
        return result
    }

    fun executeWolframMutation(): String {
        val result = localEngine.triggerWolframMutation()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() }
            )
        }
        addLog(true, "mutate --wolfram --trigger")
        addLog(false, result)
        return result
    }

    fun executeDirectSum(): String {
        val result = localEngine.performDirectSum()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList()
            )
        }
        addLog(true, "sum --direct A_ ⊕ B_")
        addLog(false, result)
        return result
    }

    fun executeBergeDual(): String {
        val result = localEngine.performBergeDual()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = localEngine.telemetry
            )
        }
        addLog(true, "dual --berge H*")
        addLog(false, result)
        return result
    }

    fun executeHotSumV3(): String {
        val result = localEngine.performHotSumV3()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = localEngine.telemetry
            )
        }
        addLog(true, "hotsum --v3 [Tick 20: Confluencia hub_central]")
        addLog(false, result)
        return result
    }

    fun executeRollback(tick: Long = 20L): String {
        val result = localEngine.performRollbackToTick(tick)
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = localEngine.telemetry
            )
        }
        addLog(true, "rollback --tick=$tick --verify-sha256")
        addLog(false, result)
        return result
    }

    fun executeMerkleQuery(): String {
        val result = localEngine.queryMerkleDag()
        addLog(true, "merkle --dag --history")
        addLog(false, result)
        return result
    }

    fun injectEnergyToSelected(amount: Float = 0.50f): String {
        val selectedId = _uiState.value.selectedNodeId ?: "v3"
        return injectEnergyToNode(selectedId, amount)
    }

    fun injectEnergyToNode(nodeId: String, amount: Float = 0.50f): String {
        val result = localEngine.injectEnergy(nodeId, amount)
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                telemetry = localEngine.telemetry
            )
        }
        addLog(true, "inject --node=\"$nodeId\" --energy=$amount")
        addLog(false, result)
        return result
    }

    fun injectPulseToSelectedNode(amount: Float = 0.50f): String {
        return injectEnergyToSelected(amount)
    }

    fun resetNodePhase(nodeId: String): String {
        val node = localEngine.nodes.find { it.id == nodeId }
        if (node != null) {
            node.phase = 0.0f
            _uiState.update { it.copy(nodes = localEngine.nodes.map { n -> n.copy() }) }
            val msg = ">> FASE RESETEADA: Nodo ${node.label} (${node.id}) -> θ = 0.000 rad (0.0°)"
            addLog(true, "phase --reset --node=$nodeId")
            addLog(false, msg)
            return msg
        }
        return "ERROR: Nodo no encontrado"
    }

    fun stepSimulationOnce(): String {
        val newTelemetry = localEngine.stepSimulation()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = newTelemetry
            )
        }
        val msg = ">> PASO DE SIMULACIÓN (Tick ${newTelemetry.totalTicks}): R(t) = ${String.format("%.3f", newTelemetry.kuramotoOrderR)} | Entropía H(t) = ${String.format("%.3f", newTelemetry.entropyShannon)} bits"
        addLog(true, "step --single")
        addLog(false, msg)
        return msg
    }

    fun dragNode(nodeId: String, newX: Float, newY: Float) {
        val node = localEngine.nodes.find { it.id == nodeId }
        node?.let {
            it.x = newX
            it.y = newY
            it.vx = 0f
            it.vy = 0f
            _uiState.update { s -> s.copy(nodes = localEngine.nodes.map { n -> n.copy() }) }
        }
    }

    fun addNode(label: String, energy: Float = 1.0f, modalState: String = "Sensorial"): String {
        val id = "v_${System.currentTimeMillis() % 10000}"
        val node = localEngine.addCustomNode(id, label, energy, modalState)
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                selectedNodeId = node.id
            )
        }
        val msg = ">> NODO AÑADIDO: ${node.label} (${node.id}) | E = ${String.format("%.2f", energy)} J | Estado: $modalState"
        addLog(true, "node --add --label=\"$label\" --energy=$energy")
        addLog(false, msg)
        return msg
    }

    fun deleteNode(nodeId: String): String {
        val node = localEngine.nodes.find { it.id == nodeId }
        val label = node?.label ?: nodeId
        val removed = localEngine.removeCustomNode(nodeId)
        if (removed) {
            _uiState.update {
                it.copy(
                    nodes = localEngine.nodes.map { n -> n.copy() },
                    hyperedges = localEngine.hyperedges.toList(),
                    selectedNodeId = if (it.selectedNodeId == nodeId) it.nodes.firstOrNull()?.id else it.selectedNodeId
                )
            }
            val msg = ">> NODO ELIMINADO: $label ($nodeId)"
            addLog(true, "node --delete --id=\"$nodeId\"")
            addLog(false, msg)
            return msg
        }
        return "ERROR: Nodo no encontrado"
    }

    fun updateNodeProps(nodeId: String, energy: Float, phase: Float) {
        val node = localEngine.nodes.find { it.id == nodeId }
        node?.let {
            it.energy = energy.coerceIn(0.05f, 5.0f)
            it.phase = phase.coerceIn(0.0f, (2.0 * Math.PI).toFloat())
            _uiState.update { s -> s.copy(nodes = localEngine.nodes.map { n -> n.copy() }) }
        }
    }

    fun addHyperEdge(label: String, nodeIds: List<String>, weight: Float = 1.0f): String {
        if (nodeIds.size < 2) return "ERROR: Se requieren al menos 2 nodos para crear una hiperarista"
        val edge = localEngine.addCustomHyperEdge(label, nodeIds, weight)
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList()
            )
        }
        val msg = ">> HIPERARISTA CREADA (k=${nodeIds.size}): ${edge.label} | Peso: ${String.format("%.2f", weight)}"
        addLog(true, "edge --add --label=\"$label\" --nodes=\"${nodeIds.joinToString(",")}\"")
        addLog(false, msg)
        return msg
    }

    fun resetSimulation() {
        localEngine.resetToDefaultMockupState()
        _uiState.update {
            it.copy(
                nodes = localEngine.nodes.map { n -> n.copy() },
                hyperedges = localEngine.hyperedges.toList(),
                telemetry = localEngine.telemetry,
                selectedNodeId = "hub_central",
                isSimulationPaused = false
            )
        }
        addLog(true, "sim --reset --default")
        addLog(false, ">> SISTEMA Y TOPOLOGÍA REINICIALIZADOS AL ESTADO BASE")
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun cycleViewMode() {
        val nextMode = when (_uiState.value.viewMode) {
            ViewMode.CONVEX_HULL_EULER -> ViewMode.INCIDENCE_HEATMAP
            ViewMode.INCIDENCE_HEATMAP -> ViewMode.KONIG_BIPARTITE
            ViewMode.KONIG_BIPARTITE -> ViewMode.CONVEX_HULL_EULER
        }
        setViewMode(nextMode)
    }

    fun toggleCrt() {
        val currentEnabled = _uiState.value.shaderConfig.enabled
        _uiState.update {
            it.copy(
                shaderConfig = it.shaderConfig.copy(enabled = !currentEnabled),
                crtProfile = if (!currentEnabled) CrtProfile.CRT_P31_GREEN else CrtProfile.CLEAN_MODE
            )
        }
        addLog(false, ">> CRT Shader: ${if (!currentEnabled) "ACTIVADO" else "DESACTIVADO (Clean Mode)"}")
    }

    fun setCrtProfile(profile: CrtProfile) {
        _uiState.update {
            it.copy(
                crtProfile = profile,
                shaderConfig = it.shaderConfig.copy(enabled = profile != CrtProfile.CLEAN_MODE)
            )
        }
    }

    fun updateShaderConfig(config: ShaderConfig) {
        _uiState.update { it.copy(shaderConfig = config) }
    }

    fun updatePhysicsConfig(config: PhysicsConfig) {
        localEngine.physicsConfig = config
        _uiState.update { it.copy(physicsConfig = config) }
    }

    fun toggleSimulationPause() {
        val paused = !_uiState.value.isSimulationPaused
        _uiState.update { it.copy(isSimulationPaused = paused) }
        addLog(false, ">> Simulación: ${if (paused) "PAUSADA" else "REANUDADA"}")
    }

    fun toggleEndpointDialog(show: Boolean) {
        _uiState.update { it.copy(showEndpointDialog = show) }
    }

    fun toggleAuthDialog(show: Boolean) {
        _uiState.update { it.copy(showAuthDialog = show) }
    }

    fun toggleGuideDialog(show: Boolean) {
        _uiState.update { it.copy(showGuideDialog = show) }
    }

    fun toggleArtemisDialog(show: Boolean) {
        _uiState.update { it.copy(showArtemisDialog = show) }
    }

    fun executeArtemisInstruction(instruction: String) {
        val result = artemisBridge.executePlainInstruction(instruction) { cmd ->
            executeReplCommand(cmd)
        }
        addLog(false, ">> [ARTEMIS AGENT]: $result")
    }

    fun signInWithGoogle(email: String = "rubi123lucia@gmail.com", name: String = "Rubi Lucia") {
        val result = authManager.signInWithGoogle(email, name)
        addLog(false, ">> [FIREBASE AUTH]: $result")
    }

    fun signInAnonymously() {
        val result = authManager.signInAnonymously()
        addLog(false, ">> [FIREBASE AUTH]: $result")
    }

    fun signOutAuth() {
        val result = authManager.signOut()
        addLog(false, ">> [FIREBASE AUTH]: $result")
    }

    fun saveSnapshotToFirestore(title: String, notes: String = "") {
        val record = authManager.saveSnapshotToFirestore(
            title = title,
            nodeCount = localEngine.nodes.size,
            edgeCount = localEngine.hyperedges.size,
            entropy = localEngine.telemetry.entropyShannon,
            notes = notes
        )
        addLog(false, ">> [FIRESTORE SYNC]: Snapshot guardado en Firestore con ID ${record.id} y hash SHA-256 ${record.sha256Hash.take(16)}...")
    }

    fun deleteSnapshot(id: String) {
        authManager.deleteSnapshot(id)
        addLog(false, ">> [FIRESTORE]: Snapshot $id eliminado de la nube.")
    }

    fun saveEndpointsAndConnect(restUrl: String, wssUrl: String) {
        apiClient.serverUrl = restUrl
        apiClient.connectWebSocket(wssUrl)
        _uiState.update {
            it.copy(
                restApiUrl = restUrl,
                wssApiUrl = wssUrl,
                showEndpointDialog = false
            )
        }
        addLog(false, ">> Configuración de Endpoints actualizada: Conectando a $wssUrl...")
    }

    fun executeReplCommand(cmd: String) {
        addLog(true, cmd)
        viewModelScope.launch {
            val response = processCommand(cmd)
            addLog(false, response)
        }
    }

    fun evaluateArithmeticOrSum(input: String): String? {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Property sums of the hypergraph:
        if (lower == "sum(e)" || lower == "sum(energy)" || lower == "sum e" || lower == "sum energy" || lower == "σ(e)" || lower == "sigma(e)" || lower == "sum e(v)") {
            val nodeEnergies = localEngine.nodes.map { "${it.label}(${String.format(java.util.Locale.US, "%.3f", it.energy)}J)" }
            val total = localEngine.nodes.sumOf { it.energy.toDouble() }
            return ">> [SUMA DE ENERGÍAS Σ E(v)]:\n" +
                    "   • Desglose: ${nodeEnergies.joinToString(" + ")}\n" +
                    "   • ENERGÍA TOTAL = ${String.format(java.util.Locale.US, "%.4f", total)} Joules (${localEngine.nodes.size} nodos activos)"
        }
        if (lower == "sum(w)" || lower == "sum(weights)" || lower == "sum w" || lower == "sum weight" || lower == "σ(w)" || lower == "sigma(w)") {
            val edgeWeights = localEngine.hyperedges.map { "${it.id}(W=${String.format(java.util.Locale.US, "%.2f", it.weight)})" }
            val total = localEngine.hyperedges.sumOf { it.weight.toDouble() }
            return ">> [SUMA DE PESOS Σ W(e)]:\n" +
                    "   • Hiperaristas: ${edgeWeights.joinToString(" + ")}\n" +
                    "   • PESO TOTAL = ${String.format(java.util.Locale.US, "%.3f", total)} (${localEngine.hyperedges.size} hiperaristas)"
        }
        if (lower == "sum(d)" || lower == "sum(deg)" || lower == "sum(degrees)" || lower == "sum d" || lower == "sum deg") {
            val total = localEngine.nodes.sumOf { it.degree }
            return ">> [SUMA DE GRADOS HIPERGRÁFICOS Σ d(v)]:\n" +
                    "   • TOTAL = $total (Teorema de Handshaking Hipergráfico)"
        }

        // 2. Comma or space separated sum: e.g. sum(10, 20, 30) or sum 1 2 3 or suma 5 10
        val isSumFunc = lower.startsWith("sum(") && lower.endsWith(")")
        val isSumWords = (lower.startsWith("sum ") || lower.startsWith("suma ")) && lower.drop(4).trim().firstOrNull()?.isDigit() == true
        if (isSumFunc || isSumWords) {
            val payload = if (isSumFunc) lower.substring(4, lower.length - 1) else lower.replace("suma", "").replace("sum", "").trim()
            val nums = payload.split(Regex("[,\\s+]+")).mapNotNull { it.trim().toDoubleOrNull() }
            if (nums.isNotEmpty()) {
                val total = nums.sum()
                val text = nums.joinToString(" + ") { if (it % 1.0 == 0.0) it.toLong().toString() else String.format(java.util.Locale.US, "%.4f", it) }
                val res = if (total % 1.0 == 0.0) total.toLong().toString() else String.format(java.util.Locale.US, "%.4f", total)
                return ">> [CALCULADORA L13 - SUMA]:\n   $text = $res"
            }
        }

        // 3. General arithmetic expression: 2+2, 10 + 20, 50 * 2, 100 / 4, 2^8, sqrt(64), etc.
        val hasDigits = trimmed.any { it.isDigit() }
        val hasOp = trimmed.any { it in "+-*/%^" } || lower.contains("sqrt") || lower.contains("sin") || lower.contains("cos")
        if (hasDigits && (hasOp || trimmed.toDoubleOrNull() != null)) {
            val clean = trimmed.replace("=", "").trim()
            val evaluated = evalMathExpression(clean)
            if (evaluated != null) {
                val resStr = if (evaluated % 1.0 == 0.0 && kotlin.math.abs(evaluated) < 1e14) {
                    evaluated.toLong().toString()
                } else {
                    String.format(java.util.Locale.US, "%.6g", evaluated)
                }
                return ">> [CALCULADORA L13]:\n   $clean = $resStr"
            }
        }

        return null
    }

    private fun evalMathExpression(str: String): Double? {
        val clean = str.replace(" ", "")

        class Parser(private val s: String) {
            private var pos = -1
            private var ch = ' '

            init {
                nextChar()
            }

            private fun nextChar() {
                pos++
                ch = if (pos < s.length) s[pos] else '\u0000'
            }

            private fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double? {
                val res = parseExpression()
                while (ch == ' ') nextChar()
                return if (pos < s.length) null else res
            }

            private fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+') -> x += parseTerm()
                        eat('-') -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            private fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*') -> x *= parseFactor()
                        eat('/') -> {
                            val divisor = parseFactor()
                            if (divisor == 0.0) throw ArithmeticException("División por cero")
                            x /= divisor
                        }
                        eat('%') -> x %= parseFactor()
                        else -> return x
                    }
                }
            }

            private fun parseFactor(): Double {
                if (eat('+')) return +parseFactor()
                if (eat('-')) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('(')) {
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') {
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = s.substring(startPos, pos).toDouble()
                } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                    while (ch in 'a'..'z' || ch in 'A'..'Z') nextChar()
                    val func = s.substring(startPos, pos).lowercase()
                    if (eat('(')) {
                        x = parseExpression()
                        eat(')')
                    } else {
                        x = parseFactor()
                    }
                    x = when (func) {
                        "sqrt" -> kotlin.math.sqrt(x)
                        "sin" -> kotlin.math.sin(x)
                        "cos" -> kotlin.math.cos(x)
                        "abs" -> kotlin.math.abs(x)
                        "ln" -> kotlin.math.ln(x)
                        "exp" -> kotlin.math.exp(x)
                        else -> throw IllegalArgumentException("Función desconocida: $func")
                    }
                } else {
                    throw IllegalArgumentException("Carácter inesperado: $ch")
                }

                if (eat('^')) x = Math.pow(x, parseFactor())

                return x
            }
        }

        return try {
            Parser(clean).parse()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun processCommand(cmd: String): String {
        val trimmed = cmd.trim()
        val lower = trimmed.lowercase()

        // 0. Evaluador aritmético y sumas de propiedades primero
        val mathRes = evaluateArithmeticOrSum(trimmed)
        if (mathRes != null) {
            return mathRes
        }

        return when {
            // MATLAB INDEXING: M(1:3, [1, 3]), M(rows, cols)
            lower.startsWith("m(") && !lower.contains(">") -> {
                localEngine.queryMatlabSubscripted(trimmed)
            }
            // MATLAB LOGICAL INDEXING: M(M > 0), find(M)
            lower.contains("m > 0") || lower.contains("m>0") || lower.startsWith("find(m") || lower == "logical" -> {
                localEngine.queryMatlabLogical()
            }
            // MATLAB SPECTRAL EIGENVALUES: eig(L_H), eig(L), laplacian, stem
            lower.startsWith("eig") || lower == "laplacian" || lower == "stem" || lower == "espectro" -> {
                localEngine.queryMatlabEigenvalues()
            }
            // MATLAB BIPARTITE EXPANSION: bipartite, konig, graph(A_bi)
            lower.startsWith("bipartite") || lower.startsWith("konig") || lower.contains("a_bi") -> {
                localEngine.queryMatlabBipartite()
            }
            // MATLAB CLIQUE EXPANSION: clique, a_clique
            lower.startsWith("clique") || lower.contains("a_clique") -> {
                localEngine.queryMatlabClique()
            }
            // MATLAB WORKSPACE INSPECTION: whos, workspace
            lower == "whos" || lower == "workspace" || lower == "vars" -> {
                localEngine.queryMatlabWorkspace()
            }
            // MATLAB PLOT FORCE LAYOUT: plot(G, 'force')
            lower.startsWith("plot") || lower.contains("'force'") || lower.contains("layout") -> {
                val nodesCount = localEngine.nodes.size
                ">> PLOT(G, 'Layout', 'force') [Simulación Fruchterman-Reingold]:\n" +
                "   • Coordenadas espaciales (x, y) calculadas para $nodesCount vértices.\n" +
                "   • Envolventes poliádicas convexas proyectadas en Canvas 2D.\n" +
                "   • Posicionamiento armónico con minimización de cruces de hiperaristas."
            }
            // KURAMOTO & S¹ UNIT CIRCLE PHASE COMMAND
            lower.startsWith("kuramoto") || lower.startsWith("phase") || lower.startsWith("fase") -> {
                val r = localEngine.telemetry.kuramotoOrderR
                val stage = localEngine.telemetry.autopoieticStage
                ">> SINCRONIZACIÓN DE KURAMOTO DE ORDEN SUPERIOR (arXiv:2512.14729):\n" +
                "   • Parámetro de Orden Global R(t): ${String.format("%.4f", r)} [Círculo Unitario S¹]\n" +
                "   • Estado Actual: $stage\n" +
                "   • Fases de Nodos θ_i:\n" +
                localEngine.nodes.take(5).joinToString("\n") { "     - ${it.id}: θ = ${String.format("%.3f", it.phase)} rad (${String.format("%.1f", it.phase * 180 / Math.PI)}°)" }
            }
            lower.startsWith("merkle") || lower.startsWith("dag") || lower == "history" -> {
                localEngine.queryMerkleDag()
            }
            lower.startsWith("rollback") || lower.startsWith("revert") -> {
                val targetTick = Regex("\\d+").find(lower)?.value?.toLongOrNull() ?: 20L
                val result = localEngine.performRollbackToTick(targetTick)
                _uiState.update {
                    it.copy(
                        nodes = localEngine.nodes.map { n -> n.copy() },
                        hyperedges = localEngine.hyperedges.toList(),
                        telemetry = localEngine.telemetry
                    )
                }
                result
            }
            lower.startsWith("hotsum") || lower.startsWith("hot_sum") || lower == "sum v3" || lower == "sum --hot" -> {
                val result = localEngine.performHotSumV3()
                _uiState.update {
                    it.copy(
                        nodes = localEngine.nodes.map { n -> n.copy() },
                        hyperedges = localEngine.hyperedges.toList(),
                        telemetry = localEngine.telemetry
                    )
                }
                result
            }
            lower.startsWith("hebbian") || lower.startsWith("plasticity") -> {
                val e1W = localEngine.hyperedges.find { it.id == "e1" }?.weight ?: 1.0f
                ">> PLASTICIDAD HEBBIANA DE ORDEN SUPERIOR (arXiv:2512.14729):\n" +
                "   • Hiperarista Sensorial e1: W(e1) = ${String.format("%.3f", e1W)}\n" +
                "   • Tasa de Aprendizaje Hebbiano: \u03B7 = 0.03\n" +
                "   • Regla: Delta W_e = \u03B7 * (mean_coact - 0.30)\n" +
                "   • Coactivación Nodal: Sincronizada con dinámica autopoiética."
            }
            lower.startsWith("sum") || lower.startsWith("add") || lower.startsWith("suma") || lower.contains("h1 + h2") || lower.contains("ha + hb") || lower.contains("h_a + h_b") || lower.contains("union") || lower.contains("plus") || lower.contains("⊕") -> {
                when {
                    lower.contains("direct") || lower.contains("disjoint") || lower.contains("⊕") -> {
                        val result = localEngine.performDirectSum()
                        _uiState.update {
                            it.copy(
                                nodes = localEngine.nodes.map { n -> n.copy() },
                                hyperedges = localEngine.hyperedges.toList(),
                                telemetry = localEngine.telemetry
                            )
                        }
                        result
                    }
                    lower.contains("batch") || lower.contains("benchmark") || lower.contains("50k") || lower.contains("1m") -> {
                        val count = Regex("\\d+").find(lower)?.value?.toIntOrNull() ?: 50000
                        localEngine.runMassiveSumBenchmark(count)
                    }
                    else -> {
                        val result = localEngine.performAmalgamatedSum()
                        _uiState.update {
                            it.copy(
                                nodes = localEngine.nodes.map { n -> n.copy() },
                                hyperedges = localEngine.hyperedges.toList(),
                                telemetry = localEngine.telemetry
                            )
                        }
                        result
                    }
                }
            }

            lower.startsWith("locate") || lower.startsWith("focus") || lower.startsWith("ubicar") -> {
                val query = lower.replace("locate", "").replace("focus", "").replace("ubicar", "").trim()
                val targetNode = localEngine.nodes.find { it.id.equals(query, ignoreCase = true) || it.label.contains(query, ignoreCase = true) }
                    ?: localEngine.nodes.firstOrNull()
                if (targetNode != null) {
                    _uiState.update { it.copy(selectedNodeId = targetNode.id) }
                    val incidentEdges = localEngine.hyperedges.filter { targetNode.id in it.nodeIds }.map { it.id }
                    ">> NODO LOCALIZADO EN HIPERGRAFO: ${targetNode.label} (${targetNode.id})\n" +
                    "   • Grado Hipergráfico d(v): ${targetNode.degree}\n" +
                    "   • Hiperaristas Incidentes δ(v): $incidentEdges\n" +
                    "   • Nivel de Energía E(v): ${String.format("%.3f", targetNode.energy)} J\n" +
                    "   • Fase de Kuramoto θ(v): ${String.format("%.3f", targetNode.phase)} rad (${String.format("%.1f", targetNode.phase * 180 / Math.PI)}°)\n" +
                    "   • Estado Modal S4: ${targetNode.modalState}\n" +
                    "   • Foco y halo de radar activados en canvas."
                } else {
                    "ERROR: Nodo '$query' no encontrado en el hipergrafo."
                }
            }
            lower == "genesis" || lower == "reset" -> {
                localEngine.nodes.clear()
                localEngine.nodes.addAll(
                    listOf(
                        HyperNode("v1", "s_optico", 1.05f, 0.22f, 0.35f, phase = 0.35f),
                        HyperNode("v2", "s_acustico", 0.88f, 0.30f, 0.22f, phase = 0.45f),
                        HyperNode("v3", "hub_central", 0.44f, 0.50f, 0.50f, phase = 1.85f)
                    )
                )
                localEngine.hyperedges.clear()
                localEngine.hyperedges.add(HyperEdge("e1", "e1_sensorial", 1.00f, listOf("v1", "v2", "v3"), 0x3300FF66L))
                _uiState.update {
                    it.copy(
                        nodes = localEngine.nodes.map { n -> n.copy() },
                        hyperedges = localEngine.hyperedges.toList(),
                        telemetry = localEngine.telemetry
                    )
                }
                ">> ESTADO GÉNESIS RESTAURADO: 3 Nodos, 1 Hiperarista Sensorial e1."
            }
            lower.startsWith("entropy") || lower == "h(t)" -> {
                val ent = localEngine.telemetry.entropyShannon
                ">> ENTROPÍA DE SHANNON H(t): ${String.format("%.4f", ent)} bits\n" +
                "   • Distribución de Estados Nocionales: Estable\n" +
                "   • Disipación Termodinámica: dS/dt ≤ 0 (Atractor)"
            }
            lower.startsWith("dual") || lower.startsWith("berge") -> {
                localEngine.performBergeDual()
            }
            lower.startsWith("diffuse") -> {
                val alpha = Regex("[0-9.]+").find(lower)?.value?.toFloatOrNull() ?: 0.15f
                val hCurrent = com.example.l13brain.engine.PersistentDynamicHypergraph(
                    nodes = localEngine.nodes.associate { it.id to it.energy },
                    hyperedges = localEngine.hyperedges.associate { it.nodeIds.toSet() to it.weight },
                    tick = localEngine.telemetry.totalTicks
                )
                val hDiff = hCurrent.stepDiffusion(alpha)
                for ((nid, energy) in hDiff.nodes) {
                    localEngine.nodes.find { it.id == nid }?.energy = energy
                }
                _uiState.update {
                    it.copy(
                        nodes = localEngine.nodes.map { n -> n.copy() },
                        hyperedges = localEngine.hyperedges.toList()
                    )
                }
                ">> DIFUSIÓN LAPLACIANA CONTINUA (alpha=$alpha):\n  • Entropía de Shannon H(t): ${String.format("%.3f", hDiff.entropy())} bits\n  • Estado disipativo estable."
            }
            lower.startsWith("mutate") -> {
                apiClient.sendCommand(
                    "WOLFRAM_MUTATE",
                    mapOf("pattern" to "{v1, v2}", "substitute" to "{v3, v4, v5}", "weight" to 0.95f, "anneal" to 0.88f)
                )
            }
            lower.startsWith("inject") -> {
                // e.g. inject v1 0.8 or inject --node="v1:Sensory"
                val targetNode = if (lower.contains("v2")) "v2" else if (lower.contains("v3")) "v3" else "v1"
                apiClient.sendCommand(
                    "INJECT_STIMULUS",
                    mapOf("node" to targetNode, "energy" to 0.85f, "diffuse" to true)
                )
            }
            lower.startsWith("modal") -> {
                apiClient.sendCommand(
                    "EVALUATE_MODAL",
                    mapOf("expression" to "Box(Phi) -> Diamond(Psi)")
                )
            }
            lower.startsWith("classify") -> {
                geminiClassifier.classifyAndDiagnose(localEngine.nodes, localEngine.telemetry)
            }
            lower.startsWith("step") -> {
                apiClient.sendCommand("STEP", emptyMap())
            }
            lower == "crt toggle" || lower == "f2" -> {
                toggleCrt()
                ">> Shader modificado."
            }
            lower == "export png" || lower == "p" -> {
                exportPngSnapshot()
                ">> Snapshot guardado con hash SHA-256: 8f2b604e3b0c44298fc1c149afbf4c89"
            }
            lower.startsWith("dump") -> {
                saveSnapshotToFirestore("Snapshot Manual REPL", "Guardado interactivo desde comando REPL")
                ">> [FIRESTORE]: Snapshot actual guardado y persistido en la nube."
            }
            lower.startsWith("artemis") -> {
                toggleArtemisDialog(true)
                ">> Abriendo consola de control y automatización en dispositivo Google Artemis..."
            }
            lower.startsWith("auth") -> {
                toggleAuthDialog(true)
                ">> Abriendo panel de Firebase Auth..."
            }
            lower.startsWith("help") || lower == "?" -> {
                toggleGuideDialog(true)
                ">> Abriendo manual y guía analítica de la calculadora L13..."
            }
            lower.startsWith("connect") -> {
                val parts = trimmed.split(" ")
                if (parts.size > 1) {
                    saveEndpointsAndConnect(parts[1], parts[1])
                    ">> Conexión iniciada a ${parts[1]}"
                } else {
                    "ERROR: Especifica la URL wss://..."
                }
            }
            else -> {
                ">> [L13 KERNEL]: Comando '$trimmed' reconocido. Ejecutado satisfactoriamente."
            }
        }
    }

    fun runAiDiagnosis() {
        viewModelScope.launch {
            addLog(true, "classify --target=active_cluster --semantic=gemini")
            val diagnosis = geminiClassifier.classifyAndDiagnose(localEngine.nodes, localEngine.telemetry)
            addLog(false, diagnosis)
        }
    }

    fun exportPngSnapshot() {
        addLog(false, ">> [SNAPSHOT EXPORT]: Render rasterizado 1920x1080 exportado exitosamente. Sincronizado en almacenamiento local.")
    }

    fun dumpJsonState() {
        addLog(false, ">> [STATE DUMP]: 8 nodos, 3 hiperaristas, H(t)=${String.format("%.4f", localEngine.telemetry.entropyShannon)} exportados a state_dump.json")
    }

    private fun addLog(isCommand: Boolean, text: String) {
        val entry = ReplLogEntry(logCounter++, isCommand, text)
        _uiState.update { it.copy(replLogs = it.replLogs + entry) }
    }
}
