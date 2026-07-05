package com.v2ray.app.repository

import android.content.Context
import android.content.SharedPreferences

object SettingsRepository {

    private lateinit var prefs: SharedPreferences

    fun initialize(ctx: Context) {
        prefs = ctx.getSharedPreferences("v2ray_settings", Context.MODE_PRIVATE)
    }

    // ---------------- PROTOCOL ----------------

    fun setDefaultProtocol(protocol: String) {
        prefs.edit().putString("default_protocol", protocol).apply()
    }

    fun getDefaultProtocol(): String {
        return prefs.getString("default_protocol", "VLESS") ?: "VLESS"
    }

    // ---------------- SWITCHES ----------------

    fun setAutoConnect(value: Boolean) {
        prefs.edit().putBoolean("auto_connect", value).apply()
    }

    fun getAutoConnect(): Boolean {
        return prefs.getBoolean("auto_connect", false)
    }

    fun setStayConnected(value: Boolean) {
        prefs.edit().putBoolean("stay_connected", value).apply()
    }

    fun getStayConnected(): Boolean {
        return prefs.getBoolean("stay_connected", false)
    }

    fun setShowNotification(value: Boolean) {
        prefs.edit().putBoolean("show_notification", value).apply()
    }

    fun getShowNotification(): Boolean {
        return prefs.getBoolean("show_notification", false)
    }
}
