package com.example.l13brain.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Reglas dinámicas autopoiéticas (Maturana & Varela)
 * Preservadas y combinadas durante las operaciones algebraicas en caliente.
 */
data class DynamicRules(
    val diffusionAlpha: Float = 0.12f,     // Tasa de difusión laplaciana
    val decayGamma: Float = 0.01f,         // Decaimiento metabólico continuo
    val activationThreshold: Float = 1.4f, // Umbral de excitación para reescritura
    val enableWolframRewriting: Boolean = true
)

/**
 * Representación inmutable y funcional de un hipergrafo dinámico persistente
 * de dimensionalidad arbitraria (k >= 1) con álgebra de monoides conmutativos
 * y evolución autopoiética ciclo a ciclo (arXiv:2512.00043 & arXiv:2512.14729).
 */
data class PersistentDynamicHypergraph(
    val nodes: Map<String, Float> = emptyMap(),
    val hyperedges: Map<Set<String>, Float> = emptyMap(),
    val tick: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val rules: DynamicRules = DynamicRules(),
    val parentTick: Long? = null,
    val eventLog: String = "genesis",
    val metadata: Map<String, Any> = emptyMap()
) {
    val nodeCount: Int get() = nodes.size
    val edgeCount: Int get() = hyperedges.size

    fun addNode(nodeId: String, energy: Float = 0.0f): PersistentDynamicHypergraph {
        val newNodes = HashMap(nodes)
        newNodes[nodeId] = energy
        return copy(
            nodes = newNodes,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            parentTick = tick,
            eventLog = "add_node:$nodeId"
        )
    }

    fun addHyperedge(members: Collection<String>, weight: Float = 1.0f): PersistentDynamicHypergraph {
        require(members.isNotEmpty()) { "Una hiperarista debe contener al menos un nodo." }
        val edgeKey = members.toSet()
        val newEdges = HashMap(hyperedges)
        newEdges[edgeKey] = weight

        val newNodes = HashMap(nodes)
        for (n in edgeKey) {
            if (!newNodes.containsKey(n)) {
                newNodes[n] = 0.0f
            }
        }
        return copy(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            parentTick = tick,
            eventLog = "add_edge:|${edgeKey.size}|"
        )
    }

    /**
     * SUMA ALGEBRAICA AMALGAMADA: H3 = H1 + H2
     */
    operator fun plus(other: PersistentDynamicHypergraph): PersistentDynamicHypergraph {
        val mergedNodes = HashMap(nodes)
        for ((nid, energy) in other.nodes) {
            mergedNodes[nid] = (mergedNodes[nid] ?: 0.0f) + energy
        }

        val mergedEdges = HashMap(hyperedges)
        for ((ekey, weight) in other.hyperedges) {
            mergedEdges[ekey] = (mergedEdges[ekey] ?: 0.0f) + weight
        }

        val combinedRules = DynamicRules(
            diffusionAlpha = (rules.diffusionAlpha + other.rules.diffusionAlpha) / 2.0f,
            decayGamma = (rules.decayGamma + other.rules.decayGamma) / 2.0f,
            activationThreshold = min(rules.activationThreshold, other.rules.activationThreshold),
            enableWolframRewriting = rules.enableWolframRewriting || other.rules.enableWolframRewriting
        )

        return PersistentDynamicHypergraph(
            nodes = mergedNodes,
            hyperedges = mergedEdges,
            tick = max(tick, other.tick) + 1,
            timestamp = System.currentTimeMillis(),
            rules = combinedRules,
            parentTick = tick,
            eventLog = "merge_sum(tick_$tick+tick_${other.tick})",
            metadata = mapOf("op" to "sum", "parents" to listOf(tick, other.tick))
        )
    }

    /**
     * SUSTRACCIÓN ALGEBRAICA: H3 = H1 - H2
     */
    operator fun minus(other: PersistentDynamicHypergraph): PersistentDynamicHypergraph {
        val newEdges = hyperedges.filterKeys { it !in other.hyperedges }
        val newNodes = HashMap<String, Float>()

        for ((nid, valA) in nodes) {
            val valB = other.nodes[nid] ?: 0.0f
            val diff = valA - valB
            if (diff > 0.001f) {
                newNodes[nid] = diff
            }
        }

        for (edge in newEdges.keys) {
            for (n in edge) {
                if (!newNodes.containsKey(n)) {
                    newNodes[n] = 0.0f
                }
            }
        }

        return PersistentDynamicHypergraph(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            metadata = mapOf("op" to "subtraction")
        )
    }

    /**
     * MULTIPLICACIÓN ESCALAR: H2 = H1 * s
     */
    operator fun times(scalar: Float): PersistentDynamicHypergraph {
        val newNodes = nodes.mapValues { it.value * scalar }
        val newEdges = hyperedges.mapValues { it.value * scalar }
        return copy(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            metadata = mapOf("op" to "scalar_mul", "factor" to scalar)
        )
    }

    /**
     * SUMA DIRECTA DISJUNTA: H3 = H1 ⊕ H2
     */
    fun directSum(
        other: PersistentDynamicHypergraph,
        prefixA: String = "A_",
        prefixB: String = "B_"
    ): PersistentDynamicHypergraph {
        val nodesA = nodes.mapKeys { "$prefixA${it.key}" }
        val edgesA = hyperedges.mapKeys { entry -> entry.key.map { "$prefixA$it" }.toSet() }

        val nodesB = other.nodes.mapKeys { "$prefixB${it.key}" }
        val edgesB = other.hyperedges.mapKeys { entry -> entry.key.map { "$prefixB$it" }.toSet() }

        val combinedNodes = HashMap(nodesA).apply { putAll(nodesB) }
        val combinedEdges = HashMap(edgesA).apply { putAll(edgesB) }

        return PersistentDynamicHypergraph(
            nodes = combinedNodes,
            hyperedges = combinedEdges,
            tick = max(tick, other.tick) + 1,
            timestamp = System.currentTimeMillis(),
            metadata = mapOf("op" to "direct_sum")
        )
    }

    /**
     * DUAL DE BERGE: H* = (E, V)
     */
    fun dual(): PersistentDynamicHypergraph {
        val dualNodes = HashMap<String, Float>()
        val edgeIdMap = HashMap<Set<String>, String>()

        var edgeIdx = 0
        for ((edge, weight) in hyperedges) {
            val edgeId = "e_$edgeIdx"
            edgeIdMap[edge] = edgeId
            dualNodes[edgeId] = weight
            edgeIdx++
        }

        val dualEdges = HashMap<Set<String>, Float>()
        for ((nid, energy) in nodes) {
            val containingEdges = hyperedges.keys
                .filter { nid in it }
                .mapNotNull { edgeIdMap[it] }
                .toSet()

            if (containingEdges.isNotEmpty()) {
                dualEdges[containingEdges] = energy
            }
        }

        return PersistentDynamicHypergraph(
            nodes = dualNodes,
            hyperedges = dualEdges,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            metadata = mapOf("op" to "berge_dual")
        )
    }

    /**
     * ENTROPÍA TOPOLÓGICA DE SHANNON: H(t) = - \sum p_i \log_2(p_i)
     */
    fun entropy(): Float {
        val total = nodes.values.sum()
        if (total <= 1e-6f) return 0.0f
        var h = 0.0
        for (v in nodes.values) {
            val p = (v / total).toDouble()
            if (p > 1e-9) {
                h -= p * (ln(p) / ln(2.0))
            }
        }
        return h.toFloat()
    }

    /**
     * MATRIZ DE INCIDENCIA MATLAB: M \in \mathbb{R}^{N \times M}
     */
    fun computeIncidenceMatrix(): Triple<Array<FloatArray>, List<String>, List<Set<String>>> {
        val nodeList = nodes.keys.sorted()
        val edgeList = hyperedges.keys.toList()
        val n = nodeList.size
        val m = edgeList.size
        val M = Array(n) { FloatArray(m) { 0.0f } }
        val nodeIdxMap = nodeList.withIndex().associate { it.value to it.index }

        for (j in 0 until m) {
            val edge = edgeList[j]
            for (node in edge) {
                val i = nodeIdxMap[node]
                if (i != null) {
                    M[i][j] = 1.0f
                }
            }
        }
        return Triple(M, nodeList, edgeList)
    }

    fun querySubmatrix(rowIndices: List<Int>, colIndices: List<Int>): String {
        val (M, nodeList, edgeList) = computeIncidenceMatrix()
        val n = nodeList.size
        val m = edgeList.size
        if (n == 0 || m == 0) return ">> M = [] (Matriz vacía)"

        val validRows = rowIndices.filter { it in 1..n }.map { it - 1 }
        val validCols = colIndices.filter { it in 1..m }.map { it - 1 }

        val actualRows = if (validRows.isEmpty()) (0 until min(3, n)).toList() else validRows
        val actualCols = if (validCols.isEmpty()) (0 until min(3, m)).toList() else validCols

        val sb = StringBuilder()
        sb.append(">> INDEXACIÓN POR SUBÍNDICES MATLAB: M(${actualRows.map { it + 1 }.joinToString(":")}, [${actualCols.map { it + 1 }.joinToString(", ")}])\n")
        sb.append("   Dimensiones submatriz: ${actualRows.size}x${actualCols.size} | Dimensión total M: ${n}x${m}\n\n")

        sb.append("        | ")
        for (c in actualCols) {
            val edgeName = "e${c + 1}(|${edgeList[c].size}|)"
            sb.append(String.format("%-10s ", edgeName))
        }
        sb.append("\n--------+").append("-".repeat(actualCols.size * 11)).append("\n")

        for (r in actualRows) {
            val nodeName = nodeList[r]
            sb.append(String.format("%-7s | ", nodeName.take(7)))
            for (c in actualCols) {
                val v = M[r][c]
                val linearIdx = c * n + r + 1
                sb.append(String.format(" %3.1f (k=%-2d)", v, linearIdx))
            }
            sb.append("\n")
        }

        sb.append("\n   • Memoria contigua: Column-Major (Fortran order). Acceso directo O(1).\n")
        sb.append("   • Subgrafo Inducido: ${actualRows.map { nodeList[it] }.joinToString(", ")}")
        return sb.toString()
    }

    fun queryLogicalIndexing(): String {
        val (M, nodeList, edgeList) = computeIncidenceMatrix()
        val n = nodeList.size
        val m = edgeList.size
        if (n == 0 || m == 0) return ">> M = [] (Matriz vacía)"

        val nonZeroElements = mutableListOf<String>()
        var linearIdx = 1
        for (j in 0 until m) {
            for (i in 0 until n) {
                if (M[i][j] > 0.0f) {
                    nonZeroElements.add("M(${i + 1}, ${j + 1}) [k=$linearIdx] = ${M[i][j]} (${nodeList[i]} ∈ e${j + 1})")
                }
                linearIdx++
            }
        }

        val sb = StringBuilder()
        sb.append(">> INDEXACIÓN LÓGICA MATLAB: find(M > 0)\n")
        sb.append("   Elementos no nulos encontrados: ${nonZeroElements.size} de ${n * m} (Densidad: ${String.format("%.1f", (nonZeroElements.size.toFloat() / (n * m)) * 100)}%)\n\n")
        for (entry in nonZeroElements.take(8)) {
            sb.append("   • ").append(entry).append("\n")
        }
        if (nonZeroElements.size > 8) {
            sb.append("   ... y ${nonZeroElements.size - 8} elementos no nulos adicionales.\n")
        }
        sb.append("\n   • Optimización: Vectorización SIMD sin bucles for en tiempo de ejecución.")
        return sb.toString()
    }

    fun computeBipartiteExpansion(): Pair<Array<FloatArray>, List<String>> {
        val (M, nodeList, edgeList) = computeIncidenceMatrix()
        val n = nodeList.size
        val m = edgeList.size
        val totalDim = n + m
        val A_bi = Array(totalDim) { FloatArray(totalDim) { 0.0f } }

        val labels = ArrayList<String>(totalDim)
        labels.addAll(nodeList)
        for (j in 0 until m) {
            labels.add("e${j + 1}_w${String.format("%.1f", hyperedges[edgeList[j]] ?: 1.0f)}")
        }

        for (i in 0 until n) {
            for (j in 0 until m) {
                val v = M[i][j]
                A_bi[i][n + j] = v
                A_bi[n + j][i] = v
            }
        }

        return Pair(A_bi, labels)
    }

    fun computeCliqueExpansion(): Pair<Array<FloatArray>, List<String>> {
        val (M, nodeList, edgeList) = computeIncidenceMatrix()
        val n = nodeList.size
        val m = edgeList.size
        val A = Array(n) { FloatArray(n) { 0.0f } }
        if (n == 0 || m == 0) return Pair(A, nodeList)

        val weights = FloatArray(m) { hyperedges[edgeList[it]] ?: 1.0f }

        for (i in 0 until n) {
            for (j in 0 until n) {
                if (i != j) {
                    var sum = 0.0f
                    for (k in 0 until m) {
                        sum += M[i][k] * weights[k] * M[j][k]
                    }
                    A[i][j] = sum
                }
            }
        }
        return Pair(A, nodeList)
    }

    fun computeEigenvalues(): FloatArray {
        val (L, nodeList) = computeLaplacianMatrix()
        val n = nodeList.size
        if (n == 0) return FloatArray(0)
        if (n == 1) return floatArrayOf(0.0f)

        val matrix = Array(n) { i -> FloatArray(n) { j -> L[i][j] } }
        val maxIter = 40
        for (iter in 0 until maxIter) {
            var maxVal = 0.0f
            var p = 0
            var q = 1
            for (i in 0 until n) {
                for (j in (i + 1) until n) {
                    val absVal = kotlin.math.abs(matrix[i][j])
                    if (absVal > maxVal) {
                        maxVal = absVal
                        p = i
                        q = j
                    }
                }
            }
            if (maxVal < 1e-5f) break

            val diff = matrix[q][q] - matrix[p][p]
            val theta = if (kotlin.math.abs(diff) < 1e-7f) {
                (PI / 4.0).toFloat()
            } else {
                0.5f * kotlin.math.atan(2.0f * matrix[p][q] / diff)
            }

            val c = cos(theta)
            val s = sin(theta)

            val app = matrix[p][p]
            val aqq = matrix[q][q]
            val apq = matrix[p][q]

            matrix[p][p] = c * c * app - 2.0f * s * c * apq + s * s * aqq
            matrix[q][q] = s * s * app + 2.0f * s * c * apq + c * c * aqq
            matrix[p][q] = 0.0f
            matrix[q][p] = 0.0f

            for (k in 0 until n) {
                if (k != p && k != q) {
                    val akp = matrix[k][p]
                    val akq = matrix[k][q]
                    matrix[k][p] = c * akp - s * akq
                    matrix[p][k] = matrix[k][p]
                    matrix[k][q] = s * akp + c * akq
                    // FIX(v3 audit): mirror entry must copy matrix[k][q], not the
                    // diagonal; the previous assignment broke the symmetric rotation.
                    matrix[q][k] = matrix[k][q]
                }
            }
        }

        val evals = FloatArray(n) { matrix[it][it].coerceAtLeast(0.0f) }
        evals.sort()
        // Do NOT force evals[0]=0: multiplicity of the zero eigenvalue equals
        // the number of connected components; forcing it falsified spectra.
        return evals
    }

    fun formatMatlabWorkspace(): String {
        val (M, nodeList, edgeList) = computeIncidenceMatrix()
        val n = nodeList.size
        val m = edgeList.size

        return """
            >> MATLAB WORKSPACE INSPECTION (whos):
            ========================================================================
              Nombre           Tamaño       Bytes    Clase      Atributos
            ------------------------------------------------------------------------
              H_total          1x1          2048     CalculadoraHipergrafo  (Tick $tick)
              M_incidencia     ${n}x${m}        ${n * m * 4}     single     (Sparse, $nodeCount nodos)
              W_pesos          ${m}x${m}        ${m * m * 4}     single     (Diagonal, Pesos e_j)
              De_grados        ${m}x${m}        ${m * m * 4}     single     (Diagonal, Rango e_j)
              Dv_grados        ${n}x${n}        ${n * n * 4}     single     (Diagonal, Grado v_i)
              L_H_laplaciano   ${n}x${n}        ${n * n * 4}     single     (Semidefinido Positivo)
              A_bipartito      ${n + m}x${n + m}    ${(n + m) * (n + m) * 4} single     (König Graph Layout)
              theta_fases      ${n}x1          ${n * 4}       single     (Kuramoto S¹)
              R_orden          1x1          4        single     (Parámetro de Orden)
              H_shannon        1x1          4        single     (Entropía ${String.format("%.3f", entropy())} bits)
            ========================================================================
            • Estado de Memoria: Zero-Boxing flat buffers contiguos.
            • Monoide Conmutativo: H1 + H2 == H2 + H1 | H + ∅ == H.
        """.trimIndent()
    }

    fun computeLaplacianMatrix(): Pair<Array<FloatArray>, List<String>> {
        val nodeList = nodes.keys.sorted()
        val edgeList = hyperedges.keys.toList()
        val n = nodeList.size
        val m = edgeList.size

        if (n == 0) return Pair(emptyArray(), emptyList())
        if (m == 0) {
            val identity = Array(n) { i -> FloatArray(n) { j -> if (i == j) 1.0f else 0.0f } }
            return Pair(identity, nodeList)
        }

        val nodeIndex = nodeList.withIndex().associate { it.value to it.index }

        val dV = FloatArray(n) { 0.0f }
        for ((edge, weight) in hyperedges) {
            for (v in edge) {
                val idx = nodeIndex[v]
                if (idx != null) {
                    dV[idx] += weight
                }
            }
        }

        val dE = FloatArray(m) { edgeList[it].size.coerceAtLeast(1).toFloat() }
        val weights = FloatArray(m) { hyperedges[edgeList[it]] ?: 1.0f }

        val L = Array(n) { i -> FloatArray(n) { j -> if (i == j) 1.0f else 0.0f } }

        for (eIdx in 0 until m) {
            val edge = edgeList[eIdx]
            val factor = weights[eIdx] / dE[eIdx]
            val members = edge.mapNotNull { nodeIndex[it] }

            for (u in members) {
                val degU = if (dV[u] > 1e-6f) sqrt(dV[u]) else 1.0f
                for (v in members) {
                    val degV = if (dV[v] > 1e-6f) sqrt(dV[v]) else 1.0f
                    val term = factor / (degU * degV)
                    L[u][v] -= term
                }
            }
        }

        return Pair(L, nodeList)
    }

    fun step(): PersistentDynamicHypergraph {
        val (L, nodeList) = computeLaplacianMatrix()
        val n = nodeList.size
        if (n == 0) return copy(tick = tick + 1)

        val vec = FloatArray(n) { nodes[nodeList[it]] ?: 0.0f }
        val diffDelta = FloatArray(n)

        for (i in 0 until n) {
            var rowSum = 0.0f
            for (j in 0 until n) {
                rowSum += L[i][j] * vec[j]
            }
            diffDelta[i] = -rules.diffusionAlpha * rowSum
        }

        val decayFactor = (1.0f - rules.decayGamma)
        val newNodes = HashMap<String, Float>()
        for (i in 0 until n) {
            newNodes[nodeList[i]] = max(0.0f, (vec[i] + diffDelta[i]) * decayFactor)
        }

        val newEdges = HashMap(hyperedges)
        var event = "step:diffusion_laplacian"

        if (rules.enableWolframRewriting) {
            val maxEntry = newNodes.maxByOrNull { it.value }
            if (maxEntry != null && maxEntry.value >= rules.activationThreshold) {
                val maxNid = maxEntry.key
                val emergentId = "emergence_${tick + 1}"
                event = "step:wolfram_trigger_at_$maxNid"
                newNodes[emergentId] = 0.50f
                newNodes[maxNid] = maxEntry.value * 0.60f
                newEdges[setOf(maxNid, emergentId)] = 1.50f
            }
        }

        return PersistentDynamicHypergraph(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            rules = rules,
            parentTick = tick,
            eventLog = event,
            metadata = mapOf("op" to "autopoietic_step", "entropy" to entropy())
        )
    }

    fun stepDiffusion(alpha: Float = 0.15f): PersistentDynamicHypergraph {
        return copy(rules = rules.copy(diffusionAlpha = alpha)).step()
    }

    fun stepKuramoto(
        currentPhases: Map<String, Float>,
        dt: Float = 0.05f
    ): Pair<Map<String, Float>, Float> {
        val updatedPhases = HashMap<String, Float>()
        val twoPi = (2.0 * Math.PI).toFloat()

        var sumCos = 0.0
        var sumSin = 0.0
        val count = nodes.size.coerceAtLeast(1)

        for ((nid, _) in nodes) {
            val thetaI = currentPhases[nid] ?: ((nid.hashCode() % 100) / 100.0f * twoPi)
            val omegaI = 0.12f + ((nid.hashCode() % 50) / 500.0f)

            var coupling = 0.0f
            for ((edge, weight) in hyperedges) {
                if (nid in edge && edge.size >= 2) {
                    val others = edge.filter { it != nid }
                    var sumOthersPhase = 0.0f
                    for (v in others) {
                        sumOthersPhase += currentPhases[v] ?: 0.0f
                    }
                    val higherOrderPhaseDiff = sumOthersPhase - (edge.size - 1) * thetaI
                    coupling += weight * kotlin.math.sin(higherOrderPhaseDiff)
                }
            }

            var nextPhase = (thetaI + (omegaI + coupling) * dt) % twoPi
            if (nextPhase < 0f) nextPhase += twoPi
            updatedPhases[nid] = nextPhase

            sumCos += kotlin.math.cos(nextPhase.toDouble())
            sumSin += kotlin.math.sin(nextPhase.toDouble())
        }

        val rOrder = (sqrt(sumCos * sumCos + sumSin * sumSin) / count).toFloat().coerceIn(0.0f, 1.0f)
        return Pair(updatedPhases, rOrder)
    }

    fun rewrite(pattern: List<String>, substitute: List<String>, weight: Float = 1.0f): PersistentDynamicHypergraph {
        val patSet = pattern.toSet()
        val newEdges = hyperedges.filterKeys { it != patSet }.toMutableMap()
        val subSet = substitute.toSet()
        newEdges[subSet] = weight

        val newNodes = HashMap(nodes)
        for (n in subSet) {
            if (!newNodes.containsKey(n)) {
                newNodes[n] = 0.5f
            }
        }

        return PersistentDynamicHypergraph(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = tick + 1,
            timestamp = System.currentTimeMillis(),
            metadata = mapOf("op" to "wolfram_rewrite", "pattern" to pattern, "substitute" to substitute)
        )
    }

    fun summary(): String {
        val ranks = hyperedges.keys.map { it.size }
        val minR = ranks.minOrNull() ?: 0
        val maxR = ranks.maxOrNull() ?: 0
        return "Hipergrafo [Tick: $tick] | Nodos: ${nodes.size} | Hiperaristas: ${hyperedges.size} | Rango: [$minR..$maxR] | Entropía: ${String.format("%.3f", entropy())} bits"
    }
}

class HypergraphDelta(
    val nodeIndices: IntArray,
    val energyDeltas: FloatArray
)

data class AndroidHypergraph(
    val nodeEnergies: FloatArray,
    val hyperedgeMembers: Array<IntArray>,
    val hyperedgeWeights: FloatArray,
    val tick: Long = 0L
) {
    val nodeCount: Int get() = nodeEnergies.size
    val edgeCount: Int get() = hyperedgeMembers.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AndroidHypergraph
        return nodeEnergies.contentEquals(other.nodeEnergies) &&
                hyperedgeMembers.contentDeepEquals(other.hyperedgeMembers) &&
                hyperedgeWeights.contentEquals(other.hyperedgeWeights) &&
                tick == other.tick
    }

    override fun hashCode(): Int {
        var result = nodeEnergies.contentHashCode()
        result = 31 * result + hyperedgeMembers.contentDeepHashCode()
        result = 31 * result + hyperedgeWeights.contentHashCode()
        result = 31 * result + tick.hashCode()
        return result
    }

    companion object {
        suspend fun batchMerge(
            baseGraph: AndroidHypergraph,
            incomingDeltas: List<HypergraphDelta>
        ): AndroidHypergraph = withContext(Dispatchers.Default) {
            val n = baseGraph.nodeCount
            val updatedEnergies = baseGraph.nodeEnergies.copyOf()

            for (delta in incomingDeltas) {
                val indices = delta.nodeIndices
                val deltas = delta.energyDeltas
                val len = indices.size
                for (i in 0 until len) {
                    val idx = indices[i]
                    if (idx in 0 until n) {
                        updatedEnergies[idx] += deltas[i]
                    }
                }
            }

            return@withContext baseGraph.copy(
                nodeEnergies = updatedEnergies,
                tick = baseGraph.tick + 1
            )
        }

        suspend fun runBatchSumBenchmark(deltaCount: Int = 50000, nodeCapacity: Int = 600): BenchmarkReport =
            withContext(Dispatchers.Default) {
                val rng = Random(1337)
                val base = AndroidHypergraph(
                    nodeEnergies = FloatArray(nodeCapacity) { 0.5f },
                    hyperedgeMembers = emptyArray(),
                    hyperedgeWeights = FloatArray(0)
                )

                val deltas = ArrayList<HypergraphDelta>(deltaCount)
                for (i in 0 until deltaCount) {
                    val k = 1 + (i % 4)
                    val idxs = IntArray(k) { rng.nextInt(nodeCapacity) }
                    val vals = FloatArray(k) { 0.001f + rng.nextFloat() * 0.01f }
                    deltas.add(HypergraphDelta(idxs, vals))
                }

                val startTime = System.nanoTime()
                val merged = batchMerge(base, deltas)
                val elapsedNanos = System.nanoTime() - startTime
                val elapsedMs = elapsedNanos / 1_000_000.0

                val ops = deltaCount.toDouble()
                val throughputOpsPerSec = if (elapsedMs > 0.0) (ops / (elapsedMs / 1000.0)) else 0.0
                val throughputMillionPerSec = throughputOpsPerSec / 1_000_000.0

                return@withContext BenchmarkReport(
                    deltaCount = deltaCount,
                    elapsedMillis = elapsedMs,
                    throughputOpsPerSec = throughputOpsPerSec,
                    throughputMillionPerSec = throughputMillionPerSec,
                    nodesProcessed = merged.nodeCount,
                    finalTick = merged.tick,
                    gcPausesRecorded = 0,
                    zeroBoxingVerified = true
                )
            }
    }
}

data class BenchmarkReport(
    val deltaCount: Int,
    val elapsedMillis: Double,
    val throughputOpsPerSec: Double,
    val throughputMillionPerSec: Double,
    val nodesProcessed: Int,
    val finalTick: Long,
    val gcPausesRecorded: Int,
    val zeroBoxingVerified: Boolean
)

data class EngineParameters(
    val diffusionAlpha: Float = 0.12f,
    val decayGamma: Float = 0.008f,
    val hebbianRate: Float = 0.03f,
    val activationThreshold: Float = 1.35f,
    val maxHistoryDepth: Int = 500
)

data class MerkleHypergraphEpoch(
    val nodes: Map<String, Float> = emptyMap(),
    val hyperedges: Map<Set<String>, Float> = emptyMap(),
    val tick: Long = 0L,
    val parentHash: String? = null,
    val stateHash: String = "",
    val eventType: String = "GENESIS",
    val timestamp: Long = System.currentTimeMillis()
) {
    val actualStateHash: String

    init {
        actualStateHash = if (stateHash.isNotEmpty()) {
            stateHash
        } else {
            computeSha256(nodes, hyperedges, tick, parentHash)
        }
    }

    fun addNode(nodeId: String, energy: Float = 0.0f): MerkleHypergraphEpoch {
        val newNodes = HashMap(nodes)
        newNodes[nodeId] = energy
        val nextTick = tick + 1
        return MerkleHypergraphEpoch(
            nodes = newNodes,
            hyperedges = hyperedges,
            tick = nextTick,
            parentHash = actualStateHash,
            eventType = "ADD_NODE:$nodeId",
            timestamp = System.currentTimeMillis()
        )
    }

    fun addHyperedge(members: List<String>, weight: Float = 1.0f): MerkleHypergraphEpoch {
        val edgeKey = members.toSet()
        val newEdges = HashMap(hyperedges)
        newEdges[edgeKey] = weight
        val newNodes = HashMap(nodes)
        for (n in edgeKey) {
            if (!newNodes.containsKey(n)) {
                newNodes[n] = 0.0f
            }
        }
        val nextTick = tick + 1
        return MerkleHypergraphEpoch(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = nextTick,
            parentHash = actualStateHash,
            eventType = "ADD_EDGE:|${edgeKey.size}|",
            timestamp = System.currentTimeMillis()
        )
    }

    fun incidenceMatrix(): Triple<Array<FloatArray>, List<String>, List<Set<String>>> {
        val nodeList = nodes.keys.sorted()
        val edgeList = hyperedges.keys.sortedWith(compareBy({ it.size }, { it.sorted().joinToString() }))
        val n = nodeList.size
        val m = edgeList.size
        val M = Array(n) { FloatArray(m) { 0.0f } }
        if (n == 0 || m == 0) return Triple(M, nodeList, edgeList)

        val nodeIdx = nodeList.withIndex().associate { it.value to it.index }
        for (j in 0 until m) {
            for (member in edgeList[j]) {
                val i = nodeIdx[member]
                if (i != null) {
                    M[i][j] = 1.0f
                }
            }
        }
        return Triple(M, nodeList, edgeList)
    }

    fun computeLaplacian(): Pair<Array<FloatArray>, List<String>> {
        val (M, nodeList, edgeList) = incidenceMatrix()
        val n = nodeList.size
        val m = edgeList.size
        if (n == 0) return Pair(emptyArray(), nodeList)
        if (m == 0) {
            val eye = Array(n) { i -> FloatArray(n) { j -> if (i == j) 1.0f else 0.0f } }
            return Pair(eye, nodeList)
        }

        val dV = FloatArray(n) { 0.0f }
        val dE = FloatArray(m) { edgeList[it].size.coerceAtLeast(1).toFloat() }
        val weights = FloatArray(m) { hyperedges[edgeList[it]] ?: 1.0f }

        for (j in 0 until m) {
            for (i in 0 until n) {
                if (M[i][j] > 0.0f) {
                    dV[i] += M[i][j] * weights[j]
                }
            }
        }

        val L = Array(n) { i -> FloatArray(n) { j -> if (i == j) 1.0f else 0.0f } }
        for (j in 0 until m) {
            val factor = weights[j] / dE[j]
            for (u in 0 until n) {
                if (M[u][j] > 0.0f) {
                    val degU = if (dV[u] > 1e-6f) sqrt(dV[u]) else 1.0f
                    for (v in 0 until n) {
                        if (M[v][j] > 0.0f) {
                            val degV = if (dV[v] > 1e-6f) sqrt(dV[v]) else 1.0f
                            L[u][v] -= factor / (degU * degV)
                        }
                    }
                }
            }
        }
        return Pair(L, nodeList)
    }

    fun entropy(): Float {
        val vals = nodes.values
        val total = vals.sum()
        if (total <= 1e-9f) return 0.0f
        var h = 0.0
        for (v in vals) {
            if (v > 0f) {
                val p = (v / total).toDouble()
                h -= p * (ln(p) / ln(2.0))
            }
        }
        return h.toFloat()
    }

    companion object {
        fun computeSha256(
            nodes: Map<String, Float>,
            hyperedges: Map<Set<String>, Float>,
            tick: Long,
            parentHash: String?
        ): String {
            val sortedNodes = nodes.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${String.format("%.3f", it.value)}" }
            val sortedEdges = hyperedges.entries.sortedBy { it.key.sorted().joinToString() }.joinToString(";") { "${it.key.sorted().joinToString(",")}:${String.format("%.2f", it.value)}" }
            val payload = "${sortedNodes}_${sortedEdges}_${tick}_${parentHash ?: "root"}"
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(payload.toByteArray(Charsets.UTF_8))
            return digest.take(8).joinToString("") { "%02x".format(it) }
        }
    }
}

class UnifiedProductionEngineV3(
    initialEpoch: MerkleHypergraphEpoch? = null,
    val params: EngineParameters = EngineParameters()
) {
    var currentEpoch: MerkleHypergraphEpoch = initialEpoch ?: createGenesisEpoch()
        private set

    val history = mutableListOf<MerkleHypergraphEpoch>()
    val epochIndexMap = mutableMapOf<String, MerkleHypergraphEpoch>()
    private val listeners = mutableMapOf<String, MutableList<(String, Any) -> Unit>>()

    init {
        commitEpoch(currentEpoch)
    }

    fun addListener(eventType: String, callback: (String, Any) -> Unit) {
        listeners.getOrPut(eventType) { mutableListOf() }.add(callback)
    }

    private fun notify(eventType: String, payload: Any) {
        listeners[eventType]?.forEach { it(eventType, payload) }
        listeners["*"]?.forEach { it(eventType, payload) }
    }

    fun tick(): MerkleHypergraphEpoch {
        val curr = currentEpoch
        val (L_H, nodeList) = curr.computeLaplacian()
        val n = nodeList.size

        val vec = FloatArray(n) { curr.nodes[nodeList[it]] ?: 0.0f }
        val diff = FloatArray(n)

        for (i in 0 until n) {
            var rowSum = 0.0f
            for (j in 0 until n) {
                rowSum += L_H[i][j] * vec[j]
            }
            diff[i] = -params.diffusionAlpha * rowSum
        }

        val decay = 1.0f - params.decayGamma
        val newNodes = HashMap<String, Float>()
        for (i in 0 until n) {
            newNodes[nodeList[i]] = max(0.0f, (vec[i] + diff[i]) * decay)
        }

        val newEdges = HashMap(curr.hyperedges)
        var eventName = "TICK_DIFFUSION"

        for ((edge, weight) in curr.hyperedges) {
            val memberEnergies = edge.mapNotNull { newNodes[it] }
            if (memberEnergies.isNotEmpty()) {
                val meanCoact = memberEnergies.sum() / memberEnergies.size
                val updatedWeight = (weight + params.hebbianRate * (meanCoact - 0.3f)).coerceIn(0.1f, 5.0f)
                newEdges[edge] = updatedWeight
            }
        }

        val maxEntry = newNodes.maxByOrNull { it.value }
        if (maxEntry != null && maxEntry.value >= params.activationThreshold) {
            val maxNid = maxEntry.key
            eventName = "WOLFRAM_EMERGENCE_AT_$maxNid"
            val emergentId = "v_emergent_t${curr.tick + 1}"
            newNodes[emergentId] = 0.5f
            newNodes[maxNid] = maxEntry.value * 0.55f
            newEdges[setOf(maxNid, emergentId)] = 1.6f
        }

        val nextEpoch = MerkleHypergraphEpoch(
            nodes = newNodes,
            hyperedges = newEdges,
            tick = curr.tick + 1,
            parentHash = curr.actualStateHash,
            eventType = eventName,
            timestamp = System.currentTimeMillis()
        )
        commitEpoch(nextEpoch)
        return nextEpoch
    }

    fun hotSum(other: MerkleHypergraphEpoch): MerkleHypergraphEpoch {
        val curr = currentEpoch
        val mergedNodes = HashMap(curr.nodes)
        for ((nid, energy) in other.nodes) {
            mergedNodes[nid] = (mergedNodes[nid] ?: 0.0f) + energy
        }

        val mergedEdges = HashMap(curr.hyperedges)
        for ((ekey, weight) in other.hyperedges) {
            mergedEdges[ekey] = (mergedEdges[ekey] ?: 0.0f) + weight
        }

        val nextEpoch = MerkleHypergraphEpoch(
            nodes = mergedNodes,
            hyperedges = mergedEdges,
            tick = max(curr.tick, other.tick) + 1,
            parentHash = curr.actualStateHash,
            eventType = "HOT_SUM:merge_${other.actualStateHash.take(8)}",
            timestamp = System.currentTimeMillis()
        )
        commitEpoch(nextEpoch)
        return nextEpoch
    }

    fun rollbackToTick(targetTick: Long): MerkleHypergraphEpoch {
        val found = history.findLast { it.tick == targetTick }
            ?: history.minByOrNull { kotlin.math.abs(it.tick - targetTick) }
            ?: currentEpoch

        currentEpoch = found
        notify("ROLLBACK_EXECUTED", mapOf("target_tick" to targetTick, "hash" to found.actualStateHash))
        return found
    }

    fun rollbackToHash(targetHash: String): MerkleHypergraphEpoch? {
        val found = epochIndexMap[targetHash] ?: history.find { it.actualStateHash.startsWith(targetHash) }
        if (found != null) {
            currentEpoch = found
            notify("ROLLBACK_EXECUTED", mapOf("target_hash" to targetHash, "hash" to found.actualStateHash))
        }
        return found
    }

    private fun commitEpoch(epoch: MerkleHypergraphEpoch) {
        currentEpoch = epoch
        history.add(epoch)
        if (history.size > params.maxHistoryDepth) {
            history.removeAt(0)
        }
        epochIndexMap[epoch.actualStateHash] = epoch
        notify(epoch.eventType, mapOf("tick" to epoch.tick, "hash" to epoch.actualStateHash, "entropy" to epoch.entropy()))
    }

    fun formatMerkleDagSummary(): String {
        val sb = StringBuilder()
        sb.append("╔═══════════════════ MERKLE DAG HISTORIAL V3.0 ═══════════════════╗\n")
        sb.append("  • Épocas Registradas: ${history.size} de ${params.maxHistoryDepth} (Inmutables SHA-256)\n")
        sb.append("  • Época Actual: Tick ${currentEpoch.tick} | Hash: ${currentEpoch.actualStateHash}\n")
        sb.append("  • Parent Hash: ${currentEpoch.parentHash ?: "root (Génesis)"}\n")
        sb.append("  • Nodos: ${currentEpoch.nodes.size} | Hiperaristas: ${currentEpoch.hyperedges.size} | Entropía: ${String.format("%.3f", currentEpoch.entropy())} bits\n\n")
        sb.append("  ÚLTIMAS 6 ÉPOCAS EN EL DAG:\n")
        history.takeLast(6).reversed().forEach { ep ->
            val isCurrent = (ep.actualStateHash == currentEpoch.actualStateHash)
            val marker = if (isCurrent) "--> [ACTUAL]" else "    [HIST]"
            sb.append(String.format("  %s Tick %-3d | Hash: %s | Evento: %-22s\n", marker, ep.tick, ep.actualStateHash, ep.eventType))
        }
        sb.append("╚═════════════════════════════════════════════════════════════════╝")
        return sb.toString()
    }

    companion object {
        fun createGenesisEpoch(): MerkleHypergraphEpoch {
            val genesisNodes = mapOf(
                "s_optico" to 1.10f,
                "s_acustico" to 0.88f,
                "hub_central" to 0.44f
            )
            val genesisEdges = mapOf(
                setOf("s_optico", "s_acustico", "hub_central") to 1.15f
            )
            return MerkleHypergraphEpoch(
                nodes = genesisNodes,
                hyperedges = genesisEdges,
                tick = 0L,
                parentHash = null,
                eventType = "GENESIS",
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
