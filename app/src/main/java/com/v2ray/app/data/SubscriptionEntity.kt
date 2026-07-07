package com.v2ray.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val name: String,
    val autoUpdate: Boolean = true,
    val updateInterval: Int = 60, // دقیقه
    val lastUpdated: Long = 0,
    val enabled: Boolean = true
) {
    fun needsUpdate(): Boolean {
        if (!autoUpdate) return false
        val elapsed = (System.currentTimeMillis() - lastUpdated) / 1000 / 60 // دقیقه
        return elapsed >= updateInterval
    }
}
