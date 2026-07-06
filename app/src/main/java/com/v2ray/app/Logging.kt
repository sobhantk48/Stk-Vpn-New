package com.v2ray.app

import com.v2ray.app.utils.Logger

fun writeLog(message: String) {
    Logger.log(message)
}

fun writeError(message: String, throwable: Throwable? = null) {
    Logger.e(message, throwable)
}
