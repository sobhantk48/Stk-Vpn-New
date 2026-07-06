package com.v2ray.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * نتیجه‌ی تست SNI (پینگ)
 * معادل SNIResult در Go
 */
data class SniResult(
    val host: String,
    val ip: String? = null,
    val ok: Boolean,
    val latency: Int,        // میلی‌ثانیه، -1 در صورت خطا
    val error: String? = null
)

/**
 * نتیجه‌ی تست Relay (شامل TCP, TLS, و HTTP HEAD)
 * معادل RelayResult در Go
 */
data class RelayResult(
    val tcpMs: Int = -1,
    val tlsMs: Int = -1,
    val relayMs: Int = -1,
    val ok: Boolean = false,
    val error: String? = null
)

/**
 * نتیجه‌ی تست دسته‌جمعی Mass
 * معادل MassResult در Go
 */
data class MassResult(
    val sni: String,
    val ok: Boolean,
    val tcpMs: Int,
    val tlsMs: Int,
    val totalMs: Int,
    val httpOk: Boolean,
    val error: String? = null
)

/**
 * کلاس تست سرعت و پینگ
 * معادل CheckSNI, RelayTest, MassTest در sni.go
 */
object SpeedTester {

    private const val TIMEOUT_SECONDS = 5L
    private val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

    /**
     * تست پینگ ساده با TLS handshake
     * معادل CheckSNI
     */
    suspend fun checkSni(
        host: String,
        port: Int = 443,
        timeoutSeconds: Long = TIMEOUT_SECONDS
    ): SniResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        var socket: Socket? = null
        var sslSocket: SSLSocket? = null

        try {
            // ۱. اتصال TCP
            socket = Socket()
            socket.soTimeout = (timeoutSeconds * 1000).toInt()
            socket.connect(InetSocketAddress(host, port), (timeoutSeconds * 1000).toInt())

            // ۲. گرفتن IP واقعی
            val ip = socket.inetAddress?.hostAddress

            // ۳. شروع TLS handshake با SNI مشخص
            sslSocket = sslFactory.createSocket(socket, host, port, true) as SSLSocket
            val params = SSLParameters()
            params.serverNames = listOf(javax.net.ssl.SNIHostName(host))
            sslSocket.sslParameters = params

            // ۴. تنظیم timeout
            sslSocket.soTimeout = (timeoutSeconds * 1000).toInt()
            sslSocket.startHandshake()

            // ۵. پایان موفق
            val latency = (System.currentTimeMillis() - start).toInt()
            SniResult(
                host = host,
                ip = ip,
                ok = true,
                latency = latency
            )
        } catch (e: SocketTimeoutException) {
            SniResult(
                host = host,
                ok = false,
                latency = -1,
                error = "Connection timeout"
            )
        } catch (e: IOException) {
            SniResult(
                host = host,
                ok = false,
                latency = -1,
                error = e.message?.take(60) ?: "IO error"
            )
        } catch (e: Exception) {
            SniResult(
                host = host,
                ok = false,
                latency = -1,
                error = e.message?.take(60) ?: "Unknown error"
            )
        } finally {
            sslSocket?.close()
            socket?.close()
        }
    }

    /**
     * تست کامل Relay: TCP + TLS + HTTP HEAD
     * معادل RelayTest
     */
    suspend fun relayTest(
        connectIp: String,
        connectPort: Int = 443,
        fakeSni: String,
        timeoutSeconds: Long = TIMEOUT_SECONDS
    ): RelayResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        var sslSocket: SSLSocket? = null

        try {
            val t0 = System.currentTimeMillis()

            // ۱. اتصال TCP
            socket = Socket()
            socket.soTimeout = (timeoutSeconds * 1000).toInt()
            socket.connect(InetSocketAddress(connectIp, connectPort), (timeoutSeconds * 1000).toInt())
            val tcpMs = (System.currentTimeMillis() - t0).toInt()

            val t1 = System.currentTimeMillis()

            // ۲. TLS handshake با SNI جعلی
            sslSocket = sslFactory.createSocket(socket, fakeSni, connectPort, true) as SSLSocket
            val params = SSLParameters()
            params.serverNames = listOf(javax.net.ssl.SNIHostName(fakeSni))
            sslSocket.sslParameters = params
            sslSocket.soTimeout = (timeoutSeconds * 1000).toInt()
            sslSocket.startHandshake()
            val tlsMs = (System.currentTimeMillis() - t1).toInt()

            val t2 = System.currentTimeMillis()

            // ۳. ارسال درخواست HEAD
            val request = "HEAD / HTTP/1.1\r\nHost: $fakeSni\r\nConnection: close\r\nUser-Agent: SNI-Probe/1.0\r\n\r\n"
            sslSocket.outputStream.write(request.toByteArray())
            sslSocket.outputStream.flush()

            // ۴. خواندن پاسخ (حداقل ۱ بایت)
            val buffer = ByteArray(256)
            val n = sslSocket.inputStream.read(buffer, 0, buffer.size)

            val relayMs = (System.currentTimeMillis() - t2).toInt()

            RelayResult(
                tcpMs = tcpMs,
                tlsMs = tlsMs,
                relayMs = relayMs,
                ok = n > 0,
                error = if (n <= 0) "No response" else null
            )
        } catch (e: SocketTimeoutException) {
            RelayResult(error = "Timeout")
        } catch (e: IOException) {
            RelayResult(error = e.message?.take(80) ?: "IO error")
        } catch (e: Exception) {
            RelayResult(error = e.message?.take(80) ?: "Unknown error")
        } finally {
            sslSocket?.close()
            socket?.close()
        }
    }

    /**
     * تست دسته‌جمعی SNI روی یک IP مشخص
     * معادل MassTest
     */
    suspend fun massTest(
        connectIp: String,
        connectPort: Int = 443,
        sniName: String,
        timeoutSeconds: Long = TIMEOUT_SECONDS
    ): MassResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        var sslSocket: SSLSocket? = null

        try {
            val t0 = System.currentTimeMillis()

            // ۱. اتصال TCP
            socket = Socket()
            socket.soTimeout = (timeoutSeconds * 1000).toInt()
            socket.connect(InetSocketAddress(connectIp, connectPort), (timeoutSeconds * 1000).toInt())
            val tcpMs = (System.currentTimeMillis() - t0).toInt()

            val t1 = System.currentTimeMillis()

            // ۲. TLS handshake با SNI مشخص
            sslSocket = sslFactory.createSocket(socket, sniName, connectPort, true) as SSLSocket
            val params = SSLParameters()
            params.serverNames = listOf(javax.net.ssl.SNIHostName(sniName))
            sslSocket.sslParameters = params
            sslSocket.soTimeout = (timeoutSeconds * 1000).toInt()
            sslSocket.startHandshake()
            val tlsMs = (System.currentTimeMillis() - t1).toInt()

            val t2 = System.currentTimeMillis()

            // ۳. ارسال HEAD
            val request = "HEAD / HTTP/1.1\r\nHost: $sniName\r\nConnection: close\r\n\r\n"
            sslSocket.outputStream.write(request.toByteArray())
            sslSocket.outputStream.flush()

            val buffer = ByteArray(512)
            val n = sslSocket.inputStream.read(buffer, 0, buffer.size)

            val totalMs = (System.currentTimeMillis() - t2).toInt()
            val httpOk = n > 0

            MassResult(
                sni = sniName,
                ok = httpOk,
                tcpMs = tcpMs,
                tlsMs = tlsMs,
                totalMs = totalMs,
                httpOk = httpOk,
                error = if (!httpOk) "No HTTP response" else null
            )
        } catch (e: SocketTimeoutException) {
            MassResult(sni = sniName, ok = false, tcpMs = -1, tlsMs = -1, totalMs = -1, httpOk = false, error = "Timeout")
        } catch (e: IOException) {
            MassResult(sni = sniName, ok = false, tcpMs = -1, tlsMs = -1, totalMs = -1, httpOk = false, error = e.message?.take(40) ?: "IO")
        } catch (e: Exception) {
            MassResult(sni = sniName, ok = false, tcpMs = -1, tlsMs = -1, totalMs = -1, httpOk = false, error = e.message?.take(40) ?: "Error")
        } finally {
            sslSocket?.close()
            socket?.close()
        }
    }

    /**
     * تابع کمکی برای تست پینگ همزمان چندین سرور
     */
    suspend fun pingMultiple(
        hosts: List<Pair<String, Int>>,
        timeoutSeconds: Long = TIMEOUT_SECONDS
    ): List<SniResult> = withContext(Dispatchers.IO) {
        hosts.map { (host, port) ->
            checkSni(host, port, timeoutSeconds)
        }
    }

    /**
     * تابع کمکی برای تست دسته‌جمعی SNI روی یک IP
     */
    suspend fun massScan(
        ip: String,
        port: Int = 443,
        sniList: List<String>,
        timeoutSeconds: Long = TIMEOUT_SECONDS
    ): List<MassResult> = withContext(Dispatchers.IO) {
        sniList.map { sni ->
            massTest(ip, port, sni, timeoutSeconds)
        }
    }
}
