package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class InternetQuality(
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val ping: Int = 0,
    val jitter: Int = 0,
    val packetLoss: Double = 0.0,
    val gamingScore: Int = 0,
    val browsingScore: Int = 0,
    val streamingScore: Int = 0,
    val videoCallScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
