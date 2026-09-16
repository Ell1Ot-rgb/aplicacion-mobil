package com.ell1ot.l13monitor.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ell1ot.l13monitor.data.local.dao.CommandLogDao
import com.ell1ot.l13monitor.data.local.dao.CycleDao
import com.ell1ot.l13monitor.data.local.dao.HealthDao

@Database(
    entities = [CycleEntity::class, CommandLogEntity::class, HealthSnapshotEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun commandLogDao(): CommandLogDao
    abstract fun healthDao(): HealthDao
}
