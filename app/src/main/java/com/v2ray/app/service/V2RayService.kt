package com.v2ray.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.v2ray.app.MainActivity
import com.v2ray.app.R
import com.v2ray.app.data.ConnectionState
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.utils.Logger
import com.v2ray.app.v2ray.SingBoxManager
import kotlinx.coroutines.*

class V2RayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var manager: SingBoxManager

    companion object {
        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "v2ray_channel"

        fun start(ctx: Context, profile: Profile) {
            Logger.writeLog("V2RayService start: ${profile.name}")
            val intent = Intent(ctx, V2RayService::class.java)
            intent.putExtra("profile", profile as java.io.Serializable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, V2RayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        manager = SingBoxManager(this)
        createChannel()
        startForeground(NOTIF_ID, buildNotification("Initializing..."))
        Logger.writeLog("V2RayService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (intent?.getSerializableExtra("profile") as? Profile)?.let { connect(it) }
        return START_STICKY
    }

    private fun connect(profile: Profile) {
        scope.launch {
            try {
                updateNotification("Connecting to ${profile.name}...")
                val result = manager.startV2Ray(profile.toV2RayConfig())
                if (result.isSuccess) {
                    updateNotification("Connected to ${profile.name}")
                } else {
                    val err = result.exceptionOrNull()
                    updateNotification("Connection failed: ${err?.message}")
                }
            } catch (e: Exception) {
                Logger.writeError("Connect error", e)
                updateNotification("Error: ${e.message}")
            }
        }
    }

    fun disconnect() {
        scope.launch {
            manager.stopV2Ray()
            updateNotification("Disconnected")
            stopSelf()
            Logger.writeLog("V2RayService disconnected")
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
        disconnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
