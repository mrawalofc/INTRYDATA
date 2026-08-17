package com.example.entryrecorder.ui.dialogs

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.entryrecorder.model.EntryRecord
import com.example.entryrecorder.ui.theme.*
import java.util.Locale

@Composable
fun InvoicePrintDialog(
    record: EntryRecord,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
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
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Invoice Receipt Preview",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_invoice_preview")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Printable Receipt Canvas
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFAFAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Company Brand Header
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PrimaryLightBlue,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Bin Mishal Travels",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryDarkBlue,
                                    letterSpacing = 0.5.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Travel, Visa & Document Processing Services",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate600,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Invoice Tag Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryLightBlue,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "INVOICE: ${record.invoice.ifEmpty { "INV-0000" }}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Slate200, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Invoice Metadata Rows
                            InvoiceDetailRow(label = "Date & Time", value = "${record.date}  ${record.time}")
                            InvoiceDetailRow(label = "Customer Name", value = record.name, isBold = true)
                            InvoiceDetailRow(label = "ID / Iqama No", value = record.idNumber)
                            InvoiceDetailRow(label = "Mobile Number", value = record.mobile)
                            InvoiceDetailRow(label = "Age / Code", value = record.ageCode)
                            InvoiceDetailRow(label = "Service / Application", value = record.application, isBold = true)

                            if (record.comment.isNotBlank()) {
                                InvoiceDetailRow(label = "Comment / Note", value = record.comment)
                            }
                            if (record.requestNo.isNotBlank()) {
                                InvoiceDetailRow(label = "Request Number", value = record.requestNo)
                            }

                            InvoiceDetailRow(label = "Processed By", value = record.creator)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Total Amount Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Amount Paid",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                    )
                                    Text(
                                        text = String.format(Locale.US, "$%.2f", record.amount),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF15803D)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Thank you for choosing Bin Mishal Travels!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate400,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Surface(
                    color = Slate50,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                shareInvoiceText(context, record)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_share_invoice"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Slate700)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", color = Slate700, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                printNativeInvoice(context, record)
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("btn_print_invoice_action"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Print Invoice",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate600,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Slate900,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.weight(0.58f)
        )
    }
}

private fun shareInvoiceText(context: Context, record: EntryRecord) {
    val text = """
        ==============================
              BIN MISHAL TRAVELS
        Travel, Visa & Document Services
        ==============================
        Invoice: ${record.invoice}
        Date: ${record.date} ${record.time}
        ------------------------------
        Customer: ${record.name}
        ID Number: ${record.idNumber}
        Mobile: ${record.mobile}
        Age/Code: ${record.ageCode}
        Service: ${record.application}
        ${if (record.comment.isNotBlank()) "Comment: ${record.comment}\n" else ""}Total Amount: $${String.format(Locale.US, "%.2f", record.amount)}
        Processed By: ${record.creator}
        ==============================
        Thank you for choosing Bin Mishal Travels!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Invoice ${record.invoice} - Bin Mishal Travels")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Share Invoice")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

fun printNativeInvoice(context: Context, record: EntryRecord) {
    val webView = WebView(context)
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>Invoice ${record.invoice} - Bin Mishal Travels</title>
          <style>
            @page { margin: 15mm; size: auto; }
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #1e293b; margin: 0; padding: 20px; }
            .header { text-align: center; border-bottom: 2px solid #2563eb; padding-bottom: 14px; margin-bottom: 18px; }
            .company { font-size: 24px; font-weight: bold; color: #1e40af; margin-bottom: 4px; text-transform: uppercase; }
            .tagline { font-size: 12px; color: #64748b; margin-bottom: 10px; }
            .invoice-badge { display: inline-block; background: #eff6ff; color: #1d4ed8; padding: 5px 14px; border-radius: 6px; font-weight: bold; font-family: monospace; font-size: 15px; border: 1px solid #bfdbfe; }
            table { width: 100%; border-collapse: collapse; margin-top: 14px; }
            th, td { padding: 9px 12px; text-align: left; font-size: 13px; }
            tr:nth-child(even) { background-color: #f8fafc; }
            .label { font-weight: bold; color: #475569; width: 38%; }
            .value { color: #0f172a; }
            .amount-box { margin-top: 20px; background: #f0fdf4; border: 1.5px solid #86efac; border-radius: 8px; padding: 12px 18px; display: flex; justify-content: space-between; align-items: center; }
            .amount-title { font-size: 15px; font-weight: bold; color: #166534; }
            .amount-num { font-size: 20px; font-weight: bold; color: #15803d; }
            .footer { margin-top: 28px; text-align: center; font-size: 11px; color: #94a3b8; border-top: 1px solid #e2e8f0; padding-top: 10px; }
          </style>
        </head>
        <body>
          <div class="header">
            <div class="company">Bin Mishal Travels</div>
            <div class="tagline">Travel, Visa & Document Processing Services</div>
            <div class="invoice-badge">INVOICE: ${record.invoice}</div>
          </div>
          <table>
            <tr><td class="label">Date & Time:</td><td class="value">${record.date} ${record.time}</td></tr>
            <tr><td class="label">Customer Name:</td><td class="value"><strong>${record.name}</strong></td></tr>
            <tr><td class="label">ID / Iqama Number:</td><td class="value">${record.idNumber}</td></tr>
            <tr><td class="label">Mobile Number:</td><td class="value">${record.mobile}</td></tr>
            <tr><td class="label">Age / Code:</td><td class="value">${record.ageCode}</td></tr>
            <tr><td class="label">Application / Service:</td><td class="value"><strong>${record.application}</strong></td></tr>
            ${if (record.comment.isNotBlank()) "<tr><td class=\"label\">Comment / Note:</td><td class=\"value\">${record.comment}</td></tr>" else ""}
            <tr><td class="label">Processed By:</td><td class="value">${record.creator}</td></tr>
          </table>
          <div class="amount-box">
            <span class="amount-title">Total Amount Paid:</span>
            <span class="amount-num">$${String.format(Locale.US, "%.2f", record.amount)}</span>
          </div>
          <div class="footer">
            Thank you for choosing Bin Mishal Travels!
          </div>
        </body>
        </html>
    """.trimIndent()

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("Invoice_${record.invoice}")
            printManager?.print(
                "Bin_Mishal_Travels_Invoice_${record.invoice}",
                printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
}
