package com.example.l13brain.core

import java.util.Random
import kotlin.math.sqrt

/**
 * IncrementalPCA (Fix-01): CCIPCA (Candid Covariance-Free Incremental PCA).
 * Updates top-k eigenvectors in O(D * k) without building a D x D covariance matrix.
 * Reference: Weng et al. (2003) "Candid Covariance-Free Incremental PCA".
 */
class IncrementalPca(
    val nComponents: Int = 4,
    val nWarmup: Int = 10
) {
    var nSeen: Int = 0
        private set
    var isFitted: Boolean = false
        private set

    private var mean: DoubleArray? = null
    private var components: Array<DoubleArray>? = null // [k][D]
    private var explainedVar: DoubleArray = DoubleArray(nComponents) { 1.0 }

    fun partialFit(xRaw: DoubleArray) {
        val d = xRaw.size
        if (mean == null) {
            mean = DoubleArray(d)
            val rng = Random(42)
            components = Array(nComponents) {
                val row = DoubleArray(d) { rng.nextGaussian() * 1e-3 }
                normalize(row)
                row
            }
            explainedVar = DoubleArray(nComponents) { 1.0 }
        }

        nSeen++
        val m = mean!!
        val comp = components!!

        // Welford scalar online mean update
        val delta = DoubleArray(d) { i -> xRaw[i] - m[i] }
        for (i in 0 until d) {
            m[i] += delta[i] / nSeen.toDouble()
        }

        if (nSeen < 2) return

        // Centered vector
        val xCentered = DoubleArray(d) { i -> xRaw[i] - m[i] }
        val lr = 1.0 / nSeen.toDouble()

        var xRes = xCentered.clone()
        for (i in 0 until nComponents) {
            val u = comp[i]
            var proj = 0.0
            for (j in 0 until d) {
                proj += u[j] * xRes[j]
            }

            // CCIPCA update
            val uNew = DoubleArray(d)
            var normSq = 0.0
            for (j in 0 until d) {
                val v = (1.0 - lr) * u[j] + lr * proj * xRes[j]
                uNew[j] = v
                normSq += v * v
            }
            var uNorm = sqrt(normSq)
            if (uNorm < 1e-10) uNorm = 1.0

            explainedVar[i] = (1.0 - lr) * explainedVar[i] + lr * (proj * proj)

            for (j in 0 until d) {
                uNew[j] /= uNorm
                comp[i][j] = uNew[j]
                // Deflation
                xRes[j] -= proj * uNew[j]
            }
        }

        if (nSeen >= nWarmup) {
            isFitted = true
        }
    }

    fun transform(x: Array<DoubleArray>): Array<DoubleArray> {
        val n = x.size
        if (n == 0) return emptyArray()
        val d = x[0].size

        if (!isFitted || components == null || mean == null) {
            val kAct = minOf(nComponents, d)
            return Array(n) { i ->
                DoubleArray(nComponents) { j ->
                    if (j < kAct) x[i][j] else 0.0
                }
            }
        }

        val m = mean!!
        val comp = components!!
        val result = Array(n) { DoubleArray(nComponents) }

        for (i in 0 until n) {
            for (k in 0 until nComponents) {
                var dot = 0.0
                for (j in 0 until d) {
                    dot += (x[i][j] - m[j]) * comp[k][j]
                }
                result[i][k] = dot
            }
        }
        return result
    }

    fun explainedVarianceRatio(): DoubleArray {
        if (!isFitted) return DoubleArray(nComponents)
        var total = 0.0
        for (v in explainedVar) total += v
        if (total < 1e-10) return DoubleArray(nComponents)
        return DoubleArray(nComponents) { i -> explainedVar[i] / total }
    }

    private fun normalize(v: DoubleArray) {
        var sum = 0.0
        for (x in v) sum += x * x
        val norm = sqrt(sum)
        if (norm > 1e-10) {
            for (i in v.indices) v[i] /= norm
        }
    }
}
