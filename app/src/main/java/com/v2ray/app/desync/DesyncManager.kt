package com.v2ray.app.desync

import android.util.Log
import java.net.Socket
import kotlinx.coroutines.*

/**
 * مدیریت کلی Desync و Fragment
 * معادل desync.Config و desync.WrapConn در Go
 */
class DesyncManager {
    companion object {
        private const val TAG = "DesyncManager"
    }

    data class Config(
        val enableFragment: Boolean = false,
        val sniChunk: Int = 3,
        val fragmentDelay: Long = 500,
        // Fake Injection غیرفعال (نیاز به root و raw socket)
        val enableFakeInjection: Boolean = false
    )

    private var currentConfig = Config()
    private var isEnabled = false

    fun enable(config: Config = Config()) {
        currentConfig = config
        isEnabled = true
        Log.d(TAG, "Desync enabled: fragment=${config.enableFragment}, sniChunk=${config.sniChunk}")
    }

    fun disable() {
        isEnabled = false
        Log.d(TAG, "Desync disabled")
    }

    fun isEnabled(): Boolean = isEnabled

    fun getConfig(): Config = currentConfig

    /**
     * تابع کمکی برای ارسال داده با Fragment
     * می‌توان از این در Socket استفاده کرد
     */
    suspend fun writeWithFragment(socket: Socket, data: ByteArray): Int {
        if (!isEnabled || !currentConfig.enableFragment) {
            socket.getOutputStream().write(data)
            socket.getOutputStream().flush()
            return data.size
        }
        return FragmentManager.writeWithFragment(
            socket.getOutputStream(),
            data,
            FragmentManager.Config(
                enableFragment = currentConfig.enableFragment,
                sniChunk = currentConfig.sniChunk,
                fragmentDelay = currentConfig.fragmentDelay
            )
        )
    }

    /**
     * بررسی اینکه آیا داده ClientHello است یا خیر
     */
    fun isClientHello(data: ByteArray): Boolean = FragmentManager.findSNI(data) != null

    /**
     * گرفتن SNI از ClientHello
     */
    fun extractSNI(data: ByteArray): String? = FragmentManager.findSNI(data)?.third
}
