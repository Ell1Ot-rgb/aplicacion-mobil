package com.example.l13brain.core

import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * WolframAdapter (Fix-03).
 * Simulates Wolfram hypergraph automaton evolution with local rewriting rules.
 * Extracts structural node features and projects to a 64D embedding.
 */
class WolframAdapter(
    val embedDim: Int = 64,
    val maxNodes: Int = 64
) {
    var stepCount: Int = 0
        private set

    val hyperedges = mutableListOf<Set<Int>>()
    private val wStruct: Array<DoubleArray> // [4][embedDim]
    val quantizer = ThoughtVectorQuantizer()

    init {
        // Base triangle hypergraph
        hyperedges.add(setOf(0, 1))
        hyperedges.add(setOf(1, 2))
        hyperedges.add(setOf(2, 0))

        val rng = Random(42)
        val scale = 1.0 / sqrt(embedDim.toDouble())
        wStruct = Array(4) {
            DoubleArray(embedDim) { rng.nextGaussian() * scale }
        }
    }

    fun addHyperedge(edge: Set<Int>) {
        if (hyperedges.size < maxNodes) {
            hyperedges.add(edge)
        }
    }

    /**
     * Wolfram evolution step: local expansion {{x, y}} -> {{x, y}, {y, z}}
     */
    fun step() {
        var maxNode = 0
        for (e in hyperedges) {
            for (n in e) {
                if (n > maxNode) maxNode = n
            }
        }
        var nextNode = maxNode + 1

        val newEdges = mutableListOf<Set<Int>>()
        var applied = 0
        for (e in hyperedges) {
            if (applied >= 3) break
            if (e.size >= 2 && nextNode < maxNodes) {
                val newEdge = e.toMutableSet()
                newEdge.add(nextNode)
                newEdges.add(newEdge)
                nextNode++
                applied++
            }
        }

        for (ne in newEdges) {
            if (hyperedges.size < maxNodes) {
                hyperedges.add(ne)
            }
        }
        stepCount++
    }

    fun computeEmbeddings(): Array<DoubleArray> {
        val nodeEdges = mutableMapOf<Int, MutableList<Int>>()
        for (ei in hyperedges.indices) {
            for (n in hyperedges[ei]) {
                nodeEdges.getOrPut(n) { mutableListOf() }.add(ei)
            }
        }

        if (nodeEdges.isEmpty()) {
            return Array(1) { DoubleArray(embedDim) }
        }

        val n = nodeEdges.size
        val embeddings = Array(n) { DoubleArray(embedDim) }

        var maxDegree = 1
        for (list in nodeEdges.values) {
            if (list.size > maxDegree) maxDegree = list.size
        }

        var row = 0
        for ((node, edgeList) in nodeEdges) {
            val degree = edgeList.size.toDouble() / maxDegree.toDouble()

            var totalCard = 0.0
            val neighbors = mutableSetOf<Int>()
            for (ei in edgeList) {
                val e = hyperedges[ei]
                totalCard += e.size.toDouble()
                for (nb in e) {
                    if (nb != node) neighbors.add(nb)
                }
            }
            val avgCard = (totalCard / max(1, edgeList.size)) / 10.0
            val nNeighbors = neighbors.size.toDouble() / maxDegree.toDouble()
            val clustering = if (neighbors.isNotEmpty()) {
                min(1.0, edgeList.size.toDouble() / (neighbors.size + 1).toDouble())
            } else 0.0

            val feat = doubleArrayOf(degree, avgCard, nNeighbors, clustering)

            for (j in 0 until embedDim) {
                var dot = 0.0
                for (k in 0 until 4) {
                    dot += feat[k] * wStruct[k][j]
                }
                embeddings[row][j] = tanh(dot)
            }
            row++
        }

        return embeddings
    }

    fun pooledEmbedding(): DoubleArray {
        val embs = computeEmbeddings()
        if (embs.isEmpty()) return DoubleArray(embedDim)
        val pooled = DoubleArray(embedDim)
        for (j in 0 until embedDim) {
            var sum = 0.0
            for (i in embs.indices) {
                sum += embs[i][j]
            }
            pooled[j] = sum / embs.size.toDouble()
        }
        return pooled
    }
}
