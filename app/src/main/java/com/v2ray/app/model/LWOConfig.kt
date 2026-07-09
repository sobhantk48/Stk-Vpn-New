package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class LWOConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val enabled: Boolean = false,
    val obfuscationType: ObfuscationType = ObfuscationType.QUIC,
    val padding: Boolean = true,
    val fakePacketRatio: Double = 0.3,
    val obfuscationKey: String = ""
) {
    enum class ObfuscationType {
        QUIC,
        DNS,
        HTTP,
        TLS,
        RANDOM
    }
}
