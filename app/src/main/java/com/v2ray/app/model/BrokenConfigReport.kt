package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class BrokenConfigReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val profileId: String,
    val profileName: String,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceInfo: DeviceInfo = DeviceInfo()
) {
    @Serializable
    data class DeviceInfo(
        val androidVersion: String = android.os.Build.VERSION.RELEASE,
        val model: String = android.os.Build.MODEL,
        val manufacturer: String = android.os.Build.MANUFACTURER
    )
}
