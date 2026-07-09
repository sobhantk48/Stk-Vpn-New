package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class DynamicRoutingRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val enabled: Boolean = true,
    val condition: Condition,
    val action: Action,
    val priority: Int = 0
) {
    enum class Condition {
        WIFI,
        CELLULAR,
        VPN_CONNECTED,
        VPN_DISCONNECTED,
        TIME_RANGE,
        LOCATION,
        BATTERY_LEVEL,
        NETWORK_SPEED
    }
    
    enum class Action {
        CONNECT,
        DISCONNECT,
        CHANGE_PROFILE,
        ENABLE_SPLIT_TUNNEL,
        DISABLE_SPLIT_TUNNEL
    }
}
