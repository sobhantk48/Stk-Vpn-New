package com.v2ray.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.MainActivity
import com.v2ray.app.data.Profile
import com.v2ray.app.repository.ProfileRepository
import com.v2ray.app.bg.V2RayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ===== Backup Status =====
sealed class BackupStatus {
    data class Success(val message: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
    object Idle : BackupStatus()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val profileRepository: ProfileRepository
) : AndroidViewModel(application) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _pings = MutableStateFlow<Map<String, PingResult>>(emptyMap())
    val pings: StateFlow<Map<String, PingResult>> = _pings.asStateFlow()

    private val _frontingEnabled = MutableStateFlow(false)
    val frontingEnabled: StateFlow<Boolean> = _frontingEnabled.asStateFlow()

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

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
            val selected = _profiles.value.find { it.selected }
            _selectedProfile.value = selected
            _selectedId.value = selected?.id
        }
    }

    // ================== Profile Management ==================

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.setSelected(profile.id)
            _selectedProfile.value = profile
            _selectedId.value = profile.id
            _profiles.value = _profiles.value.map {
                it.copy(selected = it.id == profile.id)
            }
        }
    }

    fun add(profile: Profile) {
        viewModelScope.launch {
            profileRepository.insertProfile(profile)
            loadProfiles()
        }
    }

    fun delete(profileId: String) {
        viewModelScope.launch {
            val profile = _profiles.value.find { it.id == profileId }
            profile?.let { profileRepository.deleteProfile(it) }
            loadProfiles()
        }
    }

    fun select(profileId: String) {
        val profile = _profiles.value.find { it.id == profileId }
        profile?.let { selectProfile(it) }
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
        val intent = VpnService.prepare(activity)
        if (intent != null) {
            activity.requestVpnPermission()
            return
        }
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
                _selectedId.value = profileId
            }
        }
    }

    // ================== Domain Fronting ==================

    fun startFronting() {
        val profile = _selectedProfile.value ?: return
        if (_frontingEnabled.value) return

        val frontingDomain = "www.google.com"
        viewModelScope.launch {
            profileRepository.updateFrontingDomain(profile.id, frontingDomain)
            _profiles.value = _profiles.value.map {
                if (it.id == profile.id) {
                    it.copy(frontingDomain = frontingDomain)
                } else it
            }
            _selectedProfile.value = _selectedProfile.value?.copy(frontingDomain = frontingDomain)
            _selectedId.value = profile.id
            _frontingEnabled.value = true

            if (_isConnected.value) {
                disconnect()
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
            _selectedId.value = profile.id
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

    fun toggleFronting() {
        if (_frontingEnabled.value) {
            stopFronting()
        } else {
            startFronting()
        }
    }

    fun isFrontingEnabled(): Boolean = _frontingEnabled.value
    fun getCurrentProfile(): Profile? = currentProfile

    // ================== Backup & Restore ==================

    fun getBackupFiles(): List<File> {
        val dir = getApplication<Application>().filesDir
        return dir.listFiles { file ->
            file.name.startsWith("backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun backupProfiles() {
        viewModelScope.launch {
            try {
                val profiles = profileRepository.getAllProfiles()
                val json = Json.encodeToString(profiles)
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "backup_${dateFormat.format(Date())}.json"
                val file = File(getApplication<Application>().filesDir, fileName)
                file.writeText(json)
                _backupStatus.value = BackupStatus.Success("Backup saved: $fileName")
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error("Backup failed: ${e.message}")
            }
        }
    }

    fun restoreProfiles(file: File) {
        viewModelScope.launch {
            try {
                val json = file.readText()
                val profiles: List<Profile> = Json.decodeFromString(json)
                for (profile in profiles) {
                    profileRepository.insertProfile(profile)
                }
                _backupStatus.value = BackupStatus.Success("Restored ${profiles.size} profiles")
                loadProfiles()
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error("Restore failed: ${e.message}")
            }
        }
    }
}
