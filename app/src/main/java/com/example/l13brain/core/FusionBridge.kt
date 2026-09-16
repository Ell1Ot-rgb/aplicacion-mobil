package com.example.l13brain.core

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * FusionBridge (Fix-03).
 * Fuses Wolfram hypergraph adapter embeddings (64D) with Multilevel N-ary embeddings (64D)
 * into a normalized 128D representation.
 */
class FusionBridge(
    val embedDim: Int = 64
) {
    companion object {
        const val FUSED_DIM = 128
    }

    val wolfram = WolframAdapter(embedDim)
    val multilevel = MultilevelAdapter(embedDim)
    val quantizer = ThoughtVectorQuantizer()

    data class FusionResult(
        val fusedEmbedding: DoubleArray, // [128]
        val wolframPool: DoubleArray,    // [64]
        val multilevelPool: DoubleArray, // [64]
        val wolframStep: Int,
        val mlCycle: Int
    )

    fun process(thoughtVector: DoubleArray): FusionResult {
        val qResult = quantizer.quantize(thoughtVector)

        // 1. Wolfram: Add hyperedges and step
        var added = 0
        for (edge in qResult.hyperedges) {
            if (added >= 5) break
            wolfram.addHyperedge(edge)
            added++
        }
        wolfram.step()
        val embW = wolfram.pooledEmbedding()

        // 2. Multilevel: Add concepts and co-activations
        val addedNodes = mutableListOf<Int>()
        var mlAdded = 0
        for (lbl in qResult.labels) {
            if (mlAdded >= 5) break
            val nid = multilevel.addConcept(lbl)
            addedNodes.add(nid)
            mlAdded++
        }

        if (addedNodes.size >= 2) {
            var meanIntensity = 0.0
            var cnt = 0
            for (entry in qResult.intensityMap) {
                if (cnt >= 3) break
                meanIntensity += entry.value
                cnt++
            }
            meanIntensity /= max(1, cnt).toDouble()

            val subset = addedNodes.subList(0, min(addedNodes.size, 3))
            multilevel.addNaryRelation(subset, "TEMPORAL_COACTIVATION", meanIntensity)
        }
        val embM = multilevel.pooledEmbedding()
        multilevel.cycle++

        // 3. Fuse: Concat 64 + 64 -> 128 and L2 normalize
        val fused = DoubleArray(FUSED_DIM)
        for (i in 0 until embedDim) {
            fused[i] = if (i < embW.size) embW[i] else 0.0
            fused[embedDim + i] = if (i < embM.size) embM[i] else 0.0
        }

        var sumSq = 0.0
        for (x in fused) sumSq += x * x
        val norm = sqrt(sumSq)
        if (norm > 1e-8) {
            for (i in fused.indices) fused[i] /= norm
        }

        return FusionResult(
            fusedEmbedding = fused,
            wolframPool = embW,
            multilevelPool = embM,
            wolframStep = wolfram.stepCount,
            mlCycle = multilevel.cycle
        )
    }
}
