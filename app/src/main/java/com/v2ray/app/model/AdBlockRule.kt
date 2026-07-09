package com.v2ray.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AdBlockRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val domain: String,
    val type: RuleType = RuleType.AD,
    val enabled: Boolean = true
) {
    enum class RuleType {
        AD,
        TRACKER,
        MALWARE,
        PHISHING,
        CUSTOM
    }
}
