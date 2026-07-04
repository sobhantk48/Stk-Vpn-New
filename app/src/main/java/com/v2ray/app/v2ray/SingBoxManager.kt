package com.v2ray.app.v2ray

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingBoxManager(private val context: Context) {
    private var running = false

    suspend fun startV2Ray(configJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            running = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stopV2Ray(): Result<Unit> = withContext(Dispatchers.IO) {
        running = false
        Result.success(Unit)
    }

    fun isRunning(): Boolean = running
    fun getStats(): V2RayStats = V2RayStats(0, 0, 0)
}

data class V2RayStats(val uplink: Long, val downlink: Long, val connectionCount: Int)
