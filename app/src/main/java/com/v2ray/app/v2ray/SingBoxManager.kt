package com.v2ray.app.v2ray

import android.content.Context
import com.v2ray.app.utils.Logger
import io.nekohasekai.sfa.libbox.Libbox
import io.nekohasekai.sfa.libbox.LibboxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingBoxManager(private val context: Context) {
    private var running = false
    private lateinit var libboxManager: LibboxManager

    init {
        Libbox.initialize(context)
        libboxManager = LibboxManager(context)
    }

    suspend fun startV2Ray(configJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (running) return@withContext Result.success(Unit)
            libboxManager.start(configJson)
            running = true
            Logger.writeLog("sing-box started via Libbox")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.writeError("sing-box start failed", e)
            Result.failure(e)
        }
    }

    suspend fun stopV2Ray(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            libboxManager.stop()
            running = false
            Logger.writeLog("sing-box stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.writeError("sing-box stop failed", e)
            Result.failure(e)
        }
    }

    fun isRunning(): Boolean = running
    fun getStats(): V2RayStats = V2RayStats(0, 0, 0)
}

data class V2RayStats(val uplink: Long, val downlink: Long, val connectionCount: Int)
