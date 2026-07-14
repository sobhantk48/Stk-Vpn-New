package com.v2ray.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.MessageDigest

object AdminSession {
    private const val DEFAULT_PASSWORD = "admin"
    
    // متد ساده برای چک کردن پسورد (بعداً می‌تونیم هش کنیم)
    fun verifyPassword(input: String): Boolean {
        return input == DEFAULT_PASSWORD
    }

    // این بخش رو بعداً برای ذخیره امن پسورد جدید توسعه میدیم
    fun changePassword(newPassword: String) {
        // TODO: ذخیره در EncryptedSharedPreferences
    }
}
