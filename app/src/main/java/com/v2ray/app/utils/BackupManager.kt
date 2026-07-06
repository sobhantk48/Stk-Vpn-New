package com.v2ray.app.utils

import android.content.Context
import android.util.Log
import com.v2ray.app.data.Profile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    private const val TAG = "BackupManager"
    private const val BACKUP_FOLDER = "v2ray_backups"
    private const val BACKUP_FILE_PREFIX = "profiles_backup_"
    private const val BACKUP_FILE_EXTENSION = ".json"

    fun backupProfiles(context: Context, profiles: List<Profile>): File? {
        return try {
            val backupDir = File(context.filesDir, BACKUP_FOLDER)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
            val backupFile = File(backupDir, fileName)

            val jsonArray = buildJsonArray {
                profiles.forEach { profile ->
                    add(buildJsonObject {
                        put("id", profile.id)
                        put("name", profile.name)
                        put("type", profile.type)
                        put("address", profile.address)
                        put("port", profile.port)
                        put("uuid", profile.uuid)
                        put("security", profile.security)
                        put("encryption", profile.encryption)
                        put("flow", profile.flow)
                        put("sni", profile.sni)
                        put("customSni", profile.customSni)
                        put("fingerprint", profile.fingerprint)
                        put("realityPublicKey", profile.realityPublicKey)
                        put("realityShortId", profile.realityShortId)
                        put("selected", profile.selected)
                        put("ping", profile.ping)
                        put("country", profile.country)
                        put("city", profile.city)
                    })
                }
            }

            // سریالایز کردن مستقیم JsonArray به String
            val jsonString = jsonArray.toString()

            FileOutputStream(backupFile).use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            }

            Log.d(TAG, "Backup saved to: ${backupFile.absolutePath}")
            backupFile
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            null
        }
    }

    fun restoreProfiles(context: Context, file: File): List<Profile>? {
        return try {
            val jsonString = file.readText(Charsets.UTF_8)
            val jsonElement = Json.parseToJsonElement(jsonString)

            val profiles = mutableListOf<Profile>()
            val jsonArray = jsonElement.jsonArray

            jsonArray.forEach { element ->
                val obj = element.jsonObject
                val profile = Profile(
                    id = obj["id"]?.jsonPrimitive?.content ?: java.util.UUID.randomUUID().toString(),
                    name = obj["name"]?.jsonPrimitive?.content ?: "Imported",
                    type = obj["type"]?.jsonPrimitive?.content ?: "VLESS",
                    address = obj["address"]?.jsonPrimitive?.content ?: "",
                    port = obj["port"]?.jsonPrimitive?.content?.toIntOrNull() ?: 443,
                    uuid = obj["uuid"]?.jsonPrimitive?.content ?: "",
                    security = obj["security"]?.jsonPrimitive?.content ?: "auto",
                    encryption = obj["encryption"]?.jsonPrimitive?.content ?: "none",
                    flow = obj["flow"]?.jsonPrimitive?.content ?: "",
                    sni = obj["sni"]?.jsonPrimitive?.content ?: "",
                    customSni = obj["customSni"]?.jsonPrimitive?.content ?: "",
                    fingerprint = obj["fingerprint"]?.jsonPrimitive?.content ?: "chrome",
                    realityPublicKey = obj["realityPublicKey"]?.jsonPrimitive?.content ?: "",
                    realityShortId = obj["realityShortId"]?.jsonPrimitive?.content ?: "",
                    selected = obj["selected"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                    ping = obj["ping"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    country = obj["country"]?.jsonPrimitive?.content ?: "",
                    city = obj["city"]?.jsonPrimitive?.content ?: ""
                )
                profiles.add(profile)
            }

            Log.d(TAG, "Restored ${profiles.size} profiles from: ${file.absolutePath}")
            profiles
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            null
        }
    }

    fun getBackupFiles(context: Context): List<File> {
        val backupDir = File(context.filesDir, BACKUP_FOLDER)
        if (!backupDir.exists()) return emptyList()

        return backupDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith(BACKUP_FILE_PREFIX) && it.name.endsWith(BACKUP_FILE_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun deleteBackupFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Delete backup failed", e)
            false
        }
    }
}
