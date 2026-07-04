package com.v2ray.app.data

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class ConnectionState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val currentProfile: Profile? = null,
    val connectedTime: String = "00:00:00",
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val errorMessage: String? = null
)
