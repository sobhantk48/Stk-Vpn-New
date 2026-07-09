package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class BatteryOptimizationConfig(
    val enabled: Boolean = true,
    val suspendOnIdle: Boolean = true,
    val reducePollingInterval: Boolean = true,
    val pingIntervalSeconds: Int = 60,
    val trafficUpdateIntervalSeconds: Int = 2,
    val suspendAfterMinutes: Int = 10
)
