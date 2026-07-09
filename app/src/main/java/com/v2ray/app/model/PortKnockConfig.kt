package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class PortKnockConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val host: String,
    val ports: List<Int>,
    val protocol: Protocol = Protocol.TCP,
    val delay: Int = 100,
    val enabled: Boolean = true
) {
    enum class Protocol {
        TCP,
        UDP
    }
}
