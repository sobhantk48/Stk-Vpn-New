package com.v2ray.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.MainActivity
import com.v2ray.app.data.ConnectionState
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.repository.ProfileRepository
import com.v2ray.app.bg.V2RayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val profileRepository: ProfileRepository
) : AndroidViewModel(application) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _pings = MutableStateFlow<Map<String, PingResult>>(emptyMap())
    val pings: StateFlow<Map<String, PingResult>> = _pings.asStateFlow()

    private val _frontingEnabled = MutableStateFlow(false)
    val frontingEnabled: StateFlow<Boolean> = _frontingEnabled.asStateFlow()

    private var activity: MainActivity? = null
    private var currentProfile: Profile? = null

    data class PingResult(val latency: Int, val timestamp: Long)

    fun setActivity(activity: MainActivity) {
        this.activity = activity
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _profiles.value = profileRepository.getAllProfiles()
            _selectedProfile.value = _profiles.value.find { it.selected }
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.setSelected(profile.id)
            _selectedProfile.value = profile
            _profiles.value = _profiles.value.map {
                it.copy(selected = it.id == profile.id)
            }
        }
    }

    fun toggleConnection() {
        val activity = activity ?: return
        val profile = _selectedProfile.value ?: return

        if (_isConnected.value) {
            disconnect()
        } else {
            connect(profile, activity)
        }
    }

    private fun connect(profile: Profile, activity: MainActivity) {
        // درخواست مجوز VPN
        val intent = VpnService.prepare(activity)
        if (intent != null) {
            activity.requestVpnPermission()
            return
        }
        // اگر مجوز داریم، مستقیماً وصل می‌شویم
        currentProfile = profile
        activity.startVpnService(profile)
        _isConnected.value = true
    }

    private fun disconnect() {
        val activity = activity ?: return
        activity.stopVpnService()
        _isConnected.value = false
        currentProfile = null
    }

    fun onVpnPermissionGranted() {
        val profile = _selectedProfile.value ?: return
        currentProfile = profile
        activity?.startVpnService(profile)
        _isConnected.value = true
    }

    fun updateCustomSni(profileId: String, sni: String) {
        viewModelScope.launch {
            profileRepository.updateCustomSni(profileId, sni)
            _profiles.value = _profiles.value.map {
                if (it.id == profileId) it.copy(customSni = sni) else it
            }
            if (_selectedProfile.value?.id == profileId) {
                _selectedProfile.value = _selectedProfile.value?.copy(customSni = sni)
            }
        }
    }

    // ================== Domain Fronting ==================

    fun startFronting() {
        val profile = _selectedProfile.value ?: return
        if (_frontingEnabled.value) return

        // تنظیم دامنه‌ی fronting (مثلاً google.com)
        val frontingDomain = "www.google.com"
        viewModelScope.launch {
            profileRepository.updateFrontingDomain(profile.id, frontingDomain)
            _profiles.value = _profiles.value.map {
                if (it.id == profile.id) {
                    it.copy(frontingDomain = frontingDomain)
                } else it
            }
            _selectedProfile.value = _selectedProfile.value?.copy(frontingDomain = frontingDomain)
            _frontingEnabled.value = true

            // اگر متصل هستیم، اتصال را با کانفیگ جدید برقرار کنیم
            if (_isConnected.value) {
                disconnect()
                // اتصال مجدد با fronting
                val activity = activity ?: return@launch
                connectWithFronting(profile, activity, frontingDomain)
            }
        }
    }

    fun stopFronting() {
        val profile = _selectedProfile.value ?: return
        if (!_frontingEnabled.value) return

        viewModelScope.launch {
            profileRepository.updateFrontingDomain(profile.id, "")
            _profiles.value = _profiles.value.map {
                if (it.id == profile.id) {
                    it.copy(frontingDomain = "")
                } else it
            }
            _selectedProfile.value = _selectedProfile.value?.copy(frontingDomain = "")
            _frontingEnabled.value = false

            if (_isConnected.value) {
                disconnect()
                val activity = activity ?: return@launch
                connect(profile, activity)
            }
        }
    }

    private fun connectWithFronting(profile: Profile, activity: MainActivity, frontingDomain: String) {
        val intent = VpnService.prepare(activity)
        if (intent != null) {
            activity.requestVpnPermission()
            return
        }
        currentProfile = profile.copy(frontingDomain = frontingDomain)
        activity.startVpnService(currentProfile!!)
        _isConnected.value = true
    }

    // ================== Helpers ==================

    fun getCurrentProfile(): Profile? = currentProfile

    fun isFrontingEnabled(): Boolean = _frontingEnabled.value

    fun toggleFronting() {
        if (_frontingEnabled.value) {
            stopFronting()
        } else {
            startFronting()
        }
    }
}
