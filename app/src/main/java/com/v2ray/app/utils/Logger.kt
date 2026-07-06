package com.v2ray.app.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "V2RayLogger"
    private const val LOG_FILE_NAME = "v2ray_app.log"
    private const val MAX_LOG_SIZE = 1024 * 1024 // 1 MB

    private var logFile: File? = null

    fun init(context: Context) {
        logFile = File(context.filesDir, LOG_FILE_NAME)
        if (logFile!!.exists() && logFile!!.length() > MAX_LOG_SIZE) {
            logFile!!.delete()
        }
        log("App started", "INFO")
    }

    fun getLogFilePath(): String? = logFile?.absolutePath

    fun getLogContent(): String {
        return try {
            logFile?.readText() ?: "No logs available."
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    fun clearLogs() {
        try {
            logFile?.delete()
            logFile?.createNewFile()
            log("Logs cleared", "INFO")
        } catch (e: Exception) {
            Log.e(TAG, "Clear logs failed", e)
        }
    }

    fun log(message: String, level: String = "DEBUG") {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val entry = "[$timestamp] [$level] $message\n"
            logFile?.appendText(entry)
            Log.d(TAG, entry.trim())
        } catch (_: Exception) {
            // اگر فایل قابل نوشتن نبود، فقط لاگ در Logcat
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        val msg = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        log(msg, "ERROR")
        Log.e(TAG, message, throwable)
    }

    fun d(message: String) = log(message, "DEBUG")
    fun i(message: String) = log(message, "INFO")
    fun w(message: String) = log(message, "WARN")
}
