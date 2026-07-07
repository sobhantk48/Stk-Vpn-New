package com.v2ray.app.utils

import android.util.Log
import com.v2ray.app.data.Profile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.yaml.snakeyaml.Yaml
import java.io.StringReader

object SmartParser {
    private const val TAG = "SmartParser"

    /**
     * تشخیص و پارس هر نوع ورودی
     * @return لیست پروفایل‌های استخراج‌شده (ممکن است خالی باشد)
     */
    fun detectAndParse(input: String): List<Profile> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return emptyList()

        return when {
            // لینک‌های تک‌پروفایل
            trimmed.startsWith("vless://") ||
            trimmed.startsWith("vmess://") ||
            trimmed.startsWith("trojan://") ||
            trimmed.startsWith("ss://") -> {
                Profile.fromLink(trimmed)?.let { listOf(it) } ?: emptyList()
            }
            // Clash YAML (با کلمه‌ی proxies)
            trimmed.contains("proxies:") || trimmed.contains("proxy-groups:") -> {
                parseClashYAML(trimmed)
            }
            // v2rayN JSON (با outbounds)
            trimmed.contains("\"outbounds\"") || trimmed.contains("\"inbounds\"") -> {
                parseV2RayJSON(trimmed)
            }
            // تلاش برای تشخیص لینک‌های چندگانه (هر خط یک لینک)
            else -> {
                val lines = trimmed.split("\n", "\r\n")
                val profiles = mutableListOf<Profile>()
                for (line in lines) {
                    val link = line.trim()
                    if (link.isNotEmpty()) {
                        Profile.fromLink(link)?.let { profiles.add(it) }
                    }
                }
                profiles
            }
        }
    }

    /**
     * پارس Clash YAML و استخراج پروکسی‌ها
     */
    private fun parseClashYAML(yaml: String): List<Profile> {
        return try {
            val yamlMap = Yaml().load<StringReader>(StringReader(yaml)) as? Map<*, *>
            val proxies = yamlMap?.get("proxies") as? List<*> ?: return emptyList()

            val profiles = mutableListOf<Profile>()
            for (proxy in proxies) {
                val proxyMap = proxy as? Map<*, *> ?: continue
                val type = proxyMap["type"]?.toString() ?: continue
                val name = proxyMap["name"]?.toString() ?: "Clash"
                val server = proxyMap["server"]?.toString() ?: ""
                val port = (proxyMap["port"] as? Number)?.toInt() ?: 0
                val uuid = proxyMap["uuid"]?.toString() ?: ""
                val sni = proxyMap["sni"]?.toString() ?: ""
                val network = proxyMap["network"]?.toString() ?: "tcp"
                val path = proxyMap["path"]?.toString() ?: ""
                val host = proxyMap["host"]?.toString() ?: ""
                val tls = proxyMap["tls"] as? Boolean ?: false
                val fingerprint = proxyMap["fingerprint"]?.toString() ?: "chrome"

                // تبدیل type Clash به نوع Profile
                val profileType = when (type.lowercase()) {
                    "vless" -> "VLESS"
                    "vmess" -> "VMESS"
                    "trojan" -> "TROJAN"
                    "shadowsocks" -> "SHADOWSOCKS"
                    else -> "VLESS"
                }

                val profile = Profile(
                    name = name,
                    type = profileType,
                    address = server,
                    port = port,
                    uuid = uuid,
                    sni = sni,
                    network = network,
                    path = path,
                    host = host,
                    allowInsecure = !tls,
                    fingerprint = fingerprint
                )
                profiles.add(profile)
            }
            profiles
        } catch (e: Exception) {
            Log.e(TAG, "parseClashYAML error", e)
            emptyList()
        }
    }

    /**
     * پارس v2rayN JSON و استخراج outbound‌ها
     */
    private fun parseV2RayJSON(json: String): List<Profile> {
        return try {
            val element = Json.parseToJsonElement(json)
            val outbounds = element.jsonObject["outbounds"]?.jsonArray ?: return emptyList()

            val profiles = mutableListOf<Profile>()
            for (outbound in outbounds) {
                val obj = outbound.jsonObject
                val protocol = obj["protocol"]?.jsonPrimitive?.content ?: continue
                val settings = obj["settings"]?.jsonObject
                val vnext = settings?.get("vnext")?.jsonArray?.firstOrNull()?.jsonObject
                val address = vnext?.get("address")?.jsonPrimitive?.content ?: ""
                val port = vnext?.get("port")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                val users = vnext?.get("users")?.jsonArray?.firstOrNull()?.jsonObject
                val uuid = users?.get("id")?.jsonPrimitive?.content ?: ""
                val flow = users?.get("flow")?.jsonPrimitive?.content ?: ""
                val encryption = users?.get("encryption")?.jsonPrimitive?.content ?: "none"
                val streamSettings = obj["streamSettings"]?.jsonObject
                val network = streamSettings?.get("network")?.jsonPrimitive?.content ?: "tcp"
                val security = streamSettings?.get("security")?.jsonPrimitive?.content ?: ""
                val tlsSettings = streamSettings?.get("tlsSettings")?.jsonObject
                val sni = tlsSettings?.get("serverName")?.jsonPrimitive?.content ?: ""
                val fingerprint = tlsSettings?.get("fingerprint")?.jsonPrimitive?.content ?: "chrome"
                val wsSettings = streamSettings?.get("wsSettings")?.jsonObject
                val path = wsSettings?.get("path")?.jsonPrimitive?.content ?: ""
                val host = wsSettings?.get("headers")?.jsonObject?.get("Host")?.jsonPrimitive?.content ?: ""

                val profileType = when (protocol.lowercase()) {
                    "vless" -> "VLESS"
                    "vmess" -> "VMESS"
                    "trojan" -> "TROJAN"
                    "shadowsocks" -> "SHADOWSOCKS"
                    else -> continue
                }

                val profile = Profile(
                    name = "Imported",
                    type = profileType,
                    address = address,
                    port = port,
                    uuid = uuid,
                    flow = flow,
                    sni = sni,
                    network = network,
                    path = path,
                    host = host,
                    allowInsecure = security != "tls",
                    fingerprint = fingerprint
                )
                profiles.add(profile)
            }
            profiles
        } catch (e: Exception) {
            Log.e(TAG, "parseV2RayJSON error", e)
            emptyList()
        }
    }
}
