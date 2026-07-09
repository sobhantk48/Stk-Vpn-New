package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "🌐",
    val proxyIds: List<String> = emptyList(),
    val selected: Boolean = false
)
