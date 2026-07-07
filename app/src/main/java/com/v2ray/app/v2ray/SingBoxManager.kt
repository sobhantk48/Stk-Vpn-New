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
import kotlinx.coroutines.flow.update

class SingBoxManager(private val context: Context) {
    companion object {
        private const val TAG = "SingBoxManager"
        private const val LOG_LEVEL = "warn"

        enum class CoreState {
            IDLE, CONNECTING, CONNECTED, DISCONNECTED, ERROR
        }

        private val _coreState = MutableStateFlow(CoreState.IDLE)
        val coreState: StateFlow<CoreState> = _coreState.asStateFlow()
    }

    private var controller: CoreController? = null
    private var isRunning = false
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private inner class CoreCallback : CoreCallbackHandler {
        override fun onEmitStatus(status: Long, message: String): Long {
            return try {
                Log.d(TAG, "onEmitStatus: $status - $message")
                _coreState.update {
                    when (status) {
                        0L -> CoreState.IDLE
                        1L -> CoreState.CONNECTING
                        2L -> CoreState.CONNECTED
                        3L -> CoreState.DISCONNECTED
                        4L -> CoreState.ERROR
                        else -> CoreState.IDLE
                    }
                }
                0
            } catch (e: Exception) {
                Log.e(TAG, "onEmitStatus error", e)
                0
            }
        }

        override fun startup(): Long {
            return try {
                Log.d(TAG, "startup: Core is starting up")
                _coreState.update { CoreState.CONNECTING }
                0
            } catch (e: Exception) {
                Log.e(TAG, "startup error", e)
                0
            }
        }

        override fun shutdown(): Long {
            return try {
                Log.d(TAG, "shutdown: Core is shutting down")
                _coreState.update { CoreState.DISCONNECTED }
                0
            } catch (e: Exception) {
                Log.e(TAG, "shutdown error", e)
                0
            }
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
                    ?: throw Exception("Failed to create CoreController")
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
                if (vpnFd <= 0) {
                    throw Exception("Invalid VPN file descriptor: $vpnFd")
                }
                Log.d(TAG, "Starting V2Ray with fd: $vpnFd")
                try {
                    ctrl.startLoop(configJson, vpnFd)
                } catch (e: Exception) {
                    Log.e(TAG, "startLoop failed", e)
                    _coreState.update { CoreState.ERROR }
                    return@withContext Result.failure(e)
                }
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
                controller?.stopLoop()
                isRunning = false
                _coreState.update { CoreState.DISCONNECTED }
                Log.d(TAG, "V2Ray stopped")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "stopV2Ray failed", e)
                Result.failure(e)
            }
        }

    fun isRunning(): Boolean = isRunning && (controller?.isRunning ?: false)

    fun cleanup() {
        scope.launch {
            try {
                controller?.stopLoop()
            } catch (_: Exception) {
                // ignore
            } finally {
                controller = null
                isRunning = false
                isInitialized = false
                _coreState.update { CoreState.IDLE }
                Log.d(TAG, "Cleanup done")
            }
        }
    }
}
