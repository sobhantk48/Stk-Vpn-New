package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class LiteModeConfig(
    val enabled: Boolean = false,
    val reduceAnimation: Boolean = true,
    val disableLogging: Boolean = true,
    val lowerMtu: Boolean = true,
    val reducePolling: Boolean = true,
    val optimizeBattery: Boolean = true
)
