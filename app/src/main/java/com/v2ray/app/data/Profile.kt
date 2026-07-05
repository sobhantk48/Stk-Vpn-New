package com.v2ray.app.data

import android.util.Base64
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
    val fingerprint: String = "chrome",
    val realityPublicKey: String = "",
    val realityShortId: String = "",
    val selected: Boolean = false,
    val ping: Int = 0,
    val country: String = "",
    val city: String = ""
) : Serializable {

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
            put("network", "tcp")

            if (realityPublicKey.isNotBlank()) {
                put("security", "reality")
                put("realitySettings", buildJsonObject {
                    put("serverNames", JsonArray(listOf(JsonPrimitive(sni.ifEmpty { address }))))
                    put("publicKey", realityPublicKey)
                    put("shortIds", JsonArray(listOf(JsonPrimitive(realityShortId.ifEmpty { "0000000000000000" }))))
                })
            } else {
                put("security", "tls")
                put("tlsSettings", buildJsonObject {
                    put("serverName", sni.ifEmpty { address })
                    put("fingerprint", fingerprint)
                })
            }
        })
    }

    private fun buildVmessJson() = buildJsonObject {
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
    }

    private fun buildTrojanJson() = buildJsonObject {
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
    }

    private fun buildShadowsocksJson() = buildJsonObject {
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

        fun fromJson(jsonString: String): Profile? {
            return try {
                val json = Json { ignoreUnknownKeys = true }
                val obj = json.parseToJsonElement(jsonString).jsonObject

                Profile(
                    name = obj["name"]?.jsonPrimitive?.content ?: "Imported",
                    type = obj["type"]?.jsonPrimitive?.content?.uppercase() ?: "VLESS",
                    address = obj["address"]?.jsonPrimitive?.content ?: "",
                    port = obj["port"]?.jsonPrimitive?.content?.toIntOrNull() ?: 443,
                    uuid = obj["uuid"]?.jsonPrimitive?.content ?: "",
                    sni = obj["sni"]?.jsonPrimitive?.content ?: "",
                    fingerprint = obj["fingerprint"]?.jsonPrimitive?.content ?: "chrome",
                    flow = obj["flow"]?.jsonPrimitive?.content ?: "",
                    realityPublicKey = obj["pbk"]?.jsonPrimitive?.content ?: "",
                    realityShortId = obj["sid"]?.jsonPrimitive?.content ?: ""
                )
            } catch (_: Exception) { null }
        }

        private fun parseVless(link: String): Profile {
            val parts = link.substringAfter("vless://").split("@")
            val uuid = parts[0]
            val rest = parts[1].split("?")
            val addressPort = rest[0].split(":")
            val address = addressPort[0]
            val port = addressPort.getOrNull(1)?.toIntOrNull() ?: 443

            val params = rest.getOrNull(1)?.split("&")?.associate {
                val kv = it.split("=")
                kv[0] to kv.getOrNull(1).orEmpty()
            } ?: emptyMap()

            return Profile(
                name = params["ps"] ?: address,
                type = "VLESS",
                address = address,
                port = port,
                uuid = uuid,
                sni = params["sni"] ?: "",
                fingerprint = params["fp"] ?: "chrome",
                flow = params["flow"] ?: "",
                realityPublicKey = params["pbk"] ?: "",
                realityShortId = params["sid"] ?: ""
            )
        }

        private fun parseVmess(link: String): Profile {
            val json = String(Base64.decode(link.substringAfter("vmess://"), Base64.DEFAULT))
            val obj = Json.parseToJsonElement(json).jsonObject

            return Profile(
                name = obj["ps"]?.jsonPrimitive?.content ?: "VMESS",
                type = "VMESS",
                address = obj["add"]?.jsonPrimitive?.content ?: "",
                port = obj["port"]?.jsonPrimitive?.content?.toIntOrNull() ?: 443,
                uuid = obj["id"]?.jsonPrimitive?.content ?: "",
                sni = obj["sni"]?.jsonPrimitive?.content ?: "",
                encryption = obj["method"]?.jsonPrimitive?.content ?: "auto"
            )
        }

        private fun parseTrojan(link: String): Profile {
            val parts = link.substringAfter("trojan://").split("@")
            val password = parts[0]
            val addressPort = parts[1].split(":")
            val address = addressPort[0]
            val port = addressPort.getOrNull(1)?.toIntOrNull() ?: 443

            return Profile(
                name = "Trojan",
                type = "TROJAN",
                address = address,
                port = port,
                uuid = password
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
