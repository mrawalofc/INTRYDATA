package com.example.entryrecorder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "records")
data class EntryRecord(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),
    val serial: Int = 1,
    val date: String = "",
    val time: String = "",
    val name: String = "",
    val idNumber: String = "",
    val mobile: String = "",
    val application: String = "",
    val ageCode: String = "",
    val amount: Double = 0.0,
    val invoice: String = "",
    val requestNo: String = "",
    val creator: String = "",
    val timestamp: String = "",
    val comment: String = ""
)
