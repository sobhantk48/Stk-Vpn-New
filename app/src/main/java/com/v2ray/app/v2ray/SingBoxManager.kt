package com.v2ray.app.v2ray

import android.content.Context
import android.util.Log
import com.v2ray.app.data.Profile
import com.v2ray.app.bg.LocalResolver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import io.nekohasekai.libbox.BoxInstance
import io.nekohasekai.libbox.Libbox
import org.json.JSONArray
import org.json.JSONObject

class SingBoxManager(private val context: Context) {
    companion object {
        private const val TAG = "SingBoxManager"
        enum class CoreState {
            IDLE, CONNECTING, CONNECTED, DISCONNECTED, ERROR
        }

        private val _coreState = MutableStateFlow(CoreState.IDLE)
        val coreState: StateFlow<CoreState> = _coreState.asStateFlow()
    }

    private var isRunning = false
    private var boxInstance: BoxInstance? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun buildSingBoxConfig(profile: Profile): String {
        return try {
            val templateJson = context.assets.open("singbox_config.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(templateJson)

            val outboundJson = JSONObject(profile.toV2RayConfig())
            if (!outboundJson.has("tag")) {
                outboundJson.put("tag", "proxy")
            }

            if (profile.frontingDomain.isNotBlank()) {
                val streamSettings = outboundJson.optJSONObject("streamSettings")
                if (streamSettings != null) {
                    if (streamSettings.has("tlsSettings")) {
                        val tlsSettings = streamSettings.getJSONObject("tlsSettings")
                        tlsSettings.put("serverName", profile.frontingDomain)
                    }
                    if (streamSettings.has("wsSettings")) {
                        val wsSettings = streamSettings.getJSONObject("wsSettings")
                        if (wsSettings.has("headers")) {
                            val headers = wsSettings.getJSONObject("headers")
                            headers.put("Host", profile.frontingDomain)
                        } else {
                            wsSettings.put("headers", JSONObject().apply {
                                put("Host", profile.frontingDomain)
                            })
                        }
                    }
                    if (streamSettings.has("httpSettings")) {
                        val httpSettings = streamSettings.getJSONObject("httpSettings")
                        if (httpSettings.has("host")) {
                            httpSettings.put("host", JSONArray(listOf(profile.frontingDomain)))
                        } else {
                            httpSettings.put("host", JSONArray(listOf(profile.frontingDomain)))
                        }
                    }
                }
                Log.d(TAG, "Domain Fronting applied: ${profile.frontingDomain}")
            }

            val outboundsArray = root.getJSONArray("outbounds")
            if (outboundsArray.length() > 0) {
                outboundsArray.put(0, outboundJson)
            } else {
                outboundsArray.put(outboundJson)
            }

            val route = root.optJSONObject("route")
            if (route != null) {
                val rules = route.optJSONArray("rules")
                if (rules == null || rules.length() == 0) {
                    val newRules = JSONArray()
                    newRules.put(JSONObject().apply {
                        put("outbound", "proxy")
                        put("network", "tcp,udp")
                        put("ip_version", 4)
                    })
                    route.put("rules", newRules)
                } else {
                    for (i in 0 until rules.length()) {
                        val rule = rules.getJSONObject(i)
                        if (rule.optString("outbound") == "direct") {
                            rule.put("outbound", "proxy")
                            break
                        }
                    }
                }
            }

            root.toString(2)
        } catch (e: Exception) {
            Log.e(TAG, "buildSingBoxConfig failed", e)
            buildFallbackConfig(profile)
        }
    }

    private fun buildFallbackConfig(profile: Profile): String {
        return JSONObject().apply {
            put("log", JSONObject().apply { put("level", "info") })
            put("inbounds", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "tun")
                    put("tag", "tun-in")
                    put("interface_name", "tun0")
                    put("address", JSONArray(listOf("172.19.0.1/30")))
                    put("mtu", 9000)
                    put("auto_route", true)
                })
            })
            put("outbounds", JSONArray().apply {
                put(JSONObject(profile.toV2RayConfig()))
                put(JSONObject().apply {
                    put("type", "direct")
                    put("tag", "direct")
                })
                put(JSONObject().apply {
                    put("type", "block")
                    put("tag", "block")
                })
            })
            put("route", JSONObject().apply {
                put("rules", JSONArray().apply {
                    put(JSONObject().apply {
                        put("outbound", "proxy")
                        put("network", "tcp,udp")
                    })
                })
            })
        }.toString(2)
    }

    suspend fun startV2Ray(configJson: String, vpnFd: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (isRunning) {
                    stopV2Ray().getOrThrow()
                }

                // استفاده از Libbox.newBoxInstance با LocalResolver
                val box = Libbox.newBoxInstance(configJson, LocalResolver)
                boxInstance = box
                box.start()
                isRunning = true
                _coreState.update { CoreState.CONNECTED }
                Log.d(TAG, "✅ Sing-box started successfully with Libbox")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ startV2Ray failed", e)
                _coreState.update { CoreState.ERROR }
                Result.failure(e)
            }
        }

    suspend fun stopV2Ray(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                boxInstance?.close()
                boxInstance = null
                isRunning = false
                _coreState.update { CoreState.DISCONNECTED }
                Log.d(TAG, "Sing-box stopped")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "stopV2Ray failed", e)
                Result.failure(e)
            }
        }

    fun isRunning(): Boolean = isRunning

    fun cleanup() {
        scope.launch {
            try {
                boxInstance?.close()
            } catch (_: Exception) {}
            boxInstance = null
            isRunning = false
            _coreState.update { CoreState.IDLE }
            Log.d(TAG, "Cleanup done")
        }
    }
}
