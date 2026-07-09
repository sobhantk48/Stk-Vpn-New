package com.v2ray.app.service

import com.v2ray.app.model.LWOConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class LWOHandler @Inject constructor() {
    
    fun obfuscatePacket(data: ByteArray, config: LWOConfig): ByteArray {
        if (!config.enabled) return data
        
        val obfuscated = data.copyOf()
        when (config.obfuscationType) {
            LWOConfig.ObfuscationType.QUIC -> {
                // Add QUIC-like header
                val quicHeader = byteArrayOf(0xC0, 0x01, 0x02, 0x03)
                return quicHeader + obfuscated
            }
            LWOConfig.ObfuscationType.DNS -> {
                // Wrap in DNS query format
                val dnsHeader = byteArrayOf(0x00, 0x01, 0x01, 0x00, 0x00, 0x01)
                return dnsHeader + obfuscated
            }
            LWOConfig.ObfuscationType.HTTP -> {
                // Add HTTP header
                val httpHeader = "GET / HTTP/1.1\r\nHost: example.com\r\n\r\n".toByteArray()
                return httpHeader + obfuscated
            }
            LWOConfig.ObfuscationType.TLS -> {
                // Add TLS ClientHello-like header
                val tlsHeader = byteArrayOf(0x16, 0x03, 0x03, 0x00, 0x01)
                return tlsHeader + obfuscated
            }
            LWOConfig.ObfuscationType.RANDOM -> {
                // Add random padding
                val padding = ByteArray(Random.nextInt(10, 50)) { Random.nextInt(0, 255).toByte() }
                return obfuscated + padding
            }
        }
    }
    
    fun deobfuscatePacket(data: ByteArray, config: LWOConfig): ByteArray {
        if (!config.enabled) return data
        
        // Remove obfuscation headers based on type
        return when (config.obfuscationType) {
            LWOConfig.ObfuscationType.QUIC -> {
                if (data.size > 4) data.drop(4).toByteArray() else data
            }
            LWOConfig.ObfuscationType.DNS -> {
                if (data.size > 12) data.drop(12).toByteArray() else data
            }
            LWOConfig.ObfuscationType.HTTP -> {
                // Find end of HTTP header
                val headerEnd = data.indexOf("\r\n\r\n".toByteArray())
                if (headerEnd > 0 && headerEnd + 4 < data.size) {
                    data.drop(headerEnd + 4).toByteArray()
                } else data
            }
            LWOConfig.ObfuscationType.TLS -> {
                if (data.size > 5) data.drop(5).toByteArray() else data
            }
            LWOConfig.ObfuscationType.RANDOM -> {
                // Remove random padding (simplified)
                data
            }
        }
    }
}
