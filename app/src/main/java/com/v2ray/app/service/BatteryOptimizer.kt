package com.v2ray.app.service

import android.content.Context
import android.os.PowerManager
import com.v2ray.app.model.BatteryOptimizationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryOptimizer @Inject constructor(
    private val context: Context
) {
    private var isIdle = false
    private var idleStartTime = 0L
    
    fun shouldSuspend(config: BatteryOptimizationConfig): Boolean {
        if (!config.enabled) return false
        
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInteractive = pm.isInteractive
        
        if (!isInteractive) {
            if (idleStartTime == 0L) {
                idleStartTime = System.currentTimeMillis()
            }
            val idleDuration = (System.currentTimeMillis() - idleStartTime) / 60000
            if (idleDuration >= config.suspendAfterMinutes) {
                return true
            }
        } else {
            idleStartTime = 0L
            isIdle = false
        }
        return false
    }
    
    fun getOptimalPingInterval(config: BatteryOptimizationConfig): Int {
        return if (config.enabled && config.reducePollingInterval) {
            config.pingIntervalSeconds
        } else {
            30 // Default
        }
    }
    
    fun getOptimalTrafficUpdateInterval(config: BatteryOptimizationConfig): Int {
        return if (config.enabled && config.reducePollingInterval) {
            config.trafficUpdateIntervalSeconds
        } else {
            1 // Default (seconds)
        }
    }
}
