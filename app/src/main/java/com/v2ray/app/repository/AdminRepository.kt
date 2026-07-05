package com.v2ray.app.repository

import android.content.Context
import android.content.SharedPreferences

object AdminRepository {

    private lateinit var prefs: SharedPreferences

    fun initialize(ctx: Context) {
        prefs = ctx.getSharedPreferences("admin_settings", Context.MODE_PRIVATE)
    }

    // رمز پیش‌فرض 1311
    fun getPassword(): String {
        return prefs.getString("admin_password", "1311") ?: "1311"
    }

    fun setPassword(newPass: String) {
        prefs.edit().putString("admin_password", newPass).apply()
    }

    fun checkPassword(input: String): Boolean {
        return input == getPassword()
    }
}
