package com.v2ray.app.fmt

import com.v2ray.app.data.Profile
import kotlinx.serialization.json.*

/**
 * Builder برای ساخت outboundهای sing-box
 * بر اساس پروتکل و تنظیمات پروفایل
 */
object OutboundBuilder {

    fun build(profile: Profile): JsonObject {
        return when (profile.type.uppercase()) {
            "VLESS" -> buildVless(profile)
            "VMESS" -> buildVmess(profile)
            "TROJAN" -> buildTrojan(profile)
            "SHADOWSOCKS" -> buildShadowsocks(profile)
            "HYSTERIA2" -> buildHysteria2(profile)
            "WIREGUARD" -> buildWireGuard(profile)
            else -> buildVless(profile)
        }
    }

    // ===== VLESS =====
    private fun buildVless(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "vless")
            put("server", profile.address)
            put("server_port", profile.port)
            put("uuid", profile.uuid)
            if (profile.flow.isNotBlank()) put("flow", profile.flow)

            // TLS
            val sni = profile.getEffectiveSni()
            if (sni.isNotBlank()) {
                put("tls", buildJsonObject {
                    put("enabled", true)
                    put("server_name", sni)
                    put("insecure", profile.allowInsecure)
                    put("fingerprint", profile.fingerprint)
                })
            }

            // Transport
            if (profile.network == "ws") {
                put("transport", buildWebSocketTransport(profile))
            } else if (profile.network == "grpc") {
                put("transport", buildGrpcTransport(profile))
            } else if (profile.network == "http") {
                put("transport", buildHttpTransport(profile))
            }
        }
    }

    // ===== VMess =====
    private fun buildVmess(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "vmess")
            put("server", profile.address)
            put("server_port", profile.port)
            put("uuid", profile.uuid)
            put("security", profile.security.ifEmpty { "auto" })
            put("alter_id", 0)

            val sni = profile.getEffectiveSni()
            if (sni.isNotBlank()) {
                put("tls", buildJsonObject {
                    put("enabled", true)
                    put("server_name", sni)
                    put("insecure", profile.allowInsecure)
                    put("fingerprint", profile.fingerprint)
                })
            }

            if (profile.network == "ws") {
                put("transport", buildWebSocketTransport(profile))
            }
        }
    }

    // ===== Trojan =====
    private fun buildTrojan(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "trojan")
            put("server", profile.address)
            put("server_port", profile.port)
            put("password", profile.uuid)

            val sni = profile.getEffectiveSni()
            if (sni.isNotBlank()) {
                put("tls", buildJsonObject {
                    put("enabled", true)
                    put("server_name", sni)
                    put("insecure", profile.allowInsecure)
                    put("fingerprint", profile.fingerprint)
                })
            }

            if (profile.network == "ws") {
                put("transport", buildWebSocketTransport(profile))
            }
        }
    }

    // ===== Shadowsocks =====
    private fun buildShadowsocks(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "shadowsocks")
            put("server", profile.address)
            put("server_port", profile.port)
            put("method", profile.encryption.ifEmpty { "chacha20-ietf-poly1305" })
            put("password", profile.uuid)
        }
    }

    // ===== Hysteria2 =====
    private fun buildHysteria2(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "hysteria2")
            put("server", profile.address)
            put("server_port", profile.port)
            put("password", profile.uuid)

            val sni = profile.getEffectiveSni()
            if (sni.isNotBlank()) {
                put("tls", buildJsonObject {
                    put("enabled", true)
                    put("server_name", sni)
                    put("insecure", profile.allowInsecure)
                    put("fingerprint", profile.fingerprint)
                })
            }
        }
    }

    // ===== WireGuard =====
    private fun buildWireGuard(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "wireguard")
            put("server", profile.address)
            put("server_port", profile.port)
            put("private_key", profile.uuid)
            if (profile.host.isNotBlank()) {
                put("peer_public_key", profile.host)
            }
            // TODO: اضافه کردن allowed_ips و reserved
        }
    }

    // ===== Transports =====
    private fun buildWebSocketTransport(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "ws")
            put("path", profile.path.ifEmpty { "/" })
            if (profile.host.isNotBlank()) {
                put("headers", buildJsonObject {
                    put("Host", profile.host)
                })
            }
        }
    }

    private fun buildGrpcTransport(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "grpc")
            put("service_name", profile.path.ifEmpty { "grpc" })
        }
    }

    private fun buildHttpTransport(profile: Profile): JsonObject {
        return buildJsonObject {
            put("type", "http")
            put("host", JsonArray(listOf(JsonPrimitive(profile.host.ifEmpty { profile.address }))))
            put("path", profile.path.ifEmpty { "/" })
        }
    }
}
