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
import com.v2ray.app.data.Profile
import com.v2ray.app.model.SplitMode
import com.v2ray.app.utils.Logger
import com.v2ray.app.v2ray.SingBoxManager
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.TunOptions
import kotlinx.coroutines.*

class V2RayService : VpnService(), PlatformInterface {
    companion object {
        private const val TAG = "V2RayService"
        const val ACTION_CONNECT = "com.v2ray.app.CONNECT"
        const val ACTION_DISCONNECT = "com.v2ray.app.DISCONNECT"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "v2ray_channel"
        const val EXTRA_CONFIG = "config"
        const val EXTRA_PROFILE = "profile"
        const val EXTRA_PROFILE_ID = "profile_id"

        var killSwitchEnabled = false
        var splitTunnelingEnabled = false
        var splitMode = SplitMode.INCLUDE
        val splitApps = mutableSetOf<String>()
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private val singBoxManager = SingBoxManager(this, this) // ارسال this به عنوان PlatformInterface
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
                    val profile = intent.getSerializableExtra(EXTRA_PROFILE) as? Profile
                    val config = intent.getStringExtra(EXTRA_CONFIG)
                    val profileId = intent.getStringExtra(EXTRA_PROFILE_ID) ?: ""

                    if (profile != null) {
                        Logger.i("Received CONNECT with Profile: ${profile.name}")
                        startVpn(profile = profile, profileId = profileId)
                    } else if (config != null) {
                        Logger.i("Received CONNECT with config (fallback)")
                        startVpn(config = config, profileId = profileId)
                    } else {
                        Logger.e("No profile or config provided")
                        return START_NOT_STICKY
                    }
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

    // ================== PlatformInterface Implementation ==================

    override fun openTun(options: TunOptions): Int {
        if (prepare(this) != null) {
            Logger.e("VPN permission not granted")
            return -1
        }

        val builder = Builder()
            .setSession("V2Ray VPN")
            .setMtu(options.mtu)
            .setBlocking(true)
            .setConfigureIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        // اضافه کردن آدرس‌های IPv4
        options.inet4Address?.forEach { addr ->
            val parts = addr.split("/")
            if (parts.size == 2) {
                builder.addAddress(parts[0], parts[1].toInt())
            }
        }

        // اضافه کردن آدرس‌های IPv6
        options.inet6Address?.forEach { addr ->
            val parts = addr.split("/")
            if (parts.size == 2) {
                builder.addAddress(parts[0], parts[1].toInt())
            }
        }

        // اضافه کردن routeها
        if (options.autoRoute) {
            options.inet4RouteAddress?.forEach { route ->
                val parts = route.split("/")
                if (parts.size == 2) {
                    builder.addRoute(parts[0], parts[1].toInt())
                }
            }
            options.inet6RouteAddress?.forEach { route ->
                val parts = route.split("/")
                if (parts.size == 2) {
                    builder.addRoute(parts[0], parts[1].toInt())
                }
            }
        } else {
            builder.addRoute("0.0.0.0", 0)
        }

        // Split Tunneling
        if (splitTunnelingEnabled && splitApps.isNotEmpty()) {
            when (splitMode) {
                SplitMode.INCLUDE -> {
                    splitApps.forEach { pkg ->
                        try {
                            builder.addDisallowedApplication(pkg)
                        } catch (_: Exception) {}
                    }
                }
                SplitMode.EXCLUDE -> {
                    splitApps.forEach { pkg ->
                        try {
                            builder.addAllowedApplication(pkg)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // اضافه کردن DNS
        options.dnsServerAddress?.forEach { dns ->
            builder.addDnsServer(dns)
        }

        return try {
            val pfd = builder.establish()
            vpnInterface = pfd
            pfd?.fd ?: -1
        } catch (e: Exception) {
            Logger.e("openTun failed", e)
            -1
        }
    }

    override fun autoDetectInterfaceControl(fd: Int) {
        protect(fd)
    }

    // ================== VPN Management ==================

    private fun startVpn(profile: Profile? = null, config: String? = null, profileId: String = "") {
        if (isRunning) {
            Logger.d("VPN already running, stopping first")
            stopVpn()
        }

        serviceScope.launch {
            try {
                val finalConfig = if (profile != null) {
                    singBoxManager.buildSingBoxConfig(profile)
                } else if (config != null) {
                    config
                } else {
                    throw Exception("No config available")
                }

                Logger.i("Starting sing-box with config length: ${finalConfig.length}")
                val result = singBoxManager.startV2Ray(finalConfig, 0) // fd در PlatformInterface مدیریت می‌شود
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
        if (killSwitchEnabled && !isRunning) {
            return false
        }
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
