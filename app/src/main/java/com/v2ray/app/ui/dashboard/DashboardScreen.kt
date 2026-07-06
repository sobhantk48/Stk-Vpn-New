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

    // وضعیت SNI Tunnel
    var sniTunnelEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                onClick = { /* TODO: Open server list */ }
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
            // دکمه‌ی SNI Tunnel با قابلیت toggle
            QuickActionButton(
                icon = Icons.Default.Tune,
                label = if (sniTunnelEnabled) "SNI ✓" else "SNI",
                onClick = {
                    sniTunnelEnabled = !sniTunnelEnabled
                    if (sniTunnelEnabled) {
                        // فعال‌سازی SNI Tunnel: اگر سرور انتخاب شده باشد، customSni را تنظیم کن
                        selected?.let { profile ->
                            val newSni = "www.google.com" // می‌توانید مقدار دلخواه تنظیم کنید
                            vm.updateCustomSni(profile.id, newSni)
                        }
                    } else {
                        // غیرفعال‌سازی: حذف customSni
                        selected?.let { profile ->
                            vm.updateCustomSni(profile.id, "")
                        }
                    }
                }
            )
            QuickActionButton(
                icon = Icons.Default.Verified,
                label = "Bypass OPI",
                onClick = { /* TODO: Enable Bypass OPI */ }
            )
        }

        // لیست سرورها با پینگ
        Text(
            text = "Servers (${profiles.size})",
            color = WhiteText,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn {
            items(profiles, key = { it.id }) { profile ->
                ServerItem(
                    profile = profile,
                    isSelected = selected?.id == profile.id,
                    ping = pings[profile.id]?.latency ?: -1,
                    onClick = { vm.selectProfile(profile) }
                )
            }
        }

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
fun ServerItem(
    profile: Profile,
    isSelected: Boolean,
    ping: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyanAccent.copy(0.2f) else DarkSurface
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = profile.name,
                    color = WhiteText,
                    fontSize = 14.sp
                )
                Text(
                    text = "${profile.address}:${profile.port}",
                    color = WhiteText.copy(0.5f),
                    fontSize = 11.sp
                )
                if (profile.customSni.isNotBlank()) {
                    Text(
                        text = "SNI: ${profile.customSni}",
                        color = CyanAccent.copy(0.7f),
                        fontSize = 10.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = GreenSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (ping > 0) "${ping} ms" else "--",
                    color = if (ping > 0 && ping < 100) GreenSuccess else if (ping > 0 && ping < 200) CyanAccent else WhiteText.copy(0.5f),
                    fontSize = 12.sp
                )
            }
        }
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
