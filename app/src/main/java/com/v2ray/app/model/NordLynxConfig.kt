package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class NordLynxConfig(
    val enabled: Boolean = false,
    val privateKey: String = "",
    val publicKey: String = "",
    val address: String = "",
    val dns: List<String> = listOf("1.1.1.1"),
    val allowedIPs: List<String> = listOf("0.0.0.0/0"),
    val persistentKeepalive: Int = 25
)
