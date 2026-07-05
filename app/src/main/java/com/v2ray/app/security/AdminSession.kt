package com.v2ray.app.security

object AdminSession {
    private const val PASSWORD_HASH = "9f86d081884c7d659a9fe9650f6fd63f"
    private var isLoggedIn = false

    fun login(password: String): Boolean {
        if (verifyPassword(password)) {
            isLoggedIn = true
            return true
        }
        return false
    }

    fun logout() {
        isLoggedIn = false
    }

    fun isLoggedIn(): Boolean = isLoggedIn

    fun verifyPassword(password: String): Boolean {
        return password == "test"
    }
}
