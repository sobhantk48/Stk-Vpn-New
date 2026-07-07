package com.v2ray.app.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.MainActivity
import com.v2ray.app.data.AppDatabase
import com.v2ray.app.data.ConnectionHistory
import com.v2ray.app.data.Profile
import com.v2ray.app.service.V2RayService
import com.v2ray.app.utils.BackupManager
import com.v2ray.app.utils.SpeedTester
import com.v2ray.app.utils.SniResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    private val TAG = "MainViewModel"

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

    private var connectStartTime: Long = 0
    private var currentProfileId: String? = null
    private var activityRef: MainActivity? = null

    init {
        loadDefaultProfiles()
        startPingTimer()
        loadRecentHistory()
    }

    private fun loadDefaultProfiles() {
        val defaultLink = "vless://528b1e29-c2a8-474a-82d7-b3300591536e@104.17.115.177:443?path=%2F&security=tls&encryption=none&host=delicate-mud-ce14.sobhantk.workers.dev&fp=chrome&type=ws&sni=delicate-mud-ce14.sobhantk.workers.dev#CF%E5%AE%98%E6%96%B9%E4%BC%98%E9%80%899"

        val defaultProfile = Profile.fromLink(defaultLink) ?: Profile(
            id = "default",
            name = "CF Official 9",
            type = "VLESS",
            address = "104.17.115.177",
            port = 443,
            uuid = "528b1e29-c2a8-474a-82d7-b3300591536e",
            sni = "delicate-mud-ce14.sobhantk.workers.dev",
            fingerprint = "chrome",
            network = "ws",
            path = "/",
            host = "delicate-mud-ce14.sobhantk.workers.dev"
        )

        _profiles.value = listOf(defaultProfile)
    }

    fun setActivity(activity: MainActivity) { activityRef = activity }

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
        _profiles.update { current -> current.filter { it.id != id } }
        if (_selectedId.value == id) _selectedId.value = null
    }

    fun update(profile: Profile) {
        _profiles.update { current ->
            current.map { if (it.id == profile.id) profile else it }
        }
    }

    fun select(id: String) { _selectedId.value = id }
    fun selectProfile(profile: Profile) { _selectedId.value = profile.id }

    fun updateCustomSni(profileId: String, newSni: String) {
        _profiles.update { current ->
            current.map { if (it.id == profileId) it.copy(customSni = newSni) else it }
        }
    }

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

    suspend fun backupProfiles(): File? = withContext(Dispatchers.IO) {
        try {
            val current = _profiles.value
            if (current.isEmpty()) {
                _backupStatus.value = BackupStatus.Error("No profiles")
                return@withContext null
            }
            val file = BackupManager.backupProfiles(context, current)
            if (file != null) {
                _backupStatus.value = BackupStatus.Success("Backup saved: ${file.name}")
            } else {
                _backupStatus.value = BackupStatus.Error("Backup failed")
            }
            file
        } catch (e: Exception) {
            _backupStatus.value = BackupStatus.Error(e.message ?: "Backup error")
            null
        }
    }

    suspend fun restoreProfiles(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val restored = BackupManager.restoreProfiles(context, file)
            if (restored != null && restored.isNotEmpty()) {
                addAll(restored)
                _backupStatus.value = BackupStatus.Success("Restored ${restored.size} profiles")
                true
            } else {
                _backupStatus.value = BackupStatus.Error("Restore failed")
                false
            }
        } catch (e: Exception) {
            _backupStatus.value = BackupStatus.Error(e.message ?: "Restore error")
            false
        }
    }

    fun getBackupFiles(): List<File> = BackupManager.getBackupFiles(context)
    fun deleteBackupFile(file: File): Boolean = BackupManager.deleteBackupFile(file)
    fun clearBackupStatus() { _backupStatus.value = null }

    fun toggleConnection() {
        if (_isConnected.value) {
            context.stopService(Intent(context, V2RayService::class.java))
            _isConnected.value = false
            viewModelScope.launch {
                currentProfileId?.let { id ->
                    val profile = _profiles.value.find { it.id == id }
                    profile?.let {
                        val duration = if (connectStartTime > 0) System.currentTimeMillis() - connectStartTime else 0
                        addHistory(it, "DISCONNECT", duration)
                    }
                }
                currentProfileId = null
                connectStartTime = 0
            }
        } else {
            activityRef?.requestVpnPermission()
        }
    }

    fun onVpnPermissionGranted() {
        val selected = selectedProfile.value ?: return
        val config = buildConfigFromProfile(selected)
        Log.d(TAG, "Generated config: $config")
        val intent = Intent(context, V2RayService::class.java).apply {
            action = V2RayService.ACTION_CONNECT
            putExtra(V2RayService.EXTRA_CONFIG, config)
            putExtra(V2RayService.EXTRA_PROFILE_ID, selected.id)
        }
        context.startService(intent)
        _isConnected.value = true
        connectStartTime = System.currentTimeMillis()
        currentProfileId = selected.id
        viewModelScope.launch {
            addHistory(selected, "CONNECT")
        }
    }

    private fun buildConfigFromProfile(profile: Profile): String {
        // ===== inbound SOCKS (به جای TUN) =====
        val inbound = buildJsonObject {
            put("type", "socks")
            put("tag", "socks-in")
            put("listen", "127.0.0.1")
            put("listen_port", 1080)
        }

        // ===== outbound از پروفایل =====
        val outbound = buildJsonObject {
            put("type", profile.type.lowercase())
            put("server", profile.address)
            put("server_port", profile.port)
            put("uuid", profile.uuid)
            if (profile.flow.isNotBlank()) put("flow", profile.flow)
            val sni = profile.getEffectiveSni()
            if (sni.isNotBlank()) {
                put("tls", buildJsonObject {
                    put("enabled", true)
                    put("server_name", sni)
                    put("insecure", profile.allowInsecure)
                    put("fingerprint", profile.fingerprint)
                })
            }
            if (profile.network == "ws") {
                put("transport", buildJsonObject {
                    put("type", "ws")
                    put("path", profile.path.ifEmpty { "/" })
                    if (profile.host.isNotBlank()) {
                        put("headers", buildJsonObject {
                            put("Host", profile.host)
                        })
                    }
                })
            }
        }

        val direct = buildJsonObject {
            put("type", "direct")
            put("tag", "direct")
        }

        val config = buildJsonObject {
            put("log", buildJsonObject { put("level", "warn") })
            put("inbounds", JsonArray(listOf(inbound)))
            put("outbounds", JsonArray(listOf(outbound, direct)))
            put("route", buildJsonObject {
                put("auto_detect_interface", true)
                put("final", "direct")
            })
        }
        return config.toString()
    }

    fun startFronting() { /* TODO */ }
    fun stopFronting() { /* TODO */ }
}

sealed class BackupStatus {
    data class Success(val message: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}
