package com.v2ray.app.fronting

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class DohResolver {
    companion object {
        private const val DOH_URL = "https://cloudflare-dns.com/dns-query"
        private const val DOH_IP = "1.1.1.1"
        private const val TIMEOUT_SECONDS = 10L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun resolve(host: String): String? {
        // اگر IP مستقیم است، برگردان
        if (isIPAddress(host)) return host

        return try {
            val url = "$DOH_URL?name=$host&type=A"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/dns-json")
                .header("Host", "cloudflare-dns.com")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null

            val json = JSONObject(body)
            val answers = json.getJSONArray("Answer")
            for (i in 0 until answers.length()) {
                val answer = answers.getJSONObject(i)
                if (answer.getInt("type") == 1) { // A record
                    return answer.getString("data")
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isIPAddress(host: String): Boolean {
        return try {
            InetAddress.getByName(host)
            true
        } catch (_: UnknownHostException) {
            false
        }
    }
}
