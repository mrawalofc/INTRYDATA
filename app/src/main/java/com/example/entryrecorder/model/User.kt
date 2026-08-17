package com.example.entryrecorder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val password: String,
    val role: String = "user", // "admin" or "user"
    val canViewAmounts: Boolean = false
)
