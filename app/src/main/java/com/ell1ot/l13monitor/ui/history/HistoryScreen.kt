package com.ell1ot.l13monitor.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell1ot.l13monitor.data.local.db.CycleEntity
import com.ell1ot.l13monitor.data.repository.L13Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryState(val items: List<CycleEntity> = emptyList())

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repo: L13Repository,
) : ViewModel() {
    val state: StateFlow<HistoryState> = repo.cycleHistory()
        .map { HistoryState(items = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryState())
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Historial", style = MaterialTheme.typography.titleLarge)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.items) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "cycle=${item.cycle} status=${item.status} stab=${item.stability}",
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}
