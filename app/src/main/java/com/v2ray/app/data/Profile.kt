package com.v2ray.app.data

import java.io.Serializable

data class Profile(
    val id: String = "",
    val name: String = "",
    val server: String = "",
    val port: Int = 443,
    val uuid: String = "",
    val method: String = "vless"
) : Serializable {
    fun toV2RayConfig(): String {
        return """{"server":"$server","port":$port,"uuid":"$uuid"}"""
    }
}
