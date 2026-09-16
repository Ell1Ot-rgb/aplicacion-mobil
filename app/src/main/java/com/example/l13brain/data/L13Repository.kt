package com.example.l13brain.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Repository to persist and observe L13 cycle records.
 */
class L13Repository(context: Context) {
    private val dbHelper = L13CycleDbHelper(context)
    private val _historyFlow = MutableStateFlow<List<L13CycleRecord>>(emptyList())
    val historyFlow: StateFlow<List<L13CycleRecord>> = _historyFlow.asStateFlow()

    suspend fun loadHistory() {
        withContext(Dispatchers.IO) {
            val records = dbHelper.getAllCycles(100)
            _historyFlow.value = records
        }
    }

    suspend fun saveCycle(record: L13CycleRecord) {
        withContext(Dispatchers.IO) {
            dbHelper.insertOrUpdateCycle(record)
            val updated = dbHelper.getAllCycles(100)
            _historyFlow.value = updated
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            dbHelper.clearAllCycles()
            _historyFlow.value = emptyList()
        }
    }
}
