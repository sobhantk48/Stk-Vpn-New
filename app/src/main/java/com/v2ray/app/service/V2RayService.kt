package com.v2ray.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.v2ray.app.MainActivity
import com.v2ray.app.R
import com.v2ray.app.data.ConnectionState
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.utils.Logger
import com.v2ray.app.v2ray.SingBoxManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class V2RayService : VpnService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var singBoxManager: SingBoxManager
    private var vpnInterface: ParcelFileDescriptor? = null
    private var currentProfile: Profile? = null

    companion object {
        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "v2ray_channel"

        private val _state = MutableStateFlow(ConnectionState())
        val state: StateFlow<ConnectionState> = _state.asStateFlow()

        fun start(ctx: Context, profile: Profile) {
            Logger.writeLog("V2RayService start: ${profile.name}")
            _state.value = ConnectionState(
                status = ConnectionStatus.CONNECTING,
                currentProfile = profile
            )
            val intent = Intent(ctx, V2RayService::class.java)
            intent.putExtra("profile", profile as java.io.Serializable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        fun stop(ctx: Context) {
            Logger.writeLog("V2RayService stop requested")
            ctx.stopService(Intent(ctx, V2RayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        singBoxManager = SingBoxManager(this)
        createChannel()
        startForeground(NOTIF_ID, buildNotification("Initializing..."))
        Logger.writeLog("V2RayService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (intent?.getSerializableExtra("profile") as? Profile)?.let { profile ->
            currentProfile = profile
            connect(profile)
        }
        return START_STICKY
    }

    private fun connect(profile: Profile) {
        scope.launch {
            try {
                updateNotification("Connecting to ${profile.name}...")
                _state.value = _state.value.copy(
                    status = ConnectionStatus.CONNECTING,
                    currentProfile = profile
                )

                // 1. ایجاد VPN interface
                val vpnBuilder = Builder()
                    .setSession(profile.name)
                    .addAddress("10.0.0.1", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("1.1.1.1")
                    .setMtu(1500)
                    .setBlocking(true)

                val pfd = vpnBuilder.establish()
                this@V2RayService.vpnInterface = pfd

                // ✅ استفاده از safe call و سپس بررسی null
                val fd = pfd?.fd
                if (fd == null || fd <= 0) {
                    throw Exception("VPN interface is null or invalid")
                }

                // 2. مقداردهی اولیه هسته
                val initResult = singBoxManager.initialize()
                if (initResult.isFailure) {
                    val err = initResult.exceptionOrNull()
                    handleError(err?.message ?: "Initialization failed")
                    return@launch
                }

                // 3. راه‌اندازی VPN با کانفیگ
                val configJson = profile.toV2RayConfig()
                val result = singBoxManager.startV2Ray(configJson, fd)

                if (result.isSuccess) {
                    updateNotification("Connected to ${profile.name}")
                    _state.value = _state.value.copy(
                        status = ConnectionStatus.CONNECTED,
                        currentProfile = profile
                    )
                    Logger.writeLog("V2Ray connected successfully")
                } else {
                    val err = result.exceptionOrNull()
                    handleError(err?.message ?: "Connection failed")
                }
            } catch (e: Exception) {
                Logger.writeError("Connect error", e)
                handleError(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun handleError(message: String) {
        Logger.writeError("Connection error: $message")
        updateNotification("Error: $message")
        _state.value = _state.value.copy(
            status = ConnectionStatus.ERROR,
            errorMessage = message
        )
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    private fun disconnect() {
        scope.launch {
            try {
                singBoxManager.stopV2Ray()
                vpnInterface?.close()
                vpnInterface = null
                updateNotification("Disconnected")
                _state.value = _state.value.copy(
                    status = ConnectionStatus.DISCONNECTED,
                    errorMessage = null
                )
                Logger.writeLog("V2RayService disconnected")
            } catch (e: Exception) {
                Logger.writeError("Disconnect error", e)
            } finally {
                stopSelf()
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "V2RAY STK",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("V2RAY STK")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(true)
            .build()

    private fun updateNotification(text: String) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIF_ID, buildNotification(text))
    }

    override fun onDestroy() {
        runBlocking {
            try {
                singBoxManager.stopV2Ray()
                vpnInterface?.close()
                vpnInterface = null
            } catch (_: Exception) {}
        }
        singBoxManager.cleanup()
        super.onDestroy()
        Logger.writeLog("V2RayService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
