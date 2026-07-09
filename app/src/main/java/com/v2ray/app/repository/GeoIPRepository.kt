package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.GeoIP
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "geoip")

@Singleton
class GeoIPRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val GEOIP_KEY = stringPreferencesKey("geoip_cache")
    private val cache: MutableMap<String, GeoIP> = mutableMapOf()

    suspend fun getGeoIP(ip: String): GeoIP? {
        cache[ip]?.let { return it }
        val cached = loadFromCache(ip)
        if (cached != null) {
            cache[ip] = cached
            return cached
        }
        return try {
            val geo = fetchFromApi(ip)
            if (geo != null) {
                cache[ip] = geo
                saveToCache(ip, geo)
            }
            geo
        } catch (e: Exception) { null }
    }

    private suspend fun fetchFromApi(ip: String): GeoIP? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipapi.co/$ip/json/")
            val conn = url.openConnection()
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val response = conn.getInputStream().bufferedReader().readText()
            val obj = Json.decodeFromString<Map<String, Any>>(response)
            GeoIP(
                ip = ip,
                country = obj["country_name"]?.toString() ?: "",
                countryCode = obj["country_code"]?.toString() ?: "",
                city = obj["city"]?.toString() ?: "",
                region = obj["region"]?.toString() ?: "",
                latitude = (obj["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (obj["longitude"] as? Number)?.toDouble() ?: 0.0,
                flagEmoji = getFlagEmoji(obj["country_code"]?.toString() ?: ""),
                asn = obj["asn"]?.toString() ?: "",
                org = obj["org"]?.toString() ?: ""
            )
        } catch (e: Exception) { null }
    }

    private fun getFlagEmoji(code: String): String {
        if (code.length != 2) return ""
        val first = code[0].code - 65 + 0x1F1E6
        val second = code[1].code - 65 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }

    private suspend fun loadFromCache(ip: String): GeoIP? {
        val all = context.dataStore.data.map { prefs ->
            val str = prefs[GEOIP_KEY] ?: "{}"
            json.decodeFromString<Map<String, GeoIP>>(str)
        }.firstOrNull() ?: emptyMap()
        return all[ip]
    }

    private suspend fun saveToCache(ip: String, geo: GeoIP) {
        val current = context.dataStore.data.map { prefs ->
            val str = prefs[GEOIP_KEY] ?: "{}"
            json.decodeFromString<MutableMap<String, GeoIP>>(str)
        }.firstOrNull()?.toMutableMap() ?: mutableMapOf()
        current[ip] = geo
        context.dataStore.edit { prefs ->
            prefs[GEOIP_KEY] = json.encodeToString(current)
        }
    }
}
