package com.v2ray.app.data

import android.util.Base64
import android.net.Uri
import java.io.Serializable
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.json.*
import java.io.ByteArrayInputStream
import java.util.zip.InflaterInputStream

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
    val wgReserved: String = "",
    // AWG fields (AmneziaWG)
    val awgJc: Int? = null,
    val awgJmin: Int? = null,
    val awgJmax: Int? = null,
    val awgS1: Int? = null,
    val awgS2: Int? = null,
    val awgS3: Int? = null,
    val awgS4: Int? = null,
    val awgH1: String? = null,
    val awgH2: String? = null,
    val awgH3: String? = null,
    val awgH4: String? = null,
    val awgI1: String? = null,
    val awgI2: String? = null,
    val awgI3: String? = null,
    val awgI4: String? = null,
    val awgI5: String? = null,
    // Domain Fronting
    val frontingDomain: String = ""
) : Serializable {

    fun getEffectiveSni(): String {
        return if (customSni.isNotBlank()) customSni else sni
    }

    fun getEffectiveHost(): String {
        return if (frontingDomain.isNotBlank()) frontingDomain else host
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
            "AWG" -> buildAwgJson()
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

    private fun buildAwgJson(): JsonObject {
        return buildJsonObject {
            put("protocol", "wireguard") // AWG همان WireGuard است با پارامترهای اضافی
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
                        put("mtu", if (wgMtu > 0 && wgMtu <= 1280) wgMtu else 1280) // AWG MTU clamp
                        // AWG پارامترهای خاص
                        awgJc?.let { put("jc", it) }
                        awgJmin?.let { put("jmin", it) }
                        awgJmax?.let { put("jmax", it) }
                        awgS1?.let { put("s1", it) }
                        awgS2?.let { put("s2", it) }
                        awgS3?.let { put("s3", it) }
                        awgS4?.let { put("s4", it) }
                        awgH1?.takeIf { it.isNotBlank() }?.let { put("h1", it) }
                        awgH2?.takeIf { it.isNotBlank() }?.let { put("h2", it) }
                        awgH3?.takeIf { it.isNotBlank() }?.let { put("h3", it) }
                        awgH4?.takeIf { it.isNotBlank() }?.let { put("h4", it) }
                        awgI1?.takeIf { it.isNotBlank() }?.let { put("i1", it) }
                        awgI2?.takeIf { it.isNotBlank() }?.let { put("i2", it) }
                        awgI3?.takeIf { it.isNotBlank() }?.let { put("i3", it) }
                        awgI4?.takeIf { it.isNotBlank() }?.let { put("i4", it) }
                        awgI5?.takeIf { it.isNotBlank() }?.let { put("i5", it) }
                        // Domain Fronting: اگر frontingDomain تنظیم شده باشد، Host Header را تغییر می‌دهیم
                        if (frontingDomain.isNotBlank()) {
                            put("host", frontingDomain)
                        }
                    }
                )))
            })
        }
    }

    private fun buildStreamSettings(): JsonObject {
        return buildJsonObject {
            put("network", network.ifEmpty { "tcp" })

            // Domain Fronting: اگر frontingDomain تنظیم شده باشد، SNI را با آن جایگزین می‌کنیم
            val effectiveSni = if (frontingDomain.isNotBlank()) frontingDomain else getEffectiveSni()

            if (sni.isNotBlank() || customSni.isNotBlank() || frontingDomain.isNotBlank()) {
                put("security", "tls")
                put("tlsSettings", buildJsonObject {
                    put("serverName", effectiveSni.ifEmpty { address })
                    put("fingerprint", fingerprint)
                    if (allowInsecure) put("allowInsecure", true)
                })
            }

            if (network == "ws") {
                put("wsSettings", buildJsonObject {
                    put("path", path.ifEmpty { "/" })
                    if (host.isNotBlank() || frontingDomain.isNotBlank()) {
                        put("headers", buildJsonObject {
                            if (frontingDomain.isNotBlank()) {
                                put("Host", frontingDomain)
                            } else if (host.isNotBlank()) {
                                put("Host", host)
                            }
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
                    link.startsWith("awg://") -> parseAwg(link)
                    link.startsWith("vpn://") -> parseAmneziaLink(link)
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

        private fun parseAwg(link: String): Profile {
            val uri = Uri.parse(link.replace("awg://", "http://"))
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
            val mtu = params["mtu"]?.toIntOrNull()?.let { if (it > 1280) 1280 else it } ?: 1280

            // پارامترهای AWG
            val jc = params["jc"]?.toIntOrNull()
            val jmin = params["jmin"]?.toIntOrNull()
            val jmax = params["jmax"]?.toIntOrNull()
            val s1 = params["s1"]?.toIntOrNull()
            val s2 = params["s2"]?.toIntOrNull()
            val s3 = params["s3"]?.toIntOrNull()
            val s4 = params["s4"]?.toIntOrNull()
            val h1 = params["h1"]?.takeIf { it.isNotBlank() }
            val h2 = params["h2"]?.takeIf { it.isNotBlank() }
            val h3 = params["h3"]?.takeIf { it.isNotBlank() }
            val h4 = params["h4"]?.takeIf { it.isNotBlank() }
            val i1 = params["i1"]?.takeIf { it.isNotBlank() }
            val i2 = params["i2"]?.takeIf { it.isNotBlank() }
            val i3 = params["i3"]?.takeIf { it.isNotBlank() }
            val i4 = params["i4"]?.takeIf { it.isNotBlank() }
            val i5 = params["i5"]?.takeIf { it.isNotBlank() }

            return Profile(
                name = fragment.ifEmpty { host },
                type = "AWG",
                address = host,
                port = port,
                wgPrivateKey = privateKey,
                wgPeerPublicKey = publicKey,
                wgLocalAddress = localAddress,
                wgMtu = mtu,
                awgJc = jc,
                awgJmin = jmin,
                awgJmax = jmax,
                awgS1 = s1,
                awgS2 = s2,
                awgS3 = s3,
                awgS4 = s4,
                awgH1 = h1,
                awgH2 = h2,
                awgH3 = h3,
                awgH4 = h4,
                awgI1 = i1,
                awgI2 = i2,
                awgI3 = i3,
                awgI4 = i4,
                awgI5 = i5
            )
        }

        private fun parseAmneziaLink(link: String): Profile? {
            try {
                val b64 = link.substringAfter("vpn://")
                val bytes = Base64.decode(b64, Base64.URL_SAFE)
                var jsonString: String? = null
                if (bytes.size > 4) {
                    val claimed = ((bytes[0].toInt() and 0xFF) shl 24) or
                            ((bytes[1].toInt() and 0xFF) shl 16) or
                            ((bytes[2].toInt() and 0xFF) shl 8) or
                            (bytes[3].toInt() and 0xFF)
                    if (claimed > 0 && claimed < 4 * 1024 * 1024) {
                        try {
                            val inflater = InflaterInputStream(ByteArrayInputStream(bytes, 4, bytes.size - 4))
                            jsonString = inflater.bufferedReader().use { it.readText() }
                        } catch (_: Exception) { }
                    }
                }
                if (jsonString == null) {
                    jsonString = String(bytes, Charsets.UTF_8)
                }
                if (jsonString == null) return null

                val root = Json.parseToJsonElement(jsonString).jsonObject
                val containers = root["containers"]?.jsonArray ?: return null
                for (container in containers) {
                    val obj = container.jsonObject
                    val awg = obj["awg"]?.jsonObject
                    val wg = obj["wireguard"]?.jsonObject
                    val protoObj = awg ?: wg ?: continue
                    val lastConfig = protoObj["last_config"]
                    val ini: String? = when (lastConfig) {
                        is JsonPrimitive -> lastConfig.content
                        is JsonObject -> lastConfig["config"]?.jsonPrimitive?.content
                        else -> null
                    }
                    if (ini != null && ini.contains("[Interface]") && ini.contains("[Peer]")) {
                        return parseAwgIni(ini, root)
                    }
                }
                return null
            } catch (_: Exception) {
                return null
            }
        }

        private fun parseAwgIni(ini: String, root: JsonObject? = null): Profile? {
            try {
                val lines = ini.lines()
                var address = ""
                var privateKey = ""
                var mtu = 1280
                var jc: Int? = null
                var jmin: Int? = null
                var jmax: Int? = null
                var s1: Int? = null
                var s2: Int? = null
                var s3: Int? = null
                var s4: Int? = null
                var h1: String? = null
                var h2: String? = null
                var h3: String? = null
                var h4: String? = null
                var i1: String? = null
                var i2: String? = null
                var i3: String? = null
                var i4: String? = null
                var i5: String? = null
                var endpointHost = ""
                var endpointPort = 0
                var publicKey = ""
                var preSharedKey = ""

                var inInterface = false
                var inPeer = false
                for (line in lines) {
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("[Interface]") -> {
                            inInterface = true
                            inPeer = false
                        }
                        trimmed.startsWith("[Peer]") -> {
                            inInterface = false
                            inPeer = true
                        }
                        trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";") -> continue
                        else -> {
                            val (key, value) = trimmed.split("=", limit = 2).map { it.trim() }
                            when {
                                inInterface -> {
                                    when (key) {
                                        "Address" -> address = value
                                        "PrivateKey" -> privateKey = value
                                        "MTU" -> mtu = value.toIntOrNull()?.let { if (it > 1280) 1280 else it } ?: 1280
                                        "Jc" -> jc = value.toIntOrNull()
                                        "Jmin" -> jmin = value.toIntOrNull()
                                        "Jmax" -> jmax = value.toIntOrNull()
                                        "S1" -> s1 = value.toIntOrNull()
                                        "S2" -> s2 = value.toIntOrNull()
                                        "S3" -> s3 = value.toIntOrNull()
                                        "S4" -> s4 = value.toIntOrNull()
                                        "H1" -> h1 = value
                                        "H2" -> h2 = value
                                        "H3" -> h3 = value
                                        "H4" -> h4 = value
                                        "I1" -> i1 = value
                                        "I2" -> i2 = value
                                        "I3" -> i3 = value
                                        "I4" -> i4 = value
                                        "I5" -> i5 = value
                                    }
                                }
                                inPeer -> {
                                    when (key) {
                                        "PublicKey" -> publicKey = value
                                        "PresharedKey" -> preSharedKey = value
                                        "Endpoint" -> {
                                            val parts = value.split(":")
                                            if (parts.size == 2) {
                                                endpointHost = parts[0]
                                                endpointPort = parts[1].toIntOrNull() ?: 0
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (privateKey.isBlank() || publicKey.isBlank() || endpointHost.isBlank()) {
                    return null
                }

                val name = root?.get("description")?.jsonPrimitive?.content ?: endpointHost
                val dns1 = root?.get("dns1")?.jsonPrimitive?.content ?: ""
                val dns2 = root?.get("dns2")?.jsonPrimitive?.content ?: ""

                if (address.contains("\$PRIMARY_DNS") && dns1.isNotBlank()) {
                    address = address.replace("\$PRIMARY_DNS", dns1)
                }
                if (address.contains("\$SECONDARY_DNS") && dns2.isNotBlank()) {
                    address = address.replace("\$SECONDARY_DNS", dns2)
                }

                return Profile(
                    name = name,
                    type = "AWG",
                    address = endpointHost,
                    port = if (endpointPort > 0) endpointPort else 51820,
                    wgPrivateKey = privateKey,
                    wgPeerPublicKey = publicKey,
                    wgPreSharedKey = preSharedKey,
                    wgLocalAddress = address,
                    wgMtu = mtu,
                    awgJc = jc,
                    awgJmin = jmin,
                    awgJmax = jmax,
                    awgS1 = s1,
                    awgS2 = s2,
                    awgS3 = s3,
                    awgS4 = s4,
                    awgH1 = h1,
                    awgH2 = h2,
                    awgH3 = h3,
                    awgH4 = h4,
                    awgI1 = i1,
                    awgI2 = i2,
                    awgI3 = i3,
                    awgI4 = i4,
                    awgI5 = i5
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}
