package com.v2ray.app

import android.app.Application
import com.v2ray.app.subscription.SubscriptionUpdater
import com.v2ray.app.utils.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class V2RayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        Logger.i("Application started")
        // راه‌اندازی SubscriptionUpdater
        SubscriptionUpdater.init(this)
    }
}
