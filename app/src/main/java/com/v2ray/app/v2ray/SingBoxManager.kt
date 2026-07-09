package com.v2ray.app.v2ray

import android.util.Log
import com.v2ray.app.data.Profile
import com.v2ray.app.utils.Logger
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.PlatformInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class SingBoxManager(
    private val platformInterface: PlatformInterface,
    private val dnsTransport: io.nekohasekai.libbox.LocalDNSTransport
) {
    companion object {
        private const val TAG = "SingBoxManager"
    }

    private var boxInstance: io.nekohasekai.libbox.BoxInstance? = null
    private var trafficStats: TrafficStats = TrafficStats()

    data class TrafficStats(
        var downloadBytes: Long = 0,
        var uploadBytes: Long = 0
    )

    @Synchronized
    suspend fun startV2Ray(config: String, fd: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stopV2Ray()
            val instance = io.nekohasekai.libbox.BoxInstance(config, platformInterface, dnsTransport)
            boxInstance = instance
            instance.start()
            trafficStats = TrafficStats()
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "startV2Ray failed", e)
            Result.failure(e)
        }
    }

    @Synchronized
    suspend fun stopV2Ray() = withContext(Dispatchers.IO) {
        try {
            boxInstance?.close()
            boxInstance = null
        } catch (e: Exception) {
            Logger.e(TAG, "stopV2Ray failed", e)
        }
    }

    fun getTrafficStats(): TrafficStats? {
        // در نسخه‌های جدید libbox می‌توان آمار را از instance گرفت
        // این یک پیاده‌سازی ساده است
        return trafficStats
    }

    // ================== Build Config ==================
    fun buildSingBoxConfig(profile: Profile): String {
        return when (profile.type) {
            Profile.ProtocolType.VLESS -> buildVlessConfig(profile)
            Profile.ProtocolType.VLESS_REALITY -> buildVlessRealityConfig(profile)
            Profile.ProtocolType.VMESS -> buildVmessConfig(profile)
            Profile.ProtocolType.TROJAN -> buildTrojanConfig(profile)
            Profile.ProtocolType.TROJAN_GO -> buildTrojanGoConfig(profile)
            Profile.ProtocolType.SHADOWSOCKS -> buildShadowsocksConfig(profile)
            Profile.ProtocolType.SHADOWSOCKS_R -> buildShadowsocksRConfig(profile)
            Profile.ProtocolType.HYSTERIA2 -> buildHysteria2Config(profile)
            Profile.ProtocolType.HYSTERIA -> buildHysteriaConfig(profile)
            Profile.ProtocolType.TUIC -> buildTuicConfig(profile)
            Profile.ProtocolType.WIREGUARD -> buildWireGuardConfig(profile)
            Profile.ProtocolType.AMNEZIA_WG -> buildAmneziaWGConfig(profile)
            Profile.ProtocolType.NAIVE_PROXY -> buildNaiveProxyConfig(profile)
            Profile.ProtocolType.SSH -> buildSSHConfig(profile)
            Profile.ProtocolType.SOCKS5 -> buildSocks5Config(profile)
            Profile.ProtocolType.HTTP -> buildHttpConfig(profile)
        }
    }

    // ================== Config Builders ==================
    
    private fun buildVlessConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject {
                put("disabled", true)
                put("level", "info")
                put("timestamp", true)
            })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject {
                        put("tag", "dns")
                        put("address", "1.1.1.1")
                        put("address_resolver", "dns")
                        put("strategy", "ipv4_only")
                    })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun")
                    put("tag", "tun-in")
                    put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000)
                    put("auto_route", true)
                    put("strict_route", false)
                    put("endpoint_independent_nat", true)
                    put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject {
                            put("enabled", true)
                            put("listen", "127.0.0.1")
                            put("port", 10809)
                        })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "vless")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("uuid", profile.uuid)
                    put("flow", profile.flow.ifEmpty { "xtls-rprx-vision" })
                    put("tls", buildJsonObject {
                        put("enabled", profile.security == "tls")
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    put("packet_encoding", "xudp")
                    put("multiplex", buildJsonObject {
                        put("enabled", true)
                        put("protocol", "h2mux")
                        put("max_connections", 8)
                        put("min_streams", 4)
                        put("max_streams", 16)
                    })
                })
                add(buildJsonObject {
                    put("type", "direct")
                    put("tag", "direct")
                })
                add(buildJsonObject {
                    put("type", "block")
                    put("tag", "block")
                })
                add(buildJsonObject {
                    put("type", "dns")
                    put("tag", "dns-out")
                })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject {
                        put("protocol", buildJsonArray { add("dns") })
                        put("outbound", "dns-out")
                    })
                    add(buildJsonObject {
                        put("geoip", buildJsonArray { add("private") })
                        put("outbound", "direct")
                    })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildVlessRealityConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject {
                put("disabled", true)
                put("level", "info")
                put("timestamp", true)
            })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject {
                        put("tag", "dns")
                        put("address", "1.1.1.1")
                        put("address_resolver", "dns")
                        put("strategy", "ipv4_only")
                    })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun")
                    put("tag", "tun-in")
                    put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000)
                    put("auto_route", true)
                    put("strict_route", false)
                    put("endpoint_independent_nat", true)
                    put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject {
                            put("enabled", true)
                            put("listen", "127.0.0.1")
                            put("port", 10809)
                        })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "vless")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("uuid", profile.uuid)
                    put("flow", profile.flow.ifEmpty { "xtls-rprx-vision" })
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.realityServerName.ifEmpty { "www.google.com" })
                        put("reality", buildJsonObject {
                            put("enabled", true)
                            put("public_key", profile.realityPublicKey)
                            put("short_id", profile.realityShortId)
                            put("fingerprint", profile.realityFingerprint.ifEmpty { "chrome" })
                        })
                    })
                    put("packet_encoding", "xudp")
                    put("multiplex", buildJsonObject {
                        put("enabled", true)
                        put("protocol", "h2mux")
                        put("max_connections", 8)
                        put("min_streams", 4)
                        put("max_streams", 16)
                    })
                })
                add(buildJsonObject {
                    put("type", "direct")
                    put("tag", "direct")
                })
                add(buildJsonObject {
                    put("type", "block")
                    put("tag", "block")
                })
                add(buildJsonObject {
                    put("type", "dns")
                    put("tag", "dns-out")
                })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject {
                        put("protocol", buildJsonArray { add("dns") })
                        put("outbound", "dns-out")
                    })
                    add(buildJsonObject {
                        put("geoip", buildJsonArray { add("private") })
                        put("outbound", "direct")
                    })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildVmessConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "vmess")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("uuid", profile.uuid)
                    put("security", profile.encryption.ifEmpty { "auto" })
                    put("alter_id", 0)
                    put("tls", buildJsonObject {
                        put("enabled", profile.security == "tls")
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    put("packet_encoding", "xudp")
                    put("multiplex", buildJsonObject {
                        put("enabled", true); put("protocol", "h2mux")
                        put("max_connections", 8); put("min_streams", 4); put("max_streams", 16)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildTrojanConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "trojan")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("password", profile.password)
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    put("multiplex", buildJsonObject {
                        put("enabled", true); put("protocol", "h2mux")
                        put("max_connections", 8); put("min_streams", 4); put("max_streams", 16)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildTrojanGoConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "trojan")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("password", profile.trojanGoPassword)
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    put("multiplex", buildJsonObject {
                        put("enabled", profile.trojanGoMux)
                        put("protocol", "h2mux")
                        put("max_connections", profile.trojanGoMuxConcurrency)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildShadowsocksConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "shadowsocks")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("method", profile.method)
                    put("password", profile.passwordSs)
                    put("multiplex", buildJsonObject {
                        put("enabled", true); put("protocol", "h2mux")
                        put("max_connections", 8); put("min_streams", 4); put("max_streams", 16)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildShadowsocksRConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "shadowsocksr")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("method", profile.method)
                    put("password", profile.passwordSs)
                    put("obfs", profile.obfs)
                    put("obfs_param", profile.obfsParam)
                    put("protocol", profile.protocol)
                    put("protocol_param", profile.protocolParam)
                    put("multiplex", buildJsonObject {
                        put("enabled", true); put("protocol", "h2mux")
                        put("max_connections", 8); put("min_streams", 4); put("max_streams", 16)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildHysteria2Config(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "hysteria2")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("password", profile.hysteriaPassword)
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    if (profile.hysteriaObfs.isNotBlank()) {
                        put("obfs", buildJsonObject {
                            put("type", "salamander")
                            put("password", profile.hysteriaObfs)
                        })
                    }
                    if (profile.hysteriaUpMbps > 0) {
                        put("bandwidth", buildJsonObject {
                            put("up", "${profile.hysteriaUpMbps} Mbps")
                            put("down", "${profile.hysteriaDownMbps.ifZero { 100 }} Mbps")
                        })
                    }
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildHysteriaConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "hysteria")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("up_mbps", profile.hysteriaUp)
                    put("down_mbps", profile.hysteriaDown.ifZero { 100 })
                    put("auth", buildJsonObject {
                        put("auth_str", profile.hysteriaObfsPassword)
                        put("mode", if (profile.hysteriaObfsPassword.isNotBlank()) "md5" else "none")
                    })
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    put("protocol", profile.hysteriaProtocol)
                    put("multiplex", buildJsonObject {
                        put("enabled", true); put("protocol", "h2mux")
                        put("max_connections", 8); put("min_streams", 4); put("max_streams", 16)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildTuicConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tuic")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("uuid", profile.uuid)
                    put("password", profile.tuicPassword)
                    put("congestion_control", profile.tuicCongestion)
                    put("udp_relay_mode", profile.tuicUdpRelayMode)
                    put("zero_rtt_handshake", profile.tuicZeroRtt)
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    put("multiplex", buildJsonObject {
                        put("enabled", true); put("protocol", "h2mux")
                        put("max_connections", 8); put("min_streams", 4); put("max_streams", 16)
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildWireGuardConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "wireguard")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("private_key", profile.wgPrivateKey)
                    put("peer_public_key", profile.wgPublicKey)
                    if (profile.wgPresharedKey.isNotBlank()) {
                        put("peer_preshared_key", profile.wgPresharedKey)
                    }
                    if (profile.wgReserved.isNotBlank()) {
                        put("reserved", profile.wgReserved.split(",").map { it.trim().toInt() })
                    }
                    put("mtu", profile.wgMtu)
                    put("allowed_ips", buildJsonArray {
                        profile.wgAllowedIps.split(",").forEach { add(it.trim()) }
                    })
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildAmneziaWGConfig(profile: Profile): String {
        // AmneziaWG is a fork of WireGuard with obfuscation
        // It uses the same wireguard outbound with additional parameters
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "wireguard")
                    put("tag", "proxy")
                    put("server", profile.awgEndpoint)
                    put("server_port", profile.port)
                    put("private_key", profile.awgPrivateKey)
                    put("peer_public_key", profile.awgPublicKey)
                    if (profile.awgPresharedKey.isNotBlank()) {
                        put("peer_preshared_key", profile.awgPresharedKey)
                    }
                    put("mtu", 1420)
                    put("allowed_ips", buildJsonArray {
                        profile.awgAllowedIps.split(",").forEach { add(it.trim()) }
                    })
                    // AmneziaWG specific parameters - added as custom options
                    put("awg_jc", profile.awgJc)
                    put("awg_jmin", profile.awgJmin)
                    put("awg_jmax", profile.awgJmax)
                    put("awg_s1", profile.awgS1)
                    put("awg_s2", profile.awgS2)
                    put("awg_h1", profile.awgH1)
                    put("awg_h2", profile.awgH2)
                    put("awg_h3", profile.awgH3)
                    put("awg_h4", profile.awgH4)
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildNaiveProxyConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "naive")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("username", profile.naiveUsername)
                    put("password", profile.naivePassword)
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", profile.customSni.ifEmpty { profile.address })
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", "chrome")
                        })
                    })
                    if (profile.naivePadding) {
                        put("padding", "true")
                    }
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildSSHConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "ssh")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    put("username", profile.sshUsername)
                    if (profile.sshPassword.isNotBlank()) {
                        put("password", profile.sshPassword)
                    }
                    if (profile.sshPrivateKey.isNotBlank()) {
                        put("private_key", profile.sshPrivateKey)
                    }
                    if (profile.sshHostKey.isNotBlank()) {
                        put("host_key", profile.sshHostKey)
                    }
                    put("keep_alive_interval", profile.sshKeepAlive)
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildSocks5Config(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "socks")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    if (profile.socks5Username.isNotBlank() && profile.socks5Password.isNotBlank()) {
                        put("username", profile.socks5Username)
                        put("password", profile.socks5Password)
                    }
                    put("udp_over_tcp", profile.socks5Udp)
                    put("version", "5")
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }

    private fun buildHttpConfig(profile: Profile): String {
        return buildJsonObject {
            put("log", buildJsonObject { put("disabled", true); put("level", "info"); put("timestamp", true) })
            put("dns", buildJsonObject {
                put("servers", buildJsonArray {
                    add(buildJsonObject { put("tag", "dns"); put("address", "1.1.1.1"); put("address_resolver", "dns"); put("strategy", "ipv4_only") })
                })
            })
            put("inbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "tun"); put("tag", "tun-in"); put("interface_name", "stk0")
                    put("inet4_address", buildJsonArray { add("172.19.0.1/30") })
                    put("inet6_address", buildJsonArray { add("fdfe:dcba:9876::1/126") })
                    put("mtu", 9000); put("auto_route", true); put("strict_route", false)
                    put("endpoint_independent_nat", true); put("stack", "system")
                    put("platform", buildJsonObject {
                        put("http_proxy", buildJsonObject { put("enabled", true); put("listen", "127.0.0.1"); put("port", 10809) })
                    })
                })
            })
            put("outbounds", buildJsonArray {
                add(buildJsonObject {
                    put("type", "http")
                    put("tag", "proxy")
                    put("server", profile.address)
                    put("server_port", profile.port)
                    if (profile.httpUsername.isNotBlank() && profile.httpPassword.isNotBlank()) {
                        put("username", profile.httpUsername)
                        put("password", profile.httpPassword)
                    }
                    if (profile.httpTls) {
                        put("tls", buildJsonObject {
                            put("enabled", true)
                            put("server_name", profile.customSni.ifEmpty { profile.address })
                            put("utls", buildJsonObject {
                                put("enabled", true)
                                put("fingerprint", "chrome")
                            })
                        })
                    }
                })
                add(buildJsonObject { put("type", "direct"); put("tag", "direct") })
                add(buildJsonObject { put("type", "block"); put("tag", "block") })
                add(buildJsonObject { put("type", "dns"); put("tag", "dns-out") })
            })
            put("route", buildJsonObject {
                put("rules", buildJsonArray {
                    add(buildJsonObject { put("protocol", buildJsonArray { add("dns") }); put("outbound", "dns-out") })
                    add(buildJsonObject { put("geoip", buildJsonArray { add("private") }); put("outbound", "direct") })
                })
                put("auto_detect_interface", true)
            })
        }.toString()
    }
}

// Extension function for Int
private fun Int.ifZero(default: Int): Int = if (this == 0) default else this
