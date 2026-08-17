package com.example.entryrecorder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.entryrecorder.model.AppConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT value FROM app_config WHERE `key` = :key LIMIT 1")
    fun getConfigFlow(key: String): Flow<String?>

    @Query("SELECT value FROM app_config WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: AppConfig)

    @Query("DELETE FROM app_config")
    suspend fun clearConfig()
}
