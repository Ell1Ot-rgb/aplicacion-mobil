package com.ell1ot.l13monitor.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycle: Int?,
    val status: String?,
    val stability: Double?,
    val topoLoss: Double?,
    val betti0: Int?,
    val betti1: Int?,
    val nNodes: Int?,
    val nEdges: Int?,
    val tsMillis: Long,
)

@Entity(tableName = "command_log")
data class CommandLogEntity(
    @PrimaryKey val cmdId: String,
    val kind: String,
    val payloadJson: String,
    val status: String, // PENDING | RETRYING | ACK | FAILED | TIMED_OUT
    val attempt: Int,
    val createdAtMillis: Long,
    val settledAtMillis: Long,
)

@Entity(tableName = "health_snapshots")
data class HealthSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val status: String?,
    val tsMillis: Long,
)
