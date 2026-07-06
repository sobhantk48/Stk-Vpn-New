package com.v2ray.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: ConnectionHistory)

    @Query("SELECT * FROM connection_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ConnectionHistory>>

    @Query("SELECT * FROM connection_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ConnectionHistory>>

    @Query("DELETE FROM connection_history WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM connection_history")
    suspend fun clearAll()
}
