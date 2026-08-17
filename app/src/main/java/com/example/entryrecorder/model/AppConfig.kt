package com.example.entryrecorder.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey
    val key: String,
    val value: String
)
