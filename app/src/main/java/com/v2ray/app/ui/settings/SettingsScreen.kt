package com.v2ray.app.ui.settings

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.bg.V2RayService
import com.v2ray.app.model.SplitMode
import com.v2ray.app.ui.theme.*
import com.v2ray.app.viewmodel.BackupStatus
import com.v2ray.app.viewmodel.MainViewModel
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val scope = rememberCoroutineScope()

    var isDarkTheme by remember { mutableStateOf(true) }

    var killSwitchEnabled by remember { mutableStateOf(V2RayService.killSwitchEnabled) }

    var splitEnabled by remember { mutableStateOf(V2RayService.splitTunnelingEnabled) }
    var splitMode by remember { mutableStateOf(SplitMode.INCLUDE) }
    val splitApps = remember { mutableStateListOf<String>().apply { addAll(V2RayService.splitApps) } }

    val installedApps = remember {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { it.packageName }
            .sorted()
    }

    val backupStatus by vm.backupStatus.collectAsStateWithLifecycle()
    val backupFiles = remember { vm.getBackupFiles() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val tempFile = File(context.cacheDir, "temp_restore.json")
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                scope.launch {
                    vm.restoreProfiles(tempFile)
                    tempFile.delete()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, tint = WhiteText, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ===== Theme =====
            Text(
                text = "🎨 Theme",
                color = CyanAccent,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        color = WhiteText,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Kill Switch =====
            Text(
                text = "🔒 Kill Switch",
                color = CyanAccent,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable Kill Switch",
                            color = WhiteText,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = killSwitchEnabled,
                            onCheckedChange = {
                                killSwitchEnabled = it
                                V2RayService.killSwitchEnabled = it
                            }
                        )
                    }
                    Text(
                        text = "Blocks all internet traffic if VPN disconnects unexpectedly",
                        color = WhiteText.copy(0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Split Tunneling =====
            Text(
                text = "📱 Split Tunneling",
                color = CyanAccent,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable Split Tunneling",
                            color = WhiteText,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = splitEnabled,
                            onCheckedChange = {
                                splitEnabled = it
                                V2RayService.splitTunnelingEnabled = it
                            }
                        )
                    }

                    if (splitEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = splitMode == SplitMode.INCLUDE,
                                onClick = {
                                    splitMode = SplitMode.INCLUDE
                                    V2RayService.splitMode = splitMode
                                },
                                label = { Text("Include", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = splitMode == SplitMode.EXCLUDE,
                                onClick = {
                                    splitMode = SplitMode.EXCLUDE
                                    V2RayService.splitMode = splitMode
                                },
                                label = { Text("Exclude", fontSize = 12.sp) }
                            )
                        }

                        Text(
                            text = if (splitMode == SplitMode.INCLUDE) {
                                "Only selected apps use VPN"
                            } else {
                                "All apps except selected use VPN"
                            },
                            color = WhiteText.copy(0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Select Apps (${splitApps.size} selected)",
                            color = WhiteText.copy(0.7f),
                            fontSize = 12.sp
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(installedApps) { pkg ->
                                val isSelected = splitApps.contains(pkg)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = pkg,
                                        color = WhiteText.copy(0.8f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (isSelected) {
                                                splitApps.remove(pkg)
                                            } else {
                                                splitApps.add(pkg)
                                            }
                                            V2RayService.splitApps.clear()
                                            V2RayService.splitApps.addAll(splitApps)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Backup & Restore =====
            Text(
                text = "💾 Backup & Restore",
                color = CyanAccent,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup all profiles to JSON file",
                        color = WhiteText.copy(0.7f),
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    vm.backupProfiles()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup", color = WhiteText)
                        }

                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", color = WhiteText)
                        }
                    }

                    when (val status = backupStatus) {
                        is BackupStatus.Success -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(status.message, color = GreenSuccess, fontSize = 12.sp)
                            }
                        }
                        is BackupStatus.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("❌ ${status.message}", color = RedError, fontSize = 12.sp)
                        }
                        is BackupStatus.Idle -> {
                            // nothing
                        }
                    }

                    if (backupFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saved backups:",
                            color = WhiteText.copy(0.5f),
                            fontSize = 11.sp
                        )
                        backupFiles.take(3).forEach { file ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(file.name, color = WhiteText.copy(0.6f), fontSize = 10.sp)
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            vm.restoreProfiles(file)
                                        }
                                    }
                                ) {
                                    Text("Restore", color = CyanAccent, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Save settings */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save Settings", color = WhiteText)
            }
        }
    }
}
