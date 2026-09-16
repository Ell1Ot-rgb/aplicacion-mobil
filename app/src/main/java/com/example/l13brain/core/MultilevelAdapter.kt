package com.example.l13brain.core

import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * MultilevelAdapter (Fix-03).
 * N-ary hypergraph with message passing across concepts and relations.
 */
class MultilevelAdapter(
    val embedDim: Int = 64
) {
    var cycle: Int = 0

    data class Concept(
        val label: String,
        var embedding: DoubleArray
    )

    data class Hyperedge(
        val nodes: List<Int>,
        val label: String,
        val weight: Double
    )

    val concepts = mutableListOf<Concept>()
    val hedges = mutableListOf<Hyperedge>()

    private val wNode: Array<DoubleArray> // [embedDim][embedDim]
    private val wEdge: Array<DoubleArray> // [embedDim][embedDim]

    val quantizer = ThoughtVectorQuantizer()

    init {
        val rng = Random(123)
        val scale = 1.0 / sqrt(embedDim.toDouble())
        wNode = Array(embedDim) {
            DoubleArray(embedDim) { rng.nextGaussian() * scale }
        }
        wEdge = Array(embedDim) {
            DoubleArray(embedDim) { rng.nextGaussian() * scale }
        }
    }

    fun addConcept(label: String): Int {
        for (i in concepts.indices) {
            if (concepts[i].label == label) return i
        }

        val rng = Random(label.hashCode().toLong())
        val scale = 1.0 / sqrt(embedDim.toDouble())
        val emb = DoubleArray(embedDim) { rng.nextGaussian() * scale }
        concepts.add(Concept(label, emb))
        return concepts.size - 1
    }

    fun addNaryRelation(nodeIds: List<Int>, label: String, weight: Double = 1.0) {
        if (concepts.size < 100) {
            hedges.add(Hyperedge(nodeIds, label, weight))
        }
    }

    fun computeEmbeddings(): Array<DoubleArray> {
        if (concepts.isEmpty()) {
            return Array(1) { DoubleArray(embedDim) }
        }

        val n = concepts.size
        val h = Array(n) { i -> concepts[i].embedding.clone() }

        // Phase 1: Nodes -> Edges
        val edgeEmbs = Array(hedges.size) { DoubleArray(embedDim) }
        for (ei in hedges.indices) {
            val he = hedges[ei]
            if (he.nodes.isEmpty()) continue
            val agg = DoubleArray(embedDim)
            for (nid in he.nodes) {
                if (nid < n) {
                    for (d in 0 until embedDim) {
                        agg[d] += h[nid][d]
                    }
                }
            }
            val count = he.nodes.size.toDouble()
            for (d in 0 until embedDim) agg[d] /= count

            // Project: W_edge^T * agg
            for (j in 0 until embedDim) {
                var dot = 0.0
                for (k in 0 until embedDim) {
                    dot += agg[k] * wEdge[k][j]
                }
                edgeEmbs[ei][j] = tanh(dot)
            }
        }

        // Phase 2: Edges -> Nodes (Residual Update with factor 0.1)
        val hNew = Array(n) { i -> h[i].clone() }
        for (ei in hedges.indices) {
            val he = hedges[ei]
            val update = DoubleArray(embedDim)
            for (j in 0 until embedDim) {
                var dot = 0.0
                for (k in 0 until embedDim) {
                    dot += edgeEmbs[ei][k] * wNode[k][j]
                }
                update[j] = tanh(dot)
            }
            for (nid in he.nodes) {
                if (nid < n) {
                    for (d in 0 until embedDim) {
                        hNew[nid][d] += 0.1 * update[d]
                    }
                }
            }
        }

        // Phase 3: Spherical L2 normalization
        for (i in 0 until n) {
            var sumSq = 0.0
            for (d in 0 until embedDim) sumSq += hNew[i][d] * hNew[i][d]
            val norm = sqrt(sumSq)
            if (norm > 1e-8) {
                for (d in 0 until embedDim) hNew[i][d] /= norm
            }
            concepts[i].embedding = hNew[i]
        }

        return hNew
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
