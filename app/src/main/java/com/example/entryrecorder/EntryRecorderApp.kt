package com.example.entryrecorder

import android.app.Application
import com.example.entryrecorder.data.local.AppDatabase
import com.example.entryrecorder.data.repository.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EntryRecorderApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        EntryRepository(
            recordDao = database.recordDao(),
            userDao = database.userDao(),
            appConfigDao = database.appConfigDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.ensureDefaultAdmin()
        }
    }
}
