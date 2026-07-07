package com.v2ray.app.v2ray

import android.content.Context
import android.content.Intent
import android.util.Log
import io.nekohasekai.libbox.BoxService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import com.v2ray.app.data.Profile

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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun buildSingBoxConfig(profile: Profile): String {
        return try {
            Log.d(TAG, "buildSingBoxConfig: building config for ${profile.name}")
            val templateJson = context.assets.open("singbox_config.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(templateJson)

            val outboundJson = JSONObject(profile.toV2RayConfig())
            if (!outboundJson.has("tag")) {
                outboundJson.put("tag", "proxy")
            }

            // Domain Fronting
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

            val result = root.toString(2)
            Log.d(TAG, "buildSingBoxConfig: config built successfully (length=${result.length})")
            result
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
                Log.d(TAG, "startV2Ray: called with vpnFd=$vpnFd")
                Log.d(TAG, "startV2Ray: config length=${configJson.length}")
                Log.d(TAG, "startV2Ray: config preview=${configJson.take(200)}")

                if (isRunning) {
                    Log.d(TAG, "startV2Ray: already running, stopping first")
                    stopV2Ray().getOrThrow()
                }

                val intent = Intent(context, BoxService::class.java).apply {
                    putExtra("config", configJson)
                    putExtra("tun_fd", vpnFd)
                }
                context.startService(intent)
                Log.d(TAG, "startV2Ray: BoxService started successfully")

                isRunning = true
                _coreState.update { CoreState.CONNECTED }
                Log.d(TAG, "startV2Ray: V2Ray started successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "startV2Ray: failed", e)
                _coreState.update { CoreState.ERROR }
                Result.failure(e)
            }
        }

    suspend fun startV2RayWithConfig(configJson: String, vpnFd: Int): Result<Unit> =
        startV2Ray(configJson, vpnFd)

    suspend fun stopV2Ray(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "stopV2Ray: called")
                context.stopService(Intent(context, BoxService::class.java))
                isRunning = false
                _coreState.update { CoreState.DISCONNECTED }
                Log.d(TAG, "stopV2Ray: V2Ray stopped")
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
                context.stopService(Intent(context, BoxService::class.java))
            } catch (_: Exception) {}
            isRunning = false
            _coreState.update { CoreState.IDLE }
            Log.d(TAG, "Cleanup done")
        }
    }
}
