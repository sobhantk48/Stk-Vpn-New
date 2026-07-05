package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionStats(
    val downloadSpeed: Long = 0L,
    val uploadSpeed: Long = 0L,
    val totalDownload: Long = 0L,
    val totalUpload: Long = 0L,
    val connectionDuration: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
