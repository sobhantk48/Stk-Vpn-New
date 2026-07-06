package com.v2ray.app.data

import android.util.Base64
import android.net.Uri
import java.io.Serializable
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.json.*

@KSerializable
data class Profile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String,
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
    val network: String = "tcp",
    val path: String = "",
    val host: String = "",
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

    private fun buildVlessJson(): JsonObject {
        return buildJsonObject {
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
            put("streamSettings", buildStreamSettings())
        }
    }

    private fun buildVmessJson(): JsonObject {
        return buildJsonObject {
            put("protocol", "vmess")
            put("settings", buildJsonObject {
                put("vnext", JsonArray(listOf(
                    buildJsonObject {
                        put("address", address)
                        put("port", port)
                        put("users", JsonArray(listOf(
                            buildJsonObject {
                                put("id", uuid.ifEmpty { "00000000-0000-0000-0000-000000000000" })
                                put("security", "auto")
                            }
                        )))
                    }
                )))
            })
            put("streamSettings", buildStreamSettings())
        }
    }

    private fun buildTrojanJson(): JsonObject {
        return buildJsonObject {
            put("protocol", "trojan")
            put("settings", buildJsonObject {
                put("servers", JsonArray(listOf(
                    buildJsonObject {
                        put("address", address)
                        put("port", port)
                        put("password", uuid.ifEmpty { "password" })
                        put("flow", flow)
                    }
                )))
            })
            put("streamSettings", buildStreamSettings())
        }
    }

    private fun buildShadowsocksJson(): JsonObject {
        return buildJsonObject {
            put("protocol", "shadowsocks")
            put("settings", buildJsonObject {
                put("servers", JsonArray(listOf(
                    buildJsonObject {
                        put("address", address)
                        put("port", port)
                        put("method", encryption.ifEmpty { "chacha20-ietf-poly1305" })
                        put("password", uuid.ifEmpty { "password" })
                    }
                )))
            })
        }
    }

    private fun buildStreamSettings(): JsonObject {
        return buildJsonObject {
            put("network", network.ifEmpty { "tcp" })

            if (sni.isNotBlank() || customSni.isNotBlank()) {
                put("security", "tls")
                put("tlsSettings", buildJsonObject {
                    put("serverName", getEffectiveSni().ifEmpty { address })
                    put("fingerprint", fingerprint)
                    if (allowInsecure) put("allowInsecure", true)
                })
            }

            if (network == "ws") {
                put("wsSettings", buildJsonObject {
                    put("path", path.ifEmpty { "/" })
                    if (host.isNotBlank()) {
                        put("headers", buildJsonObject {
                            put("Host", host)
                        })
                    }
                })
            }
        }
    }

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
            val uri = Uri.parse(link)
            val uuid = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            val query = uri.query ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to Uri.decode(parts[1])
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

        private fun parseVmess(link: String): Profile {
            val jsonString = String(Base64.decode(link.substringAfter("vmess://"), Base64.DEFAULT))
            val obj = Json.parseToJsonElement(jsonString).jsonObject

            val add = obj["add"]?.jsonPrimitive?.content ?: ""
            val port = obj["port"]?.jsonPrimitive?.content?.toIntOrNull() ?: 443
            val id = obj["id"]?.jsonPrimitive?.content ?: ""
            val sni = obj["sni"]?.jsonPrimitive?.content ?: add
            val net = obj["net"]?.jsonPrimitive?.content ?: "tcp"
            val path = obj["path"]?.jsonPrimitive?.content ?: "/"
            val host = obj["host"]?.jsonPrimitive?.content ?: ""
            val tls = obj["tls"]?.jsonPrimitive?.content ?: ""

            return Profile(
                name = obj["ps"]?.jsonPrimitive?.content ?: "VMESS",
                type = "VMESS",
                address = add,
                port = port,
                uuid = id,
                sni = sni,
                network = net,
                path = path,
                host = host,
                allowInsecure = tls.lowercase() != "tls"
            )
        }

        private fun parseTrojan(link: String): Profile {
            val uri = Uri.parse(link)
            val password = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            val query = uri.query ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to Uri.decode(parts[1])
                else "" to ""
            }

            val sni = params["sni"] ?: host
            val fp = params["fp"] ?: "chrome"

            return Profile(
                name = "Trojan",
                type = "TROJAN",
                address = host,
                port = port,
                uuid = password,
                sni = sni,
                fingerprint = fp
            )
        }

        private fun parseShadowsocks(link: String): Profile {
            val parts = link.substringAfter("ss://").split("@")
            val methodPassword = String(Base64.decode(parts[0], Base64.DEFAULT)).split(":")
            val method = methodPassword[0]
            val password = methodPassword[1]

            val addressPort = parts[1].split(":")
            val address = addressPort[0]
            val port = addressPort.getOrNull(1)?.toIntOrNull() ?: 443

            return Profile(
                name = "Shadowsocks",
                type = "SHADOWSOCKS",
                address = address,
                port = port,
                uuid = password,
                encryption = method
            )
        }
    }
}
