package com.v2ray.app.utils

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val TAG = "V2RAY_STK"
    private const val LOG_FILE_NAME = "v2ray_stk_log.txt"
    private var logFile: File? = null
    private var isInitialized = false
    private lateinit var context: Context
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun initialize(ctx: Context) {
        context = ctx.applicationContext
        try {
            // استفاده از filesDir برای اطمینان از دسترسی نوشتن
            val appDir = File(context.filesDir, "logs")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            logFile = File(appDir, LOG_FILE_NAME)
            if (!logFile!!.exists()) {
                logFile!!.createNewFile()
            }

            isInitialized = true
            writeLog("=== V2RAY STK LOG STARTED ===")
            writeLog("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            writeLog("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            writeLog("Log file: ${logFile!!.absolutePath}")
            writeLog("================================")
        } catch (e: Exception) {
            Log.e(TAG, "Logger init failed", e)
        }
    }

    fun writeLog(message: String, tag: String = TAG) {
        try {
            if (!isInitialized || logFile == null) return
            val logEntry = "[${dateFormat.format(Date())}] [$tag] $message\n"
            logFile?.appendText(logEntry)
            Log.d(tag, message)
            if (logFile?.length() ?: 0 > 10 * 1024 * 1024) rotateLogFile()
        } catch (_: Exception) {
            Log.d(tag, message)
        }
    }

    fun writeError(message: String, throwable: Throwable? = null, tag: String = TAG) {
        val errorMsg = if (throwable != null) "$message: ${throwable.message}\n${throwable.stackTraceToString()}" else message
        writeLog("ERROR: $errorMsg", tag)
    }

    private fun rotateLogFile() {
        try {
            logFile?.let {
                val archive = File(it.parent, "v2ray_stk_log_${System.currentTimeMillis()}.txt")
                it.renameTo(archive)
                it.createNewFile()
                writeLog("Log file rotated")
            }
        } catch (_: Exception) {}
    }

    fun getLogFilePath(): String? {
        return try {
            logFile?.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun getLogContent(): String? {
        return try {
            val file = logFile
            if (file == null || !file.exists()) {
                return null
            }
            file.readText()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read log file", e)
            null
        }
    }

    fun clearLogs() {
        try {
            logFile?.delete()
            logFile?.createNewFile()
            writeLog("Logs cleared")
        } catch (_: Exception) {}
    }
}
