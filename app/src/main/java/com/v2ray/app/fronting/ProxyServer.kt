package com.v2ray.app.fronting

import android.content.Context
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class ProxyServer(
    private val context: Context,
    private val port: Int = 8087
) {
    private val certManager = CertificateManager(context)
    private val dohResolver = DohResolver()
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    // قوانین Fronting: دامنه → SNI جعلی
    private val frontingRules = mapOf(
        "google.com" to "www.google.com",
        "youtube.com" to "www.google.com",
        "twitter.com" to "www.google.com",
        "facebook.com" to "www.microsoft.com",
        "instagram.com" to "www.microsoft.com",
        "github.com" to "github.githubassets.com",
        "reddit.com" to "github.githubassets.com",
        "telegram.org" to "www.google.com"
    )

    fun start(): Boolean {
        return try {
            serverSocket = ServerSocket(port)
            isRunning = true
            scope.launch { acceptLoop() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun acceptLoop() {
        while (isRunning) {
            val client = serverSocket?.accept() ?: break
            scope.launch { handleClient(client) }
        }
    }

    private suspend fun handleClient(clientSocket: Socket) = withContext(Dispatchers.IO) {
        try {
            // ۱. خواندن اولین درخواست (ClientHello)
            val buffer = ByteArray(4096)
            val inputStream = clientSocket.getInputStream()
            val bytesRead = inputStream.read(buffer)

            if (bytesRead <= 0) return@withContext

            // ۲. استخراج SNI از ClientHello (با BouncyCastle یا ساده‌تر از Host Header)
            val host = extractHost(buffer, bytesRead) ?: return@withContext

            // ۳. تعیین SNI جعلی
            val fakeSni = frontingRules[host] ?: host

            // ۴. ایجاد SSLContext با گواهی موقت
            val sslContext = certManager.getSSLContext()
            val sslFactory = sslContext.socketFactory

            // ۵. اتصال به سرور واقعی از طریق DoH
            val realIp = dohResolver.resolve(host) ?: run {
                clientSocket.close()
                return@withContext
            }

            // استفاده از SSLSocketFactory برای اتصال به سرور
            val defaultFactory = SSLSocketFactory.getDefault()
            val serverSocket = defaultFactory.createSocket() as SSLSocket
            serverSocket.connect(InetSocketAddress(realIp, 443), 10000)
            serverSocket.startHandshake()

            // ۶. شروع Relay
            relay(clientSocket, serverSocket, fakeSni)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { clientSocket.close() } catch (_: Exception) {}
        }
    }

    private fun extractHost(buffer: ByteArray, length: Int): String? {
        // اینجا باید SNI را از ClientHello استخراج کرد
        // فعلاً یک پیاده‌سازی ساده با جستجوی Host Header در HTTP
        val data = String(buffer, 0, length)
        val lines = data.split("\r\n", "\n")
        for (line in lines) {
            if (line.lowercase().startsWith("host:")) {
                return line.substringAfter(":").trim()
            }
        }
        return null
    }

    private fun relay(client: Socket, server: Socket, fakeSni: String) {
        // Relay دوطرفه
        val clientIn = client.getInputStream()
        val clientOut = client.getOutputStream()
        val serverIn = server.getInputStream()
        val serverOut = server.getOutputStream()

        // ارسال درخواست با SNI جعلی
        // اینجا باید درخواست را بازنویسی کنیم و به سرور ارسال کنیم
        // فعلاً فقط Relay ساده انجام می‌دهیم

        val scope = CoroutineScope(Dispatchers.IO)
        val job1 = scope.launch {
            try {
                serverIn.copyTo(clientOut)
            } catch (_: Exception) {}
        }
        val job2 = scope.launch {
            try {
                clientIn.copyTo(serverOut)
            } catch (_: Exception) {}
        }
        runBlocking {
            job1.join()
            job2.join()
        }
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
        scope.cancel()
    }
}
