package com.example.l13brain.core

import java.util.ArrayDeque

/**
 * BettiStateBuffer (Fix-02).
 * Circular buffer with Exponential Moving Average (EMA) to stabilize Layer 11 targets beta_0*, beta_1*.
 */
class BettiStateBuffer(
    val window: Int = 10,
    val alpha: Double = 0.30
) {
    var nUpdates: Int = 0
        private set
    var emaB0: Double = 1.0
        private set
    var emaB1: Double = 5.0
        private set
    var isInitialized: Boolean = false
        private set

    private val histB0 = ArrayDeque<Double>()
    private val histB1 = ArrayDeque<Double>()

    fun update(b0: Double, b1: Double) {
        if (histB0.size >= window) histB0.pollFirst()
        if (histB1.size >= window) histB1.pollFirst()
        histB0.addLast(b0)
        histB1.addLast(b1)
        nUpdates++

        if (!isInitialized) {
            emaB0 = b0
            emaB1 = b1
            isInitialized = true
        } else {
            emaB0 = alpha * b0 + (1.0 - alpha) * emaB0
            emaB1 = alpha * b1 + (1.0 - alpha) * emaB1
        }
    }

    fun targetB0(): Double = if (isInitialized) emaB0 else 1.0
    fun targetB1(): Double = if (isInitialized) emaB1 else 5.0
    fun isWarm(): Boolean = nUpdates >= 3
}
