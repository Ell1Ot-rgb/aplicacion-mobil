package com.example.l13brain.core

import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * HolographicVSA (Vector Symbolic Architecture).
 * Implements circular convolution FFT Bind and tanh Bundle superposition.
 */
class HolographicVsa(
    val vsaDim: Int = 2048
) {
    val memoryVecs = mutableListOf<DoubleArray>()
    var superPosition: DoubleArray = DoubleArray(vsaDim)
        private set

    fun bind(u: DoubleArray, v: DoubleArray): DoubleArray {
        val uFixed = if (u.size == vsaDim) u else padOrTruncate(u, vsaDim)
        val vFixed = if (v.size == vsaDim) v else padOrTruncate(v, vsaDim)
        return FftUtils.circularConvolve(uFixed, vFixed)
    }

    fun bundle(vecs: List<DoubleArray>): DoubleArray {
        val sum = DoubleArray(vsaDim)
        for (v in vecs) {
            val len = min(vsaDim, v.size)
            for (i in 0 until len) {
                sum[i] += v[i]
            }
        }
        for (i in 0 until vsaDim) {
            sum[i] = tanh(sum[i])
        }
        return sum
    }

    fun store(key: DoubleArray, value: DoubleArray) {
        val bound = bind(key, value)
        memoryVecs.add(bound)

        for (i in 0 until vsaDim) {
            superPosition[i] += bound[i]
            superPosition[i] = tanh(superPosition[i])
        }

        if (memoryVecs.size > 50) {
            // v3 fix (audit #2023): evicted vector must be subtracted from the
            // superposition, otherwise the memory stays polluted forever and the
            // "bounded memory" claim was false. Because superPosition is a
            // non-linear tanh(sum) accumulator we cannot exactly subtract; the
            // correct behaviour is to rebuild from the surviving vectors.
            memoryVecs.removeAt(0)
            rebuildSuperPosition()
        }
    }

    /** Recompute the superposition from the surviving memory vectors. */
    private fun rebuildSuperPosition() {
        superPosition = DoubleArray(vsaDim)
        for (vec in memoryVecs) {
            for (i in 0 until vsaDim) {
                superPosition[i] += vec[i]
            }
        }
        for (i in 0 until vsaDim) {
            superPosition[i] = tanh(superPosition[i])
        }
    }

    fun querySimilarity(query: DoubleArray): Double {
        val qNorm = l2Norm(query)
        val sNorm = l2Norm(superPosition)
        if (qNorm < 1e-8 || sNorm < 1e-8) return 0.0

        var dot = 0.0
        val len = min(query.size, vsaDim)
        for (i in 0 until len) {
            dot += query[i] * superPosition[i]
        }
        return dot / (qNorm * sNorm)
    }

    fun memorySize(): Int = memoryVecs.size

    private fun padOrTruncate(src: DoubleArray, targetSize: Int): DoubleArray {
        val res = DoubleArray(targetSize)
        val len = min(src.size, targetSize)
        System.arraycopy(src, 0, res, 0, len)
        return res
    }

    private fun l2Norm(v: DoubleArray): Double {
        var sum = 0.0
        for (x in v) sum += x * x
        return sqrt(sum)
    }
}
