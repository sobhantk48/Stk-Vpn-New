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

/**
 * مدیریت هسته‌ی Sing-Box با استفاده از libbox.aar
 * 
 * این کلاس با libbox.aar که شامل CoreController از پروژه‌ی sing-box است کار می‌کند.
 * برای راه‌اندازی VPN باید:
 * 1. go.Seq.setContext(context) برای تنظیم Context اندروید
 * 2. Libv2ray.initCoreEnv(workingDir, logLevel) برای مقداردهی اولیه
 * 3. ایجاد CoreController با newCoreController(callback)
 * 4. startLoop(configJson, fd) برای راه‌اندازی VPN
 */
class SingBoxManager(private val context: Context) {

    companion object {
        private const val TAG = "SingBoxManager"
        private const val LOG_LEVEL = "warn"
        
        // StateFlow برای انتشار وضعیت هسته
        private val _coreState = MutableStateFlow(CoreState.IDLE)
        val coreState: StateFlow<CoreState> = _coreState.asStateFlow()
    }

    private var controller: CoreController? = null
    private var isRunning = false
    private var isInitialized = false
    private var vpnFileDescriptor: Int = -1
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // CallbackHandler برای دریافت رویدادها از هسته
    private inner class CoreCallback : CoreCallbackHandler {
        override fun onEmitStatus(status: Long, message: String): Long {
            Log.d(TAG, "onEmitStatus: $status - $message")
            // وضعیت را به StateFlow ارسال می‌کنیم
            when (status) {
                0L -> _coreState.value = CoreState.IDLE
                1L -> _coreState.value = CoreState.CONNECTING
                2L -> _coreState.value = CoreState.CONNECTED
                3L -> _coreState.value = CoreState.DISCONNECTED
                4L -> _coreState.value = CoreState.ERROR
            }
            return 0
        }

        override fun startup(): Long {
            Log.d(TAG, "startup: Core is starting up")
            return 0
        }

        override fun shutdown(): Long {
            Log.d(TAG, "shutdown: Core is shutting down")
            _coreState.value = CoreState.DISCONNECTED
            return 0
        }
    }

    /**
     * مقداردهی اولیه‌ی هسته
     * باید قبل از startV2Ray صدا زده شود
     */
    suspend fun initialize(workingDir: String = context.filesDir.absolutePath): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (isInitialized) return@withContext Result.success(Unit)

                // 1. تنظیم Context برای go.Seq
                Seq.setContext(context)
                Log.d(TAG, "Seq.setContext done")

                // 2. مقداردهی اولیه‌ی هسته
                // پارامترها: (workingDir, logLevel)
                Libv2ray.initCoreEnv(workingDir, LOG_LEVEL)
                Log.d(TAG, "Libv2ray.initCoreEnv done: $workingDir")

                // 3. ایجاد CoreController با Callback
                val callback = CoreCallback()
                controller = Libv2ray.newCoreController(callback)
                if (controller == null) {
                    throw Exception("Failed to create CoreController")
                }
                Log.d(TAG, "CoreController created successfully")

                isInitialized = true
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Initialize failed", e)
                Result.failure(e)
            }
        }

    /**
     * راه‌اندازی VPN با کانفیگ JSON
     * @param configJson کانفیگ V2Ray/Sing-Box به فرمت JSON
     * @param vpnFd فایل دیسکریپتور VPN (برای Android VpnService)
     */
    suspend fun startV2Ray(configJson: String, vpnFd: Int = -1): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // اطمینان از مقداردهی اولیه
                if (!isInitialized) {
                    initialize().getOrThrow()
                }

                val ctrl = controller ?: throw Exception("CoreController is null")

                // اگر VPN در حال اجراست، اول متوقفش کن
                if (isRunning) {
                    stopV2Ray().getOrThrow()
                }

                // تنظیم فایل دیسکریپتور VPN
                vpnFileDescriptor = vpnFd

                // راه‌اندازی هسته با کانفیگ
                // پارامترها: (configJson, fd)
                // fd = فایل دیسکریپتور VpnService (اگر -1 باشد، tun.ko استفاده می‌شود)
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

    /**
     * توقف VPN
     */
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

    /**
     * بررسی وضعیت اجرا
     */
    fun isRunning(): Boolean = isRunning && (controller?.isRunning ?: false)

    /**
     * دریافت آمار ترافیک (اختیاری)
     */
    suspend fun getStats(): V2RayStats = withContext(Dispatchers.IO) {
        try {
            val ctrl = controller
            if (ctrl != null && isRunning) {
                val up = ctrl.queryStats("outbound", "uplink")
                val down = ctrl.queryStats("outbound", "downlink")
                V2RayStats(up, down, 0)
            } else {
                V2RayStats(0, 0, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getStats failed", e)
            V2RayStats(0, 0, 0)
        }
    }

    /**
     * پاکسازی منابع
     */
    fun cleanup() {
        scope.launch {
            try {
                controller?.stopLoop()
            } catch (_: Exception) {}
            controller = null
            isRunning = false
            isInitialized = false
            vpnFileDescriptor = -1
            _coreState.value = CoreState.IDLE
            Log.d(TAG, "Cleanup done")
        }
    }
}

/**
 * وضعیت‌های اصلی هسته
 */
enum class CoreState {
    IDLE,      // آماده به کار
    CONNECTING, // در حال اتصال
    CONNECTED,  // متصل
    DISCONNECTED, // قطع
    ERROR       // خطا
}

data class V2RayStats(
    val uplink: Long,
    val downlink: Long,
    val connectionCount: Int
)
