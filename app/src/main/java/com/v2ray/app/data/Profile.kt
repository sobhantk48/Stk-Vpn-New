package com.v2ray.app.data

import android.util.Base64
import java.io.Serializable
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.json.*

@KSerializable
data class Profile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String,            // VLESS, VMESS, TROJAN, SHADOWSOCKS
    val address: String,
    val port: Int,
    val uuid: String = "",
    val security: String = "auto",
    val encryption: String = "none",
    val flow: String = "",
    val sni: String = "",
    val customSni: String = "",
    val fingerprint: String = "chrome",
    val realityPublicKey: String = "",
    val realityShortId: String = "",
    val selected: Boolean = false,
    val ping: Int = 0,
    val country: String = "",
    val city: String = "",
    // فیلدهای جدید برای transport
    val network: String = "tcp",    // tcp, ws, grpc, etc.
    val path: String = "",
    val host: String = "",          // Host header for WebSocket
    val alpn: String = "",
    val allowInsecure: Boolean = false
) : Serializable {

    fun getEffectiveSni(): String {
        return if (customSni.isNotBlank()) customSni else sni
    }

    fun toV2RayConfig(): String {
        val outbound = when (type.uppercase()) {
            "VLESS" -> buildVlessJson()
            "VMESS" -> buildVmessJson()
            "TROJAN" -> buildTrojanJson()
            "SHADOWSOCKS" -> buildShadowsocksJson()
            else -> buildVlessJson()
        }
        return outbound.toString()
    }

    private fun buildVlessJson() = buildJsonObject {
        put("protocol", "vless")
        put("settings", buildJsonObject {
            put("vnext", JsonArray(listOf(
                buildJsonObject {
                    put("address", address)
                    put("port", port)
                    put("users", JsonArray(listOf(
                        buildJsonObject {
                            put("id", uuid.ifEmpty { "00000000-0000-0000-0000-000000000000" })
                            put("flow", flow.ifEmpty { "none" })
                            put("encryption", "none")
                        }
                    )))
                }
            )))
        })

        put("streamSettings", buildJsonObject {
            put("network", network.ifEmpty { "tcp" })

            // تنظیمات TLS
            if (sni.isNotBlank() || customSni.isNotBlank()) {
                put("security", "tls")
                put("tlsSettings", buildJsonObject {
                    put("serverName", getEffectiveSni().ifEmpty { address })
                    put("fingerprint", fingerprint)
                    if (allowInsecure) put("allowInsecure", true)
                })
            }

            // تنظیمات Transport (WebSocket)
            if (network == "ws") {
                put("wsSettings", buildJsonObject {
                    put("path", path.ifEmpty { "/" })
                    if (host.isNotBlank()) put("headers", buildJsonObject {
                        put("Host", host)
                    })
                })
            }
        })
    }

    // سایر توابع مشابه قبل...
    private fun buildVmessJson() = buildJsonObject { /* ... */ }
    private fun buildTrojanJson() = buildJsonObject { /* ... */ }
    private fun buildShadowsocksJson() = buildJsonObject { /* ... */ }

    companion object {
        fun fromLink(link: String): Profile? {
            return try {
                when {
                    link.startsWith("vless://") -> parseVless(link)
                    link.startsWith("vmess://") -> parseVmess(link)
                    link.startsWith("trojan://") -> parseTrojan(link)
                    link.startsWith("ss://") -> parseShadowsocks(link)
                    else -> null
                }
            } catch (_: Exception) { null }
        }

        private fun parseVless(link: String): Profile {
            // پارس کردن کامل vless با پشتیبانی از ws و sni و host و path
            val uri = android.net.Uri.parse(link)
            val uuid = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            val query = uri.query ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to android.net.Uri.decode(parts[1])
                else "" to ""
            }

            val sni = params["sni"] ?: host
            val network = params["type"] ?: "tcp"
            val path = params["path"] ?: "/"
            val hostHeader = params["host"] ?: ""
            val fp = params["fp"] ?: "chrome"
            val flow = params["flow"] ?: ""
            val security = params["security"] ?: "none"

            return Profile(
                name = params["ps"] ?: host,
                type = "VLESS",
                address = host,
                port = port,
                uuid = uuid,
                sni = sni,
                fingerprint = fp,
                flow = flow,
                network = network,
                path = path,
                host = hostHeader,
                allowInsecure = security == "none"
            )
        }

        // سایر parseها مشابه قبل با اضافه کردن فیلدهای جدید
        private fun parseVmess(link: String): Profile { /* ... */ }
        private fun parseTrojan(link: String): Profile { /* ... */ }
        private fun parseShadowsocks(link: String): Profile { /* ... */ }
    }
}
