package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val name: String = "",
    val enabled: Boolean = true,
    val autoUpdate: Boolean = false,
    val updateInterval: Long = 6, // hours
    val lastUpdate: Long = 0,
    val nodeCount: Int = 0,
    val selected: Boolean = false
)
