package com.example.entryrecorder.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.ui.EntryViewModel
import com.example.entryrecorder.ui.components.DashboardStatsGrid
import com.example.entryrecorder.ui.components.HeaderBar
import com.example.entryrecorder.ui.components.RecordItemCard
import com.example.entryrecorder.ui.dialogs.ConfirmDeleteDialog
import com.example.entryrecorder.ui.dialogs.InvoicePrintDialog
import com.example.entryrecorder.ui.dialogs.NewEntryDialog
import com.example.entryrecorder.ui.dialogs.QuickEditRecordDialog
import com.example.entryrecorder.ui.theme.*
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: EntryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val records by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterColumn by viewModel.filterColumn.collectAsStateWithLifecycle()
    val sortColumn by viewModel.sortColumn.collectAsStateWithLifecycle()
    val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
    val startDateFilter by viewModel.startDateFilter.collectAsStateWithLifecycle()
    val endDateFilter by viewModel.endDateFilter.collectAsStateWithLifecycle()

    var showNewEntryDialog by remember { mutableStateOf(false) }
    var selectedInvoiceRecord by remember { mutableStateOf<EntryRecord?>(null) }
    var recordToEdit by remember { mutableStateOf<EntryRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<Long?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val isAdmin = currentUser?.role == "admin"

    val filterOptions = listOf(
        "all" to "All Columns",
        "name" to "Name",
        "id" to "ID Number",
        "mobile" to "Mobile",
        "application" to "Application",
        "invoice" to "Invoice",
        "comment" to "Comment",
        "creator" to "Entered By"
    )

    val sortOptions = listOf(
        "serial" to "S.No",
        "date" to "Date",
        "time" to "Time",
        "name" to "Name",
        "idNumber" to "ID Number",
        "mobile" to "Mobile",
        "application" to "Application",
        "ageCode" to "Age / Code",
        "amount" to "Amount",
        "invoice" to "Invoice",
        "comment" to "Comment",
        "creator" to "Entered By"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientEnd,
                        Color(0xFFF3F4F6)
                    ),
                    startY = 0f,
                    endY = 700f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            item {
                HeaderBar(
                    user = currentUser,
                    syncState = syncState,
                    onAdminClick = { viewModel.navigateToAdmin() },
                    onLogoutClick = { viewModel.logout() }
                )
            }

            // Stats Cards Grid (Role-based: Total Amount only visible to Admin)
            item {
                DashboardStatsGrid(
                    stats = stats,
                    isAdmin = isAdmin
                )
            }

            // Add New Entry Button (Top)
            item {
                Button(
                    onClick = { showNewEntryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_add_new_entry_top"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Entry",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Records Card Header & Search / Filter Toolbar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Title bar
                        Surface(
                            color = Slate800,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = PrimaryLightBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bin Mishal Travels - Records",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .clickable { viewModel.printRecordsTable(context) }
                                            .testTag("btn_print_records_header")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Print,
                                                contentDescription = "Print Records Table",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Print",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${records.size} record${if (records.size != 1) "s" else ""}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Toolbar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Search box
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_input"),
                                placeholder = { Text("Search by name, ID, phone, invoice, comment...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Slate400
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = Slate400
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Date Range Filter Bar
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("date_range_filter_bar")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Date Range Filter",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate700
                                                )
                                            )
                                        }

                                        if (startDateFilter.isNotEmpty() || endDateFilter.isNotEmpty()) {
                                            TextButton(
                                                onClick = { viewModel.clearDateRange() },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear date range",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = DangerRed
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    "Clear",
                                                    fontSize = 11.sp,
                                                    color = DangerRed,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    // Pickers Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Start date button
                                        OutlinedButton(
                                            onClick = {
                                                showDatePicker(context, startDateFilter) { date ->
                                                    viewModel.setStartDate(date)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_filter_start_date"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (startDateFilter.isNotEmpty()) PrimaryLightBlue else Color.White
                                            )
                                        ) {
                                            Column(horizontalAlignment = Alignment.Start) {
                                                Text("From Date", fontSize = 10.sp, color = Slate400)
                                                Text(
                                                    text = if (startDateFilter.isNotEmpty()) startDateFilter else "Pick Start",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (startDateFilter.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (startDateFilter.isNotEmpty()) PrimaryDarkBlue else Slate600
                                                )
                                            }
                                        }

                                        // End date button
                                        OutlinedButton(
                                            onClick = {
                                                showDatePicker(context, endDateFilter) { date ->
                                                    viewModel.setEndDate(date)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_filter_end_date"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (endDateFilter.isNotEmpty()) PrimaryLightBlue else Color.White
                                            )
                                        ) {
                                            Column(horizontalAlignment = Alignment.Start) {
                                                Text("To Date", fontSize = 10.sp, color = Slate400)
                                                Text(
                                                    text = if (endDateFilter.isNotEmpty()) endDateFilter else "Pick End",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (endDateFilter.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (endDateFilter.isNotEmpty()) PrimaryDarkBlue else Slate600
                                                )
                                            }
                                        }
                                    }

                                    // Preset chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val isToday = remember(startDateFilter, endDateFilter) {
                                            val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                                            startDateFilter == today && endDateFilter == today
                                        }
                                        val isAll = startDateFilter.isEmpty() && endDateFilter.isEmpty()

                                        FilterChip(
                                            selected = isToday,
                                            onClick = { viewModel.setQuickDateRange("today") },
                                            label = { Text("Today", fontSize = 11.sp) },
                                            modifier = Modifier.height(30.dp)
                                        )
                                        FilterChip(
                                            selected = !isToday && !isAll && startDateFilter.endsWith("-01"),
                                            onClick = { viewModel.setQuickDateRange("month") },
                                            label = { Text("This Month", fontSize = 11.sp) },
                                            modifier = Modifier.height(30.dp)
                                        )
                                        FilterChip(
                                            selected = isAll,
                                            onClick = { viewModel.setQuickDateRange("all") },
                                            label = { Text("All Time", fontSize = 11.sp) },
                                            modifier = Modifier.height(30.dp)
                                        )
                                    }
                                }
                            }

                            // Action buttons row: Filter column, Sort, Export CSV, Clear All
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Filter column dropdown button
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(
                                        onClick = { showFilterMenu = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = PrimaryBlue
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = filterOptions.find { it.first == filterColumn }?.second ?: "Filter",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            color = Slate700
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showFilterMenu,
                                        onDismissRequest = { showFilterMenu = false }
                                    ) {
                                        filterOptions.forEach { (col, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    viewModel.setFilterColumn(col)
                                                    showFilterMenu = false
                                                },
                                                leadingIcon = {
                                                    if (filterColumn == col) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                // Sort dropdown button
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(
                                        onClick = { showSortMenu = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = PrimaryBlue
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = sortOptions.find { it.first == sortColumn }?.second ?: "Sort",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            color = Slate700
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        sortOptions.forEach { (col, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    viewModel.toggleSort(col)
                                                    showSortMenu = false
                                                },
                                                leadingIcon = {
                                                    if (sortColumn == col) {
                                                        Icon(
                                                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                            contentDescription = null,
                                                            tint = PrimaryBlue
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                // Print Table
                                OutlinedButton(
                                    onClick = { viewModel.printRecordsTable(context) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_print_table_toolbar")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Slate600
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Print", fontSize = 12.sp, color = Slate700)
                                }

                                // Export CSV
                                OutlinedButton(
                                    onClick = { viewModel.exportCsv(context) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_export_csv")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Slate600
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CSV", fontSize = 12.sp, color = Slate700)
                                }

                                // Clear All (Admin only)
                                if (isAdmin) {
                                    OutlinedButton(
                                        onClick = { showClearAllDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = DangerLightRed,
                                            contentColor = DangerRed
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                                        modifier = Modifier.testTag("btn_clear_all")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = DangerRed
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear", fontSize = 12.sp, color = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Records List Items or Empty State
            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No records found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Slate600,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No entries match your search criteria." else "Click 'Add New Entry' to record your first entry.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                            )
                        }
                    }
                }
            } else {
                items(records, key = { it.id }) { record ->
                    RecordItemCard(
                        record = record,
                        isAdmin = isAdmin,
                        onInvoiceClick = { selectedInvoiceRecord = it },
                        onEditClick = { recordToEdit = it },
                        onDeleteClick = { recordToDelete = record.id }
                    )
                }
            }

            // Bottom Add New Entry Button
            item {
                Button(
                    onClick = { showNewEntryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_add_new_entry_bottom"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Entry",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }

    // New Entry Modal
    if (showNewEntryDialog) {
        NewEntryDialog(
            viewModel = viewModel,
            onDismiss = { showNewEntryDialog = false }
        )
    }

    // Invoice Print Preview Dialog
    selectedInvoiceRecord?.let { record ->
        InvoicePrintDialog(
            record = record,
            onDismiss = { selectedInvoiceRecord = null }
        )
    }

    // Quick Edit Record Dialog (Age Code, Phone Number, ID Number, Comment)
    recordToEdit?.let { record ->
        QuickEditRecordDialog(
            record = record,
            viewModel = viewModel,
            onDismiss = { recordToEdit = null }
        )
    }

    // Delete single record confirmation (Admin only)
    recordToDelete?.let { id ->
        ConfirmDeleteDialog(
            title = "Confirm Deletion",
            message = "Are you sure you want to delete this record? This action cannot be undone.",
            confirmButtonText = "Delete Record",
            onConfirm = { viewModel.deleteRecord(id) },
            onDismiss = { recordToDelete = null }
        )
    }

    // Clear all records confirmation (Admin only)
    if (showClearAllDialog) {
        ConfirmDeleteDialog(
            title = "Clear All Records?",
            message = "Are you sure you want to delete all stored records? This action cannot be undone.",
            confirmButtonText = "Delete All",
            onConfirm = { viewModel.deleteAllRecords() },
            onDismiss = { showClearAllDialog = false }
        )
    }
}

private fun showDatePicker(
    context: Context,
    initialDate: String,
    onDateSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    if (initialDate.isNotEmpty()) {
        try {
            val parts = initialDate.split("-")
            if (parts.size == 3) {
                cal.set(Calendar.YEAR, parts[0].toInt())
                cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            }
        } catch (_: Exception) {
        }
    }
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formatted)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
