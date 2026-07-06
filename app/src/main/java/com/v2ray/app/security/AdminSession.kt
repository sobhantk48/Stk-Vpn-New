package com.v2ray.app.security
import com.v2ray.app.utils.Logger
import com.v2ray.app.writeError
import com.v2ray.app.writeLog

import com.v2ray.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AdminSession {
    private const val DEFAULT_PASSWORD = "1311"
    private var currentPassword = DEFAULT_PASSWORD
    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn

    fun validatePassword(input: String): Boolean {
        val result = input == currentPassword
        Logger.Logger.log("Admin login: ${if (result) "success" else "failed"}")
        return result
    }

    fun login() { _loggedIn.value = true; Logger.Logger.log("Admin logged in") }
    fun logout() { _loggedIn.value = false; Logger.Logger.log("Admin logged out") }

    fun changePassword(old: String, new: String): Boolean {
        return if (old == currentPassword && new.length >= 4) {
            currentPassword = new
            Logger.Logger.log("Password changed")
            true
        } else {
            Logger.Logger.log("Password change failed")
            false
        }
    }
}
