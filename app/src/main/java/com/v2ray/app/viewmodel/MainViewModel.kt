package com.v2ray.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.data.ConnectionHistory
import com.v2ray.app.data.AppDatabase
import com.v2ray.app.data.Profile
import com.v2ray.app.fronting.ProxyServer
import com.v2ray.app.utils.BackupManager
import com.v2ray.app.utils.SpeedTester
import com.v2ray.app.utils.SniResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)

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

    private val _recentHistory = MutableStateFlow<List<ConnectionHistory>>(emptyList())
    val recentHistory: StateFlow<List<ConnectionHistory>> = _recentHistory.asStateFlow()

    private val _backupStatus = MutableStateFlow<BackupStatus?>(null)
    val backupStatus: StateFlow<BackupStatus?> = _backupStatus.asStateFlow()

    // Domain Fronting
    private var proxyServer: ProxyServer? = null
    private val _frontingStatus = MutableStateFlow(false)
    val frontingStatus: StateFlow<Boolean> = _frontingStatus.asStateFlow()

    private var connectStartTime: Long = 0
    private var currentProfileId: String? = null

    init {
        loadSampleProfiles()
        startPingTimer()
        loadRecentHistory()
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

    private fun loadRecentHistory() {
        viewModelScope.launch {
            database.historyDao().getRecentHistory().collect { history ->
                _recentHistory.value = history
            }
        }
    }

    // ===== توابع مدیریت پروفایل =====

    fun add(profile: Profile) {
        _profiles.update { current ->
            current + profile.copy(id = java.util.UUID.randomUUID().toString())
        }
    }

    fun addAll(profiles: List<Profile>) {
        _profiles.update { current ->
            current + profiles.map { it.copy(id = java.util.UUID.randomUUID().toString()) }
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

    // ===== Recent Activity =====

    suspend fun addHistory(profile: Profile, action: String, duration: Long = 0) {
        val history = ConnectionHistory(
            profileId = profile.id,
            profileName = profile.name,
            server = profile.address,
            action = action,
            duration = duration
        )
        database.historyDao().insert(history)
    }

    // ===== Backup & Restore =====

    suspend fun backupProfiles(): File? = withContext(Dispatchers.IO) {
        try {
            val currentProfiles = _profiles.value
            if (currentProfiles.isEmpty()) {
                _backupStatus.value = BackupStatus.Error("No profiles to backup")
                return@withContext null
            }

            val backupFile = BackupManager.backupProfiles(getApplication(), currentProfiles)
            if (backupFile != null) {
                _backupStatus.value = BackupStatus.Success("Backup saved: ${backupFile.name}")
            } else {
                _backupStatus.value = BackupStatus.Error("Backup failed")
            }
            backupFile
        } catch (e: Exception) {
            _backupStatus.value = BackupStatus.Error(e.message ?: "Backup error")
            null
        }
    }

    suspend fun restoreProfiles(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val restored = BackupManager.restoreProfiles(getApplication(), file)
            if (restored != null && restored.isNotEmpty()) {
                addAll(restored)
                _backupStatus.value = BackupStatus.Success("Restored ${restored.size} profiles")
                true
            } else {
                _backupStatus.value = BackupStatus.Error("Restore failed or empty")
                false
            }
        } catch (e: Exception) {
            _backupStatus.value = BackupStatus.Error(e.message ?: "Restore error")
            false
        }
    }

    fun getBackupFiles(): List<File> {
        return BackupManager.getBackupFiles(getApplication())
    }

    fun deleteBackupFile(file: File): Boolean {
        return BackupManager.deleteBackupFile(file)
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    // ===== Connection Toggle =====

    fun toggleConnection() {
        viewModelScope.launch {
            if (_isConnected.value) {
                val connectedAt = connectStartTime
                val duration = if (connectedAt > 0) System.currentTimeMillis() - connectedAt else 0
                _isConnected.value = false
                currentProfileId?.let { profileId ->
                    val profile = _profiles.value.find { it.id == profileId }
                    profile?.let {
                        addHistory(it, "DISCONNECT", duration)
                    }
                }
                currentProfileId = null
                connectStartTime = 0
            } else {
                _isConnected.value = true
                connectStartTime = System.currentTimeMillis()
                selectedProfile.value?.let { profile ->
                    currentProfileId = profile.id
                    addHistory(profile, "CONNECT")
                }
            }
        }
    }

    // ===== Domain Fronting =====

    fun startFronting() {
        viewModelScope.launch {
            try {
                proxyServer = ProxyServer(getApplication())
                val started = proxyServer?.start() ?: false
                _frontingStatus.value = started
            } catch (e: Exception) {
                _frontingStatus.value = false
            }
        }
    }

    fun stopFronting() {
        proxyServer?.stop()
        proxyServer = null
        _frontingStatus.value = false
    }

    fun setVpnPermissionLauncher(launcher: (android.content.Intent) -> Unit) {
        // پیاده‌سازی برای درخواست مجوز VPN
    }
}

sealed class BackupStatus {
    data class Success(val message: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}
