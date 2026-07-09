package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class TrafficStats(
    val proxyId: String,
    var download: Long = 0,
    var upload: Long = 0,
    var lastUpdated: Long = System.currentTimeMillis()
)
