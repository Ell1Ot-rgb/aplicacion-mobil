package com.example.l13brain.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite Database Helper for persisting L13 Brain cycles locally on Android.
 */
class L13CycleDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "l13_brain.db"
        const val DATABASE_VERSION = 1

        const val TABLE_CYCLES = "l13_cycles"
        const val COL_CYCLE = "cycle"
        const val COL_TS = "ts"
        const val COL_STATUS = "status"
        const val COL_STABILITY = "stability"
        const val COL_TOPO_LOSS = "topo_loss"
        const val COL_BETTI_0 = "betti_0_l13"
        const val COL_BETTI_1 = "betti_1_l13"
        const val COL_VSA_SIM = "vsa_similarity"
        const val COL_PCA_VAR = "pca_variance_pct"
        const val COL_N_NODES = "n_nodes"
        const val COL_N_EDGES = "n_edges"
        const val COL_SIM_THRESH = "similarity_threshold"
        const val COL_TOPO_EVENT = "topo_event_type"
        const val COL_WOLFRAM_STEPS = "wolfram_steps"
        const val COL_ML_CYCLES = "ml_cycles"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_CYCLES (
                $COL_CYCLE INTEGER PRIMARY KEY,
                $COL_TS INTEGER,
                $COL_STATUS TEXT,
                $COL_STABILITY REAL,
                $COL_TOPO_LOSS REAL,
                $COL_BETTI_0 REAL,
                $COL_BETTI_1 REAL,
                $COL_VSA_SIM REAL,
                $COL_PCA_VAR REAL,
                $COL_N_NODES INTEGER,
                $COL_N_EDGES INTEGER,
                $COL_SIM_THRESH REAL,
                $COL_TOPO_EVENT TEXT,
                $COL_WOLFRAM_STEPS INTEGER,
                $COL_ML_CYCLES INTEGER
            );
        """.trimIndent()
        db.execSQL(createSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CYCLES")
        onCreate(db)
    }

    fun insertOrUpdateCycle(record: L13CycleRecord) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CYCLE, record.cycle)
            put(COL_TS, record.timestamp)
            put(COL_STATUS, record.status)
            put(COL_STABILITY, record.stability)
            put(COL_TOPO_LOSS, record.topoLoss)
            put(COL_BETTI_0, record.betti0L13)
            put(COL_BETTI_1, record.betti1L13)
            put(COL_VSA_SIM, record.vsaSimilarity)
            put(COL_PCA_VAR, record.pcaVariancePct)
            put(COL_N_NODES, record.nNodes)
            put(COL_N_EDGES, record.nEdges)
            put(COL_SIM_THRESH, record.similarityThreshold)
            put(COL_TOPO_EVENT, record.topoEventType)
            put(COL_WOLFRAM_STEPS, record.wolframSteps)
            put(COL_ML_CYCLES, record.mlCycles)
        }
        db.insertWithOnConflict(TABLE_CYCLES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllCycles(limit: Int = 100): List<L13CycleRecord> {
        val db = readableDatabase
        val list = mutableListOf<L13CycleRecord>()
        val cursor = db.query(
            TABLE_CYCLES,
            null,
            null,
            null,
            null,
            null,
            "$COL_CYCLE DESC",
            limit.toString()
        )

        cursor.use { c ->
            val idxCycle = c.getColumnIndexOrThrow(COL_CYCLE)
            val idxTs = c.getColumnIndexOrThrow(COL_TS)
            val idxStatus = c.getColumnIndexOrThrow(COL_STATUS)
            val idxStab = c.getColumnIndexOrThrow(COL_STABILITY)
            val idxLoss = c.getColumnIndexOrThrow(COL_TOPO_LOSS)
            val idxB0 = c.getColumnIndexOrThrow(COL_BETTI_0)
            val idxB1 = c.getColumnIndexOrThrow(COL_BETTI_1)
            val idxVsa = c.getColumnIndexOrThrow(COL_VSA_SIM)
            val idxPca = c.getColumnIndexOrThrow(COL_PCA_VAR)
            val idxNodes = c.getColumnIndexOrThrow(COL_N_NODES)
            val idxEdges = c.getColumnIndexOrThrow(COL_N_EDGES)
            val idxThresh = c.getColumnIndexOrThrow(COL_SIM_THRESH)
            val idxEvent = c.getColumnIndexOrThrow(COL_TOPO_EVENT)
            val idxWSteps = c.getColumnIndexOrThrow(COL_WOLFRAM_STEPS)
            val idxMl = c.getColumnIndexOrThrow(COL_ML_CYCLES)

            while (c.moveToNext()) {
                list.add(
                    L13CycleRecord(
                        cycle = c.getLong(idxCycle),
                        timestamp = c.getLong(idxTs),
                        status = c.getString(idxStatus),
                        stability = c.getDouble(idxStab),
                        topoLoss = c.getDouble(idxLoss),
                        betti0L13 = c.getDouble(idxB0),
                        betti1L13 = c.getDouble(idxB1),
                        vsaSimilarity = c.getDouble(idxVsa),
                        pcaVariancePct = c.getDouble(idxPca),
                        nNodes = c.getInt(idxNodes),
                        nEdges = c.getInt(idxEdges),
                        similarityThreshold = c.getDouble(idxThresh),
                        topoEventType = c.getString(idxEvent),
                        wolframSteps = c.getInt(idxWSteps),
                        mlCycles = c.getInt(idxMl)
                    )
                )
            }
        }
        return list
    }

    fun clearAllCycles() {
        val db = writableDatabase
        db.delete(TABLE_CYCLES, null, null)
    }
}
