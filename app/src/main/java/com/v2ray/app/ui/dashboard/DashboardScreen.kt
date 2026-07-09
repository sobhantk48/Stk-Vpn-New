import androidx.compose.material3.ScaffoldState
import androidx.compose.material3.rememberScaffoldState
package com.v2ray.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.*
import com.v2ray.app.viewmodel.MainViewModel
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: MainViewModel,
    nav: androidx.navigation.NavController,
    drawer: androidx.compose.material3.DrawerState,
    onAdminClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLocations: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val profiles by vm.filteredProfiles.collectAsStateWithLifecycle()
    val selected by vm.selectedProfile.collectAsStateWithLifecycle()
    val isConnected by vm.isConnected.collectAsStateWithLifecycle()
    val pings by vm.pings.collectAsStateWithLifecycle()
    val errorMessage by vm.errorMessage.collectAsStateWithLifecycle()
    val traffic by vm.traffic.collectAsStateWithLifecycle()
    val groups by vm.groups.collectAsStateWithLifecycle()
    val selectedGroupId by vm.selectedGroupId.collectAsStateWithLifecycle()
    val selectedProxyIds by vm.selectedProxyIds.collectAsStateWithLifecycle()
    val searchQuery by vm.proxySearchQuery.collectAsStateWithLifecycle()
    var showServerList by remember { mutableStateOf(true) }
    var sniTunnelEnabled by remember { mutableStateOf(false) }
    var frontingEnabled by remember { mutableStateOf(false) }
    var showBulkActions by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val decimalFormat = remember { DecimalFormat("#.##") }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            scaffoldState.snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
            vm.clearError()
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024 * 1024 -> "${decimalFormat.format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
            bytes >= 1024L * 1024 -> "${decimalFormat.format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${decimalFormat.format(bytes / 1024.0)} KB"
            else -> "$bytes B"
        }
    }

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // Get profiles for selected group
    val displayProfiles = if (selectedGroupId != null) {
        vm.getProfilesForGroup(selectedGroupId!!)
    } else profiles

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Top Bar with search
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { scope.launch { if (drawer.isClosed) drawer.open() else drawer.close() } }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = WhiteText)
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vm.setProxySearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search proxies...", color = WhiteText.copy(0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = WhiteText.copy(0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhiteText.copy(0.3f),
                        unfocusedBorderColor = WhiteText.copy(0.2f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    ),
                    singleLine = true
                )
                IconButton(onClick = { /* TODO: QR Scanner */ }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Scan", tint = WhiteText)
                }
            }

            // Groups Tabs
            if (groups.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = groups.indexOfFirst { it.id == selectedGroupId }.takeIf { it >= 0 } ?: 0,
                    containerColor = DarkBackground,
                    edgePadding = 0.dp
                ) {
                    groups.forEachIndexed { index, group ->
                        Tab(
                            selected = group.id == selectedGroupId,
                            onClick = { vm.selectGroup(group.id) },
                            text = { Text("${group.icon} ${group.name}", color = WhiteText, fontSize = 12.sp) }
                        )
                    }
                    Tab(
                        selected = false,
                        onClick = { /* TODO: Add group dialog */ },
                        text = { Text("+", color = WhiteText, fontSize = 16.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bulk selection bar
            if (selectedProxyIds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selectedProxyIds.size} selected", color = WhiteText.copy(0.7f), fontSize = 14.sp)
                    Row {
                        IconButton(onClick = { vm.clearSelection() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = WhiteText)
                        }
                        IconButton(onClick = { vm.bulkDeleteSelected() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = RedError)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Connection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isConnected) "🟢 Connected" else "🔴 Disconnected",
                            color = if (isConnected) GreenSuccess else RedError,
                            fontSize = 18.sp
                        )
                        if (isConnected) {
                            Text(text = formatTime(traffic.connectionTime), color = WhiteText.copy(0.7f), fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isConnected && selected != null) {
                        Text("${selected!!.name} - ${selected!!.type}", color = WhiteText, fontSize = 14.sp)
                        if (selected!!.customSni.isNotBlank()) {
                            Text("🔒 SNI: ${selected!!.customSni}", color = CyanAccent, fontSize = 12.sp)
                        }
                        if (frontingEnabled) Text("🌐 Domain Fronting: ON", color = GreenAccent, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Download", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            Text(formatBytes(traffic.download), color = CyanAccent, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upload", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            Text(formatBytes(traffic.upload), color = CyanAccent, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ping", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            val ping = selected?.let { pings[it.id]?.latency } ?: -1
                            Text(
                                text = if (ping > 0) "${ping} ms" else "--",
                                color = if (ping > 0 && ping < 100) GreenSuccess else CyanAccent,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(Icons.Default.List, "Servers") { showServerList = !showServerList }
                QuickActionButton(Icons.Default.Speed, "Test All") { /* TODO: Ping all */ }
                QuickActionButton(Icons.Default.Security, "Tor") { /* TODO: Tor */ }
                QuickActionButton(
                    Icons.Default.Tune,
                    if (sniTunnelEnabled) "SNI ✓" else "SNI"
                ) {
                    sniTunnelEnabled = !sniTunnelEnabled
                    vm.setSniTunnelEnabled(sniTunnelEnabled)
                }
                QuickActionButton(
                    Icons.Default.Verified,
                    if (frontingEnabled) "Front ✓" else "Front"
                ) {
                    frontingEnabled = !frontingEnabled
                    if (frontingEnabled) vm.startFronting() else vm.stopFronting()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connect/Disconnect & Admin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { vm.toggleConnection() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) RedError else GreenSuccess
                    )
                ) {
                    Text(text = if (isConnected) "🔴 Disconnect" else "🟢 Connect", color = WhiteText, fontSize = 16.sp)
                }
                Button(
                    onClick = onAdminClick,
                    modifier = Modifier.weight(0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin", color = WhiteText, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Proxy List
            if (showServerList) {
                if (displayProfiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No proxies in this group.", color = WhiteText.copy(0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(displayProfiles, key = { it.id }) { profile ->
                            ServerListItem(
                                profile = profile,
                                isSelected = selected?.id == profile.id,
                                isSelectedBulk = profile.id in selectedProxyIds,
                                traffic = vm.getTrafficForProxy(profile.id),
                                ping = pings[profile.id]?.latency ?: -1,
                                onSelect = { vm.selectProfile(profile) },
                                onLongPress = { vm.toggleProxySelection(profile.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomActionButton(Icons.Default.Settings, "Settings", onNavigateToSettings)
                BottomActionButton(Icons.Default.LocationOn, "Locations", onNavigateToLocations)
                BottomActionButton(Icons.Default.Info, "About", onNavigateToAbout)
                BottomActionButton(Icons.Default.History, "Logs", onNavigateToLogs)
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = CyanAccent, modifier = Modifier.size(24.dp))
        Text(label, color = WhiteText.copy(0.7f), fontSize = 10.sp)
    }
}

@Composable
fun BottomActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = WhiteText.copy(0.7f), modifier = Modifier.size(20.dp))
        Text(label, color = WhiteText.copy(0.5f), fontSize = 10.sp)
    }
}

@Composable
fun ServerListItem(
    profile: Profile,
    isSelected: Boolean,
    isSelectedBulk: Boolean,
    traffic: Long,
    ping: Int,
    onSelect: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .then(if (isSelectedBulk) Modifier.background(PrimaryBlue.copy(alpha = 0.3f)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌐", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = profile.name,
                    color = if (isSelected) CyanAccent else WhiteText,
                    fontSize = 14.sp
                )
                Text(
                    text = "${profile.type} - ${profile.address}:${profile.port}",
                    color = WhiteText.copy(0.5f),
                    fontSize = 12.sp
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (traffic > 0) {
                Text(
                    text = when {
                        traffic >= 1024 * 1024 * 1024 -> "${traffic / (1024 * 1024 * 1024)}GB"
                        traffic >= 1024 * 1024 -> "${traffic / (1024 * 1024)}MB"
                        traffic >= 1024 -> "${traffic / 1024}KB"
                        else -> "${traffic}B"
                    },
                    color = WhiteText.copy(0.5f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = if (ping > 0) "${ping}ms" else "--",
                color = when {
                    ping > 0 && ping < 100 -> GreenSuccess
                    ping > 0 -> Color(0xFFFFC107)
                    else -> WhiteText.copy(0.3f)
                },
                fontSize = 12.sp
            )
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = GreenSuccess, modifier = Modifier.size(16.dp))
            }
            if (isSelectedBulk) {
                Icon(Icons.Default.CheckBox, contentDescription = "Selected Bulk", tint = CyanAccent, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// اضافه کردن پارامترهای جدید به تابع DashboardScreen
// ... onNavigateToSpeedTest: () -> Unit,
// ... onNavigateToMultiHop: () -> Unit

// و در بخش Tools Row اضافه کنید:
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    ToolButton("🚀", "Speed Test", onNavigateToSpeedTest)
    ToolButton("🔗", "Multi-Hop", onNavigateToMultiHop)
    ToolButton("🌍", "GeoIP", onNavigateToGeoIP)
    ToolButton("🛡️", "AdBlock", onNavigateToAdBlock)
    ToolButton("📈", "History", onNavigateToTrafficHistory)
}
