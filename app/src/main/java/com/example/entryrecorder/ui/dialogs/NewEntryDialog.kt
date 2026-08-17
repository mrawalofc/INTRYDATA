package com.example.entryrecorder.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.entryrecorder.model.ApplicationServices
import com.example.entryrecorder.ui.CustomerProfile
import com.example.entryrecorder.ui.EntryViewModel
import com.example.entryrecorder.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryDialog(
    viewModel: EntryViewModel,
    onDismiss: () -> Unit,
    onEntrySaved: ((com.example.entryrecorder.model.EntryRecord) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val knownProfiles by viewModel.knownProfiles.collectAsState()

    var invoiceNumber by remember { mutableStateOf("INV-0001") }
    var dateInput by remember { mutableStateOf(todayStr) }
    var nameInput by remember { mutableStateOf("") }
    var idInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var selectedApplication by remember { mutableStateOf("") }
    var ageCodeInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }

    var isServiceDropdownOpen by remember { mutableStateOf(false) }
    var serviceSearchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQrScanner by remember { mutableStateOf(false) }

    var showNameSuggestions by remember { mutableStateOf(false) }
    var showIdSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        invoiceNumber = viewModel.getNextInvoice()
    }

    val filteredServices = remember(serviceSearchQuery) {
        if (serviceSearchQuery.isBlank()) {
            ApplicationServices.list
        } else {
            ApplicationServices.list.filter {
                it.contains(serviceSearchQuery, ignoreCase = true)
            }
        }
    }

    // Suggestions for name
    val nameSuggestions = remember(nameInput, knownProfiles) {
        if (nameInput.trim().length >= 1) {
            val q = nameInput.trim().lowercase(Locale.getDefault())
            knownProfiles.filter { it.name.lowercase(Locale.getDefault()).contains(q) }.take(5)
        } else emptyList()
    }

    // Suggestions for ID
    val idSuggestions = remember(idInput, knownProfiles) {
        if (idInput.trim().length >= 1) {
            val q = idInput.trim().lowercase(Locale.getDefault())
            knownProfiles.filter { it.idNumber.lowercase(Locale.getDefault()).contains(q) }.take(5)
        } else emptyList()
    }

    fun applyCustomerProfile(profile: CustomerProfile) {
        nameInput = profile.name
        idInput = profile.idNumber
        mobileInput = profile.mobile
        ageCodeInput = profile.ageCode
        showNameSuggestions = false
        showIdSuggestions = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Primary color
                Surface(
                    color = PrimaryBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "New Entry",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_entry_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Form Body
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(20.dp)
                ) {
                    // Invoice Display Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryLightBlue,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bin Mishal Travels - Invoice Number",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = PrimaryDarkBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = invoiceNumber,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scan QR Button (Camera)
                    Button(
                        onClick = { showQrScanner = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_scan_qr_modal"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan QR / Camera Read (Auto Fill)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Date
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_field_date"),
                        label = { Text("Date (YYYY-MM-DD)") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name with Auto-Complete & Auto-Select
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                errorMessage = null
                                showNameSuggestions = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_field_name"),
                            label = { Text("Customer Name * (Auto-suggests ID & Phone)") },
                            placeholder = { Text("Type name or scan QR") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { showQrScanner = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = "Scan for Name",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    if (nameSuggestions.isNotEmpty()) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = "Auto fill available",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Name suggestion dropdown list
                        if (showNameSuggestions && nameSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Slate50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                                shadowElevation = 4.dp
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    Text(
                                        text = "Select matching customer to auto-fill details:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryDarkBlue,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    nameSuggestions.forEach { profile ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { applyCustomerProfile(profile) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = profile.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate900
                                                    )
                                                )
                                                Text(
                                                    text = "ID: ${profile.idNumber} | Tel: ${profile.mobile}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PrimaryLightBlue
                                            ) {
                                                Text(
                                                    text = "Code: ${profile.ageCode}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryBlue),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Divider(color = Slate200)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ID Number with Auto-select suggestions
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = idInput,
                            onValueChange = {
                                idInput = it
                                errorMessage = null
                                showIdSuggestions = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_field_id"),
                            label = { Text("ID Number *") },
                            placeholder = { Text("Enter, search, or scan ID number") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryBlue)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showQrScanner = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan for ID",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // ID Suggestions dropdown
                        if (showIdSuggestions && idSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Slate50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                                shadowElevation = 4.dp
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    idSuggestions.forEach { profile ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { applyCustomerProfile(profile) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "ID: ${profile.idNumber}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate900
                                                    )
                                                )
                                                Text(
                                                    text = "${profile.name} (${profile.mobile})",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PrimaryLightBlue
                                            ) {
                                                Text(
                                                    text = profile.ageCode,
                                                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryBlue),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Divider(color = Slate200)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mobile
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = {
                            mobileInput = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_field_mobile"),
                        label = { Text("Mobile / Phone Number *") },
                        placeholder = { Text("Enter mobile number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Application (Service Selection)
                    OutlinedTextField(
                        value = selectedApplication,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isServiceDropdownOpen = true }
                            .testTag("entry_field_application"),
                        label = { Text("Application / Service *") },
                        placeholder = { Text("Select a service...") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isServiceDropdownOpen = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Service")
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Age / Code & Amount in a Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ageCodeInput,
                            onValueChange = {
                                ageCodeInput = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_field_age_code"),
                            label = { Text("Age / Code *") },
                            placeholder = { Text("Age or Code") },
                            leadingIcon = {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = {
                                amountInput = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_field_amount"),
                            label = { Text("Amount ($) *") },
                            placeholder = { Text("0.00") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = PrimaryBlue)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

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
                            .testTag("entry_field_comment"),
                        label = { Text("Comment / Remarks (Optional)") },
                        placeholder = { Text("Add comment or note for this invoice...") },
                        leadingIcon = {
                            Icon(Icons.Default.Comment, contentDescription = null, tint = PrimaryBlue)
                        },
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Button(
                        onClick = {
                            val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
                            if (nameInput.isBlank() || idInput.isBlank() || mobileInput.isBlank() ||
                                selectedApplication.isBlank() || ageCodeInput.isBlank() ||
                                amountInput.isBlank()
                            ) {
                                errorMessage = "Please fill in all required fields (Name, ID, Mobile, Service, Age/Code, Amount)."
                                return@Button
                            }

                            viewModel.saveEntry(
                                date = dateInput.ifBlank { todayStr },
                                name = nameInput,
                                idNumber = idInput,
                                mobile = mobileInput,
                                applicationName = selectedApplication,
                                ageCode = ageCodeInput,
                                amount = parsedAmount,
                                comment = commentInput,
                                onComplete = { success, _ ->
                                    if (success) {
                                        onDismiss()
                                    }
                                },
                                onRecordCreated = { createdRecord ->
                                    onEntrySaved?.invoke(createdRecord)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_entry"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Preview Invoice",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            nameInput = ""
                            idInput = ""
                            mobileInput = ""
                            selectedApplication = ""
                            ageCodeInput = ""
                            amountInput = ""
                            commentInput = ""
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_clear_entry_form"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Slate600)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Clear Form", color = Slate700)
                    }
                }
            }
        }
    }

    // Modal BottomSheet / Dialog for Application selection
    if (isServiceDropdownOpen) {
        Dialog(onDismissRequest = { isServiceDropdownOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Application / Service",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = serviceSearchQuery,
                        onValueChange = { serviceSearchQuery = it },
                        placeholder = { Text("Search service...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredServices) { service ->
                            ListItem(
                                headlineContent = { Text(service, style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedApplication = service
                                        isServiceDropdownOpen = false
                                    }
                            )
                            Divider(color = Slate100)
                        }
                    }
                }
            }
        }
    }

    // QR Code Scanner Camera Dialog
    if (showQrScanner) {
        QrScannerModalDialog(
            onDismiss = { showQrScanner = false }
        ) { scannedName, scannedId, scannedMobile, scannedAgeCode, scannedApp ->
            if (!scannedName.isNullOrBlank()) {
                nameInput = scannedName
                showNameSuggestions = false
            }
            if (!scannedId.isNullOrBlank()) {
                idInput = scannedId
                showIdSuggestions = false
            }
            if (!scannedMobile.isNullOrBlank()) {
                mobileInput = scannedMobile
            }
            if (!scannedAgeCode.isNullOrBlank()) {
                ageCodeInput = scannedAgeCode
            }
            if (!scannedApp.isNullOrBlank()) {
                selectedApplication = scannedApp
            }

            // Check if scanned ID or Name matches a known customer profile to auto-fill remainder
            val matchedProfile = if (!scannedId.isNullOrBlank()) {
                knownProfiles.find { it.idNumber.equals(scannedId.trim(), ignoreCase = true) }
            } else if (!scannedName.isNullOrBlank()) {
                knownProfiles.find { it.name.equals(scannedName.trim(), ignoreCase = true) }
            } else null

            if (matchedProfile != null) {
                if (nameInput.isBlank()) nameInput = matchedProfile.name
                if (idInput.isBlank()) idInput = matchedProfile.idNumber
                if (mobileInput.isBlank()) mobileInput = matchedProfile.mobile
                if (ageCodeInput.isBlank()) ageCodeInput = matchedProfile.ageCode
            }
        }
    }
}
