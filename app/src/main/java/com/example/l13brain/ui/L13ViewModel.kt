package com.example.l13brain.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.l13brain.core.L13UnifiedProcessor
import com.example.l13brain.data.L13CycleRecord
import com.example.l13brain.data.L13Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class L13ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = L13Repository(application)
    private var processor = L13UnifiedProcessor.shared

    val history = repository.historyFlow

    data class UiState(
        val lastResult: L13UnifiedProcessor.ProcessResult? = null,
        val isRunning: Boolean = false,
        val cycleIntervalMs: Long = 1200L,
        val selectedStimulus: String = "Gaussian Noise",
        val inputBetti0: Double = 1.0,
        val inputBetti1: Double = 3.0,
        val totalCyclesRun: Int = 0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var runJob: Job? = null
    private val rng = Random(2026)

    init {
        viewModelScope.launch {
            repository.loadHistory()
            // Execute initial cycle so the dashboard immediately shows active telemetry
            step()
        }
    }

    fun toggleRunning() {
        if (_uiState.value.isRunning) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        if (runJob?.isActive == true) return
        _uiState.value = _uiState.value.copy(isRunning = true)
        runJob = viewModelScope.launch {
            while (isActive) {
                step()
                delay(_uiState.value.cycleIntervalMs)
            }
        }
    }

    fun stop() {
        runJob?.cancel()
        runJob = null
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun setInterval(ms: Long) {
        _uiState.value = _uiState.value.copy(cycleIntervalMs = ms)
        if (_uiState.value.isRunning) {
            stop()
            start()
        }
    }

    fun setStimulus(stimulus: String) {
        _uiState.value = _uiState.value.copy(selectedStimulus = stimulus)
    }

    fun setLayer11Betti(b0: Double, b1: Double) {
        _uiState.value = _uiState.value.copy(inputBetti0 = b0, inputBetti1 = b1)
    }

    fun step() {
        viewModelScope.launch {
            val tv = generateThoughtVector(_uiState.value.selectedStimulus)
            val cv = generateConceptVector(_uiState.value.selectedStimulus)

            // Ingest Layer 11 Betti numbers
            processor.ingestL11Betti(_uiState.value.inputBetti0, _uiState.value.inputBetti1)

            // Run inference cycle
            val result = processor.process(tv, cv)

            // Persist to local database
            val record = L13CycleRecord(
                cycle = result.cycle.toLong(),
                timestamp = System.currentTimeMillis(),
                status = result.status,
                stability = result.stability,
                topoLoss = result.topoLoss,
                betti0L13 = result.betti0L13,
                betti1L13 = result.betti1L13,
                vsaSimilarity = result.vsaSimilarity,
                pcaVariancePct = result.pcaVariancePct,
                nNodes = result.nNodes,
                nEdges = result.nEdges,
                similarityThreshold = result.similarityThreshold,
                topoEventType = result.topoEventType,
                wolframSteps = result.wolframSteps,
                mlCycles = result.mlCycles
            )
            repository.saveCycle(record)

            _uiState.value = _uiState.value.copy(
                lastResult = result,
                totalCyclesRun = _uiState.value.totalCyclesRun + 1
            )
        }
    }

    fun resetEngine() {
        stop()
        processor = L13UnifiedProcessor()
        _uiState.value = UiState()
        step()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun generateThoughtVector(stimulus: String): DoubleArray {
        val d = 2048
        val tv = DoubleArray(d)
        when (stimulus) {
            "Gaussian Noise" -> {
                for (i in 0 until d) tv[i] = rng.nextGaussian()
            }
            "Cognitive Genesis" -> {
                for (i in 0 until d) {
                    val phase = i * 0.05
                    tv[i] = sin(phase) + 0.3 * rng.nextGaussian()
                }
            }
            "Neural Coherence" -> {
                for (i in 0 until d) {
                    val harmonic = sin(i * 0.01) * cos(i * 0.03)
                    tv[i] = harmonic * 2.0 + 0.2 * rng.nextGaussian()
                }
            }
            "Quantum Flux" -> {
                for (i in 0 until d) {
                    val pulse = if (i % 64 < 16) 1.5 else -0.5
                    tv[i] = pulse + rng.nextGaussian() * 0.5
                }
            }
            "Semantic Resonator" -> {
                for (i in 0 until d) {
                    tv[i] = sin(i.toDouble() / 128.0 * Math.PI) * 1.8 + rng.nextGaussian() * 0.2
                }
            }
            else -> {
                for (i in 0 until d) tv[i] = rng.nextGaussian()
            }
        }
        return tv
    }

    private fun generateConceptVector(stimulus: String): DoubleArray {
        val cd = 256
        val cv = DoubleArray(cd)
        for (i in 0 until cd) {
            cv[i] = rng.nextGaussian()
        }
        return cv
    }
}
