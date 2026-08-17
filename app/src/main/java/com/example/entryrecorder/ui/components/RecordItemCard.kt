package com.example.entryrecorder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.ui.theme.*
import java.util.Locale

@Composable
fun RecordItemCard(
    record: EntryRecord,
    isAdmin: Boolean,
    onInvoiceClick: (EntryRecord) -> Unit,
    onEditClick: (EntryRecord) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("record_card_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Serial + Invoice Badge (Clickable for Print Preview) + Right Comment Box + Amount / Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Serial + Clickable Invoice badge
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryLightBlue
                        ) {
                            Text(
                                text = "#${record.serial}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Invoice Badge - Clickable to open print preview
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AdminLightPurple,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onInvoiceClick(record) }
                                .testTag("badge_invoice_${record.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Print Invoice",
                                    tint = AdminPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = record.invoice.ifEmpty { "INV-0000" },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = AdminPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Print",
                                    tint = AdminPurple.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Right: Amount (Admin only or masked) + Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAdmin) {
                        Text(
                            text = String.format(Locale.US, "$%.2f", record.amount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Edit button for editing age code, mobile, id number & comment
                    IconButton(
                        onClick = { onEditClick(record) },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 2.dp)
                            .testTag("btn_edit_record_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Record",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button (Admin only)
                    if (isAdmin) {
                        IconButton(
                            onClick = { onDeleteClick(record.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .padding(start = 2.dp)
                                .testTag("btn_delete_record_${record.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Record",
                                tint = DangerRed.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Name and Application + Right Side Small Comment Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = record.application,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryDarkBlue,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Right Side Small Box for Comment / Remarks
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (record.comment.isNotBlank()) Color(0xFFFEF3C7) else Slate100,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (record.comment.isNotBlank()) Color(0xFFF59E0B) else Slate200
                    ),
                    modifier = Modifier
                        .widthIn(max = 140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditClick(record) }
                        .padding(start = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "Comment",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record.comment.isNotBlank()) Color(0xFFB45309) else Slate500
                            )
                        )
                        Text(
                            text = record.comment.ifBlank { "Tap to add" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (record.comment.isNotBlank()) Color(0xFF92400E) else Slate400,
                                fontStyle = if (record.comment.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meta Info Grid: ID Number, Mobile, Age/Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ID: ${record.idNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Tel: ${record.mobile}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = "Age/Code: ${record.ageCode}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Slate100)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Date & Time + Entered By
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.date} ${record.time}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate400,
                        fontSize = 11.sp
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.creator,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
