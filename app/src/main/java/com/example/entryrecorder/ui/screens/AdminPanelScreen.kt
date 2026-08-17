package com.example.entryrecorder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.entryrecorder.data.repository.EntryRepository
import com.example.entryrecorder.model.User
import com.example.entryrecorder.ui.EntryViewModel
import com.example.entryrecorder.ui.SyncState
import com.example.entryrecorder.ui.dialogs.ConfirmDeleteDialog
import com.example.entryrecorder.ui.theme.*

@Composable
fun AdminPanelScreen(
    viewModel: EntryViewModel,
    modifier: Modifier = Modifier
) {
    val serverUrlState by viewModel.serverUrl.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var serverUrlInput by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserPassword by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(serverUrlState) {
        if (serverUrlInput.isEmpty() && !serverUrlState.isNullOrBlank()) {
            serverUrlInput = serverUrlState ?: ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AdminDarkPurple,
                        AdminPurple,
                        Color(0xFFF3F4F6)
                    ),
                    startY = 0f,
                    endY = 600f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Settings, Cloud Sync & User Management",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateToDashboard() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("btn_back_to_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dashboard", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // Cloud Server Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = PrimaryBlue,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Google Sheets Cloud Sync Configuration",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Paste the Google Apps Script Web App URL to enable synchronization with Google Sheets:",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                            )

                            OutlinedTextField(
                                value = serverUrlInput,
                                onValueChange = { serverUrlInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("server_url_input"),
                                label = { Text("Server URL (Google Apps Script)") },
                                placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Buttons: Save URL, Load from Cloud, Force Sync
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.saveServerUrl(serverUrlInput) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_save_server_url"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save URL", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { viewModel.loadFromCloud(true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_load_cloud"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Load Cloud", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { viewModel.triggerSync(true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_force_sync"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync Now", fontSize = 12.sp)
                                }
                            }

                            // Sync status info
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Status: ${
                                                when (syncState) {
                                                    SyncState.SYNCED -> "Connected & Synced"
                                                    SyncState.SYNCING -> "Syncing data..."
                                                    SyncState.ERROR -> "Connection Error"
                                                    SyncState.OFFLINE -> "Offline / No URL"
                                                }
                                            }",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when (syncState) {
                                                    SyncState.SYNCED -> SuccessGreen
                                                    SyncState.SYNCING -> WarningAmber
                                                    SyncState.ERROR -> DangerRed
                                                    SyncState.OFFLINE -> Slate600
                                                }
                                            )
                                        )
                                        Text(
                                            text = "Last Sync: ${lastSyncTime ?: "Never"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate500,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Online Data Save Setup Guide Card
            item {
                val context = LocalContext.current
                var showScriptDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = SuccessGreen,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save All Data Online (Google Sheets)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "How to enable 100% online cloud saving in 3 simple steps:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Slate800)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "1. Open sheets.google.com and create a new spreadsheet.",
                                        fontSize = 12.sp,
                                        color = Slate700
                                    )
                                    Text(
                                        "2. Click Extensions > Apps Script in the Google Sheets menu.",
                                        fontSize = 12.sp,
                                        color = Slate700
                                    )
                                    Text(
                                        "3. Click the button below, paste code into Apps Script, click Deploy > New Deployment > Web App (Who has access: Anyone), then copy the Web App URL and paste it into the Server URL box above.",
                                        fontSize = 12.sp,
                                        color = Slate700
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.copyAppsScriptCode(context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_copy_apps_script"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessDarkGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Script Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showScriptDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_view_apps_script"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View Script", fontSize = 12.sp)
                                }
                            }

                            if (showScriptDialog) {
                                AlertDialog(
                                    onDismissRequest = { showScriptDialog = false },
                                    title = {
                                        Text(
                                            "Google Apps Script Code",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    },
                                    text = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 350.dp)
                                        ) {
                                            Text(
                                                "Paste this code into Extensions > Apps Script in Google Sheets:",
                                                fontSize = 12.sp,
                                                color = Slate600,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            Surface(
                                                color = Slate900,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f, fill = false)
                                            ) {
                                                LazyColumn(modifier = Modifier.padding(10.dp)) {
                                                    item {
                                                        Text(
                                                            text = EntryViewModel.GOOGLE_APPS_SCRIPT_CODE,
                                                            color = Color(0xFF86EFAC),
                                                            fontSize = 11.sp,
                                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.copyAppsScriptCode(context)
                                                showScriptDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy Code")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showScriptDialog = false }) {
                                            Text("Close")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Add User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = AdminPurple,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add New User",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (errorMessage != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = DangerLightRed
                                ) {
                                    Text(
                                        text = errorMessage ?: "",
                                        color = DangerRed,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = newUserEmail,
                                onValueChange = {
                                    newUserEmail = it
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_new_user_email"),
                                label = { Text("User Email") },
                                placeholder = { Text("user@example.com") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = AdminPurple)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = newUserPassword,
                                onValueChange = {
                                    newUserPassword = it
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_new_user_password"),
                                label = { Text("User Password") },
                                placeholder = { Text("Enter password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = AdminPurple)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.addUser(newUserEmail, newUserPassword) { success, msg ->
                                        if (success) {
                                            newUserEmail = ""
                                            newUserPassword = ""
                                            errorMessage = null
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("btn_submit_add_user"),
                                colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create User Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // User Management Table Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = PrimaryLightBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "User Accounts (${allUsers.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allUsers.forEach { user ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Slate50,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.email,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate900
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (user.role == "admin") AdminLightPurple else PrimaryLightBlue
                                                ) {
                                                    Text(
                                                        text = user.role.uppercase(),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (user.role == "admin") AdminPurple else PrimaryBlue,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        if (user.role != "admin" && user.email != EntryRepository.DEFAULT_ADMIN.email) {
                                            IconButton(
                                                onClick = { userToDelete = user },
                                                modifier = Modifier.testTag("btn_delete_user_${user.email}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete user",
                                                    tint = DangerRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    userToDelete?.let { user ->
        ConfirmDeleteDialog(
            title = "Delete User?",
            message = "Are you sure you want to delete user '${user.email}'?",
            confirmButtonText = "Delete User",
            onConfirm = { viewModel.deleteUser(user) },
            onDismiss = { userToDelete = null }
        )
    }
}
