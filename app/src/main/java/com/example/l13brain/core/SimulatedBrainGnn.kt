package com.example.l13brain.core

import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * SimulatedBrainGNN.
 * Homeostatic stability and adjustment module matching the neural GNN specifications.
 */
class SimulatedBrainGnn {

    data class GnnOutput(
        val stability: Double,
        val adjustments: DoubleArray // [16]
    )

    fun run(nodeFeatures: Array<DoubleArray>, globalVector: DoubleArray): GnnOutput {
        if (nodeFeatures.isEmpty()) {
            return GnnOutput(0.0, DoubleArray(16))
        }

        val k = nodeFeatures[0].size
        val centroid = DoubleArray(k)
        for (i in nodeFeatures.indices) {
            for (j in 0 until k) {
                centroid[j] += nodeFeatures[i][j]
            }
        }
        val count = nodeFeatures.size.toDouble()
        var sumSq = 0.0
        for (j in 0 until k) {
            centroid[j] /= count
            sumSq += centroid[j] * centroid[j]
        }
        val centroidNorm = sqrt(sumSq)

        // Stability: sigmoid(centroidNorm * 2.0)
        val stability = 1.0 / (1.0 + exp(-centroidNorm * 2.0))

        // Adjustments: 16 signals
        val adjustments = DoubleArray(16)
        for (i in 0 until 16) {
            val gvI = if (i < globalVector.size) globalVector[i] else 0.0
            adjustments[i] = tanh(gvI * 0.1)
        }

        return GnnOutput(stability, adjustments)
    }
}
