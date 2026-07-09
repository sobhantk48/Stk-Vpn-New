package com.v2ray.app.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.MainActivity
import com.v2ray.app.bg.V2RayService
import com.v2ray.app.data.Profile
import com.v2ray.app.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ================== Data Classes ==================
@Serializable
data class FullBackupData(
    val version: Int = 1,
    val profiles: List<Profile>,
    val selectedProfileId: String?,
    val frontingEnabled: Boolean,
    val frontingDomain: String?,
    val sniTunnelEnabled: Boolean,
    val customSni: String?,
    val splitTunnelingEnabled: Boolean,
    val splitMode: String,
    val splitApps: List<String>
)

@Serializable
data class LogEntry(
    val message: String,
    val level: String, // INFO, WARN, ERROR, SUCCESS
    val timestamp: Long = System.currentTimeMillis()
)

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

    private val _sniTunnelEnabled = MutableStateFlow(false)
    val sniTunnelEnabled: StateFlow<Boolean> = _sniTunnelEnabled.asStateFlow()

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ================== Logs ==================
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private var activity: MainActivity? = null
    private var currentProfile: Profile? = null

    private val connectionMutex = Mutex()
    private var isConnecting = false

    data class PingResult(val latency: Int, val timestamp: Long)

    // ================== Broadcast Receivers ==================

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != V2RayService.ACTION_STATUS_UPDATE) return
            val isConnected = intent.getBooleanExtra(V2RayService.EXTRA_IS_CONNECTED, false)
            val error = intent.getStringExtra(V2RayService.EXTRA_ERROR_MESSAGE)
            _isConnected.value = isConnected
            if (!isConnected && error != null) {
                _errorMessage.value = error
            }
            if (!isConnected) {
                currentProfile = null
            }
        }
    }

    private val logReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != V2RayService.ACTION_LOG_UPDATE) return
            val message = intent.getStringExtra(V2RayService.EXTRA_LOG_MESSAGE) ?: return
            val level = intent.getStringExtra(V2RayService.EXTRA_LOG_LEVEL) ?: "INFO"
            val entry = LogEntry(message, level)
            // اضافه کردن لاگ جدید به ابتدای لیست (جدیدترین در بالا)
            _logs.value = listOf(entry) + _logs.value
            // محدود کردن تعداد لاگ‌ها به ۱۰۰۰ تا
            if (_logs.value.size > 1000) {
                _logs.value = _logs.value.take(1000)
            }
        }
    }

    fun registerReceivers(context: Context) {
        val statusFilter = IntentFilter(V2RayService.ACTION_STATUS_UPDATE)
        context.registerReceiver(statusReceiver, statusFilter, Context.RECEIVER_NOT_EXPORTED)

        val logFilter = IntentFilter(V2RayService.ACTION_LOG_UPDATE)
        context.registerReceiver(logReceiver, logFilter, Context.RECEIVER_NOT_EXPORTED)
    }

    fun unregisterReceivers(context: Context) {
        try {
            context.unregisterReceiver(statusReceiver)
        } catch (_: Exception) {}
        try {
            context.unregisterReceiver(logReceiver)
        } catch (_: Exception) {}
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ================== Profile Management ==================

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

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.setSelected(profile.id)
            loadProfiles()
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

    // ================== Connection Management ==================

    fun toggleConnection() {
        if (isConnecting) return

        viewModelScope.launch {
            connectionMutex.withLock {
                if (isConnecting) return@withLock
                isConnecting = true
                try {
                    val activity = activity ?: return@withLock
                    val profile = _selectedProfile.value ?: return@withLock

                    if (_isConnected.value) {
                        disconnect()
                    } else {
                        connect(profile, activity)
                    }
                } finally {
                    isConnecting = false
                }
            }
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
    }

    private fun disconnect() {
        val activity = activity ?: return
        activity.stopVpnService()
        currentProfile = null
    }

    fun onVpnPermissionGranted() {
        val profile = _selectedProfile.value ?: return
        currentProfile = profile
        activity?.startVpnService(profile)
    }

    // ================== SNI & Fronting ==================

    fun updateCustomSni(profileId: String, sni: String) {
        viewModelScope.launch {
            profileRepository.updateCustomSni(profileId, sni)
            loadProfiles()
        }
    }

    fun setSniTunnelEnabled(enabled: Boolean) {
        _sniTunnelEnabled.value = enabled
        val profile = _selectedProfile.value
        if (profile != null) {
            updateCustomSni(profile.id, if (enabled) "www.google.com" else "")
        }
    }

    fun startFronting() {
        val profile = _selectedProfile.value ?: return
        if (_frontingEnabled.value) return
        val frontingDomain = "www.google.com"
        viewModelScope.launch {
            profileRepository.updateFrontingDomain(profile.id, frontingDomain)
            _frontingEnabled.value = true
            loadProfiles()
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
            _frontingEnabled.value = false
            loadProfiles()
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
        currentProfile = profile
        activity.startVpnService(currentProfile!!)
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

    // ================== Full Backup & Restore ==================

    fun getBackupFiles(): List<File> {
        val dir = getApplication<Application>().filesDir
        return dir.listFiles { file ->
            file.name.startsWith("backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun backupFull() {
        viewModelScope.launch {
            try {
                val profiles = profileRepository.getAllProfiles()
                val selectedId = _selectedId.value
                val frontingEnabled = _frontingEnabled.value
                val frontingDomain = selectedProfile.value?.frontingDomain ?: ""
                val sniTunnelEnabled = _sniTunnelEnabled.value
                val customSni = selectedProfile.value?.customSni ?: ""
                val splitTunnelingEnabled = V2RayService.splitTunnelingEnabled
                val splitMode = V2RayService.splitMode.name
                val splitApps = V2RayService.splitApps.toList()

                val backupData = FullBackupData(
                    version = 1,
                    profiles = profiles,
                    selectedProfileId = selectedId,
                    frontingEnabled = frontingEnabled,
                    frontingDomain = frontingDomain,
                    sniTunnelEnabled = sniTunnelEnabled,
                    customSni = customSni,
                    splitTunnelingEnabled = splitTunnelingEnabled,
                    splitMode = splitMode,
                    splitApps = splitApps
                )

                val json = Json { 
                    prettyPrint = true
                    encodeDefaults = true
                }.encodeToString(backupData)

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

    fun restoreFull(file: File) {
        viewModelScope.launch {
            try {
                val json = file.readText()
                val backupData: FullBackupData = Json.decodeFromString(json)

                for (profile in backupData.profiles) {
                    profileRepository.insertProfile(profile)
                }
                loadProfiles()

                backupData.selectedProfileId?.let { id ->
                    val profile = _profiles.value.find { it.id == id }
                    profile?.let { selectProfile(it) }
                }

                _frontingEnabled.value = backupData.frontingEnabled
                _sniTunnelEnabled.value = backupData.sniTunnelEnabled

                V2RayService.splitTunnelingEnabled = backupData.splitTunnelingEnabled
                V2RayService.splitMode = try {
                    com.v2ray.app.model.SplitMode.valueOf(backupData.splitMode)
                } catch (_: Exception) {
                    com.v2ray.app.model.SplitMode.INCLUDE
                }
                V2RayService.splitApps.clear()
                V2RayService.splitApps.addAll(backupData.splitApps)

                _backupStatus.value = BackupStatus.Success("Restored ${backupData.profiles.size} profiles with all settings")
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error("Restore failed: ${e.message}")
            }
        }
    }
}
