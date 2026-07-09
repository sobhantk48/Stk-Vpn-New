package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class FirewallRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val enabled: Boolean = true,
    val type: Type = Type.BLOCK,
    val target: Target,
    val action: Action = Action.BLOCK
) {
    enum class Type {
        IP,
        DOMAIN,
        PORT,
        PROTOCOL,
        COUNTRY
    }
    
    enum class Target {
        ALL,
        TORRENT,
        WEBRTC,
        ADS,
        TRACKERS,
        MALWARE,
        SPECIFIC_IP,
        SPECIFIC_DOMAIN,
        SPECIFIC_PORT,
        SPECIFIC_COUNTRY
    }
    
    enum class Action {
        BLOCK,
        ALLOW,
        BYPASS
    }
}
