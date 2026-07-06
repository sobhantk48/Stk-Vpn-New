package com.v2ray.app.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AdminSession {
    private const val DEFAULT_PASSWORD = "1311"
    private var currentPassword = DEFAULT_PASSWORD
    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn

    fun validatePassword(input: String): Boolean {
        val result = input == currentPassword
        return result
    }


    fun changePassword(old: String, new: String): Boolean {
        return if (old == currentPassword && new.length >= 4) {
            currentPassword = new
            true
        } else {
            false
        }
    }
}
