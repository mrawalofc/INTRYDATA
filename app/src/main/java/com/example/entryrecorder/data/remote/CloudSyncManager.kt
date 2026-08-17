package com.example.entryrecorder.data.remote

import android.util.Log
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class SyncPayload(
    val action: String = "syncAll",
    val users: List<User>,
    val records: List<EntryRecord>
)

@Serializable
data class SyncResponse(
    val success: Boolean = false,
    val error: String? = null
)

class CloudSyncManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    suspend fun fetchUsers(serverUrl: String): List<User>? = withContext(Dispatchers.IO) {
        try {
            val url = if (serverUrl.contains("?")) "$serverUrl&action=getUsers" else "$serverUrl?action=getUsers"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                json.decodeFromString<List<User>>(body)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "fetchUsers failed", e)
            null
        }
    }

    suspend fun fetchRecords(serverUrl: String): List<EntryRecord>? = withContext(Dispatchers.IO) {
        try {
            val url = if (serverUrl.contains("?")) "$serverUrl&action=getRecords" else "$serverUrl?action=getRecords"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                json.decodeFromString<List<EntryRecord>>(body)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "fetchRecords failed", e)
            null
        }
    }

    suspend fun pushSyncAll(serverUrl: String, users: List<User>, records: List<EntryRecord>): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = SyncPayload(action = "syncAll", users = users, records = records)
            val jsonBody = json.encodeToString(payload)
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext false
                try {
                    val res = json.decodeFromString<SyncResponse>(body)
                    res.success
                } catch (e: Exception) {
                    true // HTTP 200 is accepted
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "pushSyncAll failed", e)
            false
        }
    }
}
