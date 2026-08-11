package com.lifelocker.utils

import android.content.Context
import android.util.Log
import com.lifelocker.data.*
import java.io.InputStream
import java.io.OutputStream

object BackupManager {
    private const val TAG = "BackupManager"

    private fun escapeJson(s: String?): String {
        if (s == null) return "null"
        val sb = java.lang.StringBuilder()
        for (i in 0 until s.length) {
            val ch = s[i]
            when (ch) {
                '\\', '"' -> sb.append('\\').append(ch)
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    suspend fun createBackup(context: Context, outputStream: OutputStream): Boolean {
        return try {
            val db = LifeLockerDatabase.getDatabase(context)
            
            // 1. Fetch data
            val documents = db.documentDao().getAllDocumentsSyncForBackup()
            val vaultItems = db.vaultDao().getAllVaultItemsSyncForBackup()
            val reminders = db.reminderDao().getAllRemindersSync()
            val contacts = db.emergencyDao().getAllContactsSyncForBackup()

            // 2. Serialize to JSON string manually
            val sb = java.lang.StringBuilder()
            sb.append("{")

            // Documents
            sb.append("\"documents\":[")
            documents.forEachIndexed { index, doc ->
                sb.append("{")
                sb.append("\"id\":${doc.id},")
                sb.append("\"title\":\"${escapeJson(doc.title)}\",")
                sb.append("\"category\":\"${escapeJson(doc.category)}\",")
                sb.append("\"filePath\":\"${escapeJson(doc.filePath)}\",")
                sb.append("\"expiryDate\":${if (doc.expiryDate != null) "\"${escapeJson(doc.expiryDate)}\"" else "null"},")
                sb.append("\"notes\":\"${escapeJson(doc.notes)}\",")
                sb.append("\"fileType\":\"${escapeJson(doc.fileType)}\",")
                sb.append("\"mimeType\":\"${escapeJson(doc.mimeType)}\",")
                sb.append("\"fileSize\":${doc.fileSize},")
                sb.append("\"originalExtension\":\"${escapeJson(doc.originalExtension)}\",")
                sb.append("\"isFavorite\":${doc.isFavorite},")
                sb.append("\"tags\":\"${escapeJson(doc.tags)}\",")
                sb.append("\"isEncrypted\":${doc.isEncrypted},")
                sb.append("\"isTrash\":${doc.isTrash},")
                sb.append("\"createdAt\":${doc.createdAt},")
                sb.append("\"updatedAt\":${doc.updatedAt}")
                sb.append("}")
                if (index < documents.size - 1) sb.append(",")
            }
            sb.append("],")

            // Vault Items
            sb.append("\"vault\":[")
            vaultItems.forEachIndexed { index, item ->
                sb.append("{")
                sb.append("\"id\":${item.id},")
                sb.append("\"title\":\"${escapeJson(item.title)}\",")
                sb.append("\"itemType\":\"${escapeJson(item.itemType)}\",")
                sb.append("\"username\":\"${escapeJson(item.username)}\",")
                sb.append("\"encryptedSecret\":\"${escapeJson(item.encryptedSecret)}\",")
                sb.append("\"category\":\"${escapeJson(item.category)}\",")
                sb.append("\"notes\":\"${escapeJson(item.notes)}\",")
                sb.append("\"url\":\"${escapeJson(item.url)}\",")
                sb.append("\"tags\":\"${escapeJson(item.tags)}\",")
                sb.append("\"isFavorite\":${item.isFavorite},")
                sb.append("\"isTrash\":${item.isTrash},")
                sb.append("\"isArchived\":${item.isArchived},")
                sb.append("\"updatedAt\":${item.updatedAt}")
                sb.append("}")
                if (index < vaultItems.size - 1) sb.append(",")
            }
            sb.append("],")

            // Reminders
            sb.append("\"reminders\":[")
            reminders.forEachIndexed { index, item ->
                sb.append("{")
                sb.append("\"id\":${item.id},")
                sb.append("\"title\":\"${escapeJson(item.title)}\",")
                sb.append("\"description\":\"${escapeJson(item.description)}\",")
                sb.append("\"dueDateMillis\":${item.dueDateMillis},")
                sb.append("\"priority\":\"${escapeJson(item.priority)}\",")
                sb.append("\"category\":\"${escapeJson(item.category)}\",")
                sb.append("\"isCompleted\":${item.isCompleted},")
                sb.append("\"repeatFrequency\":\"${escapeJson(item.repeatFrequency)}\"")
                sb.append("}")
                if (index < reminders.size - 1) sb.append(",")
            }
            sb.append("],")

            // Emergency Contacts
            sb.append("\"contacts\":[")
            contacts.forEachIndexed { index, item ->
                sb.append("{")
                sb.append("\"id\":${item.id},")
                sb.append("\"name\":\"${escapeJson(item.name)}\",")
                sb.append("\"relationship\":\"${escapeJson(item.relationship)}\",")
                sb.append("\"phone\":\"${escapeJson(item.phone)}\",")
                sb.append("\"bloodGroup\":\"${escapeJson(item.bloodGroup)}\",")
                sb.append("\"allergies\":\"${escapeJson(item.allergies)}\",")
                sb.append("\"conditions\":\"${escapeJson(item.conditions)}\",")
                sb.append("\"medicines\":\"${escapeJson(item.medicines)}\",")
                sb.append("\"doctor\":\"${escapeJson(item.doctor)}\",")
                sb.append("\"hospital\":\"${escapeJson(item.hospital)}\",")
                sb.append("\"insurance\":\"${escapeJson(item.insurance)}\",")
                sb.append("\"medicalNotes\":\"${escapeJson(item.medicalNotes)}\",")
                sb.append("\"isPrimary\":${item.isPrimary}")
                sb.append("}")
                if (index < contacts.size - 1) sb.append(",")
            }
            sb.append("]")

            sb.append("}")

            val plaintext = sb.toString()
            val ciphertext = CryptoUtils.encrypt(plaintext)

            outputStream.use { out ->
                out.write(ciphertext.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating backup: ${e.message}", e)
            false
        }
    }

    suspend fun restoreBackup(context: Context, inputStream: InputStream): Boolean {
        return try {
            val ciphertext = inputStream.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
            val plaintext = CryptoUtils.decrypt(ciphertext)
            if (plaintext.isEmpty() || !plaintext.startsWith("{") || !plaintext.endsWith("}")) {
                Log.e(TAG, "Decrypted text is invalid json structure")
                return false
            }

            val db = LifeLockerDatabase.getDatabase(context)

            // Extract each section
            val docArrayJson = getJsonSection(plaintext, "documents")
            val vaultArrayJson = getJsonSection(plaintext, "vault")
            val reminderArrayJson = getJsonSection(plaintext, "reminders")
            val contactArrayJson = getJsonSection(plaintext, "contacts")

            val docsParsed = parseJsonArray(docArrayJson)
            val vaultParsed = parseJsonArray(vaultArrayJson)
            val remindersParsed = parseJsonArray(reminderArrayJson)
            val contactsParsed = parseJsonArray(contactArrayJson)

            // Restore elements in database
            db.runInTransaction {
                // Restore documents
                docsParsed.forEach { map ->
                    val doc = Document(
                        id = map["id"]?.toIntOrNull() ?: 0,
                        title = map["title"].orEmpty(),
                        category = map["category"].orEmpty(),
                        filePath = map["filePath"].orEmpty(),
                        expiryDate = map["expiryDate"].takeIf { it != "null" },
                        notes = map["notes"].orEmpty(),
                        fileType = map["fileType"] ?: "DOCUMENT",
                        mimeType = map["mimeType"] ?: "*/*",
                        fileSize = map["fileSize"]?.toLongOrNull() ?: 0L,
                        originalExtension = map["originalExtension"].orEmpty(),
                        isFavorite = map["isFavorite"]?.toBoolean() ?: false,
                        tags = map["tags"].orEmpty(),
                        isEncrypted = map["isEncrypted"]?.toBoolean() ?: false,
                        isTrash = map["isTrash"]?.toBoolean() ?: false,
                        createdAt = map["createdAt"]?.toLongOrNull() ?: System.currentTimeMillis(),
                        updatedAt = map["updatedAt"]?.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    db.openHelper.writableDatabase.execSQL(
                        "INSERT OR REPLACE INTO documents (id, title, category, filePath, expiryDate, notes, fileType, mimeType, fileSize, originalExtension, isFavorite, tags, isEncrypted, isTrash, createdAt, updatedAt) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        arrayOf(doc.id, doc.title, doc.category, doc.filePath, doc.expiryDate, doc.notes, doc.fileType, doc.mimeType, doc.fileSize, doc.originalExtension, if (doc.isFavorite) 1 else 0, doc.tags, if (doc.isEncrypted) 1 else 0, if (doc.isTrash) 1 else 0, doc.createdAt, doc.updatedAt)
                    )
                }

                // Restore vault items
                vaultParsed.forEach { map ->
                    val item = VaultItem(
                        id = map["id"]?.toIntOrNull() ?: 0,
                        title = map["title"].orEmpty(),
                        itemType = map["itemType"].orEmpty(),
                        username = map["username"].orEmpty(),
                        encryptedSecret = map["encryptedSecret"].orEmpty(),
                        category = map["category"].orEmpty(),
                        notes = map["notes"].orEmpty(),
                        url = map["url"].orEmpty(),
                        tags = map["tags"].orEmpty(),
                        isFavorite = map["isFavorite"]?.toBoolean() ?: false,
                        isTrash = map["isTrash"]?.toBoolean() ?: false,
                        isArchived = map["isArchived"]?.toBoolean() ?: false,
                        updatedAt = map["updatedAt"]?.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    db.openHelper.writableDatabase.execSQL(
                        "INSERT OR REPLACE INTO vault_items (id, title, itemType, username, encryptedSecret, category, notes, url, tags, isFavorite, isTrash, isArchived, updatedAt) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        arrayOf(item.id, item.title, item.itemType, item.username, item.encryptedSecret, item.category, item.notes, item.url, item.tags, if (item.isFavorite) 1 else 0, if (item.isTrash) 1 else 0, if (item.isArchived) 1 else 0, item.updatedAt)
                    )
                }

                // Restore reminders
                remindersParsed.forEach { map ->
                    val item = ReminderItem(
                        id = map["id"]?.toIntOrNull() ?: 0,
                        title = map["title"].orEmpty(),
                        description = map["description"].orEmpty(),
                        dueDateMillis = map["dueDateMillis"]?.toLongOrNull() ?: System.currentTimeMillis(),
                        priority = map["priority"] ?: "MEDIUM",
                        category = map["category"] ?: "General",
                        isCompleted = map["isCompleted"]?.toBoolean() ?: false,
                        repeatFrequency = map["repeatFrequency"] ?: "NONE"
                    )
                    db.openHelper.writableDatabase.execSQL(
                        "INSERT OR REPLACE INTO reminders (id, title, description, dueDateMillis, priority, category, isCompleted, repeatFrequency) VALUES (?,?,?,?,?,?,?,?)",
                        arrayOf(item.id, item.title, item.description, item.dueDateMillis, item.priority, item.category, if (item.isCompleted) 1 else 0, item.repeatFrequency)
                    )
                }

                // Restore contacts
                contactsParsed.forEach { map ->
                    val item = EmergencyContact(
                        id = map["id"]?.toIntOrNull() ?: 0,
                        name = map["name"].orEmpty(),
                        relationship = map["relationship"].orEmpty(),
                        phone = map["phone"].orEmpty(),
                        bloodGroup = map["bloodGroup"] ?: "Unknown",
                        allergies = map["allergies"].orEmpty(),
                        conditions = map["conditions"].orEmpty(),
                        medicines = map["medicines"].orEmpty(),
                        doctor = map["doctor"].orEmpty(),
                        hospital = map["hospital"].orEmpty(),
                        insurance = map["insurance"].orEmpty(),
                        medicalNotes = map["medicalNotes"].orEmpty(),
                        isPrimary = map["isPrimary"]?.toBoolean() ?: false
                    )
                    db.openHelper.writableDatabase.execSQL(
                        "INSERT OR REPLACE INTO emergency_contacts (id, name, relationship, phone, bloodGroup, allergies, conditions, medicines, doctor, hospital, insurance, medicalNotes, isPrimary) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        arrayOf(item.id, item.name, item.relationship, item.phone, item.bloodGroup, item.allergies, item.conditions, item.medicines, item.doctor, item.hospital, item.insurance, item.medicalNotes, if (item.isPrimary) 1 else 0)
                    )
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring backup: ${e.message}", e)
            false
        }
    }

    private fun getJsonSection(json: String, key: String): String {
        val startToken = "\"$key\":["
        val startIdx = json.indexOf(startToken)
        if (startIdx == -1) return "[]"
        val arrStart = startIdx + startToken.length - 1
        var bracketCount = 0
        var i = arrStart
        while (i < json.length) {
            val ch = json[i]
            if (ch == '[') bracketCount++
            else if (ch == ']') {
                bracketCount--
                if (bracketCount == 0) {
                    return json.substring(arrStart, i + 1)
                }
            }
            i++
        }
        return "[]"
    }

    private fun parseJsonArray(json: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        var i = 0
        val length = json.length
        while (i < length) {
            while (i < length && json[i] != '{') i++
            if (i >= length) break
            i++ // skip '{'
            val map = mutableMapOf<String, String>()
            while (i < length) {
                while (i < length && json[i] != '"' && json[i] != '}') i++
                if (i >= length || json[i] == '}') {
                    i++
                    break
                }
                i++ // skip '"'
                val key = java.lang.StringBuilder()
                while (i < length && json[i] != '"') {
                    key.append(json[i])
                    i++
                }
                i++ // skip '"'
                while (i < length && json[i] != ':') i++
                i++ // skip ':'
                while (i < length && json[i] != '"' && !json[i].isDigit() && json[i] != 't' && json[i] != 'f' && json[i] != 'n' && json[i] != '{' && json[i] != '[') i++
                if (i >= length) break
                val valStr = java.lang.StringBuilder()
                if (json[i] == '"') {
                    i++ // skip '"'
                    while (i < length) {
                        if (json[i] == '\\' && i + 1 < length) {
                            val next = json[i+1]
                            when (next) {
                                'n' -> valStr.append('\n')
                                'r' -> valStr.append('\r')
                                't' -> valStr.append('\t')
                                else -> valStr.append(next)
                            }
                            i += 2
                        } else if (json[i] == '"') {
                            i++ // skip '"'
                            break
                        } else {
                            valStr.append(json[i])
                            i++
                        }
                    }
                } else {
                    // number, null, or boolean
                    while (i < length && json[i] != ',' && json[i] != '}' && json[i] != ']' && !json[i].isWhitespace()) {
                        valStr.append(json[i])
                        i++
                    }
                }
                map[key.toString()] = valStr.toString()
                while (i < length && json[i] != ',' && json[i] != '}') i++
                if (i < length && json[i] == ',') i++
            }
            list.add(map)
        }
        return list
    }
}
