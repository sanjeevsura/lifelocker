package com.lifelocker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StoredFileInfo(
    val absolutePath: String,
    val fileName: String,
    val extension: String,
    val mimeType: String,
    val fileSize: Long,
    val checksum: String = ""
)

object FileStorageHelper {

    private const val DOCS_DIR_NAME = "vault_docs"

    fun getDocsDir(context: Context): File {
        val dir = File(context.filesDir, DOCS_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun calculateFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    fun saveUriToInternalStorage(context: Context, sourceUri: Uri, preferredName: String? = null): StoredFileInfo? {
        return try {
            val contentResolver = context.contentResolver
            var displayName = preferredName
            var sizeBytes = 0L

            contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (displayName.isNullOrEmpty() && nameIndex != -1) {
                        displayName = cursor.getString(nameIndex)
                    }
                    if (sizeIndex != -1) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }

            val cleanFileName = displayName?.replace("[^a-zA-Z0-9._-]".toRegex(), "_") ?: "file_${System.currentTimeMillis()}"
            val extension = getExtensionFromNameOrUri(context, sourceUri, cleanFileName)
            val mimeType = getMimeType(context, sourceUri, extension)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val targetFileName = "${timeStamp}_$cleanFileName"
            val targetFile = File(getDocsDir(context), targetFileName)

            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            if (inputStream == null) return null

            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            val finalSize = if (sizeBytes > 0L) sizeBytes else targetFile.length()
            val hash = calculateFileHash(targetFile)

            StoredFileInfo(
                absolutePath = targetFile.absolutePath,
                fileName = cleanFileName,
                extension = extension,
                mimeType = mimeType,
                fileSize = finalSize,
                checksum = hash
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportFileToUri(context: Context, sourcePath: String, targetUri: Uri): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return false
            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openFile(context: Context, filePath: String, mimeType: String? = null): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false

        return try {
            val uri = getContentUriForFile(context, filePath) ?: return false
            val resolvedMime = mimeType.takeUnless { it.isNullOrEmpty() || it == "*/*" }
                ?: getMimeTypeFromExtension(file.extension)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, resolvedMime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open File With...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareFile(context: Context, filePath: String, mimeType: String? = null, title: String = "Share File") {
        val file = File(filePath)
        if (!file.exists()) return
        try {
            val uri = getContentUriForFile(context, filePath) ?: return
            val resolvedMime = mimeType.takeUnless { it.isNullOrEmpty() } ?: getMimeTypeFromExtension(file.extension)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = resolvedMime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createTemporaryCameraFile(context: Context): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "com.lifelocker.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    fun deleteInternalFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    fun getContentUriForFile(context: Context, path: String): Uri? {
        val file = File(path)
        if (!file.exists()) return null
        return try {
            FileProvider.getUriForFile(context, "com.lifelocker.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }
    }

    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        return String.format(Locale.getDefault(), "%.1f %s", sizeBytes / Math.pow(1024.0, index.toDouble()), units[index])
    }

    fun saveEncryptedFile(context: Context, filename: String, content: String): Boolean {
        return try {
            val file = File(context.filesDir, filename)
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getExtensionFromNameOrUri(context: Context, uri: Uri, fileName: String): String {
        if (fileName.contains(".")) {
            val ext = fileName.substringAfterLast('.', "")
            if (ext.isNotEmpty()) return ext.lowercase(Locale.getDefault())
        }
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
    }

    private fun getMimeType(context: Context, uri: Uri, extension: String): String {
        val typeFromResolver = context.contentResolver.getType(uri)
        if (!typeFromResolver.isNullOrEmpty() && typeFromResolver != "application/octet-stream") {
            return typeFromResolver
        }
        return getMimeTypeFromExtension(extension)
    }

    private fun getMimeTypeFromExtension(extension: String): String {
        val map = MimeTypeMap.getSingleton()
        return map.getMimeTypeFromExtension(extension.lowercase(Locale.getDefault())) ?: "*/*"
    }
}
