package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class MultiHopConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val enabled: Boolean = false,
    val entryProxyId: String,
    val exitProxyId: String,
    val hopCount: Int = 2,
    val strategy: Strategy = Strategy.RANDOM
) {
    enum class Strategy {
        RANDOM,
        ROUND_ROBIN,
        LEAST_LOAD,
        FASTEST
    }
}
