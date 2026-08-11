package com.lifelocker.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Offline QR code generator using ZXing.
 * SECURITY: NEVER pass passwords, PINs, or encrypted secrets.
 * Only safe metadata (title, category, expiry date, IDs) is allowed.
 */
object QrCodeHelper {

    /**
     * Generates a QR code bitmap for safe document metadata.
     * Only exposes non-sensitive fields.
     */
    fun generateDocumentQr(title: String, category: String, expiryDate: String?, notes: String): Bitmap? {
        val safeContent = buildString {
            append("LifeLocker Document\n")
            append("Title: $title\n")
            append("Category: $category\n")
            if (!expiryDate.isNullOrEmpty()) append("Expiry: $expiryDate\n")
            if (notes.isNotEmpty()) append("Notes: $notes")
        }
        return generateQrBitmap(safeContent)
    }

    /**
     * Generates a QR code bitmap for safe emergency contact metadata.
     * NEVER includes private vault credentials.
     */
    fun generateEmergencyQr(
        name: String,
        bloodGroup: String,
        allergies: String,
        conditions: String,
        doctor: String,
        hospital: String,
        phone: String
    ): Bitmap? {
        val safeContent = buildString {
            append("LifeLocker Emergency Info\n")
            append("Name: $name\n")
            if (bloodGroup.isNotEmpty() && bloodGroup != "Unknown") append("Blood Group: $bloodGroup\n")
            if (allergies.isNotEmpty()) append("Allergies: $allergies\n")
            if (conditions.isNotEmpty()) append("Conditions: $conditions\n")
            if (doctor.isNotEmpty()) append("Doctor: $doctor\n")
            if (hospital.isNotEmpty()) append("Hospital: $hospital\n")
            if (phone.isNotEmpty()) append("Phone: $phone")
        }
        return generateQrBitmap(safeContent)
    }

    private fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
