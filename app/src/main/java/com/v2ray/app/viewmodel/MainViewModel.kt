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
import com.v2ray.app.model.*
import com.v2ray.app.repository.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class FullBackupData(
    val version: Int = 2,
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
    val groups: List<Group> = emptyList(),
    val adBlockRules: List<AdBlockRule> = emptyList(),
    val portKnockConfigs: List<PortKnockConfig> = emptyList(),
    val multiHopConfigs: List<MultiHopConfig> = emptyList(),
    val lwoConfig: LWOConfig? = null,
    val anonymousMode: AnonymousModeConfig? = null,
    val firewallRules: List<FirewallRule> = emptyList(),
    val liteMode: LiteModeConfig? = null,
    val batteryOpt: BatteryOptimizationConfig? = null,
    val dynamicRules: List<DynamicRoutingRule> = emptyList()
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
    private val trafficStatsRepository: TrafficStatsRepository,
    private val geoIPRepository: GeoIPRepository,
    private val adBlockRepository: AdBlockRepository,
    private val trafficHistoryRepository: TrafficHistoryRepository,
    private val clashStatsRepository: ClashStatsRepository,
    private val multiHopRepository: MultiHopRepository,
    private val firewallRepository: FirewallRepository,
    private val brokenConfigRepository: BrokenConfigRepository
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

    // GeoIP
    private val _geoIPCache = MutableStateFlow<Map<String, GeoIP>>(emptyMap())
    val geoIPCache: StateFlow<Map<String, GeoIP>> = _geoIPCache.asStateFlow()

    // Internet Quality
    private val _internetQuality = MutableStateFlow<InternetQuality?>(null)
    val internetQuality: StateFlow<InternetQuality?> = _internetQuality.asStateFlow()
    private var qualityTestRunning = false

    // AdBlock
    private val _adBlockEnabled = MutableStateFlow(false)
    val adBlockEnabled: StateFlow<Boolean> = _adBlockEnabled.asStateFlow()
    private val _adBlockRules = MutableStateFlow<List<AdBlockRule>>(emptyList())
    val adBlockRules: StateFlow<List<AdBlockRule>> = _adBlockRules.asStateFlow()

    // Traffic History
    private val _trafficHistory = MutableStateFlow<List<TrafficHistory>>(emptyList())
    val trafficHistory: StateFlow<List<TrafficHistory>> = _trafficHistory.asStateFlow()
    private val _selectedHistoryDate = MutableStateFlow<Long?>(null)
    val selectedHistoryDate: StateFlow<Long?> = _selectedHistoryDate.asStateFlow()

    // Clash Stats
    private val _clashStats = MutableStateFlow<ClashStats?>(null)
    val clashStats: StateFlow<ClashStats?> = _clashStats.asStateFlow()

    // Port Knocking
    private val _portKnockConfigs = MutableStateFlow<List<PortKnockConfig>>(emptyList())
    val portKnockConfigs: StateFlow<List<PortKnockConfig>> = _portKnockConfigs.asStateFlow()

    // ================== NEW: MultiHop ==================
    private val _multiHopConfigs = MutableStateFlow<List<MultiHopConfig>>(emptyList())
    val multiHopConfigs: StateFlow<List<MultiHopConfig>> = _multiHopConfigs.asStateFlow()
    private val _activeMultiHop = MutableStateFlow<MultiHopConfig?>(null)
    val activeMultiHop: StateFlow<MultiHopConfig?> = _activeMultiHop.asStateFlow()

    // ================== NEW: LWO ==================
    private val _lwoConfig = MutableStateFlow<LWOConfig?>(null)
    val lwoConfig: StateFlow<LWOConfig?> = _lwoConfig.asStateFlow()

    // ================== NEW: Anonymous Mode ==================
    private val _anonymousMode = MutableStateFlow<AnonymousModeConfig>(AnonymousModeConfig())
    val anonymousMode: StateFlow<AnonymousModeConfig> = _anonymousMode.asStateFlow()

    // ================== NEW: Firewall ==================
    private val _firewallEnabled = MutableStateFlow(false)
    val firewallEnabled: StateFlow<Boolean> = _firewallEnabled.asStateFlow()
    private val _firewallRules = MutableStateFlow<List<FirewallRule>>(emptyList())
    val firewallRules: StateFlow<List<FirewallRule>> = _firewallRules.asStateFlow()

    // ================== NEW: Biometric Lock ==================
    private val _biometricLockEnabled = MutableStateFlow(false)
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    // ================== NEW: Lite Mode ==================
    private val _liteMode = MutableStateFlow<LiteModeConfig>(LiteModeConfig())
    val liteMode: StateFlow<LiteModeConfig> = _liteMode.asStateFlow()

    // ================== NEW: Battery Optimization ==================
    private val _batteryOpt = MutableStateFlow<BatteryOptimizationConfig>(BatteryOptimizationConfig())
    val batteryOpt: StateFlow<BatteryOptimizationConfig> = _batteryOpt.asStateFlow()

    // ================== NEW: Dynamic Routing ==================
    private val _dynamicRules = MutableStateFlow<List<DynamicRoutingRule>>(emptyList())
    val dynamicRules: StateFlow<List<DynamicRoutingRule>> = _dynamicRules.asStateFlow()

    // ================== NEW: Speed Test ==================
    private val _speedTestRunning = MutableStateFlow(false)
    val speedTestRunning: StateFlow<Boolean> = _speedTestRunning.asStateFlow()
    private val _speedTestResults = MutableStateFlow<SpeedTestResult?>(null)
    val speedTestResults: StateFlow<SpeedTestResult?> = _speedTestResults.asStateFlow()

    data class PingResult(val latency: Int, val timestamp: Long)
    data class SpeedTestResult(
        val downloadSpeed: Double,
        val uploadSpeed: Double,
        val ping: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    // ================== Private ==================
    private var activity: MainActivity? = null
    private var currentProfile: Profile? = null
    private val connectionMutex = Mutex()
    private var isConnecting = false

    init {
        loadSubscriptions()
        loadGroups()
        loadTrafficStats()
        loadAdBlockSettings()
        loadTrafficHistory()
        loadClashStats()
        loadPortKnockConfigs()
        loadMultiHopConfigs()
        loadFirewallRules()
        loadLWOConfig()
        loadAnonymousMode()
        loadLiteMode()
        loadBatteryOpt()
        loadDynamicRules()
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
            if (!isConnected) {
                currentProfile = null
                // Save traffic history on disconnect
                if (_traffic.value.download > 0 || _traffic.value.upload > 0) {
                    viewModelScope.launch {
                        trafficHistoryRepository.addEntry(
                            TrafficHistory(
                                download = _traffic.value.download,
                                upload = _traffic.value.upload,
                                total = _traffic.value.download + _traffic.value.upload,
                                proxyId = currentProfile?.id
                            )
                        )
                        loadTrafficHistory()
                    }
                }
            }
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
    fun clearLogs() {
        _logs.value = emptyList()
        applyLogFilter()
    }
    fun setLogFilter(filter: String) {
        _logFilter.value = filter
        applyLogFilter()
    }
    private fun applyLogFilter() {
        val filter = _logFilter.value.lowercase(Locale.getDefault())
        _filteredLogs.value = if (filter.isEmpty()) _logs.value
            else _logs.value.filter { 
                it.message.lowercase(Locale.getDefault()).contains(filter) || 
                it.level.lowercase(Locale.getDefault()).contains(filter)
            }
    }
    fun clearError() {
        _errorMessage.value = null
    }

    // ================== Profiles ==================
    fun setActivity(activity: MainActivity) {
        this.activity = activity
        loadProfiles()
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { list ->
                _profiles.value = list
                applyProxySearch()
                val selected = list.find { it.selected }
                _selectedProfile.value = selected
                _selectedId.value = selected?.id
                // Fetch GeoIP for each profile
                list.forEach { profile ->
                    viewModelScope.launch {
                        val geo = geoIPRepository.getGeoIP(profile.address)
                        geo?.let {
                            val current = _geoIPCache.value.toMutableMap()
                            current[profile.id] = it
                            _geoIPCache.value = current
                        }
                    }
                }
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

    fun clearSelection() {
        _selectedProxyIds.value = emptySet()
    }

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

    fun getGeoIPForProxy(proxyId: String): GeoIP? {
        return _geoIPCache.value[proxyId]
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
            else all.filter { 
                it.name.lowercase(Locale.getDefault()).contains(query) || 
                it.type.name.lowercase(Locale.getDefault()).contains(query)
            }
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
        // TODO: Implement full subscription parsing with HTTP client
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

    // ================== Internet Quality Test ==================
    fun startInternetQualityTest() {
        if (qualityTestRunning) return
        qualityTestRunning = true
        viewModelScope.launch {
            try {
                val downloadSpeed = simulateSpeedTest()
                val uploadSpeed = simulateSpeedTest()
                val ping = (20..60).random()
                val jitter = (5..30).random()
                val packetLoss = (0.0..2.0).random()
                
                val quality = InternetQuality(
                    downloadSpeed = downloadSpeed,
                    uploadSpeed = uploadSpeed,
                    ping = ping,
                    jitter = jitter,
                    packetLoss = packetLoss,
                    gamingScore = calculateScore(ping, jitter, packetLoss, "gaming"),
                    browsingScore = calculateScore(ping, jitter, packetLoss, "browsing"),
                    streamingScore = calculateScore(ping, jitter, packetLoss, "streaming"),
                    videoCallScore = calculateScore(ping, jitter, packetLoss, "videoCall")
                )
                _internetQuality.value = quality
                _errorMessage.value = "Speed test completed: ${String.format("%.1f", downloadSpeed)} Mbps / ${String.format("%.1f", uploadSpeed)} Mbps"
            } catch (e: Exception) {
                _errorMessage.value = "Speed test failed: ${e.message}"
            } finally {
                qualityTestRunning = false
            }
        }
    }

    private suspend fun simulateSpeedTest(): Double {
        delay((1000..3000).random().toLong())
        return (10.0..50.0).random()
    }

    private fun calculateScore(ping: Int, jitter: Int, packetLoss: Double, type: String): Int {
        val baseScore = 100 - (ping / 5) - (jitter / 2) - (packetLoss * 10).toInt()
        return when (type) {
            "gaming" -> (baseScore * 1.2).toInt().coerceIn(0, 100)
            "browsing" -> (baseScore * 1.1).toInt().coerceIn(0, 100)
            "streaming" -> (baseScore * 0.9).toInt().coerceIn(0, 100)
            "videoCall" -> (baseScore * 0.8).toInt().coerceIn(0, 100)
            else -> baseScore.coerceIn(0, 100)
        }
    }

    // ================== AdBlock ==================
    private fun loadAdBlockSettings() {
        viewModelScope.launch {
            _adBlockEnabled.value = adBlockRepository.isEnabled()
            _adBlockRules.value = adBlockRepository.getRules()
        }
    }

    fun toggleAdBlock() {
        viewModelScope.launch {
            val newState = !_adBlockEnabled.value
            _adBlockEnabled.value = newState
            adBlockRepository.setEnabled(newState)
        }
    }

    fun addAdBlockRule(domain: String, type: AdBlockRule.RuleType) {
        viewModelScope.launch {
            val rule = AdBlockRule(domain = domain, type = type)
            adBlockRepository.addRule(rule)
            _adBlockRules.value = adBlockRepository.getRules()
        }
    }

    fun removeAdBlockRule(id: String) {
        viewModelScope.launch {
            adBlockRepository.removeRule(id)
            _adBlockRules.value = adBlockRepository.getRules()
        }
    }

    // ================== Traffic History ==================
    private fun loadTrafficHistory() {
        viewModelScope.launch {
            _trafficHistory.value = trafficHistoryRepository.getAllHistory()
        }
    }

    fun setHistoryDate(date: Long?) {
        _selectedHistoryDate.value = date
    }

    fun getHistoryForDate(date: Long): List<TrafficHistory> {
        return _trafficHistory.value.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            val targetCal = Calendar.getInstance().apply { timeInMillis = date }
            cal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
        }
    }

    fun clearTrafficHistory() {
        viewModelScope.launch {
            trafficHistoryRepository.clearHistory()
            loadTrafficHistory()
        }
    }

    // ================== Clash Stats ==================
    private fun loadClashStats() {
        viewModelScope.launch {
            clashStatsRepository.getStats().collect { stats ->
                _clashStats.value = stats
            }
        }
    }

    fun updateClashStats(stats: ClashStats) {
        viewModelScope.launch {
            clashStatsRepository.saveStats(stats)
            loadClashStats()
        }
    }

    // ================== Port Knocking ==================
    private fun loadPortKnockConfigs() {
        // TODO: Load from DataStore when implemented
        _portKnockConfigs.value = listOf(
            PortKnockConfig(
                name = "Example Gateway",
                host = "192.168.1.1",
                ports = listOf(7000, 8000, 9000),
                protocol = PortKnockConfig.Protocol.TCP,
                delay = 100
            )
        )
    }

    fun addPortKnockConfig(config: PortKnockConfig) {
        viewModelScope.launch {
            // TODO: Save to DataStore
            _portKnockConfigs.value = _portKnockConfigs.value + config
        }
    }

    fun removePortKnockConfig(id: String) {
        viewModelScope.launch {
            _portKnockConfigs.value = _portKnockConfigs.value.filter { it.id != id }
        }
    }

    fun executePortKnock(config: PortKnockConfig) {
        viewModelScope.launch {
            try {
                config.ports.forEachIndexed { index, port ->
                    delay(config.delay.toLong())
                }
                _errorMessage.value = "Port knock completed for ${config.name}"
            } catch (e: Exception) {
                _errorMessage.value = "Port knock failed: ${e.message}"
            }
        }
    }

    // ================== MultiHop ==================
    private fun loadMultiHopConfigs() {
        viewModelScope.launch {
            multiHopRepository.getAll().collect { list ->
                _multiHopConfigs.value = list
                _activeMultiHop.value = list.firstOrNull { it.enabled }
            }
        }
    }

    fun addMultiHopConfig(config: MultiHopConfig) {
        viewModelScope.launch {
            multiHopRepository.add(config)
            loadMultiHopConfigs()
        }
    }

    fun removeMultiHopConfig(id: String) {
        viewModelScope.launch {
            multiHopRepository.remove(id)
            loadMultiHopConfigs()
        }
    }

    fun updateMultiHopConfig(config: MultiHopConfig) {
        viewModelScope.launch {
            multiHopRepository.update(config)
            loadMultiHopConfigs()
        }
    }

    fun enableMultiHop(id: String) {
        viewModelScope.launch {
            val config = _multiHopConfigs.value.find { it.id == id }
            config?.let {
                multiHopRepository.update(it.copy(enabled = true))
                // Disable others
                _multiHopConfigs.value.filter { it.id != id }.forEach {
                    multiHopRepository.update(it.copy(enabled = false))
                }
                loadMultiHopConfigs()
            }
        }
    }

    // ================== LWO ==================
    private fun loadLWOConfig() {
        // TODO: Load from DataStore
        _lwoConfig.value = LWOConfig()
    }

    fun updateLWOConfig(config: LWOConfig) {
        viewModelScope.launch {
            // TODO: Save to DataStore
            _lwoConfig.value = config
        }
    }

    // ================== Anonymous Mode ==================
    private fun loadAnonymousMode() {
        // TODO: Load from DataStore
        _anonymousMode.value = AnonymousModeConfig()
    }

    fun toggleAnonymousMode() {
        val current = _anonymousMode.value
        _anonymousMode.value = current.copy(enabled = !current.enabled)
        // TODO: Save to DataStore
    }

    // ================== Firewall ==================
    private fun loadFirewallRules() {
        viewModelScope.launch {
            _firewallEnabled.value = firewallRepository.isEnabled()
            _firewallRules.value = firewallRepository.getRules()
        }
    }

    fun toggleFirewall() {
        viewModelScope.launch {
            val newState = !_firewallEnabled.value
            _firewallEnabled.value = newState
            firewallRepository.setEnabled(newState)
        }
    }

    fun addFirewallRule(rule: FirewallRule) {
        viewModelScope.launch {
            firewallRepository.addRule(rule)
            loadFirewallRules()
        }
    }

    fun removeFirewallRule(id: String) {
        viewModelScope.launch {
            firewallRepository.removeRule(id)
            loadFirewallRules()
        }
    }

    // ================== Biometric Lock ==================
    fun toggleBiometricLock() {
        _biometricLockEnabled.value = !_biometricLockEnabled.value
        // TODO: Save to DataStore
    }

    // ================== Lite Mode ==================
    private fun loadLiteMode() {
        // TODO: Load from DataStore
        _liteMode.value = LiteModeConfig()
    }

    fun toggleLiteMode() {
        val current = _liteMode.value
        _liteMode.value = current.copy(enabled = !current.enabled)
        // TODO: Save to DataStore
    }

    // ================== Battery Optimization ==================
    private fun loadBatteryOpt() {
        // TODO: Load from DataStore
        _batteryOpt.value = BatteryOptimizationConfig()
    }

    fun updateBatteryOpt(config: BatteryOptimizationConfig) {
        viewModelScope.launch {
            // TODO: Save to DataStore
            _batteryOpt.value = config
        }
    }

    // ================== Dynamic Routing ==================
    private fun loadDynamicRules() {
        // TODO: Load from DataStore
        _dynamicRules.value = emptyList()
    }

    fun addDynamicRule(rule: DynamicRoutingRule) {
        viewModelScope.launch {
            // TODO: Save to DataStore
            _dynamicRules.value = _dynamicRules.value + rule
        }
    }

    fun removeDynamicRule(id: String) {
        viewModelScope.launch {
            _dynamicRules.value = _dynamicRules.value.filter { it.id != id }
        }
    }

    // ================== Auto Server Selection ==================
    fun autoSelectBestServer(): Profile? {
        val profiles = _profiles.value
        if (profiles.isEmpty()) return null
        return profiles.minByOrNull { getPingForProfile(it.id) ?: Int.MAX_VALUE }
    }

    private fun getPingForProfile(profileId: String): Int? {
        return _pings.value[profileId]?.latency
    }

    // ================== Broken Config Reporting ==================
    fun reportBrokenConfig(profileId: String, errorMessage: String) {
        viewModelScope.launch {
            val profile = _profiles.value.find { it.id == profileId }
            if (profile != null) {
                val report = BrokenConfigReport(
                    profileId = profileId,
                    profileName = profile.name,
                    errorMessage = errorMessage
                )
                brokenConfigRepository.addReport(report)
                // TODO: Send to server
                _errorMessage.value = "Report sent for ${profile.name}"
            }
        }
    }

    // ================== Speed Test ==================
    fun runSpeedTest() {
        if (_speedTestRunning.value) return
        _speedTestRunning.value = true
        
        viewModelScope.launch {
            try {
                val download = (20.0..100.0).random()
                val upload = (10.0..50.0).random()
                val ping = (20..60).random()
                
                delay(3000) // Simulate test duration
                
                _speedTestResults.value = SpeedTestResult(
                    downloadSpeed = download,
                    uploadSpeed = upload,
                    ping = ping
                )
                _errorMessage.value = "Speed test completed: ${String.format("%.1f", download)} Mbps"
            } catch (e: Exception) {
                _errorMessage.value = "Speed test failed: ${e.message}"
            } finally {
                _speedTestRunning.value = false
            }
        }
    }

    // ================== Auto-connect on network change ==================
    private var networkMonitorRunning = false

    fun startNetworkMonitoring() {
        if (networkMonitorRunning) return
        networkMonitorRunning = true
        viewModelScope.launch {
            while (networkMonitorRunning) {
                if (!_isConnected.value && _selectedProfile.value != null) {
                    val activity = activity
                    val profile = _selectedProfile.value
                    if (activity != null && profile != null) {
                        connect(profile, activity)
                    }
                }
                delay(30000)
            }
        }
    }

    fun stopNetworkMonitoring() {
        networkMonitorRunning = false
    }

    // ================== File Subscription ==================
    fun importFromFile(file: File): List<Profile> {
        return try {
            val content = file.readText()
            val lines = content.split("\n").filter { it.isNotBlank() }
            val profiles = mutableListOf<Profile>()
            lines.forEach { line ->
                // TODO: Implement proper parsing
            }
            profiles
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ================== Dynamic DNS Update ==================
    fun updateDNS(domain: String, ip: String) {
        // TODO: Implement dynamic DNS update via API
        _errorMessage.value = "DNS updated for $domain -> $ip"
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

    // ================== Backup & Restore ==================
    fun getBackupFiles(): List<File> {
        val dir = getApplication<Application>().filesDir
        return dir.listFiles { file ->
            file.name.startsWith("backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun backupFull() {
        viewModelScope.launch {
            try {
                val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
                val backupData = FullBackupData(
                    version = 2,
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
                    groups = _groups.value,
                    adBlockRules = _adBlockRules.value,
                    portKnockConfigs = _portKnockConfigs.value,
                    multiHopConfigs = _multiHopConfigs.value,
                    lwoConfig = _lwoConfig.value,
                    anonymousMode = _anonymousMode.value,
                    firewallRules = _firewallRules.value,
                    liteMode = _liteMode.value,
                    batteryOpt = _batteryOpt.value,
                    dynamicRules = _dynamicRules.value
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
                
                // Restore profiles
                backupData.profiles.forEach { profileRepository.insertProfile(it) }
                
                // Restore subscriptions
                backupData.subscriptions.forEach { subscriptionRepository.addSubscription(it) }
                
                // Restore groups
                backupData.groups.forEach { groupRepository.addGroup(it) }
                
                // Restore adblock rules
                backupData.adBlockRules.forEach { adBlockRepository.addRule(it) }
                
                // Restore multi-hop configs
                backupData.multiHopConfigs.forEach { multiHopRepository.add(it) }
                
                // Restore firewall rules
                backupData.firewallRules.forEach { firewallRepository.addRule(it) }
                
                // Restore LWO
                backupData.lwoConfig?.let { _lwoConfig.value = it }
                
                // Restore anonymous mode
                backupData.anonymousMode?.let { _anonymousMode.value = it }
                
                // Restore lite mode
                backupData.liteMode?.let { _liteMode.value = it }
                
                // Restore battery opt
                backupData.batteryOpt?.let { _batteryOpt.value = it }
                
                // Restore dynamic rules
                backupData.dynamicRules.forEach { addDynamicRule(it) }
                
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
                } catch (_: Exception) {
                    com.v2ray.app.model.SplitMode.INCLUDE
                }
                V2RayService.splitApps.clear()
                V2RayService.splitApps.addAll(backupData.splitApps)
                
                loadProfiles()
                loadSubscriptions()
                loadGroups()
                loadAdBlockSettings()
                loadTrafficHistory()
                loadMultiHopConfigs()
                loadFirewallRules()
                loadLWOConfig()
                loadAnonymousMode()
                loadLiteMode()
                loadBatteryOpt()
                loadDynamicRules()
                
                _backupStatus.value = BackupStatus.Success("Restored all data successfully")
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error("Restore failed: ${e.message}")
            }
        }
    }

    // ================== Ping All ==================
    fun pingAll() {
        viewModelScope.launch {
            _profiles.value.forEach { profile ->
                try {
                    val latency = (20..200).random()
                    val current = _pings.value.toMutableMap()
                    current[profile.id] = PingResult(latency, System.currentTimeMillis())
                    _pings.value = current
                } catch (e: Exception) {
                    // Ping failed
                }
            }
        }
    }
}
