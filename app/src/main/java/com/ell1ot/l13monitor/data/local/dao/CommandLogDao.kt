package com.ell1ot.l13monitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ell1ot.l13monitor.data.local.db.CommandLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CommandLogEntity)

    @Query("SELECT * FROM command_log ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<CommandLogEntity>>
}
