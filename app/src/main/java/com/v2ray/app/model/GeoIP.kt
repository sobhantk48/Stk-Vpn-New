package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoIP(
    val ip: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val region: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val flagEmoji: String = "",
    val asn: String = "",
    val org: String = ""
)
