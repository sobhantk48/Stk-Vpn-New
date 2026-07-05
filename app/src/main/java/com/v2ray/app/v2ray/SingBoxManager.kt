package com.v2ray.app.v2ray

import android.content.Context
import android.util.Log
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class SingBoxManager(private val context: Context) {

    companion object {
        private const val TAG = "SingBoxManager"
        private const val LOG_LEVEL = "warn"

        // Enum برای وضعیت‌های هسته
        enum class CoreState {
            IDLE, CONNECTING, CONNECTED, DISCONNECTED, ERROR
        }

        // StateFlow برای انتشار وضعیت
        private val _coreState = MutableStateFlow(CoreState.IDLE)
        val coreState: StateFlow<CoreState> = _coreState.asStateFlow()
    }

    private var controller: CoreController? = null
    private var isRunning = false
    private var isInitialized = false

    private inner class CoreCallback : CoreCallbackHandler {
        override fun onEmitStatus(status: Long, message: String): Long {
            Log.d(TAG, "onEmitStatus: $status - $message")
            when (status) {
                0L -> _coreState.value = CoreState.IDLE
                1L -> _coreState.value = CoreState.CONNECTING
                2L -> _coreState.value = CoreState.CONNECTED
                3L -> _coreState.value = CoreState.DISCONNECTED
                4L -> _coreState.value = CoreState.ERROR
                else -> _coreState.value = CoreState.IDLE
            }
            return 0
        }

        override fun startup(): Long {
            Log.d(TAG, "startup: Core is starting up")
            _coreState.value = CoreState.CONNECTING
            return 0
        }

        override fun shutdown(): Long {
            Log.d(TAG, "shutdown: Core is shutting down")
            _coreState.value = CoreState.DISCONNECTED
            return 0
        }
    }

    suspend fun initialize(workingDir: String = context.filesDir.absolutePath): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (isInitialized) return@withContext Result.success(Unit)

                Seq.setContext(context)
                Log.d(TAG, "Seq.setContext done")

                Libv2ray.initCoreEnv(workingDir, LOG_LEVEL)
                Log.d(TAG, "Libv2ray.initCoreEnv done")

                val callback = CoreCallback()
                controller = Libv2ray.newCoreController(callback)
                if (controller == null) {
                    throw Exception("Failed to create CoreController")
                }

                isInitialized = true
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Initialize failed", e)
                Result.failure(e)
            }
        }

    suspend fun startV2Ray(configJson: String, vpnFd: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (!isInitialized) {
                    initialize().getOrThrow()
                }

                val ctrl = controller ?: throw Exception("CoreController is null")

                if (isRunning) {
                    stopV2Ray().getOrThrow()
                }

                ctrl.startLoop(configJson, vpnFd)
                isRunning = true
                _coreState.value = CoreState.CONNECTED

                Log.d(TAG, "V2Ray started successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "startV2Ray failed", e)
                _coreState.value = CoreState.ERROR
                Result.failure(e)
            }
        }

    suspend fun stopV2Ray(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            controller?.stopLoop()
            isRunning = false
            _coreState.value = CoreState.DISCONNECTED
            Log.d(TAG, "V2Ray stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "stopV2Ray failed", e)
            Result.failure(e)
        }
    }

    fun isRunning(): Boolean = isRunning && (controller?.isRunning ?: false)

    fun cleanup() {
        runBlocking {
            try {
                controller?.stopLoop()
            } catch (_: Exception) {}
        }
        controller = null
        isRunning = false
        isInitialized = false
        _coreState.value = CoreState.IDLE
        Log.d(TAG, "Cleanup done")
    }
}
