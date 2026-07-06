package com.v2ray.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "connection_history")
data class ConnectionHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: String,
    val profileName: String,
    val server: String,
    val action: String, // "CONNECT", "DISCONNECT"
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0 // مدت زمان اتصال به میلی‌ثانیه (برای CONNECT)
) {
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        return android.text.format.DateFormat.format("HH:mm:ss", date).toString()
    }

    fun getFormattedDate(): String {
        val date = Date(timestamp)
        return android.text.format.DateFormat.format("yyyy-MM-dd", date).toString()
    }

    fun getDurationString(): String {
        if (action != "CONNECT" || duration == 0L) return "--"
        val seconds = duration / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
}
