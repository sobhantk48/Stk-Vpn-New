package com.v2ray.app.bg

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.v2ray.app.MainActivity
import com.v2ray.app.R
import com.v2ray.app.model.SplitMode
import com.v2ray.app.utils.Logger
import com.v2ray.app.v2ray.SingBoxManager
import kotlinx.coroutines.*

class V2RayService : VpnService() {
    companion object {
        private const val TAG = "V2RayService"
        const val ACTION_CONNECT = "com.v2ray.app.CONNECT"
        const val ACTION_DISCONNECT = "com.v2ray.app.DISCONNECT"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "v2ray_channel"
        const val EXTRA_CONFIG = "config"
        const val EXTRA_PROFILE_ID = "profile_id"

        var killSwitchEnabled = false
        var splitTunnelingEnabled = false
        var splitMode = SplitMode.INCLUDE
        val splitApps = mutableSetOf<String>()
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private val singBoxManager = SingBoxManager(this)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val binder = ServiceBinder().apply {
        setStatusCallback { status ->
            Logger.i("Service status changed: $status")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i("V2RayService onCreate (bg process)")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Initializing...", false))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_CONNECT -> {
                    val config = intent.getStringExtra(EXTRA_CONFIG) ?: return START_NOT_STICKY
                    val profileId = intent.getStringExtra(EXTRA_PROFILE_ID) ?: ""
                    Logger.i("Received CONNECT action for profile: $profileId")
                    startVpn(config, profileId)
                }
                ACTION_DISCONNECT -> {
                    Logger.i("Received DISCONNECT action")
                    stopVpn()
                }
            }
        } catch (e: Exception) {
            Logger.e("Error in onStartCommand", e)
            stopVpn()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    private fun startVpn(config: String, profileId: String) {
        if (isRunning) {
            Logger.d("VPN already running, stopping first")
            stopVpn()
        }

        serviceScope.launch {
            var fd: Int = -1
            try {
                Logger.i("Building VPN interface...")
                val builder = Builder()
                    .setSession("V2Ray VPN")
                    .setMtu(1500)
                    .addAddress("10.0.0.1", 32)
                    .addRoute("0.0.0.0", 0)
                    .setBlocking(true)
                    .setConfigureIntent(
                        PendingIntent.getActivity(
                            this@V2RayService,
                            0,
                            Intent(this@V2RayService, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )

                if (splitTunnelingEnabled && splitApps.isNotEmpty()) {
                    Logger.d("Split Tunneling enabled with ${splitApps.size} apps")
                    when (splitMode) {
                        SplitMode.INCLUDE -> {
                            splitApps.forEach { pkg -> builder.addDisallowedApplication(pkg) }
                        }
                        SplitMode.EXCLUDE -> {
                            splitApps.forEach { pkg -> builder.addAllowedApplication(pkg) }
                        }
                    }
                }

                vpnInterface = builder.establish()
                fd = vpnInterface?.fd ?: -1
                Logger.i("VPN interface established with fd: $fd")

                if (fd == -1) {
                    throw Exception("VPN interface fd is invalid")
                }

                Logger.i("Starting sing-box with config: $config")
                val result = singBoxManager.startV2Ray(config, fd)
                if (result.isSuccess) {
                    isRunning = true
                    binder.setStatus("Connected")
                    mainHandler.post {
                        updateNotification("🟢 Connected", true)
                    }
                    Logger.i("V2Ray started successfully")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Logger.e("V2Ray start failed: $error", result.exceptionOrNull())
                    binder.setError(error)
                    mainHandler.post {
                        updateNotification("❌ Connection Failed: $error", false)
                    }
                }
            } catch (e: Exception) {
                Logger.e("startVpn error", e)
                binder.setError(e.message ?: "Unknown error")
                mainHandler.post {
                    updateNotification("❌ Error: ${e.message}", false)
                }
            } finally {
                if (!isRunning) {
                    try {
                        vpnInterface?.close()
                    } catch (_: Exception) {}
                    vpnInterface = null
                }
            }
        }
    }

    private fun stopVpn() {
        serviceScope.launch {
            try {
                Logger.i("Stopping VPN...")
                singBoxManager.stopV2Ray()
                vpnInterface?.close()
                vpnInterface = null
                isRunning = false
                binder.setStatus("Disconnected")
                mainHandler.post {
                    updateNotification("⏹️ Disconnected", false)
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Logger.i("VPN stopped")
            } catch (e: Exception) {
                Logger.e("stopVpn error", e)
            }
        }
    }

    override fun protect(socket: Int): Boolean {
        // در صورت فعال بودن Kill Switch و عدم اتصال، ترافیک را مسدود کن
        if (killSwitchEnabled && !isRunning) {
            return false
        }
        // در غیر این صورت، محافظت کن
        return super.protect(socket)
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "V2Ray VPN",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "V2Ray VPN Service"
                    setShowBadge(false)
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "createNotificationChannel error", e)
            Logger.e("createNotificationChannel error", e)
        }
    }

    private fun createNotification(text: String, isConnected: Boolean): Notification {
        try {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val disconnectIntent = Intent(this, V2RayService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            val disconnectPendingIntent = PendingIntent.getService(
                this, 0, disconnectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(if (isConnected) "🟢 VPN Connected" else "🔴 VPN Disconnected")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Disconnect",
                    disconnectPendingIntent
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(isConnected)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "createNotification error", e)
            Logger.e("createNotification error", e)
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(if (isConnected) "VPN Connected" else "VPN Disconnected")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
    }

    private fun updateNotification(text: String, isConnected: Boolean) {
        try {
            val notification = createNotification(text, isConnected)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "updateNotification error", e)
            Logger.e("updateNotification error", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i("V2RayService onDestroy")
        stopVpn()
        serviceScope.cancel()
    }
}
