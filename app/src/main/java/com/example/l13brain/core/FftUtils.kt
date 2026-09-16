package com.example.l13brain.core

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cooley-Tukey radix-2 Decimation-In-Time (DIT) Fast Fourier Transform in pure Kotlin.
 * Computes exact circular convolution: bind(u, v) = IFFT(FFT(u) * FFT(v)).
 */
object FftUtils {

    data class Complex(var re: Double, var im: Double) {
        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(
            re * other.re - im * other.im,
            re * other.im + im * other.re
        )
    }

    fun fftInplace(a: Array<Complex>, inverse: Boolean) {
        val n = a.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val temp = a[i]
                a[i] = a[j]
                a[j] = temp
            }
        }

        var len = 2
        while (len <= n) {
            val ang = 2.0 * PI / len * if (inverse) -1.0 else 1.0
            val wlen = Complex(cos(ang), sin(ang))
            var i = 0
            while (i < n) {
                var w = Complex(1.0, 0.0)
                for (k in 0 until len / 2) {
                    val u = a[i + k]
                    val v = a[i + k + len / 2] * w
                    a[i + k] = u + v
                    a[i + k + len / 2] = u - v
                    w = w * wlen
                }
                i += len
            }
            len = len shl 1
        }

        if (inverse) {
            val factor = 1.0 / n.toDouble()
            for (k in a.indices) {
                a[k].re *= factor
                a[k].im *= factor
            }
        }
    }

    /**
     * Exact circular convolution: IFFT(FFT(u) * FFT(v))
     */
    fun circularConvolve(u: DoubleArray, v: DoubleArray): DoubleArray {
        val n = u.size
        var m = 1
        while (m < n) m = m shl 1

        val fu = Array(m) { i -> if (i < n) Complex(u[i], 0.0) else Complex(0.0, 0.0) }
        val fv = Array(m) { i -> if (i < n) Complex(v[i], 0.0) else Complex(0.0, 0.0) }

        fftInplace(fu, false)
        fftInplace(fv, false)

        for (i in 0 until m) {
            fu[i] = fu[i] * fv[i]
        }

        fftInplace(fu, true)

        val result = DoubleArray(n)
        for (i in 0 until n) {
            result[i] = fu[i].re
        }
        return result
    }
}
