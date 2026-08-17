package com.example.entryrecorder.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.entryrecorder.ui.theme.*
import com.example.entryrecorder.util.ParsedQrResult
import com.example.entryrecorder.util.QrScannerHelper
import com.example.entryrecorder.util.ScanTargetField
import java.util.concurrent.Executors

@Composable
fun QrScannerModalDialog(
    onDismiss: () -> Unit,
    onResultSelected: (name: String?, idNumber: String?, mobile: String?, ageCode: String?, application: String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var scannedResult by remember { mutableStateOf<ParsedQrResult?>(null) }
    var manualTextInput by remember { mutableStateOf("") }
    var isManualMode by remember { mutableStateOf(false) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val text = QrScannerHelper.decodeUri(context, uri)
            if (text != null) {
                scannedResult = QrScannerHelper.parseScannedText(text)
                scanErrorMessage = null
            } else {
                scanErrorMessage = "No QR code or readable barcode found in the selected image."
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Scanning line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 500.dp)
                .wrapContentHeight()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate800)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Scan QR / Barcode",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_qr_scanner")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                if (scannedResult != null) {
                    // Result Confirmation View
                    val result = scannedResult!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Slate800,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "QR Code Successfully Decoded",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate900,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = result.rawText,
                                        color = Color(0xFF93C5FD),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                if (result.name != null || result.idNumber != null || result.mobile != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (result.name != null) {
                                            Text(
                                                "• Name: ${result.name}",
                                                color = Slate200,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (result.idNumber != null) {
                                            Text(
                                                "• ID Number: ${result.idNumber}",
                                                color = Slate200,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (result.mobile != null) {
                                            Text(
                                                "• Mobile: ${result.mobile}",
                                                color = Slate200,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Choose which field to auto-fill:",
                            color = Slate300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Action Buttons based on parsed content
                        if (result.name != null && result.idNumber != null) {
                            Button(
                                onClick = {
                                    onResultSelected(
                                        result.name,
                                        result.idNumber,
                                        result.mobile,
                                        result.application
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("btn_apply_all_scanned"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fill All Detected Details", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val nameValue = result.name ?: result.rawText
                                    onResultSelected(nameValue, null, null, null, null)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_apply_name_scanned"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fill as Name", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val idValue = result.idNumber ?: result.rawText
                                    onResultSelected(null, idValue, null, null, null)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_apply_id_scanned"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fill as ID Number", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { scannedResult = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Again", fontSize = 12.sp)
                        }
                    }
                } else if (isManualMode) {
                    // Manual Text / Barcode Entry fallback
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Paste or type QR / Barcode text:",
                            color = Slate200,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        OutlinedTextField(
                            value = manualTextInput,
                            onValueChange = { manualTextInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("field_manual_qr_text"),
                            placeholder = { Text("Enter ID, Name, or scan text...", color = Slate500) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Slate600,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (manualTextInput.isNotBlank()) {
                                        scannedResult = QrScannerHelper.parseScannedText(manualTextInput)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_parse_manual_qr"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Process Text", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { isManualMode = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                            ) {
                                Text("Back to Camera")
                            }
                        }
                    }
                } else if (!hasCameraPermission) {
                    // Permission Request Fallback
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Slate800,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = "Camera Permission Needed",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Allow camera access to scan QR codes and barcodes directly to fill customer names and ID numbers.",
                            color = Slate400,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_grant_camera_permission"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Image", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { isManualMode = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Type Text", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // Active Camera Scanner View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (scanErrorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DangerLightRed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = scanErrorMessage ?: "",
                                    color = DangerRed,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Viewfinder Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            // Camera Preview View
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    val executor = Executors.newSingleThreadExecutor()

                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.surfaceProvider = previewView.surfaceProvider
                                            }

                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .build()

                                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                                val decoded = QrScannerHelper.decodeImageProxy(imageProxy)
                                                if (decoded != null && scannedResult == null) {
                                                    scannedResult = QrScannerHelper.parseScannedText(decoded)
                                                }
                                                imageProxy.close()
                                            }

                                            val cameraSelector = CameraSelector.Builder()
                                                .requireLensFacing(lensFacing)
                                                .build()

                                            cameraProvider.unbindAll()
                                            val camera = cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageAnalysis
                                            )
                                            cameraControl = camera.cameraControl
                                        } catch (e: Exception) {
                                            Log.e("QrScanner", "Camera binding failed", e)
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))

                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Viewfinder Overlay Reticle
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .border(2.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                            ) {
                                // Animated Laser Scanning Line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .offset(y = (200 * laserY).dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color(0xFF38BDF8),
                                                    Color.White,
                                                    Color(0xFF38BDF8),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Instruction badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = "Point camera at QR code or Barcode",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls bar below camera: Torch, Flip Camera, Gallery, Manual Text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Flashlight Toggle
                            IconButton(
                                onClick = {
                                    val newState = !isTorchOn
                                    isTorchOn = newState
                                    cameraControl?.enableTorch(newState)
                                },
                                modifier = Modifier
                                    .background(Slate800, CircleShape)
                                    .testTag("btn_toggle_torch")
                            ) {
                                Icon(
                                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flashlight",
                                    tint = if (isTorchOn) WarningAmber else Slate300
                                )
                            }

                            // Switch Camera
                            IconButton(
                                onClick = {
                                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                        CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        CameraSelector.LENS_FACING_BACK
                                    }
                                },
                                modifier = Modifier
                                    .background(Slate800, CircleShape)
                                    .testTag("btn_switch_camera")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Switch Camera",
                                    tint = Slate300
                                )
                            }

                            // Pick from Gallery
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .background(Slate800, CircleShape)
                                    .testTag("btn_pick_qr_gallery")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Pick Image",
                                    tint = PrimaryBlue
                                )
                            }

                            // Manual Type
                            IconButton(
                                onClick = { isManualMode = true },
                                modifier = Modifier
                                    .background(Slate800, CircleShape)
                                    .testTag("btn_manual_text_entry")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "Type text",
                                    tint = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
