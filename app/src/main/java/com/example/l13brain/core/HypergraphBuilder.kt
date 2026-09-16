package com.example.l13brain.core

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * HypergraphBuilder with Incremental PCA (Fix-01).
 * Manages synaptic states, synaptogenesis (temporal and semantic edges),
 * relative-index pruning, and threshold tau stabilization.
 */
class HypergraphBuilder(
    val embeddingDim: Int = 256,
    val maxHistory: Int = 100,
    simThresh: Double = 0.80,
    pcaK: Int = 4,
    pcaWarmup: Int = 10
) {
    var threshold: Double = simThresh
        private set

    val nodes = mutableListOf<DoubleArray>()
    val temporalEdges = mutableListOf<Pair<Int, Int>>()
    val semanticEdges = mutableListOf<Pair<Int, Int>>()

    val pca = IncrementalPca(pcaK, pcaWarmup)

    data class GnnInput(
        val nodeFeatures: Array<DoubleArray>, // [N, K_PCA]
        val edges: List<Pair<Int, Int>>,
        val batch: IntArray,
        val valid: Boolean
    )

    fun addState(stateVector: DoubleArray) {
        val raw = stateVector.clone()
        val norm = l2Norm(raw)
        val xNormalized = if (norm > 1e-8) {
            DoubleArray(raw.size) { i -> raw[i] / norm }
        } else {
            raw
        }

        // Partial fit PCA with normalized vector
        pca.partialFit(xNormalized)

        // Synaptogenesis
        if (nodes.isNotEmpty()) {
            val lastIdx = nodes.size - 1
            val currentIdx = nodes.size
            temporalEdges.add(Pair(lastIdx, currentIdx))

            for (i in nodes.indices) {
                val sim = dotProduct(xNormalized, nodes[i])
                if (sim > threshold) {
                    semanticEdges.add(Pair(i, currentIdx))
                }
            }
        }

        nodes.add(xNormalized)

        if (nodes.size > maxHistory) {
            pruneHistory()
        }
    }

    private fun pruneHistory() {
        val keep = maxHistory / 2
        val remove = nodes.size - keep
        if (remove <= 0) return

        // Drop oldest nodes
        val remaining = nodes.subList(remove, nodes.size).toList()
        nodes.clear()
        nodes.addAll(remaining)

        // Recalculate temporal edges with relative indices
        val newTemp = mutableListOf<Pair<Int, Int>>()
        for (e in temporalEdges) {
            val a = e.first - remove
            val b = e.second - remove
            if (a in 0 until keep && b in 0 until keep) {
                newTemp.add(Pair(a, b))
            }
        }
        temporalEdges.clear()
        temporalEdges.addAll(newTemp)

        // Recalculate semantic edges with relative indices
        val newSem = mutableListOf<Pair<Int, Int>>()
        for (e in semanticEdges) {
            val a = e.first - remove
            val b = e.second - remove
            if (a in 0 until keep && b in 0 until keep) {
                newSem.add(Pair(a, b))
            }
        }
        semanticEdges.clear()
        semanticEdges.addAll(newSem)
    }

    fun getGnnInput(): GnnInput {
        if (nodes.isEmpty()) {
            return GnnInput(emptyArray(), emptyList(), IntArray(0), false)
        }

        val n = nodes.size
        val fullFeatures = Array(n) { i -> nodes[i] }
        val nodeFeatures = pca.transform(fullFeatures)

        val allEdges = mutableListOf<Pair<Int, Int>>()
        allEdges.addAll(temporalEdges)
        allEdges.addAll(semanticEdges)

        if (allEdges.isEmpty()) {
            for (i in 0 until n) {
                allEdges.add(Pair(i, i))
            }
        }

        return GnnInput(
            nodeFeatures = nodeFeatures,
            edges = allEdges,
            batch = IntArray(n) { 0 },
            valid = true
        )
    }

    fun applyStabilization(adjustments: DoubleArray) {
        if (adjustments.isEmpty()) return
        var sum = 0.0
        for (a in adjustments) sum += a
        val meanAdj = sum / adjustments.size.toDouble()
        val magnitude = abs(meanAdj)
        val delta = 0.05 * (1.0 + magnitude)

        if (meanAdj > 0.01) {
            threshold = min(0.99, threshold + delta * meanAdj)
        } else if (meanAdj < -0.01) {
            threshold = max(0.10, threshold + delta * meanAdj)
        }
    }

    private fun l2Norm(v: DoubleArray): Double {
        var sum = 0.0
        for (x in v) sum += x * x
        return sqrt(sum)
    }

    private fun dotProduct(a: DoubleArray, b: DoubleArray): Double {
        val len = min(a.size, b.size)
        var sum = 0.0
        for (i in 0 until len) {
            sum += a[i] * b[i]
        }
        return sum
    }
}
