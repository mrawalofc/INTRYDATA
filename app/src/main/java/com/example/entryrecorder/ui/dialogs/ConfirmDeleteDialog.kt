package com.example.entryrecorder.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.entryrecorder.ui.theme.DangerRed
import com.example.entryrecorder.ui.theme.WarningAmber

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningAmber
            )
        },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                modifier = androidx.compose.ui.Modifier.testTag("btn_confirm_delete")
            ) {
                Text(text = confirmButtonText, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = androidx.compose.ui.Modifier.testTag("btn_cancel_delete")
            ) {
                Text(text = "Cancel")
            }
        }
    )
}
