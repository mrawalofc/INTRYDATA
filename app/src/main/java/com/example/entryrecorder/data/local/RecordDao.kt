package com.example.entryrecorder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.entryrecorder.model.EntryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY serial ASC")
    fun getAllRecords(): Flow<List<EntryRecord>>

    @Query("SELECT * FROM records ORDER BY serial ASC")
    suspend fun getAllRecordsSnapshot(): List<EntryRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: EntryRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<EntryRecord>)

    @Update
    suspend fun updateRecord(record: EntryRecord)

    @Query("UPDATE records SET ageCode = :ageCode, mobile = :mobile, idNumber = :idNumber, comment = :comment WHERE id = :id")
    suspend fun updateQuickFields(id: Long, ageCode: String, mobile: String, idNumber: String, comment: String)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM records")
    suspend fun deleteAllRecords()

    @Query("SELECT MAX(serial) FROM records")
    suspend fun getMaxSerial(): Int?
}
