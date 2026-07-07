package com.v2ray.app.subscription

import android.util.Base64
import com.v2ray.app.data.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object SubscriptionParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun fetchAndParse(url: String): List<Profile> = withContext(Dispatchers.IO) {
        try {
            val content = fetch(url)
            parseContent(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetch(url: String): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            response.body?.string() ?: ""
        }
    }

    private fun parseContent(content: String): List<Profile> {
        val trimmed = content.trim()
        // اگر Base64 است، decode کن
        val decoded = if (isBase64(trimmed)) {
            String(Base64.decode(trimmed, Base64.DEFAULT))
        } else {
            trimmed
        }

        val lines = decoded.split("\n", "\r\n")
        val profiles = mutableListOf<Profile>()

        for (line in lines) {
            val link = line.trim()
            if (link.isNotEmpty()) {
                Profile.fromLink(link)?.let { profiles.add(it) }
            }
        }

        return profiles
    }

    private fun isBase64(str: String): Boolean {
        return try {
            Base64.decode(str, Base64.DEFAULT)
            true
        } catch (_: Exception) {
            false
        }
    }
}
