package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AnonymousModeConfig(
    val enabled: Boolean = false,
    val multiHop: Boolean = true,
    val torRouting: Boolean = false,
    val noLogs: Boolean = true,
    val randomization: Boolean = true
)
