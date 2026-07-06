package com.v2ray.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.data.Profile
import com.v2ray.app.utils.SpeedTester
import com.v2ray.app.utils.SniResult
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

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    val selectedProfile: StateFlow<Profile?> = combine(_profiles, _selectedId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _pings = MutableStateFlow<Map<String, SniResult>>(emptyMap())
    val pings: StateFlow<Map<String, SniResult>> = _pings.asStateFlow()

    init {
        loadSampleProfiles()
        startPingTimer()
    }

    private fun loadSampleProfiles() {
        _profiles.value = listOf(
            Profile(
                id = "1",
                name = "Japan - Tokyo - 01",
                type = "VLESS",
                address = "jp.example.com",
                port = 443,
                uuid = "uuid-1",
                sni = "www.google.com",
                customSni = "",
                flow = "xtls-rprx-vision",
                fingerprint = "chrome"
            ),
            Profile(
                id = "2",
                name = "Germany - Frankfurt - 02",
                type = "VLESS",
                address = "de.example.com",
                port = 443,
                uuid = "uuid-2",
                sni = "www.google.com",
                customSni = "",
                flow = "xtls-rprx-vision",
                fingerprint = "chrome"
            ),
            Profile(
                id = "3",
                name = "United States - New York - 03",
                type = "VLESS",
                address = "us.example.com",
                port = 443,
                uuid = "uuid-3",
                sni = "www.google.com",
                customSni = "",
                flow = "xtls-rprx-vision",
                fingerprint = "chrome"
            ),
            Profile(
                id = "4",
                name = "Singapore - Singapore - 01",
                type = "VLESS",
                address = "sg.example.com",
                port = 443,
                uuid = "uuid-4",
                sni = "www.google.com",
                customSni = "",
                flow = "xtls-rprx-vision",
                fingerprint = "chrome"
            )
        )
    }

    private fun startPingTimer() {
        viewModelScope.launch {
            while (true) {
                updatePings()
                kotlinx.coroutines.delay(15000)
            }
        }
    }

    private suspend fun updatePings() {
        val currentProfiles = _profiles.value
        if (currentProfiles.isEmpty()) return

        val results = currentProfiles.map { profile ->
            viewModelScope.async {
                SpeedTester.checkSni(profile.address, profile.port, 5)
            }
        }.awaitAll()

        val pingMap = currentProfiles.mapIndexed { index, profile ->
            profile.id to results[index]
        }.toMap()

        _pings.value = pingMap
    }

    // ===== توابع مدیریت پروفایل =====

    fun add(profile: Profile) {
        _profiles.update { current ->
            current + profile.copy(id = java.util.UUID.randomUUID().toString())
        }
    }

    fun delete(id: String) {
        _profiles.update { current ->
            current.filter { it.id != id }
        }
        if (_selectedId.value == id) {
            _selectedId.value = null
        }
    }

    fun update(profile: Profile) {
        _profiles.update { current ->
            current.map { if (it.id == profile.id) profile else it }
        }
    }

    fun select(id: String) {
        _selectedId.value = id
    }

    fun selectProfile(profile: Profile) {
        _selectedId.value = profile.id
    }

    // تابع جدید برای به‌روزرسانی customSni
    fun updateCustomSni(profileId: String, newSni: String) {
        _profiles.update { current ->
            current.map { profile ->
                if (profile.id == profileId) {
                    profile.copy(customSni = newSni)
                } else {
                    profile
                }
            }
        }
    }

    fun toggleConnection() {
        viewModelScope.launch {
            _isConnected.value = !_isConnected.value
        }
    }

    fun setVpnPermissionLauncher(launcher: (android.content.Intent) -> Unit) {
        // پیاده‌سازی برای درخواست مجوز VPN
    }
}
