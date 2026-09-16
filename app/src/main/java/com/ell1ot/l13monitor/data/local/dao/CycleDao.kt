package com.ell1ot.l13monitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ell1ot.l13monitor.data.local.db.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CycleEntity)

    @Query("SELECT * FROM cycles ORDER BY tsMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<CycleEntity>>
}
