package com.example.entryrecorder.data.repository

import com.example.entryrecorder.data.local.AppConfigDao
import com.example.entryrecorder.data.local.RecordDao
import com.example.entryrecorder.data.local.UserDao
import com.example.entryrecorder.data.remote.CloudSyncManager
import com.example.entryrecorder.model.AppConfig
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntryRepository(
    private val recordDao: RecordDao,
    private val userDao: UserDao,
    private val appConfigDao: AppConfigDao,
    private val cloudSyncManager: CloudSyncManager = CloudSyncManager()
) {
    companion object {
        val DEFAULT_ADMIN = User(
            email = "mrawalyt74@gmail.com",
            password = "Aowal007",
            role = "admin"
        )
        const val KEY_SERVER_URL = "er_server_url"
        const val KEY_LAST_SYNC = "er_last_sync"
    }

    val allRecords: Flow<List<EntryRecord>> = recordDao.getAllRecords()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val serverUrlFlow: Flow<String?> = appConfigDao.getConfigFlow(KEY_SERVER_URL)
    val lastSyncFlow: Flow<String?> = appConfigDao.getConfigFlow(KEY_LAST_SYNC)

    suspend fun ensureDefaultAdmin() {
        val users = userDao.getAllUsersSnapshot()
        if (users.none { it.role == "admin" }) {
            userDao.insertUser(DEFAULT_ADMIN)
        }
    }

    suspend fun authenticate(emailOrUsername: String, pass: String): User? {
        val user = userDao.getUserByEmail(emailOrUsername.trim())
        return if (user != null && user.password == pass) user else null
    }

    suspend fun getNextInvoiceNumber(): String {
        val records = recordDao.getAllRecordsSnapshot()
        if (records.isEmpty()) return "INV-0001"
        var maxNum = 0
        val regex = Regex("INV-(\\d+)")
        for (r in records) {
            val match = regex.find(r.invoice)
            if (match != null) {
                val num = match.groupValues[1].toIntOrNull() ?: 0
                if (num > maxNum) maxNum = num
            }
        }
        val next = maxNum + 1
        return "INV-" + next.toString().padStart(4, '0')
    }

    suspend fun getNextSerial(): Int {
        val max = recordDao.getMaxSerial() ?: 0
        return max + 1
    }

    suspend fun insertRecord(record: EntryRecord) {
        recordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: EntryRecord) {
        recordDao.updateRecord(record)
    }

    suspend fun updateQuickFields(id: Long, ageCode: String, mobile: String, idNumber: String, comment: String) {
        recordDao.updateQuickFields(id, ageCode.trim(), mobile.trim(), idNumber.trim(), comment.trim())
    }

    suspend fun deleteRecord(id: Long) {
        recordDao.deleteRecordById(id)
    }

    suspend fun deleteAllRecords() {
        recordDao.deleteAllRecords()
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun deleteUser(email: String) {
        userDao.deleteUserByEmail(email)
    }

    suspend fun setServerUrl(url: String) {
        appConfigDao.setConfig(AppConfig(KEY_SERVER_URL, url.trim()))
    }

    suspend fun getServerUrl(): String? {
        return appConfigDao.getConfig(KEY_SERVER_URL)
    }

    suspend fun updateLastSyncTime() {
        val format = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault())
        val now = format.format(Date())
        appConfigDao.setConfig(AppConfig(KEY_LAST_SYNC, now))
    }

    suspend fun resetAllData() {
        recordDao.deleteAllRecords()
        userDao.deleteAllUsers()
        appConfigDao.clearConfig()
        userDao.insertUser(DEFAULT_ADMIN)
    }

    suspend fun syncWithCloud(): Result<Unit> {
        val serverUrl = getServerUrl()
        if (serverUrl.isNullOrBlank()) {
            return Result.failure(Exception("No server URL configured."))
        }

        return try {
            val serverUsers = cloudSyncManager.fetchUsers(serverUrl)
            val serverRecords = cloudSyncManager.fetchRecords(serverUrl)

            val localUsers = userDao.getAllUsersSnapshot().toMutableList()
            val localRecords = recordDao.getAllRecordsSnapshot().toMutableList()

            // Merge users
            if (serverUsers != null) {
                val localEmails = localUsers.map { it.email }.toSet()
                for (su in serverUsers) {
                    if (!localEmails.contains(su.email)) {
                        localUsers.add(su)
                    }
                }
                userDao.insertUsers(localUsers)
            }

            // Merge records by id
            if (serverRecords != null) {
                val localIds = localRecords.map { it.id }.toSet()
                for (sr in serverRecords) {
                    if (!localIds.contains(sr.id)) {
                        localRecords.add(sr)
                    }
                }
                recordDao.insertRecords(localRecords)
            }

            // Ensure admin
            if (localUsers.none { it.role == "admin" }) {
                localUsers.add(DEFAULT_ADMIN)
                userDao.insertUser(DEFAULT_ADMIN)
            }

            // Push merged data to server
            val pushSuccess = cloudSyncManager.pushSyncAll(serverUrl, localUsers, localRecords)
            if (pushSuccess) {
                updateLastSyncTime()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server returned error during sync."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadFromCloud(): Result<Unit> {
        val serverUrl = getServerUrl()
        if (serverUrl.isNullOrBlank()) {
            return Result.failure(Exception("No server URL configured."))
        }

        return try {
            val serverUsers = cloudSyncManager.fetchUsers(serverUrl)
            val serverRecords = cloudSyncManager.fetchRecords(serverUrl)

            if (serverUsers != null && serverUsers.isNotEmpty()) {
                userDao.insertUsers(serverUsers)
            }
            if (serverRecords != null) {
                recordDao.insertRecords(serverRecords)
            }
            updateLastSyncTime()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
