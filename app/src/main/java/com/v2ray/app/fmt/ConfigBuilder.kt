package com.v2ray.app.fmt

import com.v2ray.app.data.Profile
import kotlinx.serialization.json.*

/**
 * سازنده‌ی کانفیگ کامل sing-box
 * شامل inbound TUN، outboundها و route
 */
class ConfigBuilder(private val profile: Profile) {

    fun build(): String {
        val inbound = buildInbound()
        val outbound = OutboundBuilder.build(profile)
        val direct = buildDirectOutbound()
        val route = buildRoute()

        return buildJsonObject {
            put("log", buildJsonObject {
                put("level", "debug")
            })
            put("inbounds", JsonArray(listOf(inbound)))
            put("outbounds", JsonArray(listOf(outbound, direct)))
            put("route", route)
        }.toString()
    }

    private fun buildInbound(): JsonObject {
        return buildJsonObject {
            put("type", "tun")
            put("tag", "tun-in")
            put("interface_name", "v2ray-tun")
            put("address", JsonArray(listOf(JsonPrimitive("172.19.0.1/30"))))
            put("auto_route", true)
            put("strict_route", true)
            put("stack", "system")
            put("sniff", true)
        }
    }

    private fun buildDirectOutbound(): JsonObject {
        return buildJsonObject {
            put("type", "direct")
            put("tag", "direct")
        }
    }

    private fun buildRoute(): JsonObject {
        return buildJsonObject {
            put("auto_detect_interface", true)
            put("final", "direct")
        }
    }
}
