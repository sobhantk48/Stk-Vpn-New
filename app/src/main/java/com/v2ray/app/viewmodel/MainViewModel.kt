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
import com.v2ray.app.model.Group
import com.v2ray.app.model.Subscription
import com.v2ray.app.model.TrafficStats
import com.v2ray.app.repository.GroupRepository
import com.v2ray.app.repository.ProfileRepository
import com.v2ray.app.repository.SubscriptionRepository
import com.v2ray.app.repository.TrafficStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

// Data Classes
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
    val splitApps: List<String>,
    val subscriptions: List<Subscription> = emptyList(),
    val groups: List<Group> = emptyList()
)

@Serializable
data class LogEntry(
    val message: String,
    val level: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class TrafficData(
    val download: Long = 0,
    val upload: Long = 0,
    val connectionTime: Long = 0
)

sealed class BackupStatus {
    data class Success(val message: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
    object Idle : BackupStatus()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val profileRepository: ProfileRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val groupRepository: GroupRepository,
    private val trafficStatsRepository: TrafficStatsRepository
) : AndroidViewModel(application) {

    // ================== State Flows ==================
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

    // Logs
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // Traffic
    private val _traffic = MutableStateFlow<TrafficData>(TrafficData())
    val traffic: StateFlow<TrafficData> = _traffic.asStateFlow()

    // Search
    private val _logFilter = MutableStateFlow("")
    val logFilter: StateFlow<String> = _logFilter.asStateFlow()
    private val _filteredLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val filteredLogs: StateFlow<List<LogEntry>> = _filteredLogs.asStateFlow()

    // Subscriptions
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    // Groups
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    // Traffic stats per proxy
    private val _trafficStats = MutableStateFlow<List<TrafficStats>>(emptyList())
    val trafficStats: StateFlow<List<TrafficStats>> = _trafficStats.asStateFlow()

    // Search in proxies
    private val _proxySearchQuery = MutableStateFlow("")
    val proxySearchQuery: StateFlow<String> = _proxySearchQuery.asStateFlow()
    private val _filteredProfiles = MutableStateFlow<List<Profile>>(emptyList())
    val filteredProfiles: StateFlow<List<Profile>> = _filteredProfiles.asStateFlow()

    // Bulk selection
    private val _selectedProxyIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedProxyIds: StateFlow<Set<String>> = _selectedProxyIds.asStateFlow()

    // ================== Private ==================
    private var activity: MainActivity? = null
    private var currentProfile: Profile? = null
    private val connectionMutex = Mutex()
    private var isConnecting = false

    data class PingResult(val latency: Int, val timestamp: Long)

    init {
        loadSubscriptions()
        loadGroups()
        loadTrafficStats()
        observeProfiles()
    }

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
            if (!isConnected) currentProfile = null
        }
    }

    private val logReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != V2RayService.ACTION_LOG_UPDATE) return
            val message = intent.getStringExtra(V2RayService.EXTRA_LOG_MESSAGE) ?: return
            val level = intent.getStringExtra(V2RayService.EXTRA_LOG_LEVEL) ?: "INFO"
            val entry = LogEntry(message, level)
            _logs.value = listOf(entry) + _logs.value
            if (_logs.value.size > 1000) _logs.value = _logs.value.take(1000)
            applyLogFilter()
        }
    }

    private val trafficReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != V2RayService.ACTION_TRAFFIC_UPDATE) return
            val download = intent.getLongExtra(V2RayService.EXTRA_TRAFFIC_DOWNLOAD, 0)
            val upload = intent.getLongExtra(V2RayService.EXTRA_TRAFFIC_UPLOAD, 0)
            val connectionTime = intent.getLongExtra(V2RayService.EXTRA_CONNECTION_TIME, 0)
            _traffic.value = TrafficData(download, upload, connectionTime)
            // Update per-proxy stats
            currentProfile?.let { profile ->
                viewModelScope.launch {
                    trafficStatsRepository.updateStats(profile.id, download, upload)
                    loadTrafficStats()
                }
            }
        }
    }

    // ================== Registration ==================
    fun registerReceivers(context: Context) {
        val statusFilter = IntentFilter(V2RayService.ACTION_STATUS_UPDATE)
        context.registerReceiver(statusReceiver, statusFilter, Context.RECEIVER_NOT_EXPORTED)
        val logFilter = IntentFilter(V2RayService.ACTION_LOG_UPDATE)
        context.registerReceiver(logReceiver, logFilter, Context.RECEIVER_NOT_EXPORTED)
        val trafficFilter = IntentFilter(V2RayService.ACTION_TRAFFIC_UPDATE)
        context.registerReceiver(trafficReceiver, trafficFilter, Context.RECEIVER_NOT_EXPORTED)
    }

    fun unregisterReceivers(context: Context) {
        try { context.unregisterReceiver(statusReceiver) } catch (_: Exception) {}
        try { context.unregisterReceiver(logReceiver) } catch (_: Exception) {}
        try { context.unregisterReceiver(trafficReceiver) } catch (_: Exception) {}
    }

    // ================== Logs ==================
    fun clearLogs() { _logs.value = emptyList(); applyLogFilter() }
    fun setLogFilter(filter: String) { _logFilter.value = filter; applyLogFilter() }
    private fun applyLogFilter() {
        val filter = _logFilter.value.lowercase(Locale.getDefault())
        _filteredLogs.value = if (filter.isEmpty()) _logs.value
            else _logs.value.filter { it.message.lowercase().contains(filter) || it.level.lowercase().contains(filter) }
    }
    fun clearError() { _errorMessage.value = null }

    // ================== Profiles ==================
    fun setActivity(activity: MainActivity) { this.activity = activity; loadProfiles() }

    private fun observeProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { list ->
                _profiles.value = list
                applyProxySearch()
                val selected = list.find { it.selected }
                _selectedProfile.value = selected
                _selectedId.value = selected?.id
            }
        }
    }

    fun loadProfiles() { /* handled by observe */ }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.setSelected(profile.id)
            loadProfiles()
        }
    }

    fun addProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.insertProfile(profile)
            loadProfiles()
        }
    }

    fun deleteProfile(profileId: String) {
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

    // Bulk selection
    fun toggleProxySelection(proxyId: String) {
        _selectedProxyIds.value = if (_selectedProxyIds.value.contains(proxyId)) {
            _selectedProxyIds.value - proxyId
        } else {
            _selectedProxyIds.value + proxyId
        }
    }

    fun clearSelection() { _selectedProxyIds.value = emptySet() }

    fun bulkDeleteSelected() {
        viewModelScope.launch {
            _selectedProxyIds.value.forEach { id ->
                val profile = _profiles.value.find { it.id == id }
                profile?.let { profileRepository.deleteProfile(it) }
            }
            _selectedProxyIds.value = emptySet()
            loadProfiles()
        }
    }

    fun getTrafficForProxy(proxyId: String): Long {
        return _trafficStats.value.find { it.proxyId == proxyId }?.download ?: 0
    }

    // Search in proxies
    fun setProxySearchQuery(query: String) {
        _proxySearchQuery.value = query
        applyProxySearch()
    }

    private fun applyProxySearch() {
        val query = _proxySearchQuery.value.lowercase(Locale.getDefault())
        val all = _profiles.value
        _filteredProfiles.value = if (query.isEmpty()) all
            else all.filter { it.name.lowercase().contains(query) || it.type.lowercase().contains(query) }
    }

    // ================== Subscriptions ==================
    private fun loadSubscriptions() {
        viewModelScope.launch {
            subscriptionRepository.getAllSubscriptions().collect { list ->
                _subscriptions.value = list
            }
        }
    }

    fun addSubscription(url: String, name: String = "") {
        viewModelScope.launch {
            val sub = Subscription(url = url, name = name)
            subscriptionRepository.addSubscription(sub)
            loadSubscriptions()
        }
    }

    fun removeSubscription(id: String) {
        viewModelScope.launch {
            subscriptionRepository.removeSubscription(id)
            loadSubscriptions()
        }
    }

    fun updateSubscription(sub: Subscription) {
        viewModelScope.launch {
            subscriptionRepository.updateSubscription(sub)
            loadSubscriptions()
        }
    }

    fun parseSubscription(url: String): List<Profile> {
        // این تابع باید لینک اشتراک را گرفته و لیست پروفایل‌ها را برگرداند
        // پیاده‌سازی کامل نیاز به HTTP client و parsing دارد
        return emptyList()
    }

    fun importFromSubscription(url: String) {
        viewModelScope.launch {
            try {
                val profiles = parseSubscription(url)
                if (profiles.isNotEmpty()) {
                    profiles.forEach { profileRepository.insertProfile(it) }
                    addSubscription(url)
                    loadProfiles()
                    _errorMessage.value = "Imported ${profiles.size} profiles from subscription"
                } else {
                    _errorMessage.value = "No valid profiles found in subscription"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to parse subscription: ${e.message}"
            }
        }
    }

    // ================== Groups ==================
    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { list ->
                _groups.value = list
                if (_selectedGroupId.value == null && list.isNotEmpty()) {
                    _selectedGroupId.value = list.firstOrNull { it.selected }?.id ?: list.first().id
                }
            }
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            val group = Group(name = name)
            groupRepository.addGroup(group)
            loadGroups()
        }
    }

    fun removeGroup(id: String) {
        viewModelScope.launch {
            groupRepository.removeGroup(id)
            if (_selectedGroupId.value == id) _selectedGroupId.value = null
            loadGroups()
        }
    }

    fun selectGroup(id: String) {
        _selectedGroupId.value = id
        viewModelScope.launch {
            _groups.value.find { it.id == id }?.let { group ->
                groupRepository.updateGroup(group.copy(selected = true))
            }
            loadGroups()
        }
    }

    fun getProfilesForGroup(groupId: String): List<Profile> {
        val group = _groups.value.find { it.id == groupId }
        return group?.proxyIds?.mapNotNull { id -> _profiles.value.find { it.id == id } } ?: _profiles.value
    }

    fun addProfileToGroup(profileId: String, groupId: String) {
        viewModelScope.launch {
            val group = _groups.value.find { it.id == groupId }
            if (group != null && !group.proxyIds.contains(profileId)) {
                groupRepository.updateGroup(group.copy(proxyIds = group.proxyIds + profileId))
                loadGroups()
            }
        }
    }

    fun removeProfileFromGroup(profileId: String, groupId: String) {
        viewModelScope.launch {
            val group = _groups.value.find { it.id == groupId }
            if (group != null) {
                groupRepository.updateGroup(group.copy(proxyIds = group.proxyIds - profileId))
                loadGroups()
            }
        }
    }

    // ================== Traffic Stats ==================
    private fun loadTrafficStats() {
        viewModelScope.launch {
            trafficStatsRepository.getAllStats().collect { list ->
                _trafficStats.value = list
            }
        }
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
                    if (_isConnected.value) disconnect() else connect(profile, activity)
                } finally { isConnecting = false }
            }
        }
    }

    private fun connect(profile: Profile, activity: MainActivity) {
        val intent = VpnService.prepare(activity)
        if (intent != null) { activity.requestVpnPermission(); return }
        currentProfile = profile
        activity.startVpnService(profile)
    }

    private fun disconnect() {
        activity?.stopVpnService()
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
        if (intent != null) { activity.requestVpnPermission(); return }
        currentProfile = profile
        activity.startVpnService(currentProfile!!)
    }

    fun toggleFronting() {
        if (_frontingEnabled.value) stopFronting() else startFronting()
    }

    fun isFrontingEnabled(): Boolean = _frontingEnabled.value
    fun getCurrentProfile(): Profile? = currentProfile

    // ================== Backup & Restore ==================
    fun getBackupFiles(): List<File> {
        val dir = getApplication<Application>().filesDir
        return dir.listFiles { it.name.startsWith("backup_") && it.name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun backupFull() {
        viewModelScope.launch {
            try {
                val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
                val backupData = FullBackupData(
                    profiles = profiles,
                    selectedProfileId = _selectedId.value,
                    frontingEnabled = _frontingEnabled.value,
                    frontingDomain = selectedProfile.value?.frontingDomain ?: "",
                    sniTunnelEnabled = _sniTunnelEnabled.value,
                    customSni = selectedProfile.value?.customSni ?: "",
                    splitTunnelingEnabled = V2RayService.splitTunnelingEnabled,
                    splitMode = V2RayService.splitMode.name,
                    splitApps = V2RayService.splitApps.toList(),
                    subscriptions = _subscriptions.value,
                    groups = _groups.value
                )
                val json = Json { prettyPrint = true; encodeDefaults = true }.encodeToString(backupData)
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
                // Restore profiles
                backupData.profiles.forEach { profileRepository.insertProfile(it) }
                // Restore subscriptions
                backupData.subscriptions.forEach { subscriptionRepository.addSubscription(it) }
                // Restore groups
                backupData.groups.forEach { groupRepository.addGroup(it) }
                // Restore settings
                backupData.selectedProfileId?.let { id ->
                    val profile = _profiles.value.find { it.id == id }
                    profile?.let { selectProfile(it) }
                }
                _frontingEnabled.value = backupData.frontingEnabled
                _sniTunnelEnabled.value = backupData.sniTunnelEnabled
                V2RayService.splitTunnelingEnabled = backupData.splitTunnelingEnabled
                V2RayService.splitMode = try {
                    com.v2ray.app.model.SplitMode.valueOf(backupData.splitMode)
                } catch (_: Exception) { com.v2ray.app.model.SplitMode.INCLUDE }
                V2RayService.splitApps.clear()
                V2RayService.splitApps.addAll(backupData.splitApps)
                loadProfiles()
                loadSubscriptions()
                loadGroups()
                _backupStatus.value = BackupStatus.Success("Restored all data successfully")
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error("Restore failed: ${e.message}")
            }
        }
    }
}
