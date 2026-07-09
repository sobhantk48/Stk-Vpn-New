package com.v2ray.app.service

import com.v2ray.app.data.Profile
import com.v2ray.app.model.MultiHopConfig
import com.v2ray.app.v2ray.SingBoxManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiHopService @Inject constructor(
    private val singBoxManager: SingBoxManager
) {
    fun buildMultiHopConfig(config: MultiHopConfig, entryProfile: Profile, exitProfile: Profile): String {
        // Build a chained configuration
        return """
        {
            "log": {"disabled": true, "level": "info"},
            "outbounds": [
                {
                    "type": "vless",
                    "tag": "entry",
                    "server": "${entryProfile.address}",
                    "server_port": ${entryProfile.port},
                    "uuid": "${entryProfile.uuid}",
                    "flow": "xtls-rprx-vision",
                    "tls": {"enabled": true, "server_name": "${entryProfile.customSni}"}
                },
                {
                    "type": "vless",
                    "tag": "exit",
                    "server": "${exitProfile.address}",
                    "server_port": ${exitProfile.port},
                    "uuid": "${exitProfile.uuid}",
                    "flow": "xtls-rprx-vision",
                    "tls": {"enabled": true, "server_name": "${exitProfile.customSni}"}
                }
            ],
            "route": {
                "rules": [
                    {
                        "outbound": "entry"
                    }
                ]
            }
        }
        """.trimIndent()
    }
}
