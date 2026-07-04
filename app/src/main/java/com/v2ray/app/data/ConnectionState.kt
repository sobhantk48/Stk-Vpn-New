package com.v2ray.app.data

enum class ConnectionStatus { IDLE, CONNECTING, CONNECTED, DISCONNECTED, ERROR }

data class ConnectionState(
    val status: ConnectionStatus = ConnectionStatus.IDLE,
    val currentProfile: Profile? = null,
    val connectedTime: String = "00:00:00",
    val ping: Int = 0,
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val errorMessage: String? = null
)
