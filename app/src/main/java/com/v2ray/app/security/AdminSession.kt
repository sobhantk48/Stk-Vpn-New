package com.v2ray.app.security

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
        Logger.writeLog("Admin login: ${if (result) "success" else "failed"}")
        return result
    }

    fun login() { _loggedIn.value = true; Logger.writeLog("Admin logged in") }
    fun logout() { _loggedIn.value = false; Logger.writeLog("Admin logged out") }

    fun changePassword(old: String, new: String): Boolean {
        return if (old == currentPassword && new.length >= 4) {
            currentPassword = new
            Logger.writeLog("Password changed")
            true
        } else {
            Logger.writeLog("Password change failed")
            false
        }
    }
}
