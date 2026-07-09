package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class TrafficHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val download: Long = 0,
    val upload: Long = 0,
    val total: Long = 0,
    val proxyId: String? = null
)
