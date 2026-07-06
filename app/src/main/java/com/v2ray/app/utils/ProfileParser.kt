package com.v2ray.app.utils

import android.net.Uri
import java.net.URLDecoder
import java.util.Base64

/**
 * مدل داده‌ی پروفایل استخراج‌شده از لینک اشتراک
 * معادل ParsedURI در Go
 */
data class ParsedProfile(
    val protocol: String,        // vless, vmess, trojan, shadowsocks
    val host: String,
    val port: Int,
    val uuid: String? = null,    // vless, vmess
    val password: String? = null, // trojan, shadowsocks
    val method: String? = null,   // shadowsocks
    val sni: String,
    val type: String,            // tcp, ws, grpc, etc.
    val path: String? = null,
    val wsHost: String? = null,
    val alpn: String? = null,
    val fingerprint: String? = null,
    val tls: Boolean,
    val allowInsecure: Boolean,
    val raw: String,
    val valid: Boolean = false,
    val error: String? = null
)

/**
 * پارسر لینک‌های اشتراک V2Ray
 * معادل ParseURI در sni.go
 */
object ProfileParser {

    fun parse(uri: String): ParsedProfile {
        val raw = uri.trim().trim('"', '\'')
        if (raw.isEmpty()) {
            return ParsedProfile(
                protocol = "", host = "", port = 0,
                sni = "", type = "", tls = false, allowInsecure = false,
                raw = raw, valid = false, error = "Empty URI"
            )
        }

        return when {
            raw.startsWith("vless://") -> parseVless(raw)
            raw.startsWith("vmess://") -> parseVmess(raw)
            raw.startsWith("trojan://") -> parseTrojan(raw)
            raw.startsWith("ss://") -> parseShadowsocks(raw)
            else -> ParsedProfile(
                protocol = "", host = "", port = 0,
                sni = "", type = "", tls = false, allowInsecure = false,
                raw = raw, valid = false, error = "Unknown protocol"
            )
        }
    }

    private fun parseVless(uri: String): ParsedProfile {
        try {
            val u = Uri.parse(uri)
            val host = u.host ?: return invalid(uri, "vless: missing host")
            val port = u.port ?: 443
            val uuid = u.userInfo ?: return invalid(uri, "vless: missing UUID")
            val query = u.query ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], "UTF-8")
                else "" to ""
            }

            val sni = params["sni"] ?: host
            val typ = params["type"] ?: "tcp"
            val path = params["path"] ?: "/"
            val wsHost = params["host"]
            val alpn = params["alpn"]
            val fp = params["fp"]
            val security = params["security"]
            val allowInsecure = truthy(params["allowInsecure"]) || truthy(params["allowinsecure"])

            return ParsedProfile(
                protocol = "vless",
                host = host,
                port = port,
                uuid = uuid,
                sni = sni,
                type = typ,
                path = path,
                wsHost = wsHost,
                alpn = alpn,
                fingerprint = fp,
                tls = security == "tls" || security == "reality" || security == "xtls",
                allowInsecure = allowInsecure,
                raw = uri,
                valid = true
            )
        } catch (e: Exception) {
            return invalid(uri, "vless: ${e.message}")
        }
    }

    private fun parseVmess(uri: String): ParsedProfile {
        try {
            val b64 = uri.substringAfter("vmess://")
            val decoded = decodeBase64Loose(b64)
                ?: return invalid(uri, "vmess: cannot decode base64")

            val json = org.json.JSONObject(decoded)
            val add = json.optString("add", "")
            val port = json.opt("port")?.toString()?.toIntOrNull() ?: 443
            val id = json.optString("id", "")
            val sni = json.optString("sni", "").ifEmpty { json.optString("host", add) }
            val net = json.optString("net", "tcp")
            val path = json.optString("path", "/")
            val host = json.optString("host", "")
            val tls = json.optString("tls", "").lowercase() == "tls"
            val allowInsecure = truthy(json.optString("allowInsecure")) ||
                    (json.has("verify_cert") && !truthy(json.optString("verify_cert")))

            return ParsedProfile(
                protocol = "vmess",
                host = add,
                port = port,
                uuid = id,
                sni = sni,
                type = net,
                path = path,
                wsHost = host,
                tls = tls,
                allowInsecure = allowInsecure,
                raw = uri,
                valid = true
            )
        } catch (e: Exception) {
            return invalid(uri, "vmess: ${e.message}")
        }
    }

    private fun parseTrojan(uri: String): ParsedProfile {
        try {
            val u = Uri.parse(uri)
            val host = u.host ?: return invalid(uri, "trojan: missing host")
            val port = u.port ?: 443
            val password = u.userInfo ?: return invalid(uri, "trojan: missing password")
            val query = u.query ?: ""

            val params = query.split("&").associate {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], "UTF-8")
                else "" to ""
            }

            val sni = params["sni"] ?: params["peer"] ?: host
            val typ = params["type"] ?: "tcp"
            val path = params["path"] ?: "/"
            val wsHost = params["host"]
            val alpn = params["alpn"]
            val fp = params["fp"]
            val allowInsecure = truthy(params["allowInsecure"]) || truthy(params["allowinsecure"])

            return ParsedProfile(
                protocol = "trojan",
                host = host,
                port = port,
                password = password,
                sni = sni,
                type = typ,
                path = path,
                wsHost = wsHost,
                alpn = alpn,
                fingerprint = fp,
                tls = params["security"] != "none",
                allowInsecure = allowInsecure,
                raw = uri,
                valid = true
            )
        } catch (e: Exception) {
            return invalid(uri, "trojan: ${e.message}")
        }
    }

    private fun parseShadowsocks(uri: String): ParsedProfile {
        try {
            var body = uri.substringAfter("ss://")
            // حذف بخش #name
            val hashIndex = body.indexOf('#')
            if (hashIndex >= 0) body = body.substring(0, hashIndex)
            // حذف بخش ?plugin
            val qIndex = body.indexOf('?')
            if (qIndex >= 0) body = body.substring(0, qIndex)
            body = body.trim()

            if (body.isEmpty()) return invalid(uri, "ss: empty")

            val atIndex = body.lastIndexOf('@')
            var method = ""
            var password = ""
            var host = ""
            var port = 0

            if (atIndex >= 0) {
                val userinfo = body.substring(0, atIndex)
                val hostPart = body.substring(atIndex + 1)
                val (h, p) = splitHostPortLoose(hostPart)
                host = h
                port = p

                var mp = userinfo
                decodeBase64Loose(userinfo)?.let {
                    if (it.contains(":")) mp = it
                }

                val colon = mp.indexOf(':')
                if (colon < 0) return invalid(uri, "ss: bad method:password")
                method = mp.substring(0, colon)
                password = mp.substring(colon + 1)
            } else {
                val decoded = decodeBase64Loose(body)
                    ?: return invalid(uri, "ss: cannot decode")
                val at = decoded.lastIndexOf('@')
                if (at < 0) return invalid(uri, "ss: missing @host:port")
                val mp = decoded.substring(0, at)
                val hostPart = decoded.substring(at + 1)
                val (h, p) = splitHostPortLoose(hostPart)
                host = h
                port = p

                val colon = mp.indexOf(':')
                if (colon < 0) return invalid(uri, "ss: bad method:password")
                method = mp.substring(0, colon)
                password = mp.substring(colon + 1)
            }

            if (host.isEmpty() || port == 0) return invalid(uri, "ss: missing host/port")
            if (method.isEmpty() || password.isEmpty()) return invalid(uri, "ss: missing method/password")

            return ParsedProfile(
                protocol = "shadowsocks",
                host = host,
                port = port,
                password = password,
                method = method,
                sni = host,
                type = "tcp",
                tls = false,
                allowInsecure = false,
                raw = uri,
                valid = true
            )
        } catch (e: Exception) {
            return invalid(uri, "ss: ${e.message}")
        }
    }

    // ===== ابزارهای کمکی =====

    private fun splitHostPortLoose(hp: String): Pair<String, Int> {
        val trimmed = hp.trim()
        val parts = trimmed.split(':')
        return when (parts.size) {
            2 -> parts[0] to (parts[1].toIntOrNull() ?: 0)
            else -> trimmed to 0
        }
    }

    private fun decodeBase64Loose(s: String): String? {
        val trimmed = s.trim()
        val decoders = listOf(
            Base64.getUrlDecoder(),
            Base64.getDecoder()
        )
        for (decoder in decoders) {
            try {
                return String(decoder.decode(trimmed))
            } catch (_: Exception) {
                // continue
            }
        }
        // تلاش با padding
        try {
            var padded = trimmed
            while (padded.length % 4 != 0) padded += "="
            return String(Base64.getDecoder().decode(padded))
        } catch (_: Exception) {
            return null
        }
    }

    private fun truthy(value: String?): Boolean {
        if (value == null) return false
        val v = value.lowercase().trim()
        return v == "true" || v == "1" || v == "yes" || v == "on"
    }

    private fun invalid(raw: String, error: String): ParsedProfile {
        return ParsedProfile(
            protocol = "", host = "", port = 0,
            sni = "", type = "", tls = false, allowInsecure = false,
            raw = raw, valid = false, error = error
        )
    }
}
