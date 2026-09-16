package com.ell1ot.l13monitor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ell1ot.l13monitor.data.local.db.CycleEntity
import com.ell1ot.l13monitor.data.repository.L13Repository
import com.ell1ot.l13monitor.ui.components.MetricRow
import com.ell1ot.l13monitor.ui.components.StatusIndicator
import com.ell1ot.l13monitor.ui.components.TopologyCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class DashboardState(
    val loading: Boolean = false,
    val healthy: Boolean = false,
    val lastCycle: CycleEntity? = null,
    val statusText: String = "desconocido",
)

sealed interface DashboardEvent { 
    data object Refresh : DashboardEvent
    data class SetLast(val cycle: CycleEntity?) : DashboardEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: L13Repository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun reduce(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Refresh -> refresh()
            is DashboardEvent.SetLast -> _state.update { it.copy(lastCycle = event.cycle) }
        }
    }

    init {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            repo.cycleHistory().collect { list ->
                _state.update { s -> s.copy(lastCycle = list.firstOrNull()) }
            }
        }
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            val res = repo.refreshHealth()
            _state.update {
                it.copy(
                    loading = false,
                    healthy = res.isSuccess,
                    statusText = res.getOrNull() ?: res.exceptionOrNull()?.message ?: "error",
                )
            }
            repo.refreshLastCycle()
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("L13 Monitor", style = MaterialTheme.typography.headlineMedium)
        StatusIndicator(ok = state.healthy, label = state.statusText)
        TopologyCard(
            title = "Último ciclo",
            subtitle = state.lastCycle?.let { "ciclo #${it.cycle} · status=${it.status}" } ?: "sin datos",
        ) {
            state.lastCycle?.let {
                MetricRow("estabilidad", it.stability?.toString() ?: "—")
                MetricRow("betti", "b0=${it.betti0 ?: "-"} b1=${it.betti1 ?: "-"}")
                MetricRow("nodos/aristas", "${it.nNodes ?: "-"} / ${it.nEdges ?: "-"}")
            }
        }
        Button(onClick = { viewModel.reduce(DashboardEvent.Refresh) }) {
            Text(if (state.loading) "actualizando…" else "Refrescar")
        }
    }
}

