package com.v2ray.app.v2ray

import android.content.Context
import android.util.Log
import libbox.BoxInstance
import libbox.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class SingBoxManager(private val context: Context) {
    companion object {
        private const val TAG = "SingBoxManager"
        enum class CoreState {
            IDLE, CONNECTING, CONNECTED, DISCONNECTED, ERROR
        }

        private val _coreState = MutableStateFlow(CoreState.IDLE)
        val coreState: StateFlow<CoreState> = _coreState.asStateFlow()
    }

    private var boxInstance: BoxInstance? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Logger implementation for libbox
    private val boxLogger = object : Logger {
        override fun log(level: Int, message: String?) {
            when (level) {
                0 -> Log.d(TAG, "DEBUG: $message")
                1 -> Log.i(TAG, "INFO: $message")
                2 -> Log.w(TAG, "WARN: $message")
                3 -> Log.e(TAG, "ERROR: $message")
                else -> Log.d(TAG, "LOG: $message")
            }
        }
    }

    suspend fun initialize(workingDir: String = context.cacheDir.absolutePath): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // ایجاد پوشه‌های مورد نیاز
                File(workingDir).mkdirs()
                // BoxInstance نیازی به initialization جداگانه ندارد، در start ایجاد می‌شود
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Initialize failed", e)
                Result.failure(e)
            }
        }

    suspend fun startV2Ray(configJson: String, vpnFd: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (isRunning) {
                    stopV2Ray().getOrThrow()
                }

                // ایجاد BoxInstance با کانفیگ
                val instance = BoxInstance(configJson, boxLogger, workingDir = context.cacheDir.absolutePath)
                instance.start()
                boxInstance = instance
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
                boxInstance?.stop()
                boxInstance?.close()
                boxInstance = null
                isRunning = false
                _coreState.update { CoreState.DISCONNECTED }
                Log.d(TAG, "V2Ray stopped")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "stopV2Ray failed", e)
                Result.failure(e)
            }
        }

    fun isRunning(): Boolean = isRunning && (boxInstance?.isRunning() == true)

    fun cleanup() {
        scope.launch {
            try {
                boxInstance?.stop()
                boxInstance?.close()
            } catch (_: Exception) {
                // ignore
            } finally {
                boxInstance = null
                isRunning = false
                _coreState.update { CoreState.IDLE }
                Log.d(TAG, "Cleanup done")
            }
        }
    }
}
