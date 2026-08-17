package com.example.entryrecorder.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.json.JSONObject
import java.nio.ByteBuffer

data class ParsedQrResult(
    val rawText: String,
    val name: String? = null,
    val idNumber: String? = null,
    val mobile: String? = null,
    val ageCode: String? = null,
    val application: String? = null,
    val suggestedField: ScanTargetField = ScanTargetField.ID_OR_NAME
)

enum class ScanTargetField {
    NAME,
    ID_NUMBER,
    ALL_FIELDS,
    ID_OR_NAME
}

object QrScannerHelper {

    private val multiFormatReader = MultiFormatReader()

    fun decodeImageProxy(imageProxy: ImageProxy): String? {
        val plane = imageProxy.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val width = imageProxy.width
        val height = imageProxy.height

        val source = PlanarYUVLuminanceSource(
            bytes,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        return try {
            val result = multiFormatReader.decodeWithState(binaryBitmap)
            result.text
        } catch (_: Exception) {
            null
        } finally {
            multiFormatReader.reset()
        }
    }

    fun decodeBitmap(bitmap: Bitmap): String? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        return try {
            val result = multiFormatReader.decodeWithState(binaryBitmap)
            result.text
        } catch (_: Exception) {
            null
        } finally {
            multiFormatReader.reset()
        }
    }

    fun decodeUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return null
                decodeBitmap(bitmap)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun parseScannedText(raw: String): ParsedQrResult {
        val trimmed = raw.trim()

        // 1. Try parsing JSON format
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                val name = json.optString("name").ifBlank { json.optString("Name") }.takeIf { it.isNotBlank() }
                val id = json.optString("idNumber").ifBlank {
                    json.optString("id").ifBlank {
                        json.optString("ID").ifBlank { json.optString("iqama") }
                    }
                }.takeIf { it.isNotBlank() }
                val mobile = json.optString("mobile").ifBlank {
                    json.optString("phone").ifBlank { json.optString("Mobile") }
                }.takeIf { it.isNotBlank() }
                val ageCode = json.optString("ageCode").ifBlank { json.optString("code") }.takeIf { it.isNotBlank() }
                val app = json.optString("application").ifBlank { json.optString("service") }.takeIf { it.isNotBlank() }

                return ParsedQrResult(
                    rawText = trimmed,
                    name = name,
                    idNumber = id,
                    mobile = mobile,
                    ageCode = ageCode,
                    application = app,
                    suggestedField = ScanTargetField.ALL_FIELDS
                )
            } catch (_: Exception) {
                // fall through
            }
        }

        // 2. Try parsing Key: Value lines
        var parsedName: String? = null
        var parsedId: String? = null
        var parsedMobile: String? = null
        var parsedCode: String? = null
        var parsedApp: String? = null

        val lines = trimmed.split("\n", ";", ",")
        for (line in lines) {
            val parts = line.split(":", "=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim()
                if (value.isNotBlank()) {
                    when {
                        key.contains("name") || key.contains("اسم") -> parsedName = value
                        key.contains("id") || key.contains("iqama") || key.contains("national") || key.contains("هوية") || key.contains("اقامة") -> parsedId = value
                        key.contains("mobile") || key.contains("phone") || key.contains("tel") || key.contains("جوال") -> parsedMobile = value
                        key.contains("code") || key.contains("age") -> parsedCode = value
                        key.contains("app") || key.contains("service") -> parsedApp = value
                    }
                }
            }
        }

        if (parsedName != null || parsedId != null || parsedMobile != null) {
            return ParsedQrResult(
                rawText = trimmed,
                name = parsedName,
                idNumber = parsedId,
                mobile = parsedMobile,
                ageCode = parsedCode,
                application = parsedApp,
                suggestedField = ScanTargetField.ALL_FIELDS
            )
        }

        // 3. Check if purely numeric or typical ID / Iqama (e.g. 8 to 15 digits)
        val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")
        if (digitsOnly.length >= 8 && digitsOnly.length <= 15 && digitsOnly.length == trimmed.length) {
            return ParsedQrResult(
                rawText = trimmed,
                idNumber = trimmed,
                suggestedField = ScanTargetField.ID_NUMBER
            )
        }

        // 4. Check if text without many digits (looks like a Name)
        if (trimmed.length in 3..60 && !trimmed.contains("http://") && !trimmed.contains("https://") && digitsOnly.length <= 2) {
            return ParsedQrResult(
                rawText = trimmed,
                name = trimmed,
                suggestedField = ScanTargetField.NAME
            )
        }

        // 5. Default fallback
        return ParsedQrResult(
            rawText = trimmed,
            idNumber = if (trimmed.any { it.isDigit() }) trimmed else null,
            name = if (!trimmed.all { it.isDigit() }) trimmed else null,
            suggestedField = ScanTargetField.ID_OR_NAME
        )
    }
}
