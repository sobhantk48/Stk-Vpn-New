package com.v2ray.app.utils

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "V2RAY_STK"
    private val logs = mutableListOf<String>()
    private val maxLogSize = 200

    fun writeLog(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] [$TAG] $message"
        Log.i(TAG, message)
        addLog(logLine)
    }

    fun writeError(message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] [$TAG] ERROR: $message"
        Log.e(TAG, message, throwable)
        addLog(logLine)
        throwable?.stackTrace?.forEach {
            addLog("  at $it")
        }
    }

    private fun addLog(line: String) {
        logs.add(line)
        if (logs.size > maxLogSize) {
            logs.removeAt(0)
        }
        // ذخیره در فایل
        try {
            val file = File("/data/user/0/com.v2ray.app/files/logs/v2ray_stk_log.txt")
            file.parentFile?.mkdirs()
            file.appendText("$line\n")
        } catch (_: Exception) { }
    }

    fun getLogs(): List<String> = logs.toList()

    fun clearLogs() {
        logs.clear()
    }
}
