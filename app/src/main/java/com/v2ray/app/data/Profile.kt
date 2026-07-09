package com.v2ray.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: ProtocolType,
    val address: String,
    val port: Int,
    val selected: Boolean = false,
    
    // VLESS & VMess
    val uuid: String = "",
    val encryption: String = "none",
    val flow: String = "",
    val security: String = "tls",
    
    // Reality specific
    val realityPublicKey: String = "",
    val realityShortId: String = "",
    val realityServerName: String = "",
    val realityFingerprint: String = "chrome",
    
    // Trojan
    val password: String = "",
    
    // Shadowsocks
    val method: String = "aes-256-gcm",
    val passwordSs: String = "",
    
    // ShadowsocksR
    val obfs: String = "plain",
    val obfsParam: String = "",
    val protocol: String = "origin",
    val protocolParam: String = "",
    
    // Hysteria2
    val hysteriaPassword: String = "",
    val hysteriaObfs: String = "",
    val hysteriaUpMbps: Int = 0,
    val hysteriaDownMbps: Int = 0,
    
    // Hysteria (v1)
    val hysteriaUp: Int = 0,
    val hysteriaDown: Int = 0,
    val hysteriaObfsPassword: String = "",
    val hysteriaProtocol: String = "udp",
    
    // TUIC
    val tuicPassword: String = "",
    val tuicCongestion: String = "bbr",
    val tuicUdpRelayMode: String = "native",
    val tuicZeroRtt: Boolean = false,
    
    // WireGuard
    val wgPrivateKey: String = "",
    val wgPublicKey: String = "",
    val wgPresharedKey: String = "",
    val wgAllowedIps: String = "0.0.0.0/0",
    val wgEndpoint: String = "",
    val wgReserved: String = "",
    val wgMtu: Int = 1420,
    
    // AmneziaWG (AWG)
    val awgPrivateKey: String = "",
    val awgPublicKey: String = "",
    val awgPresharedKey: String = "",
    val awgAllowedIps: String = "0.0.0.0/0",
    val awgEndpoint: String = "",
    val awgJc: Int = 5,
    val awgJmin: Int = 5,
    val awgJmax: Int = 35,
    val awgS1: Int = 65,
    val awgS2: Int = 75,
    val awgH1: Int = 85,
    val awgH2: Int = 95,
    val awgH3: Int = 105,
    val awgH4: Int = 115,
    
    // NaïveProxy
    val naiveUsername: String = "",
    val naivePassword: String = "",
    val naivePadding: Boolean = true,
    
    // SSH
    val sshUsername: String = "",
    val sshPassword: String = "",
    val sshPrivateKey: String = "",
    val sshHostKey: String = "",
    val sshKeepAlive: Int = 30,
    
    // SOCKS5
    val socks5Username: String = "",
    val socks5Password: String = "",
    val socks5Udp: Boolean = true,
    
    // HTTP
    val httpUsername: String = "",
    val httpPassword: String = "",
    val httpTls: Boolean = false,
    
    // Trojan-Go
    val trojanGoPassword: String = "",
    val trojanGoMux: Boolean = false,
    val trojanGoMuxConcurrency: Int = 8,
    
    // Custom SNI & Fronting
    val customSni: String = "",
    val frontingDomain: String = "",
    
    // Fragment & Desync
    val fragmentSize: Int = 0,
    val fragmentDelay: Int = 0,
    val fragmentPackets: Int = 0,
    
    // Split & Kill (global, not per profile)
    val splitTunneling: Boolean = false,
    val killSwitch: Boolean = false
) {
    enum class ProtocolType {
        VLESS,
        VLESS_REALITY,
        VMESS,
        TROJAN,
        TROJAN_GO,
        SHADOWSOCKS,
        SHADOWSOCKS_R,
        HYSTERIA2,
        HYSTERIA,
        TUIC,
        WIREGUARD,
        AMNEZIA_WG,
        NAIVE_PROXY,
        SSH,
        SOCKS5,
        HTTP
    }
}
