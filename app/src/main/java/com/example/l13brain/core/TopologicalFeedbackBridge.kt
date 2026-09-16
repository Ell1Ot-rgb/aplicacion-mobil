package com.example.l13brain.core

import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * TopologicalFeedbackBridge (Fix-02).
 * Computes beta_0 and beta_1 via 20-step Vietoris-Rips filtration.
 * Includes Vineyards temporal persistence tracking and topological event detection (BIRTH/DEATH).
 */
class TopologicalFeedbackBridge(
    val maxNodes: Int = 30
) {
    companion object {
        const val N_FILTRATION_STEPS = 20
        const val BETTI_TEMP = 0.01
        const val VINEYARD_HISTORY = 20
    }

    val buffer = BettiStateBuffer(window = 10, alpha = 0.30)
    var lastB0: Double = 0.0
    var lastB1: Double = 0.0
    var lastTopoLoss: Double = 0.0
    var hadEvent: Boolean = false
    var lastEventType: String = "NONE"

    data class PersistenceDiagram(
        val betti0Curve: DoubleArray,
        val betti1Curve: DoubleArray,
        var meanB0: Double = 0.0,
        var meanB1: Double = 0.0,
        var deltaB0: Double = 0.0,
        var deltaB1: Double = 0.0
    )

    data class TopoResult(
        val topoLoss: Double,
        val betti0: Double,
        val betti1: Double,
        val targetB0: Double,
        val targetB1: Double,
        val hadEvent: Boolean,
        val eventType: String,
        val betti0Curve: DoubleArray,
        val betti1Curve: DoubleArray
    )

    val vineyardHistory = ArrayDeque<PersistenceDiagram>()

    fun ingestL11(betti0: Double, betti1: Double) {
        buffer.update(betti0, betti1)
    }

    fun processL13(thoughtVector: DoubleArray): TopoResult {
        val d = thoughtVector.size
        var nPts = min(maxNodes, d / 4)
        if (nPts < 2) nPts = 2

        // Reshape into [nPts, 4] points in R^4
        val points = Array(nPts) { i ->
            DoubleArray(4) { j ->
                val idx = i * 4 + j
                if (idx < d) thoughtVector[idx] else 0.0
            }
        }

        // Pairwise Euclidean distances
        val dists = Array(nPts) { i ->
            DoubleArray(nPts) { j ->
                var sumSq = 0.0
                for (k in 0 until 4) {
                    val diff = points[i][k] - points[j][k]
                    sumSq += diff * diff
                }
                sqrt(sumSq)
            }
        }

        var maxDist = 0.0
        for (i in 0 until nPts) {
            for (j in 0 until nPts) {
                if (dists[i][j] > maxDist) maxDist = dists[i][j]
            }
        }
        if (maxDist < 1e-10) maxDist = 1.0

        val diagram = PersistenceDiagram(
            betti0Curve = DoubleArray(N_FILTRATION_STEPS),
            betti1Curve = DoubleArray(N_FILTRATION_STEPS)
        )

        var sumB0 = 0.0
        var sumB1 = 0.0
        for (k in 0 until N_FILTRATION_STEPS) {
            val epsK = (k + 1).toDouble() / N_FILTRATION_STEPS * maxDist
            val (b0K, b1K) = computeBettiAtEps(dists, nPts, epsK)
            diagram.betti0Curve[k] = b0K
            diagram.betti1Curve[k] = b1K
            sumB0 += b0K
            sumB1 += b1K
        }

        diagram.meanB0 = sumB0 / N_FILTRATION_STEPS
        diagram.meanB1 = sumB1 / N_FILTRATION_STEPS

        // Vineyards temporal delta
        if (vineyardHistory.isNotEmpty()) {
            val prev = vineyardHistory.peekLast()!!
            diagram.deltaB0 = diagram.meanB0 - prev.meanB0
            diagram.deltaB1 = diagram.meanB1 - prev.meanB1
        } else {
            diagram.deltaB0 = 0.0
            diagram.deltaB1 = 0.0
        }

        vineyardHistory.addLast(diagram)
        if (vineyardHistory.size > VINEYARD_HISTORY) {
            vineyardHistory.pollFirst()
        }

        val b0 = diagram.meanB0
        val b1 = diagram.meanB1
        val tb0 = buffer.targetB0()
        val tb1 = buffer.targetB1()

        var vineyardBonus = 0.0
        if (vineyardHistory.size >= 2) {
            vineyardBonus = 0.1 * diagram.deltaB1 * diagram.deltaB1
        }
        val topoLoss = max(
            0.0,
            (b0 - tb0) * (b0 - tb0) + (b1 - tb1) * (b1 - tb1) - vineyardBonus
        )

        // Event detection
        var event = false
        var eventType = "NONE"
        if (abs(diagram.deltaB1) > 0.5) {
            event = true
            eventType = if (diagram.deltaB1 > 0) "BIRTH" else "DEATH"
        }

        lastB0 = b0
        lastB1 = b1
        lastTopoLoss = topoLoss
        hadEvent = event
        lastEventType = eventType

        return TopoResult(
            topoLoss = topoLoss,
            betti0 = b0,
            betti1 = b1,
            targetB0 = tb0,
            targetB1 = tb1,
            hadEvent = event,
            eventType = eventType,
            betti0Curve = diagram.betti0Curve,
            betti1Curve = diagram.betti1Curve
        )
    }

    private fun computeBettiAtEps(dists: Array<DoubleArray>, nPts: Int, eps: Double): Pair<Double, Double> {
        val a = Array(nPts) { DoubleArray(nPts) }
        val deg = DoubleArray(nPts)
        var m = 0.0

        for (i in 0 until nPts) {
            for (j in 0 until nPts) {
                if (i != j && dists[i][j] < eps) {
                    a[i][j] = 1.0
                    deg[i] += 1.0
                    if (j > i) {
                        m += 1.0
                    }
                }
            }
        }

        // Graph Laplacian L = D - A
        val l = Array(nPts) { i ->
            DoubleArray(nPts) { j ->
                if (i == j) deg[i] - a[i][j] else -a[i][j]
            }
        }

        // Eigenvalues of symmetric Laplacian
        val eigenvalues = computeSymmetricEigenvalues(l, nPts)
        var softRank = 0.0
        for (ev in eigenvalues) {
            val s = abs(ev)
            softRank += tanh(s / BETTI_TEMP)
        }

        val b0 = max(0.0, nPts.toDouble() - softRank)
        val b1 = max(0.0, m - nPts.toDouble() + b0)

        return Pair(b0, b1)
    }

    /**
     * Classic Jacobi eigenvalue algorithm for symmetric positive semi-definite matrices.
     */
    private fun computeSymmetricEigenvalues(matrix: Array<DoubleArray>, n: Int): DoubleArray {
        val a = Array(n) { i -> matrix[i].clone() }
        val maxIter = 50

        for (iter in 0 until maxIter) {
            var maxVal = 0.0
            var p = 0
            var q = 1
            for (i in 0 until n) {
                for (j in i + 1 until n) {
                    val absVal = abs(a[i][j])
                    if (absVal > maxVal) {
                        maxVal = absVal
                        p = i
                        q = j
                    }
                }
            }

            if (maxVal < 1e-9) break

            val app = a[p][p]
            val aqq = a[q][q]
            val apq = a[p][q]

            val phi = 0.5 * kotlin.math.atan2(2.0 * apq, aqq - app)
            val c = kotlin.math.cos(phi)
            val s = kotlin.math.sin(phi)

            for (i in 0 until n) {
                if (i != p && i != q) {
                    val aip = a[i][p]
                    val aiq = a[i][q]
                    a[i][p] = c * aip - s * aiq
                    a[p][i] = a[i][p]
                    a[i][q] = s * aip + c * aiq
                    a[q][i] = a[i][q]
                }
            }

            a[p][p] = c * c * app - 2.0 * s * c * apq + s * s * aqq
            a[q][q] = s * s * app + 2.0 * s * c * apq + c * c * aqq
            a[p][q] = 0.0
            a[q][p] = 0.0
        }

        return DoubleArray(n) { i -> a[i][i] }
    }
}
