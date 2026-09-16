package com.example.l13brain.core

import kotlin.math.abs

/**
 * ThoughtVectorQuantizer (Fix-03).
 * Converts continuous R^D thought vector into discrete hyperedges and concept labels.
 */
class ThoughtVectorQuantizer(
    val blockSize: Int = 64,
    val edgeArity: Int = 3
) {
    data class QuantizeResult(
        val hyperedges: List<Set<Int>>,
        val labels: List<String>,
        val intensityMap: Map<Int, Double>
    )

    fun quantize(tv: DoubleArray): QuantizeResult {
        val d = tv.size
        val nBlocks = d / blockSize
        val hyperedges = mutableListOf<Set<Int>>()
        val labels = mutableListOf<String>()
        val intensityMap = mutableMapOf<Int, Double>()

        val nodeIds = IntArray(nBlocks)

        for (b in 0 until nBlocks) {
            val start = b * blockSize
            var nodeId = 0
            var maxAbs = 0.0
            for (j in 0 until blockSize) {
                val idx = start + j
                val v = if (idx < d) abs(tv[idx]) else 0.0
                if (v > maxAbs) {
                    maxAbs = v
                    nodeId = j
                }
            }
            nodeIds[b] = nodeId
            intensityMap[nodeId] = maxAbs
        }

        var j = 0
        while (j + edgeArity <= nBlocks) {
            val edge = mutableSetOf<Int>()
            val sb = StringBuilder("CONCEPT_${j / edgeArity}_")
            for (k in 0 until edgeArity) {
                val nid = nodeIds[j + k]
                edge.add(nid)
                sb.append(nid)
                if (k < edgeArity - 1) sb.append("_")
            }
            hyperedges.add(edge)
            labels.add(sb.toString())
            j += edgeArity
        }

        return QuantizeResult(hyperedges, labels, intensityMap)
    }
}
