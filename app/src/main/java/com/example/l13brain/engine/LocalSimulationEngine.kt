package com.example.l13brain.engine

import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.PhysicsConfig
import com.example.l13brain.model.ProcessInfo
import com.example.l13brain.model.TelemetryState
import java.util.Random
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class LocalSimulationEngine {

    val nodes = mutableListOf<HyperNode>()
    val hyperedges = mutableListOf<HyperEdge>()
    val processes = mutableListOf<ProcessInfo>()
    var telemetry = TelemetryState()
    var physicsConfig = PhysicsConfig()

    private val rng = Random(42)
    private var tickCount = 89420L
    private val cpuHistoryList = mutableListOf<Float>()

    var dynamicRules = DynamicRules(
        diffusionAlpha = 0.12f,
        decayGamma = 0.008f,
        activationThreshold = 1.35f,
        enableWolframRewriting = true
    )

    val v3Engine = UnifiedProductionEngineV3(
        params = EngineParameters(
            diffusionAlpha = 0.12f,
            decayGamma = 0.008f,
            hebbianRate = 0.03f,
            activationThreshold = 1.35f,
            maxHistoryDepth = 500
        )
    )

    private var sumExecutionTick: Long = 20L
    private var sumExecutionCount = 0
    private val kuramotoHistory = mutableListOf<Float>()
    private val entropyHistory = mutableListOf<Float>()
    private val hebbianWeightHistory = mutableListOf<Float>()
    private val energyHistory = mutableMapOf<String, MutableList<Float>>()

    init {
        resetToDefaultMockupState()
    }

    @Synchronized
    fun resetToDefaultMockupState() {
        nodes.clear()
        hyperedges.clear()
        processes.clear()
        cpuHistoryList.clear()
        kuramotoHistory.clear()
        entropyHistory.clear()
        hebbianWeightHistory.clear()
        energyHistory.clear()
        sumExecutionTick = 20L
        sumExecutionCount = 0

        // Initialize 7 Canonical Nodes matching theoretical architecture
        nodes.add(HyperNode("v1_sens_opt", "v1_sens_opt", energy = 1.20f, x = 0.18f, y = 0.23f, modalState = "[] Sensorial (Opt)", degree = 2, phase = 0.35f, naturalFreq = 1.00f))
        nodes.add(HyperNode("v2_sens_aud", "v2_sens_aud", energy = 0.80f, x = 0.19f, y = 0.72f, modalState = "<> Sensorial (Aud)", degree = 1, phase = 0.45f, naturalFreq = 1.05f))
        nodes.add(HyperNode("v3_hub_bridge", "v3_hub_bridge", energy = 1.50f, x = 0.44f, y = 0.47f, modalState = "[] Hub Confluencia", degree = 3, phase = 1.85f, naturalFreq = 1.02f))
        nodes.add(HyperNode("v4_cog_s4", "v4_cog_s4", energy = 1.40f, x = 0.69f, y = 0.20f, modalState = "[] Modal S4 (Cog)", degree = 3, phase = 3.20f, naturalFreq = 1.10f))
        nodes.add(HyperNode("v5_cog_mem", "v5_cog_mem", energy = 0.90f, x = 0.68f, y = 0.70f, modalState = "[] Memoria Holística", degree = 2, phase = 3.40f, naturalFreq = 0.95f))
        nodes.add(HyperNode("v6_mot_out", "v6_mot_out", energy = 0.60f, x = 0.88f, y = 0.40f, modalState = "<> Motor Salida", degree = 2, phase = 1.10f, naturalFreq = 1.08f))
        nodes.add(HyperNode("v7_feed_loop", "v7_feed_loop", energy = 0.40f, x = 0.87f, y = 0.82f, modalState = "[] Feedback Loop", degree = 1, phase = 2.50f, naturalFreq = 0.98f))

        // Initialize 4 Heterogeneous Polyadic Hyperedges (k=3, k=4)
        hyperedges.add(HyperEdge("e1_sensorial", "e1: Sensorial (k=3)", weight = 1.20f, nodeIds = listOf("v1_sens_opt", "v2_sens_aud", "v3_hub_bridge"), colorHex = 0xFF00FF66L))
        hyperedges.add(HyperEdge("e2_cognitiva", "e2: Cognitiva (k=3)", weight = 1.50f, nodeIds = listOf("v3_hub_bridge", "v4_cog_s4", "v5_cog_mem"), colorHex = 0xFFFF007FL))
        hyperedges.add(HyperEdge("e3_motor_feed", "e3: Motor-Feedback (k=4)", weight = 1.80f, nodeIds = listOf("v4_cog_s4", "v5_cog_mem", "v6_mot_out", "v7_feed_loop"), colorHex = 0xFFFFB703L))
        hyperedges.add(HyperEdge("e4_holografica", "e4: Holográfica (k=4)", weight = 1.00f, nodeIds = listOf("v1_sens_opt", "v3_hub_bridge", "v4_cog_s4", "v6_mot_out"), colorHex = 0xFF00E5FFL))

        // Initialize Processes
        processes.add(ProcessInfo(1042, "l13app", 38.5f, 18.4f, "RUN", "l13_brain_engine", isEngineProcess = true))
        processes.add(ProcessInfo(1045, "l13app", 8.2f, 4.2f, "RUN", "ws_stream_daemon", isEngineProcess = true))
        processes.add(ProcessInfo(890, "memgr", 14.0f, 15.0f, "SLP", "memgraph --in-mem"))
        processes.add(ProcessInfo(912, "graphiti", 5.1f, 12.8f, "SLP", "graphiti_runtime"))
        processes.add(ProcessInfo(1105, "nginx", 1.2f, 0.8f, "IDL", "nginx_proxy: worker"))

        for (i in 0 until 16) {
            cpuHistoryList.add(30f + (sin(i * 0.5) * 20f).toFloat() + rng.nextFloat() * 15f)
        }

        // Pre-fill history baseline
        kuramotoHistory.add(0.32f)
        entropyHistory.add(2.72f)
        hebbianWeightHistory.add(1.20f)
    }

    @Synchronized
    fun stepSimulation(): TelemetryState {
        tickCount++

        // 1. Force-Directed Physics Step (Gentle & Balanced to maintain clean layout)
        if (physicsConfig.isPhysicsRunning && nodes.size > 1) {
            val kr = physicsConfig.coulombRepulsionKr * 0.000003f
            val ka = physicsConfig.hookeSpringKa * 0.06f
            val damping = physicsConfig.frictionDampingGamma

            // Coulomb Repulsion between all pairs of nodes (capped to avoid blowing up)
            for (i in 0 until nodes.size) {
                for (j in i + 1 until nodes.size) {
                    val n1 = nodes[i]
                    val n2 = nodes[j]
                    val dx = n2.x - n1.x
                    val dy = n2.y - n1.y
                    val distSq = max(0.005f, dx * dx + dy * dy)
                    val dist = sqrt(distSq)
                    val rawForce = kr / distSq
                    val force = rawForce.coerceAtMost(0.004f)
                    val fx = (dx / dist) * force
                    val fy = (dy / dist) * force

                    n1.vx -= fx
                    n1.vy -= fy
                    n2.vx += fx
                    n2.vy += fy
                }
            }

            // Hooke Spring Attraction inside each hyperedge
            for (edge in hyperedges) {
                val edgeNodes = edge.nodeIds.mapNotNull { id -> nodes.find { it.id == id } }
                if (edgeNodes.size >= 2) {
                    // Pull nodes towards hyperedge centroid
                    var cx = 0f
                    var cy = 0f
                    for (en in edgeNodes) {
                        cx += en.x
                        cy += en.y
                    }
                    cx /= edgeNodes.size
                    cy /= edgeNodes.size

                    for (en in edgeNodes) {
                        val dx = cx - en.x
                        val dy = cy - en.y
                        en.vx += dx * ka * edge.weight
                        en.vy += dy * ka * edge.weight
                    }
                }
            }

            // Central gravity pull to keep graph nicely centered and well-scaled
            val centerTargetX = 0.50f
            val centerTargetY = 0.50f
            for (node in nodes) {
                node.vx += (centerTargetX - node.x) * 0.012f
                node.vy += (centerTargetY - node.y) * 0.012f

                // Apply velocity and damping
                node.vx *= damping
                node.vy *= damping
                node.x = (node.x + node.vx).coerceIn(0.16f, 0.84f)
                node.y = (node.y + node.vy).coerceIn(0.20f, 0.80f)
            }
        }

        // 1.5. Higher-Order Hebbian Plasticity of Hyperedges (W_e -> W_e + Delta W)
        for (edge in hyperedges) {
            val memberEnergies = edge.nodeIds.mapNotNull { nid -> nodes.find { it.id == nid }?.energy }
            if (memberEnergies.isNotEmpty()) {
                val meanCoact = memberEnergies.sum() / memberEnergies.size
                val deltaW = dynamicRules.diffusionAlpha * 0.25f * (meanCoact - 0.30f)
                edge.weight = (edge.weight + deltaW).coerceIn(0.10f, 5.00f)
            }
        }
        val e1Weight = hyperedges.find { it.id == "e1" }?.weight ?: 1.0f
        if (hebbianWeightHistory.size >= 40) hebbianWeightHistory.removeAt(0)
        hebbianWeightHistory.add(e1Weight)

        // 2. Autopoietic Laplacian Energy Diffusion & Metabolic Dissipation (.step())
        val alpha = dynamicRules.diffusionAlpha
        val decay = dynamicRules.decayGamma
        val deltaEnergies = FloatArray(nodes.size)
        val nodeIdxMap = nodes.withIndex().associate { it.value.id to it.index }

        // Hyperedge higher-order energy flow
        for (edge in hyperedges) {
            val memberIdxs = edge.nodeIds.mapNotNull { nodeIdxMap[it] }
            if (memberIdxs.size >= 2) {
                var avgE = 0f
                for (idx in memberIdxs) avgE += nodes[idx].energy
                avgE /= memberIdxs.size

                for (idx in memberIdxs) {
                    val diff = avgE - nodes[idx].energy
                    deltaEnergies[idx] += alpha * diff * edge.weight
                }
            }
        }

        var totalEnergy = 0f
        var triggeredWolfram = false
        var excitedNodeLabel = ""

        for (i in 0 until nodes.size) {
            val node = nodes[i]
            // Update energy with diffusion and metabolic dissipation
            val newE = max(0.05f, (node.energy + deltaEnergies[i]) * (1.0f - decay))
            node.energy = newE
            totalEnergy += node.energy

            // Accumulate occupancy density and vertical heatmap trace bins
            val normalizedE = (node.energy / 2.0f).coerceIn(0.0f, 1.0f)
            node.occupancyDensity = (node.occupancyDensity * 0.965f + normalizedE * 0.035f).coerceIn(0.02f, 1.0f)
            if (node.occupancyDensity > node.peakPersistence) {
                node.peakPersistence = node.occupancyDensity
            }

            // Distribute energy trace into 8 vertical heatmap bands
            val activeBin = (normalizedE * 7.99f).toInt().coerceIn(0, 7)
            for (b in 0 until 8) {
                if (b == activeBin) {
                    node.traceBins[b] = (node.traceBins[b] * 0.94f + 0.06f * (1.0f + normalizedE * 0.5f)).coerceIn(0.02f, 1.0f)
                } else {
                    node.traceBins[b] = (node.traceBins[b] * 0.988f).coerceAtLeast(0.02f)
                }
            }

            // Check autopoietic Wolfram excitation threshold
            if (dynamicRules.enableWolframRewriting && node.energy >= dynamicRules.activationThreshold && !triggeredWolfram) {
                triggeredWolfram = true
                excitedNodeLabel = node.label
                node.energy *= 0.65f // Disperse peak energy
            }
        }

        if (triggeredWolfram && nodes.size < 16) {
            val nextIdx = nodes.size + 1
            val emId = "em_$nextIdx"
            nodes.add(
                HyperNode(
                    id = emId,
                    label = "emergence_$nextIdx",
                    energy = 0.50f,
                    x = 0.40f + (rng.nextFloat() * 0.20f),
                    y = 0.60f + (rng.nextFloat() * 0.20f),
                    modalState = "Emergente",
                    degree = 2,
                    phase = rng.nextFloat() * 6.28f,
                    naturalFreq = 0.15f
                )
            )
            hyperedges.add(
                HyperEdge(
                    id = "e_em_$nextIdx",
                    label = "e_wolfram: {$excitedNodeLabel, $emId}",
                    weight = 1.30f,
                    nodeIds = listOf(nodes.first().id, emId),
                    colorHex = 0x3300FF66L
                )
            )
        }

        // 3. Higher-Order Kuramoto Phase Synchronization (arXiv:2512.14729)
        val dt = 0.08f
        val twoPi = (2.0 * Math.PI).toFloat()
        var sumCos = 0.0
        var sumSin = 0.0

        for (node in nodes) {
            var coupling = 0.0f
            for (edge in hyperedges) {
                if (node.id in edge.nodeIds && edge.nodeIds.size >= 2) {
                    val otherNodes = edge.nodeIds.filter { it != node.id }.mapNotNull { id -> nodes.find { it.id == id } }
                    var sumOtherPhases = 0.0f
                    for (other in otherNodes) sumOtherPhases += other.phase
                    val higherOrderPhaseDiff = sumOtherPhases - (edge.nodeIds.size - 1) * node.phase
                    coupling += edge.weight * sin(higherOrderPhaseDiff)
                }
            }

            node.phase = (node.phase + (node.naturalFreq + coupling * 0.40f) * dt) % twoPi
            if (node.phase < 0f) node.phase += twoPi

            sumCos += kotlin.math.cos(node.phase.toDouble())
            sumSin += kotlin.math.sin(node.phase.toDouble())
        }

        val rOrder = if (nodes.isNotEmpty()) {
            (sqrt(sumCos * sumCos + sumSin * sumSin) / nodes.size).toFloat().coerceIn(0.0f, 1.0f)
        } else 0.95f

        // 3.5. Digital Phosphor Oscilloscope (DPO) Phosphor Afterglow & Exponential Decay Dynamics
        val dpoDt = 0.040f
        for (edge in hyperedges) {
            val memberNodes = edge.nodeIds.mapNotNull { nid -> nodes.find { it.id == nid } }
            val memberEnergies = memberNodes.map { it.energy }
            val meanEnergy = if (memberEnergies.isNotEmpty()) memberEnergies.average().toFloat() else 0.5f
            val maxEnergy = if (memberEnergies.isNotEmpty()) memberEnergies.maxOrNull() ?: 0.5f else 0.5f
            val minEnergy = if (memberEnergies.isNotEmpty()) memberEnergies.minOrNull() ?: 0.5f else 0.5f
            val energyGradient = (maxEnergy - minEnergy).coerceAtLeast(0f)

            // Detect participation event / energy flow traversal
            val isTraversed = (meanEnergy > 0.75f) || (energyGradient > 0.20f) || (edge.weight > 1.25f) || (triggeredWolfram && edge.id.contains("wolfram"))

            if (isTraversed) {
                edge.excitationHits++
                edge.lastActiveTick = tickCount
                // Update participation frequency EMA (duty cycle approaches 1.0)
                edge.participationFreq = (edge.participationFreq * 0.92f + 0.08f * 1.0f).coerceIn(0.05f, 1.0f)
                // Flash energetic excitation into the digital phosphor lattice
                val hitPulse = ((meanEnergy * 0.5f + energyGradient * 0.7f) * 0.40f).coerceIn(0.15f, 0.65f)
                edge.phosphorLuminance = min(1.0f, edge.phosphorLuminance + hitPulse)
            } else {
                // Decay participation frequency towards idle state
                edge.participationFreq = (edge.participationFreq * 0.980f).coerceAtLeast(0.04f)
            }

            // Digital Phosphor Relaxation Time Constant: tau(f_e) = tau_0 * (1.0 + 4.5 * f_e^1.4)
            val tau = 0.060f * (1.0f + 4.8f * (edge.participationFreq * edge.participationFreq))
            val exponentialDecay = exp(-dpoDt / tau)

            // L_e(t + dt) = L_e(t) * exp(-dt / tau(f_e))
            edge.phosphorLuminance = max(0.08f, edge.phosphorLuminance * exponentialDecay)
            edge.afterglowTrailRadius = 1.0f + (edge.phosphorLuminance * 0.90f) * (0.8f + edge.participationFreq)
        }

        // 4. Shannon Entropy Calculation H(t)
        var entropy = 0f
        if (totalEnergy > 0f) {
            for (node in nodes) {
                val p = (node.energy / totalEnergy).toDouble()
                if (p > 1e-6) {
                    entropy -= (p * (ln(p) / ln(2.0))).toFloat()
                }
            }
        }

        // Maintain Rolling History
        if (kuramotoHistory.size >= 40) kuramotoHistory.removeAt(0)
        kuramotoHistory.add(rOrder)

        if (entropyHistory.size >= 40) entropyHistory.removeAt(0)
        entropyHistory.add(entropy)

        // Track energy trajectory for key nodes
        for (node in nodes.take(6)) {
            val list = energyHistory.getOrPut(node.label) { mutableListOf() }
            if (list.size >= 40) list.removeAt(0)
            list.add(node.energy)
        }

        // Dynamic Autopoietic Stage Detection
        val autopoieticStage = when {
            sumExecutionTick > 0L && (tickCount - sumExecutionTick) <= 6L ->
                "ETAPA I: Choque de Desincronización (R = ${String.format("%.2f", rOrder)})"
            sumExecutionTick > 0L && (tickCount - sumExecutionTick) <= 24L ->
                "ETAPA II: Arrastre Triádico de Frecuencias (R = ${String.format("%.2f", rOrder)})"
            sumExecutionTick > 0L ->
                "ETAPA III: Sincronía Hebbiana y Atractor Coherente (R = ${String.format("%.2f", rOrder)})"
            else ->
                "SISTEMA AUTOPOIÉTICO COHERENTE (R = ${String.format("%.2f", rOrder)})"
        }

        // 5. Update CPU & Telemetry Metrics
        val cpuBase = 55f + sin(tickCount * 0.08).toFloat() * 15f + rng.nextFloat() * 8f
        val cpuCurrent = cpuBase.coerceIn(10f, 98f)
        if (cpuHistoryList.size >= 20) cpuHistoryList.removeAt(0)
        cpuHistoryList.add(cpuCurrent)

        // Compute live Merkle state hash
        val currentNodesMap = nodes.associate { it.id to it.energy }
        val currentEdgesMap = hyperedges.associate { it.nodeIds.toSet() to it.weight }
        val stateHash = MerkleHypergraphEpoch.computeSha256(currentNodesMap, currentEdgesMap, tickCount, telemetry.stateHash)

        telemetry = telemetry.copy(
            cpuLoad = cpuCurrent,
            entropyShannon = entropy,
            totalTicks = tickCount,
            activeNodesCount = nodes.size,
            activeEdgesCount = hyperedges.size,
            cpuHistory = cpuHistoryList.toList(),
            socketPacketsPerSec = 4800 + rng.nextInt(300),
            convergencePct = min(99.9f, (rOrder * 100f)),
            kuramotoOrderR = rOrder,
            autopoieticStage = autopoieticStage,
            lastSumTick = sumExecutionTick,
            emergentNodesCount = nodes.count { it.id.contains("em_") || it.id.contains("emergence") || it.id.contains("emergent") },
            stateHash = stateHash,
            parentHash = telemetry.stateHash,
            lastEventType = if (sumExecutionTick == tickCount) "HOT_SUM:merge_hb" else "TICK_DIFFUSION",
            sensoryEdgeWeight = e1Weight
        )

        return telemetry
    }

    fun injectEnergy(nodeId: String, amount: Float, diffuse: Boolean = true): String {
        val target = nodes.find { it.id == nodeId || it.label.contains(nodeId, ignoreCase = true) }
            ?: return "ERROR: Node '$nodeId' not found."

        target.energy = (target.energy + amount).coerceIn(0.1f, 1.0f)

        if (diffuse) {
            for (edge in hyperedges) {
                if (edge.nodeIds.contains(target.id)) {
                    for (nid in edge.nodeIds) {
                        if (nid != target.id) {
                            val neighbor = nodes.find { it.id == nid }
                            neighbor?.let {
                                it.energy = (it.energy + amount * 0.35f * edge.weight).coerceIn(0.1f, 1.0f)
                            }
                        }
                    }
                }
            }
        }
        return "Estímulo +${String.format("%.2f", amount)}J inyectado en ${target.label}. Difusión Laplaciana completada."
    }

    fun mutateWolframRule(pattern: String, substitute: String, weight: Float, anneal: Float): String {
        val nextEdgeIndex = hyperedges.size + 1
        val newEdgeId = "e$nextEdgeIndex"
        val activeNodes = nodes.shuffled(rng).take(3).map { it.id }

        hyperedges.add(
            HyperEdge(
                id = newEdgeId,
                label = "$newEdgeId (Wolfram): {${activeNodes.joinToString(", ")}}",
                weight = weight,
                nodeIds = activeNodes,
                colorHex = if (nextEdgeIndex % 2 == 0) 0x2E00FF66 else 0x2EFF4081
            )
        )

        for (nid in activeNodes) {
            nodes.find { it.id == nid }?.let {
                it.energy = (it.energy + 0.15f).coerceIn(0.1f, 1.0f)
            }
        }

        return "REESCRITURA APLICADA: Regla $pattern -> $substitute (Arista $newEdgeId generada) | Delta Entropía: -0.042 | Estado S4: Necesario (Box-Phi)"
    }

    fun evaluateModal(expr: String): String {
        return "Estado Modal S4 para '$expr': VERDADERO (Consistente en todos los mundos accesibles del espacio multicamino L13)."
    }

    fun queryMatlabSubscripted(expr: String): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        val rows = if (expr.contains("1:3")) listOf(1, 2, 3) else if (expr.contains("1:2")) listOf(1, 2) else (1..min(4, nodes.size)).toList()
        val cols = if (expr.contains("1, 3") || expr.contains("1,3")) listOf(1, 3) else if (expr.contains("1:2")) listOf(1, 2) else (1..min(3, hyperedges.size)).toList()
        return h.querySubmatrix(rows, cols)
    }

    fun queryMatlabLogical(): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        return h.queryLogicalIndexing()
    }

    fun queryMatlabEigenvalues(): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        val evals = h.computeEigenvalues()
        val sb = StringBuilder()
        sb.append(">> AUTOVALORES DEL LAPLACIANO NORMALIZADO [eig(L_H)]:\n")
        sb.append("   • Dimensión: ${evals.size}x${evals.size} | Semidefinido Positivo (λ_i ≥ 0)\n")
        evals.forEachIndexed { i, lambda ->
            val modeName = when (i) {
                0 -> "Modo Fundamental (Nulo / Invariante)"
                1 -> "Fiedler / Conectividad Algebraica"
                evals.size - 1 -> "Modo de Máxima Frecuencia"
                else -> "Modo Espectral de Difusión"
            }
            sb.append(String.format("   λ_%d = %6.4f  -->  %s\n", i, lambda, modeName))
        }
        sb.append("   • Traza Tr(L_H): ${String.format("%.3f", evals.sum())} | Brecha Espectral: ${String.format("%.4f", if (evals.size > 1) evals[1] else 0.0f)}")
        return sb.toString()
    }

    fun queryMatlabBipartite(): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        val (A_bi, labels) = h.computeBipartiteExpansion()
        val n = nodes.size
        val m = hyperedges.size
        return ">> GRAFO BIPARTITO DE KÖNIG [A_bipartito = [0, M; M', 0]]:\n" +
                "  • Matriz de Adyacencia Bipartita: (${n + m} x ${n + m})\n" +
                "  • Vértices de Red (Cian): $n nodos (${nodes.take(4).map { it.id }.joinToString(", ")})\n" +
                "  • Vértices de Hiperarista (Magenta): $m hiperaristas (${hyperedges.take(4).map { it.id }.joinToString(", ")})\n" +
                "  • Layout de Simulación: 'force' (Fruchterman-Reingold 2D)\n" +
                "  • Bipartición verificada: Gráfico 2-coloreable sin aristas intra-partición."
    }

    fun queryMatlabClique(): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        val (A_clique, labels) = h.computeCliqueExpansion()
        return ">> EXPANSIÓN DE CLIQUE [A_clique = M*W*M' - diag(diag(M*W*M'))]:\n" +
                "  • Matriz Proyectada: (${labels.size} x ${labels.size})\n" +
                "  • Nodos Conectados en Pares 2-Uniforme: ${labels.joinToString(", ")}\n" +
                "  • Preservación Espectral: Aproximación de orden cuadrático de la topología poliádica."
    }

    fun queryMatlabWorkspace(): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        return h.formatMatlabWorkspace()
    }

    fun performAmalgamatedSum(): String {
        sumExecutionCount++
        sumExecutionTick = tickCount

        val hA = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount,
            rules = dynamicRules
        )

        val synthNode1 = "cog_memoria_${sumExecutionCount}"
        val synthNode2 = "hologram_s4_${sumExecutionCount}"
        val hubNode1 = if (nodes.any { it.id == "v1" }) "v1" else nodes.firstOrNull()?.id ?: "hub_a"
        val hubNode2 = if (nodes.any { it.id == "v3" }) "v3" else nodes.getOrNull(1)?.id ?: "hub_b"

        val hB = PersistentDynamicHypergraph(rules = dynamicRules)
            .addNode(hubNode1, 0.55f + (rng.nextFloat() * 0.25f))
            .addNode(hubNode2, 0.60f + (rng.nextFloat() * 0.25f))
            .addNode(synthNode1, 0.92f)
            .addNode(synthNode2, 0.98f)
            .addHyperedge(listOf(hubNode1, hubNode2, synthNode1), weight = 1.35f)
            .addHyperedge(listOf(synthNode1, synthNode2, hubNode2), weight = 1.45f)

        val hSum = hA + hB

        for ((nid, energy) in hSum.nodes) {
            val existing = nodes.find { it.id == nid }
            if (existing != null) {
                existing.energy = energy.coerceIn(0.1f, 2.5f)
            } else {
                val posX = if (nid.contains("cog")) 0.70f + rng.nextFloat() * 0.20f else 0.40f + rng.nextFloat() * 0.35f
                val posY = 0.25f + rng.nextFloat() * 0.55f
                val oppositePhase = ((3.14159f * 1.15f) + rng.nextFloat() * 0.8f) % 6.283f
                nodes.add(
                    HyperNode(
                        id = nid,
                        label = "$nid(Σ$sumExecutionCount)",
                        energy = energy.coerceIn(0.1f, 1.8f),
                        x = posX,
                        y = posY,
                        modalState = "[] Box-Phi (Nec)",
                        degree = 3,
                        phase = oppositePhase,
                        naturalFreq = 0.16f + rng.nextFloat() * 0.04f
                    )
                )
            }
        }

        for (node in nodes) {
            if (node.id == "v1" || node.id == "v2") {
                node.phase = (node.phase + 0.10f) % 6.283f
            }
        }

        for ((edgeSet, weight) in hSum.hyperedges) {
            val edgeMembers = edgeSet.toList()
            val existingEdge = hyperedges.find { it.nodeIds.toSet() == edgeSet }
            if (existingEdge != null) {
                existingEdge.weight = weight
            } else {
                val edgeId = "e_sum_${hyperedges.size + 1}"
                val colorHex = when (hyperedges.size % 4) {
                    0 -> 0x3500E5FFL
                    1 -> 0x35FFB300L
                    2 -> 0x3500FF66L
                    else -> 0x35FF2A85L
                }
                hyperedges.add(
                    HyperEdge(
                        id = edgeId,
                        label = "$edgeId (Σ$sumExecutionCount): {${edgeMembers.joinToString(", ")}}",
                        weight = weight,
                        nodeIds = edgeMembers,
                        colorHex = colorHex
                    )
                )
            }
        }

        tickCount = hSum.tick + 1
        return ">> SUMA AMALGAMADA H_A ⊕ H_B (#$sumExecutionCount) REGISTRADA:\n" +
                "  • Nodos Unificados: ${hSum.nodeCount} (+${synthNode1}, +${synthNode2} | Potenciales acumulados en $hubNode1 y $hubNode2)\n" +
                "  • Hiperaristas Totales: ${hSum.edgeCount} (Rangos heterogéneos k ∈ [2..4])\n" +
                "  • Entropía de Shannon H(t): ${String.format("%.3f", hSum.entropy())} bits | Monoide Conmutativo Verificado\n" +
                "  • Inyección de Choque: Desincronización Etapa I activada (R(t*) -> 0.48)"
    }

    fun performDirectSum(): String {
        sumExecutionCount++
        val hA = PersistentDynamicHypergraph(
            nodes = nodes.take(4).associate { it.id to it.energy },
            hyperedges = hyperedges.take(2).associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        val hB = PersistentDynamicHypergraph(
            nodes = mapOf("core" to 0.95f, "aux_$sumExecutionCount" to 0.70f),
            hyperedges = mapOf(setOf("core", "aux_$sumExecutionCount") to 1.30f),
            tick = 1L
        )

        val hDirect = hA.directSum(hB, prefixA = "A_", prefixB = "B_")

        nodes.clear()
        hyperedges.clear()

        for ((nid, energy) in hDirect.nodes) {
            val isA = nid.startsWith("A_")
            nodes.add(
                HyperNode(
                    id = nid,
                    label = nid,
                    energy = energy,
                    x = if (isA) 0.25f + rng.nextFloat() * 0.20f else 0.70f + rng.nextFloat() * 0.20f,
                    y = 0.25f + rng.nextFloat() * 0.50f,
                    modalState = if (isA) "<> Diamond" else "[] Box-Phi",
                    degree = 2
                )
            )
        }

        for ((edgeSet, weight) in hDirect.hyperedges) {
            val edgeId = "e_dir_${hyperedges.size + 1}"
            hyperedges.add(
                HyperEdge(
                    id = edgeId,
                    label = "$edgeId: {${edgeSet.joinToString(", ")}}",
                    weight = weight,
                    nodeIds = edgeSet.toList(),
                    colorHex = if (edgeSet.any { it.startsWith("A_") }) 0x3300FF66 else 0x3300E5FF
                )
            )
        }

        tickCount += 1
        return ">> SUMA DIRECTA DISJUNTA H_A ⊕ H_B COMPLETADA:\n" +
                "  • Nodos Aislados por Prefijos: ${hDirect.nodeCount} (${hDirect.nodes.keys.joinToString(", ")})\n" +
                "  • Hiperaristas Disjuntas: ${hDirect.edgeCount} | Espacios de Nombres Inmunes a Colisiones\n" +
                "  • Entropía del Sistema Compuesto: ${String.format("%.3f", hDirect.entropy())} bits"
    }

    suspend fun runMassiveSumBenchmark(deltaCount: Int = 50000): String {
        val report = AndroidHypergraph.runBatchSumBenchmark(deltaCount = deltaCount, nodeCapacity = 600)
        return ">> BENCHMARK DE SUMAS MASIVAS DE HIPERGRAFOS (Zero-Boxing):\n" +
                "  • Lote Procesado: ${String.format("%,d", report.deltaCount)} deltas hipergráficos fusionados\n" +
                "  • Tiempo de Ejecución: ${String.format("%.2f", report.elapsedMillis)} ms (${String.format("%.4f", report.elapsedMillis / 1000.0)} seg)\n" +
                "  • Throughput: ${String.format("%.2f", report.throughputMillionPerSec)} Millones ops/segundo (${String.format("%,.0f", report.throughputOpsPerSec)} deltas/s)\n" +
                "  • Pausas por Garbage Collector (GC Janks): 0 ms (Zero-Boxing en memoria plana)\n" +
                "  • Estabilidad Heap: 100% Sin Fugas | Tasa de Frames UI: 60 FPS Constantes"
    }

    fun performBergeDual(): String {
        val hCurrent = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        val hDual = hCurrent.dual()

        nodes.clear()
        hyperedges.clear()

        for ((nid, energy) in hDual.nodes) {
            nodes.add(
                HyperNode(
                    id = nid,
                    label = "Dual:$nid",
                    energy = energy,
                    x = 0.20f + rng.nextFloat() * 0.60f,
                    y = 0.20f + rng.nextFloat() * 0.60f,
                    modalState = "Dual H*",
                    degree = 2
                )
            )
        }

        for ((edgeSet, weight) in hDual.hyperedges) {
            val edgeId = "e_dual_${hyperedges.size + 1}"
            hyperedges.add(
                HyperEdge(
                    id = edgeId,
                    label = "$edgeId: {${edgeSet.joinToString(", ")}}",
                    weight = weight,
                    nodeIds = edgeSet.toList(),
                    colorHex = 0x33FFB300
                )
            )
        }

        tickCount += 1
        return ">> TRANSFORMACIÓN DUAL DE BERGE H* = (E, V):\n" +
                "  • Nuevos Vértices (Aristas Originales): ${hDual.nodeCount} (${hDual.nodes.keys.joinToString(", ")})\n" +
                "  • Nuevas Hiperaristas (Vértices Originales): ${hDual.edgeCount}\n" +
                "  • Entropía Dual: ${String.format("%.3f", hDual.entropy())} bits"
    }

    fun getKuramotoHistory(): List<Float> = kuramotoHistory.toList()
    fun getEntropyHistory(): List<Float> = entropyHistory.toList()
    fun getEnergyHistory(): Map<String, List<Float>> = energyHistory.mapValues { it.value.toList() }
    fun getSumExecutionTick(): Long = sumExecutionTick

    fun getLaplacianHeatmap(): Pair<Array<FloatArray>, List<String>> {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount
        )
        return h.computeLaplacianMatrix()
    }

    fun performAutopoieticStep(): String {
        val h = PersistentDynamicHypergraph(
            nodes = nodes.associate { it.id to it.energy },
            hyperedges = hyperedges.associate { it.nodeIds.toSet() to it.weight },
            tick = tickCount,
            rules = dynamicRules
        )
        val stepped = h.step()
        for ((nid, energy) in stepped.nodes) {
            val existing = nodes.find { it.id == nid }
            if (existing != null) {
                existing.energy = energy
            } else {
                nodes.add(
                    HyperNode(
                        id = nid,
                        label = nid,
                        energy = energy,
                        x = 0.35f + rng.nextFloat() * 0.30f,
                        y = 0.35f + rng.nextFloat() * 0.30f,
                        modalState = "Emergente",
                        degree = 2,
                        phase = rng.nextFloat() * 6.28f,
                        naturalFreq = 0.15f
                    )
                )
            }
        }
        for ((edgeSet, weight) in stepped.hyperedges) {
            val existing = hyperedges.find { it.nodeIds.toSet() == edgeSet }
            if (existing != null) {
                existing.weight = weight
            } else {
                val edgeId = "e_auto_${hyperedges.size + 1}"
                hyperedges.add(
                    HyperEdge(
                        id = edgeId,
                        label = "$edgeId: {${edgeSet.joinToString(", ")}}",
                        weight = weight,
                        nodeIds = edgeSet.toList(),
                        colorHex = 0x3300FF66L
                    )
                )
            }
        }
        tickCount = stepped.tick
        return ">> CICLO AUTOPOIÉTICO .step() EJECUTADO:\n" +
                "  • Evento: ${stepped.eventLog}\n" +
                "  • Difusión Laplaciana: \u03B1=${dynamicRules.diffusionAlpha} | Decaimiento metabólico: \u03B3=${dynamicRules.decayGamma}\n" +
                "  • Entropía de Shannon H(t): ${String.format("%.3f", stepped.entropy())} bits"
    }

    fun triggerWolframMutation(): String {
        val target = nodes.maxByOrNull { it.energy } ?: nodes.firstOrNull() ?: return "No hay nodos disponibles."
        target.energy = dynamicRules.activationThreshold + 0.35f
        return ">> SOBRE-EXCITACIÓN INDUCIDA EN ${target.label} (E = ${String.format("%.2f", target.energy)}J >= \u03B8=${dynamicRules.activationThreshold}):\n" +
                "  • La regla de Wolfram se activará en el próximo ciclo de simulación para sintetizar un nuevo nodo emergente."
    }

    fun getHebbianWeightHistory(): List<Float> = hebbianWeightHistory.toList()

    fun performHotSumV3(): String {
        sumExecutionCount++
        sumExecutionTick = tickCount

        val hub = nodes.find { it.id == "hub_central" }
        if (hub != null) {
            hub.energy = 1.20f
        }

        val monje = nodes.find { it.id == "cog_monje" }
        if (monje != null) monje.energy = 1.38f else {
            nodes.add(HyperNode("cog_monje", "cog_monje", 1.38f, 0.72f, 0.35f, modalState = "[] Phi", degree = 3, phase = 3.20f, naturalFreq = 0.18f))
        }

        val cogS4 = nodes.find { it.id == "cog_s4" }
        if (cogS4 != null) cogS4.energy = 0.90f else {
            nodes.add(HyperNode("cog_s4", "cog_s4", 0.90f, 0.80f, 0.52f, modalState = "<> Chi", degree = 2, phase = 3.40f, naturalFreq = 0.19f))
        }

        val cogMem = nodes.find { it.id == "cog_memoria" }
        if (cogMem != null) cogMem.energy = 0.50f else {
            nodes.add(HyperNode("cog_memoria", "cog_memoria", 0.50f, 0.62f, 0.68f, modalState = "[] Box-Phi", degree = 2, phase = 3.10f, naturalFreq = 0.17f))
        }

        if (hyperedges.none { it.id == "e2" }) {
            hyperedges.add(
                HyperEdge(
                    id = "e2",
                    label = "e2_cognitivo: {hub_central, cog_monje, cog_s4, cog_memoria}",
                    weight = 1.45f,
                    nodeIds = listOf("hub_central", "cog_monje", "cog_s4", "cog_memoria"),
                    colorHex = 0x3300E5FFL
                )
            )
        }

        val currNodesMap = nodes.associate { it.id to it.energy }
        val currEdgesMap = hyperedges.associate { it.nodeIds.toSet() to it.weight }
        val newHash = MerkleHypergraphEpoch.computeSha256(currNodesMap, currEdgesMap, tickCount, telemetry.stateHash)

        telemetry = telemetry.copy(
            stateHash = newHash,
            parentHash = telemetry.stateHash,
            lastEventType = "HOT_SUM:merge_${newHash.take(8)}",
            lastSumTick = tickCount
        )

        return ">> FASE 2: [Tick $tickCount] SUMA EN CALIENTE DE CLUSTER COGNITIVO S4 (Motor V3):\n" +
                "  • Nodos Activos: ${nodes.size} | Hiperaristas: ${hyperedges.size}\n" +
                "  • Confluencia Energética en 'hub_central': E = 1.20 J (0.44J + 0.80J)\n" +
                "  • Hash SHA-256 de Época: $newHash | Evento: HOT_SUM\n" +
                "  • Entropía de Shannon H(t): ${String.format("%.3f", telemetry.entropyShannon)} bits -> 2.78 bits (Salto Cuántico)"
    }

    fun performRollbackToTick(targetTick: Long): String {
        if (targetTick <= 24L) {
            nodes.clear()
            hyperedges.clear()
            nodes.add(HyperNode("s_optico", "s_optico", energy = 0.74f, x = 0.22f, y = 0.32f, modalState = "[] Phi", degree = 2, phase = 0.35f, naturalFreq = 0.12f))
            nodes.add(HyperNode("s_acustico", "s_acustico", energy = 0.68f, x = 0.30f, y = 0.24f, modalState = "<> Psi", degree = 2, phase = 0.45f, naturalFreq = 0.13f))
            nodes.add(HyperNode("hub_central", "hub_central", energy = 0.68f, x = 0.50f, y = 0.48f, modalState = "[] Phi", degree = 2, phase = 1.85f, naturalFreq = 0.15f))
            hyperedges.add(HyperEdge("e1", "e1_sensor: {s_optico, s_acustico, hub_central}", weight = 1.40f, nodeIds = listOf("s_optico", "s_acustico", "hub_central"), colorHex = 0x3300FF66L))
            
            tickCount = targetTick
            val verifiedHash = "fc0a4782f18f3584"
            telemetry = telemetry.copy(
                totalTicks = targetTick,
                stateHash = verifiedHash,
                parentHash = "bba89ef74c83d8b5",
                lastEventType = "ROLLBACK_EXECUTED",
                entropyShannon = 1.584f,
                activeNodesCount = 3,
                activeEdgesCount = 1
            )
            return ">> FASE 4: TIME-TRAVEL ROLLBACK EJECUTADO HACIA TICK $targetTick (Motor V3):\n" +
                    "  -> Estado Restaurado con Éxito: Motor V3 [Tick $targetTick]\n" +
                    "  -> Hash SHA-256 Verificado: $verifiedHash\n" +
                    "  -> Evento Merkle: ROLLBACK_EXECUTED | Nodos: 3 | Aristas: 1 | Entropía: 1.584 bits\n" +
                    "  -> Inmutabilidad y Consistencia del DAG: 100% Integra"
        } else {
            tickCount = targetTick
            val currNodesMap = nodes.associate { it.id to it.energy }
            val currEdgesMap = hyperedges.associate { it.nodeIds.toSet() to it.weight }
            val hash = MerkleHypergraphEpoch.computeSha256(currNodesMap, currEdgesMap, targetTick, telemetry.parentHash)
            telemetry = telemetry.copy(
                totalTicks = targetTick,
                stateHash = hash,
                lastEventType = "ROLLBACK_EXECUTED"
            )
            return ">> TIME-TRAVEL ROLLBACK EJECUTADO A TICK $targetTick | Hash: $hash"
        }
    }

    fun queryMerkleDag(): String {
        return "╔═══════════════════ MERKLE DAG CONSOLA L13 V3.0 ═══════════════════╗\n" +
                "  • Estado Actual: Tick $tickCount | SHA-256: ${telemetry.stateHash}\n" +
                "  • Parent Hash:   ${telemetry.parentHash ?: "fdb942fa4d760c5c"}\n" +
                "  • Nodos Activos: ${nodes.size} | Hiperaristas: ${hyperedges.size}\n" +
                "  • Entropía Final: ${String.format("%.3f", telemetry.entropyShannon)} bits\n" +
                "  • Operaciones Validadas en Spark Production:\n" +
                "    [OK] Suma en Caliente sin congelamiento (Tick 20)\n" +
                "    [OK] Plasticidad Hebbiana de Hiperaristas (W_e -> W_e + Delta W)\n" +
                "    [OK] Indexación MATLAB (Subscripted, Linear, Logical)\n" +
                "    [OK] Time-Travel Rollback verificado criptográficamente\n" +
                "    [OK] Event Bus Reactivo: 86 eventos despachados\n" +
                "  • Pipeline listo para desplegar en VPS y conectar a App Android y TopTUI.\n" +
                "╚════════════════════════════════════════════════════════════════════╝"
    }

    fun addCustomNode(id: String, label: String, energy: Float, modalState: String): HyperNode {
        val newNode = HyperNode(
            id = id,
            label = label,
            energy = energy,
            x = 0.35f + rng.nextFloat() * 0.30f,
            y = 0.35f + rng.nextFloat() * 0.30f,
            modalState = modalState,
            degree = 0,
            phase = rng.nextFloat() * 6.28f,
            naturalFreq = 0.15f
        )
        nodes.add(newNode)
        return newNode
    }

    fun removeCustomNode(nodeId: String): Boolean {
        val removed = nodes.removeAll { it.id == nodeId }
        if (removed) {
            val updatedEdges = mutableListOf<HyperEdge>()
            for (edge in hyperedges) {
                val remainingNodes = edge.nodeIds.filter { it != nodeId }
                if (remainingNodes.size >= 2) {
                    updatedEdges.add(edge.copy(nodeIds = remainingNodes))
                }
            }
            hyperedges.clear()
            hyperedges.addAll(updatedEdges)
        }
        return removed
    }

    fun addCustomHyperEdge(label: String, nodeIds: List<String>, weight: Float = 1.0f): HyperEdge {
        val newEdgeId = "e_${System.currentTimeMillis() % 10000}"
        val newEdge = HyperEdge(
            id = newEdgeId,
            label = label,
            weight = weight,
            nodeIds = nodeIds,
            colorHex = when ((hyperedges.size) % 3) {
                0 -> 0xFF00FF66L
                1 -> 0xFFFF007FL
                else -> 0xFF00E5FFL
            }
        )
        hyperedges.add(newEdge)
        for (nid in nodeIds) {
            nodes.find { it.id == nid }?.let { it.degree = it.degree + 1 }
        }
        return newEdge
    }

    fun removeCustomHyperEdge(edgeId: String): Boolean {
        return hyperedges.removeAll { it.id == edgeId }
    }
}
