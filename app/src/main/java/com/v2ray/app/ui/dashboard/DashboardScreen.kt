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
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    val selected by vm.selectedProfile.collectAsStateWithLifecycle()
    val isConnected by vm.isConnected.collectAsStateWithLifecycle()
    val pings by vm.pings.collectAsStateWithLifecycle()
    val errorMessage by vm.errorMessage.collectAsStateWithLifecycle()

    var showServerList by remember { mutableStateOf(false) }
    var sniTunnelEnabled by remember { mutableStateOf(false) }
    var frontingEnabled by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    // نمایش خطاها با Snackbar
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
            // کارت اطلاعات اتصال
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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
                            Text(
                                text = "00:15:42",
                                color = WhiteText.copy(0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isConnected && selected != null) {
                        Text(
                            text = "${selected!!.name} - ${selected!!.type} - tcp",
                            color = WhiteText,
                            fontSize = 14.sp
                        )
                        if (selected!!.customSni.isNotBlank()) {
                            Text(
                                text = "🔒 SNI: ${selected!!.customSni}",
                                color = CyanAccent,
                                fontSize = 12.sp
                            )
                        }
                        if (frontingEnabled) {
                            Text(
                                text = "🌐 Domain Fronting: ON",
                                color = GreenAccent,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Download", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            Text("125.6 MB", color = CyanAccent, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upload", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            Text("512 MB", color = CyanAccent, fontSize = 16.sp)
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

            // دکمه‌های Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.List,
                    label = "Servers",
                    onClick = { showServerList = !showServerList }
                )
                QuickActionButton(
                    icon = Icons.Default.Speed,
                    label = "Speed Test",
                    onClick = { /* TODO: Run speed test */ }
                )
                QuickActionButton(
                    icon = Icons.Default.Security,
                    label = "Tor",
                    onClick = { /* TODO: Enable Tor */ }
                )
                QuickActionButton(
                    icon = Icons.Default.Tune,
                    label = if (sniTunnelEnabled) "SNI ✓" else "SNI",
                    onClick = {
                        sniTunnelEnabled = !sniTunnelEnabled
                        vm.setSniTunnelEnabled(sniTunnelEnabled)
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.Verified,
                    label = if (frontingEnabled) "Front ✓" else "Front",
                    onClick = {
                        frontingEnabled = !frontingEnabled
                        if (frontingEnabled) {
                            vm.startFronting()
                        } else {
                            vm.stopFronting()
                        }
                    }
                )
            }

            // دکمه اتصال/قطع و Admin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { vm.toggleConnection() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) RedError else GreenSuccess
                    )
                ) {
                    Text(
                        text = if (isConnected) "🔴 Disconnect" else "🟢 Connect",
                        color = WhiteText,
                        fontSize = 16.sp
                    )
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

            // لیست سرورها (قابل نمایش/مخفی‌سازی)
            if (showServerList) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    if (profiles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No profiles. Add via import.", color = WhiteText.copy(0.5f))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(
                                items = profiles,
                                key = { it.id }  // کلید منحصربه‌فرد برای هر آیتم
                            ) { profile ->
                                ServerListItem(
                                    profile = profile,
                                    isSelected = selected?.id == profile.id,
                                    onClick = { vm.selectProfile(profile) }
                                )
                            }
                        }
                    }
                }
            }

            // Recent Activity
            Text(
                text = "📋 Recent Activity",
                color = CyanAccent,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // دکمه‌های پایین
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomActionButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = onNavigateToSettings
                )
                BottomActionButton(
                    icon = Icons.Default.LocationOn,
                    label = "Locations",
                    onClick = onNavigateToLocations
                )
                BottomActionButton(
                    icon = Icons.Default.Info,
                    label = "About",
                    onClick = onNavigateToAbout
                )
                BottomActionButton(
                    icon = Icons.Default.History,
                    label = "Logs",
                    onClick = onNavigateToLogs
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CyanAccent,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = WhiteText.copy(0.7f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun BottomActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = WhiteText.copy(0.7f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = WhiteText.copy(0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun ServerListItem(
    profile: Profile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.3f) else DarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = GreenSuccess)
        }
    }
}
