package com.ell1ot.l13monitor.ui.hypergraph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell1ot.l13monitor.data.repository.L13Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HypergraphState(val nodes: Int = 8, val edges: List<Pair<Int, Int>> = emptyList())

@HiltViewModel
class HypergraphViewModel @Inject constructor(
    private val repo: L13Repository,
) : ViewModel() {
    private val _state = MutableStateFlow(HypergraphState())
    val state: StateFlow<HypergraphState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                repo.refreshLastCycle().onSuccess { r ->
                    val n = r.nNodes ?: 8
                    val m = r.nEdges ?: 0
                    _state.value = HypergraphState(
                        nodes = n.coerceAtLeast(1),
                        edges = buildList {
                            for (i in 0 until minOf(m, n)) add(i to (i + n / 2).coerceAtMost(n - 1))
                        },
                    )
                }
                delay(30_000)
            }
        }
    }
}

@Composable
fun HypergraphScreen(viewModel: HypergraphViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val radius = minOf(w, h) / 2.4f
        val n = state.nodes
        for (e in state.edges) {
            if (e.first >= n || e.second >= n) continue
            val a = angleFor(e.first, n)
            val b = angleFor(e.second, n)
            drawLine(
                color = androidx.compose.ui.graphics.Color(0xFF4ADE80),
                start = Offset(cx + radius * cos(a), cy + radius * sin(a)),
                end = Offset(cx + radius * cos(b), cy + radius * sin(b)),
                strokeWidth = 2f,
            )
        }
        for (i in 0 until n) {
            val a = angleFor(i, n)
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF7C4DFF),
                radius = 14f,
                center = Offset(cx + radius * cos(a), cy + radius * sin(a)),
            )
        }
    }
}

private fun angleFor(index: Int, total: Int): Float =
    (2 * Math.PI * (index.toFloat() / total.toFloat())).toFloat() - (Math.PI / 2f).toFloat()
