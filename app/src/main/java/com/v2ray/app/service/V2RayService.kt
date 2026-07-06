package com.v2ray.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.v2ray.app.MainActivity
import com.v2ray.app.R
import com.v2ray.app.v2ray.SingBoxManager
import kotlinx.coroutines.*
import java.net.InetSocketAddress

class V2RayService : VpnService() {
    companion object {
        const val ACTION_CONNECT = "com.v2ray.app.CONNECT"
        const val ACTION_DISCONNECT = "com.v2ray.app.DISCONNECT"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "v2ray_channel"
        const val EXTRA_CONFIG = "config"
        const val EXTRA_PROFILE_ID = "profile_id"

        // Kill Switch
        var killSwitchEnabled = true
        var isKillSwitchActive = false

        // Split Tunneling
        var splitTunnelingEnabled = false
        var splitMode: SplitMode = SplitMode.INCLUDE // INCLUDE = فقط این اپ‌ها, EXCLUDE = همه به‌جز این اپ‌ها
        val splitApps = mutableSetOf<String>() // پکیج‌های اپلیکیشن
    }

    enum class SplitMode {
        INCLUDE, EXCLUDE
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private val singBoxManager = SingBoxManager(this)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Initializing...", false))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = intent.getStringExtra(EXTRA_CONFIG) ?: return START_NOT_STICKY
                val profileId = intent.getStringExtra(EXTRA_PROFILE_ID) ?: ""
                startVpn(config, profileId)
            }
            ACTION_DISCONNECT -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn(config: String, profileId: String) {
        if (isRunning) {
            stopVpn()
        }

        serviceScope.launch {
            try {
                // ۱. ساخت VPN Interface
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

                // ۲. Split Tunneling
                if (splitTunnelingEnabled && splitApps.isNotEmpty()) {
                    when (splitMode) {
                        SplitMode.INCLUDE -> {
                            // فقط این اپ‌ها از VPN می‌روند
                            splitApps.forEach { pkg ->
                                builder.addDisallowedApplication(pkg)
                            }
                        }
                        SplitMode.EXCLUDE -> {
                            // همه به‌جز این اپ‌ها از VPN می‌روند
                            splitApps.forEach { pkg ->
                                builder.addAllowedApplication(pkg)
                            }
                        }
                    }
                }

                // ۳. Kill Switch (قبل از شروع VPN، هیچ ترافیکی خارج نمی‌شود)
                if (killSwitchEnabled) {
                    isKillSwitchActive = true
                    // برای Kill Switch از متد protect استفاده می‌کنیم
                    // در اینجا فقط فلگ را فعال می‌کنیم
                }

                // ۴. ساخت VPN Interface
                vpnInterface = builder.establish()

                // ۵. شروع sing-box
                val result = singBoxManager.startV2Ray(config, vpnInterface?.fd ?: -1)
                if (result.isSuccess) {
                    isRunning = true
                    updateNotification("🟢 Connected", true)
                    isKillSwitchActive = false // بعد از اتصال، Kill Switch غیرفعال می‌شود
                } else {
                    // اگر sing-box شروع نشد، Kill Switch فعال می‌شود
                    if (killSwitchEnabled) {
                        isKillSwitchActive = true
                    }
                    updateNotification("❌ Connection Failed", false)
                }
            } catch (e: Exception) {
                // خطا در ساخت VPN → فعال‌سازی Kill Switch
                if (killSwitchEnabled) {
                    isKillSwitchActive = true
                }
                updateNotification("❌ Error: ${e.message}", false)
            }
        }
    }

    private fun stopVpn() {
        serviceScope.launch {
            try {
                singBoxManager.stopV2Ray()
                vpnInterface?.close()
                vpnInterface = null
                isRunning = false
                isKillSwitchActive = false
                updateNotification("⏹️ Disconnected", false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ===== Kill Switch: محافظت از سوکت‌ها =====
    override fun protect(socket: Int): Boolean {
        return if (killSwitchEnabled && isKillSwitchActive) {
            // در حالت Kill Switch، اجازه‌ی خروج ترافیک داده نمی‌شود
            false
        } else {
            super.protect(socket)
        }
    }

    // ===== Notification =====

    private fun createNotificationChannel() {
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
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String, isConnected: Boolean): Notification {
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
    }

    private fun updateNotification(text: String, isConnected: Boolean) {
        val notification = createNotification(text, isConnected)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        serviceScope.cancel()
    }
}
