package com.v2ray.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.data.Profile
import com.v2ray.app.utils.SpeedTester
import com.v2ray.app.utils.SniResult
import com.v2ray.app.v2ray.SingBoxManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // اضافه کردن StateFlow برای پینگ‌ها
    private val _pings = MutableStateFlow<Map<String, SniResult>>(emptyMap())
    val pings: StateFlow<Map<String, SniResult>> = _pings.asStateFlow()

    private val singBoxManager = SingBoxManager(getApplication())

    init {
        loadProfiles()
        startPingTimer()
    }

    private fun loadProfiles() {
        // بارگذاری نمونه‌های تست (بعداً با Room جایگزین می‌شود)
        _profiles.value = listOf(
            Profile(
                id = "1",
                name = "Japan - Tokyo - 01",
                server = "jp.example.com",
                port = 443,
                protocol = "vless",
                uuid = "uuid-1",
                sni = "www.google.com",
                network = "ws",
                path = "/",
                security = "reality"
            ),
            Profile(
                id = "2",
                name = "Germany - Frankfurt - 02",
                server = "de.example.com",
                port = 443,
                protocol = "vless",
                uuid = "uuid-2",
                sni = "www.google.com",
                network = "tcp",
                path = "/",
                security = "reality"
            ),
            Profile(
                id = "3",
                name = "United States - New York - 03",
                server = "us.example.com",
                port = 443,
                protocol = "vless",
                uuid = "uuid-3",
                sni = "www.google.com",
                network = "ws",
                path = "/",
                security = "reality"
            ),
            Profile(
                id = "4",
                name = "Singapore - Singapore - 01",
                server = "sg.example.com",
                port = 443,
                protocol = "vless",
                uuid = "uuid-4",
                sni = "www.google.com",
                network = "tcp",
                path = "/",
                security = "reality"
            )
        )
    }

    private fun startPingTimer() {
        viewModelScope.launch {
            while (true) {
                updatePings()
                kotlinx.coroutines.delay(15000) // هر ۱۵ ثانیه به‌روزرسانی
            }
        }
    }

    private suspend fun updatePings() {
        val currentProfiles = _profiles.value
        if (currentProfiles.isEmpty()) return

        // ایجاد لیست هاست‌ها برای تست
        val hosts = currentProfiles.map { profile ->
            profile.server to (profile.port ?: 443)
        }

        // تست همزمان همه سرورها
        val results = hosts.map { (host, port) ->
            async {
                SpeedTester.checkSni(host, port, 5)
            }
        }.awaitAll()

        // ایجاد Map از هاست به نتیجه
        val pingMap = currentProfiles.mapIndexed { index, profile ->
            profile.id to results[index]
        }.toMap()

        _pings.value = pingMap
    }

    fun selectProfile(profile: Profile) {
        _selectedProfile.value = profile
    }

    fun toggleConnection() {
        viewModelScope.launch {
            if (_isConnected.value) {
                singBoxManager.stopV2Ray()
                _isConnected.value = false
            } else {
                val selected = _selectedProfile.value ?: return@launch
                // ساخت کانفیگ از پروفایل (ساده‌شده)
                val config = buildConfigFromProfile(selected)
                val result = singBoxManager.startV2Ray(config, 0) // 0 = placeholder
                if (result.isSuccess) {
                    _isConnected.value = true
                }
            }
        }
    }

    private fun buildConfigFromProfile(profile: Profile): String {
        // اینجا باید کانفیگ JSON بسازید
        // فعلاً یک کانفیگ نمونه برمی‌گردانیم
        return """
        {
            "inbounds": [],
            "outbounds": [
                {
                    "type": "${profile.protocol}",
                    "server": "${profile.server}",
                    "server_port": ${profile.port},
                    "uuid": "${profile.uuid}",
                    "tls": {
                        "enabled": true,
                        "server_name": "${profile.sni}"
                    },
                    "transport": {
                        "type": "${profile.network}",
                        "path": "${profile.path}"
                    }
                }
            ]
        }
        """.trimIndent()
    }

    fun setVpnPermissionLauncher(launcher: (android.content.Intent) -> Unit) {
        // پیاده‌سازی برای درخواست مجوز VPN
    }
}
