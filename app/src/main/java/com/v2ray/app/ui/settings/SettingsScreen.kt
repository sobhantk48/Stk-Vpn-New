package com.v2ray.app.ui.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.bg.V2RayService
import com.v2ray.app.model.SplitMode
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isConnected by vm.isConnected.collectAsStateWithLifecycle()
    
    // وضعیت‌های محلی
    var killSwitchEnabled by remember { mutableStateOf(V2RayService.killSwitchEnabled) }
    var splitTunnelingEnabled by remember { mutableStateOf(V2RayService.splitTunnelingEnabled) }
    var splitMode by remember { mutableStateOf(V2RayService.splitMode) }
    var selectedApps by remember { mutableStateOf(V2RayService.splitApps.toSet()) }
    
    // لیست اپ‌های نصب‌شده
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var showAppSelector by remember { mutableStateOf(false) }

    // بارگذاری لیست اپ‌ها
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    icon = app.loadIcon(pm)
                )
            }
            .sortedBy { it.appName }
        installedApps = apps
    }

    // ذخیره‌سازی تغییرات
    fun saveSettings() {
        V2RayService.killSwitchEnabled = killSwitchEnabled
        V2RayService.splitTunnelingEnabled = splitTunnelingEnabled
        V2RayService.splitMode = splitMode
        V2RayService.splitApps.clear()
        V2RayService.splitApps.addAll(selectedApps)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = {
                        saveSettings()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhiteText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    scrolledContainerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Kill Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🔒 Kill Switch", color = WhiteText, fontSize = 16.sp)
                        Text(
                            text = "Block all traffic if VPN disconnects",
                            color = WhiteText.copy(0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = killSwitchEnabled,
                        onCheckedChange = { 
                            if (!isConnected) {
                                killSwitchEnabled = it
                            }
                        },
                        enabled = !isConnected
                    )
                }
                if (isConnected) {
                    Text(
                        text = "⚠️ Disable VPN first to change this setting",
                        color = WhiteText.copy(0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split Tunneling
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkBackground)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("📱 Split Tunneling", color = WhiteText, fontSize = 16.sp)
                            Text(
                                text = "Select apps to route through VPN",
                                color = WhiteText.copy(0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = splitTunnelingEnabled,
                            onCheckedChange = { 
                                if (!isConnected) {
                                    splitTunnelingEnabled = it
                                    if (!it) {
                                        selectedApps = emptySet()
                                        showAppSelector = false
                                    }
                                }
                            },
                            enabled = !isConnected
                        )
                    }

                    if (splitTunnelingEnabled) {
                        // انتخاب mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = splitMode == SplitMode.INCLUDE,
                                onClick = { if (!isConnected) splitMode = SplitMode.INCLUDE },
                                label = { Text("Include", color = WhiteText, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.3f),
                                    selectedLabelColor = WhiteText
                                )
                            )
                            FilterChip(
                                selected = splitMode == SplitMode.EXCLUDE,
                                onClick = { if (!isConnected) splitMode = SplitMode.EXCLUDE },
                                label = { Text("Exclude", color = WhiteText, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.3f),
                                    selectedLabelColor = WhiteText
                                )
                            )
                        }

                        Text(
                            text = if (splitMode == SplitMode.INCLUDE) 
                                "Apps selected will use VPN" 
                            else 
                                "Apps selected will bypass VPN",
                            color = WhiteText.copy(0.5f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Button(
                            onClick = { showAppSelector = !showAppSelector },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (showAppSelector) "Hide Apps" else "Select Apps (${selectedApps.size})",
                                color = WhiteText,
                                fontSize = 14.sp
                            )
                        }

                        if (showAppSelector) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    items(installedApps) { app ->
                                        AppSelectorItem(
                                            app = app,
                                            isSelected = app.packageName in selectedApps,
                                            onToggle = {
                                                selectedApps = if (app.packageName in selectedApps) {
                                                    selectedApps - app.packageName
                                                } else {
                                                    selectedApps + app.packageName
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isConnected) {
                        Text(
                            text = "⚠️ Disable VPN first to change split tunneling",
                            color = WhiteText.copy(0.5f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // دکمه ذخیره
            Button(
                onClick = {
                    saveSettings()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Text("💾 Save Settings", color = WhiteText, fontSize = 16.sp)
            }
        }
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable
)

@Composable
fun AppSelectorItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else DarkBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = androidx.compose.ui.graphics.painter.Painter.drawable(app.icon),
                contentDescription = app.appName,
                tint = WhiteText,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = app.appName,
                color = WhiteText,
                fontSize = 14.sp
            )
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = GreenSuccess)
        }
    }
}
