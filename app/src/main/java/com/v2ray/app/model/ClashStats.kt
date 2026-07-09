package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class ClashStats(
    val activeConnections: Int = 0,
    val totalConnections: Int = 0,
    val totalTraffic: Long = 0,
    val memoryUsage: Long = 0,
    val cpuUsage: Double = 0.0,
    val activeProxies: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
