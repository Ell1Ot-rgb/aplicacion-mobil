package com.example.l13brain.core

import kotlin.math.min
import kotlin.math.sqrt

/**
 * L13UnifiedProcessor.
 * Master orchestrator connecting all 11 modules into the unified L13 Semantic Consciousness pipeline:
 * IncrementalPCA, HypergraphBuilder, WolframAdapter, MultilevelAdapter, FusionBridge,
 * SimulatedBrainGNN, TopologicalFeedbackBridge (Vietoris-Rips + Vineyards), and HolographicVSA.
 */
class L13UnifiedProcessor(
    val dInput: Int = 2048,
    val conceptDim: Int = 256,
    val embedDim: Int = 64,
    val fusedDim: Int = 128,
    val vsaDim: Int = 2048
) {
    val hypergraph = HypergraphBuilder(embeddingDim = 256, maxHistory = 100, simThresh = 0.80, pcaK = 4, pcaWarmup = 10)
    val topoBridge = TopologicalFeedbackBridge(maxNodes = 30)
    val fusion = FusionBridge(embedDim)
    val vsa = HolographicVsa(vsaDim)
    val brainGnn = SimulatedBrainGnn()

    var cycle: Int = 0
        private set

    data class ProcessResult(
        val status: String,
        val cycle: Int,
        val nNodes: Int,
        val nEdges: Int,
        val similarityThreshold: Double,
        val pcaFitted: Boolean,
        val pcaNSeen: Int,
        val pcaVariancePct: Double,
        val topoLoss: Double,
        val betti0L13: Double,
        val betti1L13: Double,
        val topoTargetB0: Double,
        val topoTargetB1: Double,
        val topoEvent: Boolean,
        val topoEventType: String,
        val fusedEmbedding: DoubleArray,
        val wolframSteps: Int,
        val mlCycles: Int,
        val stability: Double,
        val adjustmentsMean: Double,
        val vsaSimilarity: Double,
        val vsaMemorySize: Int,
        val betti0Curve: DoubleArray,
        val betti1Curve: DoubleArray
    )

    fun ingestL11Betti(b0: Double, b1: Double) {
        topoBridge.ingestL11(b0, b1)
    }

    fun process(thoughtVector: DoubleArray, conceptVector: DoubleArray): ProcessResult {
        val currentCycle = cycle++

        // Step 1: Hypergraph state addition & Synaptogenesis
        hypergraph.addState(thoughtVector)
        val gnnInput = hypergraph.getGnnInput()

        if (!gnnInput.valid || gnnInput.nodeFeatures.isEmpty()) {
            return ProcessResult(
                status = "NO_DATA",
                cycle = currentCycle,
                nNodes = 0,
                nEdges = 0,
                similarityThreshold = hypergraph.threshold,
                pcaFitted = false,
                pcaNSeen = 0,
                pcaVariancePct = 0.0,
                topoLoss = 0.0,
                betti0L13 = 0.0,
                betti1L13 = 0.0,
                topoTargetB0 = 1.0,
                topoTargetB1 = 5.0,
                topoEvent = false,
                topoEventType = "NONE",
                fusedEmbedding = DoubleArray(fusedDim),
                wolframSteps = 0,
                mlCycles = 0,
                stability = 0.0,
                adjustmentsMean = 0.0,
                vsaSimilarity = 0.0,
                vsaMemorySize = 0,
                betti0Curve = DoubleArray(20),
                betti1Curve = DoubleArray(20)
            )
        }

        val nNodes = gnnInput.nodeFeatures.size
        val nEdges = gnnInput.edges.size
        val simThreshold = hypergraph.threshold
        val pcaFitted = hypergraph.pca.isFitted
        val pcaNSeen = hypergraph.pca.nSeen
        var pcaSum = 0.0
        for (r in hypergraph.pca.explainedVarianceRatio()) pcaSum += r
        val pcaVariancePct = pcaSum * 100.0

        // Step 2: Wolfram + Multilevel FusionBridge
        val fusionRes = fusion.process(thoughtVector)

        // Step 3: Enrich global_vector with fused_embedding
        val conceptFlat = DoubleArray(conceptDim)
        val cvLen = min(conceptVector.size, conceptDim)
        if (cvLen > 0) {
            for (i in 0 until cvLen) conceptFlat[i] = conceptVector[i]
            for (i in cvLen until conceptDim) conceptFlat[i] = conceptFlat[i % cvLen]
        }

        val fusedProj = DoubleArray(conceptDim)
        val fpLen = min(fusionRes.fusedEmbedding.size, conceptDim)
        if (fpLen > 0) {
            for (i in 0 until fpLen) fusedProj[i] = fusionRes.fusedEmbedding[i]
            for (i in fpLen until conceptDim) fusedProj[i] = fusedProj[i % fpLen]
        }

        val globalEnriched = DoubleArray(conceptDim) { i ->
            0.7 * conceptFlat[i] + 0.3 * fusedProj[i]
        }

        // Step 4: Brain GNN Inference
        val gnnOut = brainGnn.run(gnnInput.nodeFeatures, globalEnriched)
        hypergraph.applyStabilization(gnnOut.adjustments)

        var adjSum = 0.0
        for (a in gnnOut.adjustments) adjSum += a
        val adjustmentsMean = adjSum / gnnOut.adjustments.size.toDouble()

        // Step 5: Topological Feedback & Vineyards
        val topoRes = topoBridge.processL13(thoughtVector)

        // Step 6: Holographic VSA Memory
        val keyNorm = l2Norm(conceptFlat)
        val key = if (keyNorm > 1e-8) {
            DoubleArray(conceptFlat.size) { i -> conceptFlat[i] / keyNorm }
        } else conceptFlat

        val tvVsa = DoubleArray(vsaDim)
        val tvCopy = min(thoughtVector.size, vsaDim)
        System.arraycopy(thoughtVector, 0, tvVsa, 0, tvCopy)

        val keyVsa = DoubleArray(vsaDim)
        val keyCopy = min(key.size, vsaDim)
        System.arraycopy(key, 0, keyVsa, 0, keyCopy)

        vsa.store(keyVsa, tvVsa)
        val vsaSim = vsa.querySimilarity(tvVsa)
        val vsaMemSize = vsa.memorySize()

        return ProcessResult(
            status = "ACTIVE",
            cycle = currentCycle,
            nNodes = nNodes,
            nEdges = nEdges,
            similarityThreshold = simThreshold,
            pcaFitted = pcaFitted,
            pcaNSeen = pcaNSeen,
            pcaVariancePct = pcaVariancePct,
            topoLoss = topoRes.topoLoss,
            betti0L13 = topoRes.betti0,
            betti1L13 = topoRes.betti1,
            topoTargetB0 = topoRes.targetB0,
            topoTargetB1 = topoRes.targetB1,
            topoEvent = topoRes.hadEvent,
            topoEventType = topoRes.eventType,
            fusedEmbedding = fusionRes.fusedEmbedding,
            wolframSteps = fusionRes.wolframStep,
            mlCycles = fusionRes.mlCycle,
            stability = gnnOut.stability,
            adjustmentsMean = adjustmentsMean,
            vsaSimilarity = vsaSim,
            vsaMemorySize = vsaMemSize,
            betti0Curve = topoRes.betti0Curve,
            betti1Curve = topoRes.betti1Curve
        )
    }

    private fun l2Norm(v: DoubleArray): Double {
        var sum = 0.0
        for (x in v) sum += x * x
        return sqrt(sum)
    }
}
