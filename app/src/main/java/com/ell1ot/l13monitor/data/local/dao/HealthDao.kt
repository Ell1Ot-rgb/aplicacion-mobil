package com.ell1ot.l13monitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ell1ot.l13monitor.data.local.db.HealthSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HealthSnapshotEntity)

    @Query("SELECT * FROM health_snapshots ORDER BY tsMillis DESC LIMIT 1")
    fun observeLatest(): Flow<HealthSnapshotEntity?>
}
