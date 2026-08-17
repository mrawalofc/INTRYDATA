package com.example.entryrecorder.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.entryrecorder.EntryRecorderApp
import com.example.entryrecorder.data.repository.EntryRepository
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SyncState {
    OFFLINE,
    SYNCING,
    SYNCED,
    ERROR
}

data class DashboardStats(
    val totalCount: Int = 0,
    val totalAmount: Double = 0.0,
    val todayCount: Int = 0,
    val todayAmount: Double = 0.0,
    val monthCount: Int = 0,
    val lastEntryName: String = "-"
)

data class CustomerProfile(
    val name: String,
    val idNumber: String,
    val mobile: String
)

class EntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EntryRepository = (application as EntryRecorderApp).repository

    // Current User Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Navigation state: "login", "dashboard", "admin"
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Search and Filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterColumn = MutableStateFlow("all")
    val filterColumn: StateFlow<String> = _filterColumn.asStateFlow()

    private val _sortColumn = MutableStateFlow("serial")
    val sortColumn: StateFlow<String> = _sortColumn.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _startDateFilter = MutableStateFlow("")
    val startDateFilter: StateFlow<String> = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow("")
    val endDateFilter: StateFlow<String> = _endDateFilter.asStateFlow()

    // Toast/Snackbar notifications
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Cloud Sync State
    private val _syncState = MutableStateFlow(SyncState.OFFLINE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val serverUrl: StateFlow<String?> = repository.serverUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val lastSyncTime: StateFlow<String?> = repository.lastSyncFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawRecords: StateFlow<List<EntryRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Known customer profiles derived from past records for instant auto-select
    val knownProfiles: StateFlow<List<CustomerProfile>> = rawRecords.combine(_currentUser) { records, _ ->
        val profilesMap = mutableMapOf<String, CustomerProfile>()
        for (r in records.reversed()) { // Most recent first
            val key = r.name.trim().lowercase(Locale.getDefault())
            if (key.isNotEmpty() && !profilesMap.containsKey(key)) {
                profilesMap[key] = CustomerProfile(
                    name = r.name.trim(),
                    idNumber = r.idNumber.trim(),
                    mobile = r.mobile.trim()
                )
            }
            val idKey = r.idNumber.trim().lowercase(Locale.getDefault())
            if (idKey.isNotEmpty() && !profilesMap.containsKey(idKey)) {
                profilesMap[idKey] = CustomerProfile(
                    name = r.name.trim(),
                    idNumber = r.idNumber.trim(),
                    mobile = r.mobile.trim()
                )
            }
        }
        profilesMap.values.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class FilterParams(
        val query: String,
        val column: String,
        val sortCol: String,
        val isAsc: Boolean,
        val startDate: String,
        val endDate: String
    )

    // Filtered and Sorted Records
    val filteredRecords: StateFlow<List<EntryRecord>> = combine(
        rawRecords,
        combine(
            _searchQuery,
            _filterColumn,
            _sortColumn,
            _sortAscending,
            combine(_startDateFilter, _endDateFilter) { s, e -> Pair(s, e) }
        ) { q, col, sort, asc, datePair ->
            FilterParams(q, col, sort, asc, datePair.first, datePair.second)
        }
    ) { records, params ->
        val trimmed = params.query.trim().lowercase(Locale.getDefault())
        val filtered = records.filter { r ->
            // Date range filter
            if (params.startDate.isNotEmpty() && r.date < params.startDate) return@filter false
            if (params.endDate.isNotEmpty() && r.date > params.endDate) return@filter false

            if (trimmed.isEmpty()) return@filter true

            when (params.column) {
                "name" -> r.name.lowercase(Locale.getDefault()).contains(trimmed)
                "id" -> r.idNumber.lowercase(Locale.getDefault()).contains(trimmed)
                "mobile" -> r.mobile.lowercase(Locale.getDefault()).contains(trimmed)
                "application" -> r.application.lowercase(Locale.getDefault()).contains(trimmed)
                "invoice" -> r.invoice.lowercase(Locale.getDefault()).contains(trimmed)
                "comment" -> r.comment.lowercase(Locale.getDefault()).contains(trimmed)
                "creator" -> r.creator.lowercase(Locale.getDefault()).contains(trimmed)
                else -> {
                    r.name.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.idNumber.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.mobile.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.application.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.invoice.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.requestNo.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.comment.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.creator.lowercase(Locale.getDefault()).contains(trimmed) ||
                            r.ageCode.lowercase(Locale.getDefault()).contains(trimmed)
                }
            }
        }

        val sorted = filtered.sortedWith { a, b ->
            val comp = when (params.sortCol) {
                "date" -> a.date.compareTo(b.date)
                "time" -> a.time.compareTo(b.time)
                "name" -> a.name.compareTo(b.name, ignoreCase = true)
                "idNumber" -> a.idNumber.compareTo(b.idNumber, ignoreCase = true)
                "mobile" -> a.mobile.compareTo(b.mobile, ignoreCase = true)
                "application" -> a.application.compareTo(b.application, ignoreCase = true)
                "ageCode" -> a.ageCode.compareTo(b.ageCode, ignoreCase = true)
                "amount" -> a.amount.compareTo(b.amount)
                "invoice" -> a.invoice.compareTo(b.invoice, ignoreCase = true)
                "comment" -> a.comment.compareTo(b.comment, ignoreCase = true)
                "creator" -> a.creator.compareTo(b.creator, ignoreCase = true)
                else -> a.serial.compareTo(b.serial)
            }
            if (params.isAsc) comp else -comp
        }
        sorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats> = rawRecords.combine(_currentUser) { records, _ ->
        val total = records.size
        val totalAmt = records.sumOf { it.amount }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayRecords = records.filter { it.date == todayStr }
        val todayCount = todayRecords.size
        val todayAmt = todayRecords.sumOf { it.amount }
        val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthCount = records.count { it.date.startsWith(currentMonthPrefix) }
        val lastEntry = if (records.isNotEmpty()) records.last().name else "-"
        DashboardStats(
            totalCount = total,
            totalAmount = totalAmt,
            todayCount = todayCount,
            todayAmount = todayAmt,
            monthCount = monthCount,
            lastEntryName = lastEntry
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        viewModelScope.launch {
            repository.ensureDefaultAdmin()
            val url = repository.getServerUrl()
            if (!url.isNullOrBlank()) {
                _syncState.value = SyncState.SYNCED
                triggerSync(false)
            }
            // Auto-sync in background every 30 seconds
            while (true) {
                kotlinx.coroutines.delay(30_000)
                val currentUrl = repository.getServerUrl()
                if (!currentUrl.isNullOrBlank() && _currentUser.value != null) {
                    triggerSync(false)
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun login(emailOrUsername: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.authenticate(emailOrUsername, pass)
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = "dashboard"
                emitToast("Welcome back! Login successful.")
                onResult(true, "Success")
                // Check if server sync can be initiated
                val url = repository.getServerUrl()
                if (!url.isNullOrBlank()) {
                    triggerSync(false)
                }
            } else {
                onResult(false, "Invalid username or password. Please try again.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = "login"
        _searchQuery.value = ""
        _syncState.value = SyncState.OFFLINE
        emitToast("You have been logged out.")
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _currentUser.value = null
            _currentScreen.value = "login"
            _searchQuery.value = ""
            _syncState.value = SyncState.OFFLINE
            emitToast("All data has been reset to default.")
        }
    }

    fun navigateToAdmin() {
        if (_currentUser.value?.role == "admin") {
            _currentScreen.value = "admin"
        } else {
            emitToast("Access denied. Only admin can access admin panel.")
        }
    }

    fun navigateToDashboard() {
        _currentScreen.value = "dashboard"
    }

    // --- Filtering and Sorting Actions ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterColumn(column: String) {
        _filterColumn.value = column
    }

    fun toggleSort(col: String) {
        if (_sortColumn.value == col) {
            _sortAscending.value = !_sortAscending.value
        } else {
            _sortColumn.value = col
            _sortAscending.value = true
        }
    }

    fun setDateRange(startDate: String?, endDate: String?) {
        _startDateFilter.value = startDate ?: ""
        _endDateFilter.value = endDate ?: ""
    }

    fun setStartDate(startDate: String?) {
        _startDateFilter.value = startDate ?: ""
    }

    fun setEndDate(endDate: String?) {
        _endDateFilter.value = endDate ?: ""
    }

    fun clearDateRange() {
        _startDateFilter.value = ""
        _endDateFilter.value = ""
    }

    fun setQuickDateRange(type: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        when (type) {
            "today" -> {
                _startDateFilter.value = todayStr
                _endDateFilter.value = todayStr
            }
            "month" -> {
                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                _startDateFilter.value = String.format(Locale.US, "%04d-%02d-01", year, month)
                _endDateFilter.value = String.format(Locale.US, "%04d-%02d-%02d", year, month, maxDay)
            }
            "all" -> {
                clearDateRange()
            }
        }
    }

    // --- Entry Creation, Editing & Deletion ---
    suspend fun getNextInvoice(): String {
        return repository.getNextInvoiceNumber()
    }

    fun findCustomerByInput(query: String): CustomerProfile? {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) return null
        return knownProfiles.value.firstOrNull {
            it.name.lowercase(Locale.getDefault()) == q || it.idNumber.lowercase(Locale.getDefault()) == q
        }
    }

    fun saveEntry(
        date: String,
        name: String,
        idNumber: String,
        mobile: String,
        applicationName: String,
        amount: Double,
        comment: String = "",
        onComplete: (Boolean, String) -> Unit = { _, _ -> },
        onRecordCreated: ((EntryRecord) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val nextInvoice = repository.getNextInvoiceNumber()
                val nextSerial = repository.getNextSerial()
                val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                val timeStr = timeFormat.format(Date())
                val creator = _currentUser.value?.email ?: "Unknown"
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

                val newRecord = EntryRecord(
                    id = System.currentTimeMillis(),
                    serial = nextSerial,
                    date = date,
                    time = timeStr,
                    name = name.trim(),
                    idNumber = idNumber.trim(),
                    mobile = mobile.trim(),
                    application = applicationName.trim(),
                    amount = amount,
                    invoice = nextInvoice,
                    creator = creator,
                    timestamp = isoFormat.format(Date()),
                    comment = comment.trim()
                )

                repository.insertRecord(newRecord)
                emitToast("Entry Saved! Invoice: $nextInvoice")
                onComplete(true, nextInvoice)
                onRecordCreated?.invoke(newRecord)

                // Background sync
                triggerSync(false)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to save entry")
            }
        }
    }

    fun updateQuickRecord(
        id: Long,
        mobile: String,
        idNumber: String,
        comment: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateQuickFields(
                    id = id,
                    mobile = mobile,
                    idNumber = idNumber,
                    comment = comment
                )
                emitToast("Record updated successfully!")
                onComplete(true)
                triggerSync(false)
            } catch (e: Exception) {
                emitToast("Failed to update: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun deleteRecord(id: Long) {
        if (_currentUser.value?.role != "admin") {
            emitToast("Access denied. Only admin can delete records.")
            return
        }
        viewModelScope.launch {
            repository.deleteRecord(id)
            emitToast("Record deleted")
            triggerSync(false)
        }
    }

    fun deleteAllRecords() {
        if (_currentUser.value?.role != "admin") {
            emitToast("Access denied. Only admin can clear all records.")
            return
        }
        viewModelScope.launch {
            repository.deleteAllRecords()
            emitToast("All records cleared")
            triggerSync(false)
        }
    }

    // --- User Management (Admin) ---
    fun addUser(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        if (_currentUser.value?.role != "admin") {
            onResult(false, "Access denied. Only admin can add users.")
            return
        }
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            onResult(false, "Please enter both email and password")
            return
        }
        if (!cleanEmail.contains("@")) {
            onResult(false, "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            val existing = allUsers.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
            if (existing != null) {
                onResult(false, "A user with this email already exists")
                return@launch
            }

            repository.insertUser(User(email = cleanEmail, password = cleanPass, role = "user"))
            emitToast("New user added successfully!")
            onResult(true, "User added")
            triggerSync(false)
        }
    }

    fun deleteUser(user: User) {
        if (_currentUser.value?.role != "admin") {
            emitToast("Access denied. Only admin can remove users.")
            return
        }
        if (user.role == "admin" || user.email == EntryRepository.DEFAULT_ADMIN.email) {
            emitToast("Cannot delete admin account.")
            return
        }
        viewModelScope.launch {
            repository.deleteUser(user.email)
            emitToast("User deleted")
            triggerSync(false)
        }
    }

    // --- Cloud Server Config & Sync ---
    fun saveServerUrl(url: String) {
        val cleanUrl = url.trim()
        if (cleanUrl.isEmpty() || !cleanUrl.contains("script.google.com")) {
            emitToast("Please enter a valid Google Apps Script Web App URL")
            return
        }
        viewModelScope.launch {
            repository.setServerUrl(cleanUrl)
            emitToast("Server URL saved! Testing connection...")
            loadFromCloud(true)
        }
    }

    fun triggerSync(force: Boolean) {
        viewModelScope.launch {
            val url = repository.getServerUrl()
            if (url.isNullOrBlank()) {
                if (force) emitToast("No server URL configured. Go to Admin Panel to set it up.")
                _syncState.value = SyncState.OFFLINE
                return@launch
            }

            _syncState.value = SyncState.SYNCING
            val result = repository.syncWithCloud()
            if (result.isSuccess) {
                _syncState.value = SyncState.SYNCED
                if (force) emitToast("Data synchronized with cloud successfully!")
            } else {
                _syncState.value = SyncState.ERROR
                if (force) emitToast("Sync failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun loadFromCloud(force: Boolean) {
        viewModelScope.launch {
            val url = repository.getServerUrl()
            if (url.isNullOrBlank()) {
                if (force) emitToast("No server URL configured. Go to Admin Panel to set it up.")
                _syncState.value = SyncState.OFFLINE
                return@launch
            }

            _syncState.value = SyncState.SYNCING
            val result = repository.loadFromCloud()
            if (result.isSuccess) {
                _syncState.value = SyncState.SYNCED
                if (force) emitToast("Data loaded from cloud successfully!")
            } else {
                _syncState.value = SyncState.ERROR
                if (force) emitToast("Failed to load from server. Using local data.")
            }
        }
    }

    // --- CSV Export ---
    fun exportCsv(context: Context, recordsToExport: List<EntryRecord>? = null) {
        val records = recordsToExport ?: (if (filteredRecords.value.isNotEmpty()) filteredRecords.value else rawRecords.value)
        if (records.isEmpty()) {
            emitToast("No records to export")
            return
        }

        viewModelScope.launch {
            try {
                val headers = "Serial,Date,Time,Name,ID,Mobile,Application,Age/Code,Amount,Invoice,Comment,Entered By\n"
                val csvContent = StringBuilder(headers)
                for (r in records) {
                    val row = listOf(
                        r.serial.toString(),
                        r.date,
                        r.time,
                        "\"${r.name.replace("\"", "\"\"")}\"",
                        "\"${r.idNumber.replace("\"", "\"\"")}\"",
                        "\"${r.mobile.replace("\"", "\"\"")}\"",
                        "\"${r.application.replace("\"", "\"\"")}\"",
                        "\"${r.ageCode.replace("\"", "\"\"")}\"",
                        String.format(Locale.US, "%.2f", r.amount),
                        "\"${r.invoice.replace("\"", "\"\"")}\"",
                        "\"${r.comment.replace("\"", "\"\"")}\"",
                        "\"${r.creator.replace("\"", "\"\"")}\""
                    ).joinToString(",")
                    csvContent.append(row).append("\n")
                }

                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "records_$dateStr.csv"
                val file = File(context.cacheDir, fileName)
                file.writeText(csvContent.toString())

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Bin Mishal Travels Export - $dateStr")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Export CSV Records")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                emitToast("CSV export ready to share!")
            } catch (e: Exception) {
                emitToast("Failed to export CSV: ${e.message}")
            }
        }
    }

    fun printRecordsTable(context: Context, recordsToPrint: List<EntryRecord>? = null) {
        val records = recordsToPrint ?: filteredRecords.value
        if (records.isEmpty()) {
            emitToast("No records available to print")
            return
        }

        Handler(Looper.getMainLooper()).post {
            try {
                val totalAmount = records.sumOf { it.amount }
                val dateFormatted = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
                val currentFilter = _searchQuery.value.trim()
                val filterNote = if (currentFilter.isNotEmpty()) "<p style='color:#64748b;font-size:12px;margin:2px 0 10px 0;'>Filter Applied: \"$currentFilter\" (${records.size} records matched)</p>" else ""

                val rowsHtml = StringBuilder()
                records.forEachIndexed { index, r ->
                    val bg = if (index % 2 == 1) "background-color:#f8fafc;" else ""
                    rowsHtml.append("""
                        <tr style="$bg">
                            <td style="text-align:center;font-weight:bold;color:#2563eb;">${r.serial}</td>
                            <td style="white-space:nowrap;">${r.date}</td>
                            <td style="white-space:nowrap;color:#64748b;font-size:11px;">${r.time}</td>
                            <td style="font-weight:600;">${r.name}</td>
                            <td>${r.idNumber}</td>
                            <td>${r.mobile}</td>
                            <td>${r.application}</td>
                            <td>${r.ageCode}</td>
                            <td style="text-align:right;font-weight:bold;color:#059669;">$${String.format(Locale.US, "%.2f", r.amount)}</td>
                            <td style="font-family:monospace;font-weight:bold;color:#7c3aed;">${r.invoice}</td>
                            <td>${r.comment}</td>
                            <td style="font-size:11px;color:#64748b;">${r.creator}</td>
                        </tr>
                    """.trimIndent())
                }

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <title>Bin Mishal Travels - Records Report</title>
                        <style>
                            @page {
                                size: landscape;
                                margin: 10mm;
                            }
                            body {
                                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                                color: #1e293b;
                                margin: 0;
                                padding: 12px;
                                background: #ffffff;
                            }
                            .report-header {
                                display: flex;
                                justify-content: space-between;
                                align-items: flex-end;
                                border-bottom: 2px solid #2563eb;
                                padding-bottom: 10px;
                                margin-bottom: 12px;
                            }
                            .company-title {
                                font-size: 22px;
                                font-weight: 800;
                                color: #1e40af;
                                margin: 0 0 2px 0;
                                text-transform: uppercase;
                            }
                            .report-title {
                                font-size: 14px;
                                font-weight: 600;
                                color: #475569;
                                margin: 0;
                            }
                            .meta-box {
                                text-align: right;
                                font-size: 11px;
                                color: #64748b;
                            }
                            .summary-bar {
                                display: flex;
                                gap: 16px;
                                background: #f1f5f9;
                                border-radius: 6px;
                                padding: 8px 14px;
                                margin-bottom: 12px;
                                font-size: 12px;
                                font-weight: 600;
                            }
                            .summary-item {
                                color: #334155;
                            }
                            .summary-item strong {
                                color: #0f172a;
                            }
                            table {
                                width: 100%;
                                border-collapse: collapse;
                                font-size: 11px;
                            }
                            th {
                                background-color: #1e293b;
                                color: #ffffff;
                                padding: 8px 6px;
                                text-align: left;
                                font-weight: 600;
                                -webkit-print-color-adjust: exact;
                                print-color-adjust: exact;
                            }
                            td {
                                padding: 6px 6px;
                                border-bottom: 1px solid #e2e8f0;
                                font-size: 10.5px;
                            }
                            tr {
                                page-break-inside: avoid;
                            }
                            .footer {
                                margin-top: 14px;
                                font-size: 10px;
                                color: #94a3b8;
                                text-align: center;
                                border-top: 1px solid #e2e8f0;
                                padding-top: 6px;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="report-header">
                            <div>
                                <h1 class="company-title">Bin Mishal Travels</h1>
                                <p class="report-title">Records & Transactions Ledger Report</p>
                            </div>
                            <div class="meta-box">
                                <div>Generated: $dateFormatted</div>
                                <div>Total Count: ${records.size} records</div>
                            </div>
                        </div>
                        $filterNote
                        <div class="summary-bar">
                            <div class="summary-item">Total Listed Records: <strong>${records.size}</strong></div>
                            <div class="summary-item">Total Listed Amount: <strong>$${String.format(Locale.US, "%.2f", totalAmount)}</strong></div>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th style="width:35px;text-align:center;">#</th>
                                    <th>Date</th>
                                    <th>Time</th>
                                    <th>Name</th>
                                    <th>ID Number</th>
                                    <th>Mobile</th>
                                    <th>Application</th>
                                    <th>Age/Code</th>
                                    <th style="text-align:right;">Amount</th>
                                    <th>Invoice</th>
                                    <th>Note / Comment</th>
                                    <th>Entered By</th>
                                </tr>
                            </thead>
                            <tbody>
                                $rowsHtml
                            </tbody>
                        </table>
                        <div class="footer">
                            Bin Mishal Travels - Confidential Business Records - Page Printed Automatically
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                val webView = WebView(context)
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        val printAdapter = webView.createPrintDocumentAdapter("Records_Report_${System.currentTimeMillis()}")
                        printManager?.print(
                            "Bin_Mishal_Travels_Records_Report",
                            printAdapter,
                            PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                                .build()
                        )
                    }
                }
                webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
                emitToast("Opening print preview...")
            } catch (e: Exception) {
                emitToast("Failed to prepare print: ${e.message}")
            }
        }
    }

    fun copyAppsScriptCode(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Google Apps Script", GOOGLE_APPS_SCRIPT_CODE)
        clipboard?.setPrimaryClip(clip)
        emitToast("Google Apps Script code copied to clipboard!")
    }

    companion object {
        val GOOGLE_APPS_SCRIPT_CODE = """
function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : '';
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  
  if (action === 'getUsers') {
    var sheet = ss.getSheetByName('Users') || createUsersSheet(ss);
    var data = sheet.getDataRange().getValues();
    var users = [];
    for (var i = 1; i < data.length; i++) {
      if (data[i][0]) {
        users.push({
          email: String(data[i][0]),
          password: String(data[i][1]),
          role: String(data[i][2] || 'user')
        });
      }
    }
    return ContentService.createTextOutput(JSON.stringify(users))
      .setMimeType(ContentService.MimeType.JSON);
  }
  
  // Default: getRecords
  var recSheet = ss.getSheetByName('Records') || createRecordsSheet(ss);
  var recData = recSheet.getDataRange().getValues();
  var records = [];
  for (var j = 1; j < recData.length; j++) {
    if (recData[j][0] !== '') {
      records.push({
        id: Number(recData[j][0]) || Date.now(),
        serial: Number(recData[j][1]) || j,
        date: String(recData[j][2]),
        time: String(recData[j][3] || ''),
        name: String(recData[j][4] || ''),
        idNumber: String(recData[j][5] || ''),
        mobile: String(recData[j][6] || ''),
        application: String(recData[j][7] || ''),
        ageCode: String(recData[j][8] || ''),
        amount: Number(recData[j][9]) || 0,
        invoice: String(recData[j][10] || ''),
        requestNo: String(recData[j][11] || ''),
        creator: String(recData[j][12] || ''),
        timestamp: String(recData[j][13] || ''),
        comment: String(recData[j][14] || '')
      });
    }
  }
  return ContentService.createTextOutput(JSON.stringify(records))
    .setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  try {
    var contents = JSON.parse(e.postData.contents);
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    
    if (contents.users && Array.isArray(contents.users)) {
      var userSheet = ss.getSheetByName('Users') || createUsersSheet(ss);
      userSheet.clearContents();
      userSheet.appendRow(['Email', 'Password', 'Role']);
      contents.users.forEach(function(u) {
        userSheet.appendRow([u.email, u.password, u.role]);
      });
    }
    
    if (contents.records && Array.isArray(contents.records)) {
      var recSheet = ss.getSheetByName('Records') || createRecordsSheet(ss);
      recSheet.clearContents();
      recSheet.appendRow(['ID', 'Serial', 'Date', 'Time', 'Name', 'ID Number', 'Mobile', 'Application', 'Age/Code', 'Amount', 'Invoice', 'Request No', 'Entered By', 'Timestamp', 'Comment']);
      contents.records.forEach(function(r) {
        recSheet.appendRow([
          r.id, r.serial, r.date, r.time || '', r.name || '', r.idNumber || '',
          r.mobile || '', r.application || '', r.ageCode || '', r.amount || 0,
          r.invoice || '', r.requestNo || '', r.creator || '', r.timestamp || '', r.comment || ''
        ]);
      });
    }
    
    return ContentService.createTextOutput(JSON.stringify({ success: true }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ success: false, error: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function createUsersSheet(ss) {
  var s = ss.insertSheet('Users');
  s.appendRow(['Email', 'Password', 'Role']);
  return s;
}

function createRecordsSheet(ss) {
  var s = ss.insertSheet('Records');
  s.appendRow(['ID', 'Serial', 'Date', 'Time', 'Name', 'ID Number', 'Mobile', 'Application', 'Age/Code', 'Amount', 'Invoice', 'Request No', 'Entered By', 'Timestamp', 'Comment']);
  return s;
}
        """.trimIndent()
    }

    private fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }
}
