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
    val allowInsecure: Boolean = false,
    // Hysteria2 fields
    val hysteriaProtocolVersion: Int = 2,
    val hysteriaAuthPayload: String = "",
    val hysteriaObfs: String = "",
    val hysteriaSni: String = "",
    val hysteriaAllowInsecure: Boolean = false,
    val hysteriaUploadMbps: Int = 0,
    val hysteriaDownloadMbps: Int = 0,
    val hysteriaServerPorts: String = "",
    // TUIC fields
    val tuicToken: String = "",
    val tuicUuid: String = "",
    val tuicCongestionController: String = "cubic",
    val tuicUdpRelayMode: String = "native",
    val tuicReduceRTT: Boolean = false,
    val tuicDisableSNI: Boolean = false,
    // WireGuard fields
    val wgLocalAddress: String = "",
    val wgPrivateKey: String = "",
    val wgPeerPublicKey: String = "",
    val wgPreSharedKey: String = "",
    val wgMtu: Int = 1420,
    val wgReserved: String = ""
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
            "HYSTERIA2" -> buildHysteria2Json()
            "TUIC" -> buildTuicJson()
            "WIREGUARD" -> buildWireGuardJson()
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

    private fun buildHysteria2Json(): JsonObject {
        return buildJsonObject {
            put("protocol", "hysteria2")
            put("settings", buildJsonObject {
                put("servers", JsonArray(listOf(
                    buildJsonObject {
                        put("address", address)
                        put("port", port)
                        put("password", hysteriaAuthPayload.ifEmpty { uuid })
                        put("sni", hysteriaSni.ifEmpty { address })
                        put("allowInsecure", hysteriaAllowInsecure)
                        if (hysteriaObfs.isNotBlank()) {
                            put("obfs", hysteriaObfs)
                        }
                        if (hysteriaUploadMbps > 0) {
                            put("uploadMbps", hysteriaUploadMbps)
                        }
                        if (hysteriaDownloadMbps > 0) {
                            put("downloadMbps", hysteriaDownloadMbps)
                        }
                    }
                )))
            })
        }
    }

    private fun buildTuicJson(): JsonObject {
        return buildJsonObject {
            put("protocol", "tuic")
            put("settings", buildJsonObject {
                put("servers", JsonArray(listOf(
                    buildJsonObject {
                        put("address", address)
                        put("port", port)
                        put("uuid", tuicUuid.ifEmpty { uuid })
                        put("password", tuicToken.ifEmpty { "password" })
                        put("sni", sni.ifEmpty { address })
                        put("allowInsecure", allowInsecure)
                        if (tuicCongestionController.isNotBlank()) {
                            put("congestionController", tuicCongestionController)
                        }
                        if (tuicUdpRelayMode.isNotBlank()) {
                            put("udpRelayMode", tuicUdpRelayMode)
                        }
                        if (tuicReduceRTT) {
                            put("reduceRTT", true)
                        }
                        if (tuicDisableSNI) {
                            put("disableSNI", true)
                        }
                    }
                )))
            })
        }
    }

    private fun buildWireGuardJson(): JsonObject {
        return buildJsonObject {
            put("protocol", "wireguard")
            put("settings", buildJsonObject {
                put("servers", JsonArray(listOf(
                    buildJsonObject {
                        put("address", address)
                        put("port", port)
                        put("localAddress", wgLocalAddress.ifEmpty { "10.0.0.2/32" })
                        put("privateKey", wgPrivateKey)
                        put("peerPublicKey", wgPeerPublicKey)
                        if (wgPreSharedKey.isNotBlank()) {
                            put("preSharedKey", wgPreSharedKey)
                        }
                        if (wgMtu > 0) {
                            put("mtu", wgMtu)
                        }
                        if (wgReserved.isNotBlank()) {
                            put("reserved", wgReserved)
                        }
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
                    link.startsWith("hysteria2://") || link.startsWith("hy2://") -> parseHysteria2(link)
                    link.startsWith("tuic://") -> parseTuic(link)
                    link.startsWith("wireguard://") -> parseWireGuard(link)
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

        private fun parseHysteria2(link: String): Profile {
            val uri = Uri.parse(link.replace("hysteria2://", "http://").replace("hy2://", "http://"))
            val userInfo = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            val query = uri.query ?: ""
            val fragment = uri.fragment ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to Uri.decode(parts[1])
                else "" to ""
            }

            val auth = userInfo
            val sni = params["sni"] ?: host
            val insecure = params["insecure"] == "1" || params["insecure"] == "true"
            val obfs = params["obfs-password"] ?: ""
            val up = params["upmbps"]?.toIntOrNull() ?: 0
            val down = params["downmbps"]?.toIntOrNull() ?: 0

            return Profile(
                name = fragment.ifEmpty { host },
                type = "HYSTERIA2",
                address = host,
                port = port,
                uuid = auth,
                sni = sni,
                allowInsecure = insecure,
                hysteriaAuthPayload = auth,
                hysteriaSni = sni,
                hysteriaAllowInsecure = insecure,
                hysteriaObfs = obfs,
                hysteriaUploadMbps = up,
                hysteriaDownloadMbps = down,
                hysteriaProtocolVersion = 2
            )
        }

        private fun parseTuic(link: String): Profile {
            val uri = Uri.parse(link.replace("tuic://", "http://"))
            val userInfo = uri.userInfo ?: ""
            val host = uri.host ?: ""
            val port = uri.port ?: 443
            val query = uri.query ?: ""
            val fragment = uri.fragment ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to Uri.decode(parts[1])
                else "" to ""
            }

            val uuid = userInfo.substringBefore(":")
            val token = userInfo.substringAfter(":", "")
            val sni = params["sni"] ?: host
            val allowInsecure = params["allow_insecure"] == "1"
            val congestion = params["congestion_control"] ?: "cubic"
            val udpRelay = params["udp_relay_mode"] ?: "native"
            val disableSNI = params["disable_sni"] == "1"
            val reduceRTT = params["reduce_rtt"] == "1"

            return Profile(
                name = fragment.ifEmpty { host },
                type = "TUIC",
                address = host,
                port = port,
                uuid = uuid,
                sni = sni,
                allowInsecure = allowInsecure,
                tuicToken = token,
                tuicUuid = uuid,
                tuicCongestionController = congestion,
                tuicUdpRelayMode = udpRelay,
                tuicDisableSNI = disableSNI,
                tuicReduceRTT = reduceRTT
            )
        }

        private fun parseWireGuard(link: String): Profile {
            val uri = Uri.parse(link.replace("wireguard://", "http://"))
            val host = uri.host ?: ""
            val port = uri.port ?: 51820
            val query = uri.query ?: ""
            val fragment = uri.fragment ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to Uri.decode(parts[1])
                else "" to ""
            }

            val privateKey = params["privateKey"] ?: ""
            val publicKey = params["publicKey"] ?: ""
            val localAddress = params["address"] ?: "10.0.0.2/32"
            val mtu = params["mtu"]?.toIntOrNull() ?: 1420
            val reserved = params["reserved"] ?: ""

            return Profile(
                name = fragment.ifEmpty { host },
                type = "WIREGUARD",
                address = host,
                port = port,
                wgPrivateKey = privateKey,
                wgPeerPublicKey = publicKey,
                wgLocalAddress = localAddress,
                wgMtu = mtu,
                wgReserved = reserved
            )
        }
    }
}
