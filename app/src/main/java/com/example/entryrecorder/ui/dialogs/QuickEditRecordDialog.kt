package com.example.entryrecorder.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.ui.EntryViewModel
import com.example.entryrecorder.ui.theme.*

@Composable
fun QuickEditRecordDialog(
    record: EntryRecord,
    viewModel: EntryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var idNumberInput by remember { mutableStateOf(record.idNumber) }
    var mobileInput by remember { mutableStateOf(record.mobile) }
    var ageCodeInput by remember { mutableStateOf(record.ageCode) }
    var commentInput by remember { mutableStateOf(record.comment) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 500.dp)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Surface(
                    color = PrimaryBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Edit Record Details",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_quick_edit")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Body
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(20.dp)
                ) {
                    // Record Static Info
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
                            Column {
                                Text(
                                    text = record.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = record.application,
                                    style = MaterialTheme.typography.bodySmall.copy(color = PrimaryBlue)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AdminLightPurple
                            ) {
                                Text(
                                    text = record.invoice,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AdminPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
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

                    // ID Number
                    OutlinedTextField(
                        value = idNumberInput,
                        onValueChange = {
                            idNumberInput = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_field_id"),
                        label = { Text("ID Number *") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone / Mobile Number
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = {
                            mobileInput = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_field_mobile"),
                        label = { Text("Phone / Mobile Number *") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Comment / Small Box Remark
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = {
                            commentInput = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_field_comment"),
                        label = { Text("Comment / Note") },
                        placeholder = { Text("Add comment or note...") },
                        leadingIcon = {
                            Icon(Icons.Default.Comment, contentDescription = null, tint = PrimaryBlue)
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (idNumberInput.isBlank() || mobileInput.isBlank()) {
                                errorMessage = "Please fill in ID Number and Phone."
                                return@Button
                            }
                            isSaving = true
                            viewModel.updateQuickRecord(
                                id = record.id,
                                mobile = mobileInput,
                                idNumber = idNumberInput,
                                comment = commentInput
                            ) { success ->
                                isSaving = false
                                if (success) {
                                    onDismiss()
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_quick_edit"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaving) "Saving..." else "Update Record",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
