package com.v2ray.app.v2ray

import android.content.Context
import android.content.Intent
import android.util.Log
import io.nekohasekai.libbox.BoxService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SingBoxManager(private val context: Context) {
    companion object {
        private const val TAG = "SingBoxManager"
        enum class CoreState {
            IDLE, CONNECTING, CONNECTED, DISCONNECTED, ERROR
        }

        private val _coreState = MutableStateFlow(CoreState.IDLE)
        val coreState: StateFlow<CoreState> = _coreState.asStateFlow()
    }

    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun startV2Ray(configJson: String, vpnFd: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (isRunning) {
                    stopV2Ray().getOrThrow()
                }

                // شروع BoxService با کانفیگ
                val intent = Intent(context, BoxService::class.java).apply {
                    putExtra("config", configJson)
                }
                context.startService(intent)
                Log.d(TAG, "BoxService started with config")

                isRunning = true
                _coreState.update { CoreState.CONNECTED }
                Log.d(TAG, "V2Ray started successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "startV2Ray failed", e)
                _coreState.update { CoreState.ERROR }
                Result.failure(e)
            }
        }

    suspend fun stopV2Ray(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.stopService(Intent(context, BoxService::class.java))
                isRunning = false
                _coreState.update { CoreState.DISCONNECTED }
                Log.d(TAG, "V2Ray stopped")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "stopV2Ray failed", e)
                Result.failure(e)
            }
        }

    fun isRunning(): Boolean = isRunning

    fun cleanup() {
        scope.launch {
            try {
                context.stopService(Intent(context, BoxService::class.java))
            } catch (_: Exception) {}
            isRunning = false
            _coreState.update { CoreState.IDLE }
            Log.d(TAG, "Cleanup done")
        }
    }
}
