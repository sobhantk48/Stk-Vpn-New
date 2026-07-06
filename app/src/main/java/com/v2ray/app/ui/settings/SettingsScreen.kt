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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.service.V2RayService
import com.v2ray.app.ui.theme.*
import com.v2ray.app.viewmodel.MainViewModel
import com.v2ray.app.desync.DesyncManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val scope = rememberCoroutineScope()

    // وضعیت‌های Kill Switch
    var killSwitchEnabled by remember { mutableStateOf(V2RayService.killSwitchEnabled) }

    // وضعیت‌های Split Tunneling
    var splitEnabled by remember { mutableStateOf(V2RayService.splitTunnelingEnabled) }
    var splitMode by remember { mutableStateOf(V2RayService.SplitMode.INCLUDE) }
    val splitApps = remember { mutableStateListOf<String>().apply { addAll(V2RayService.splitApps) } }

    // لیست اپ‌های نصب‌شده
    val installedApps = remember {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { it.packageName }
            .sorted()
    }

    // وضعیت‌های Desync
    var fragmentEnabled by remember { mutableStateOf(DesyncManager.getInstance().isEnabled()) }
    var sniChunk by remember { mutableStateOf(DesyncManager.getInstance().getConfig().sniChunk) }
    var fragmentDelay by remember { mutableStateOf(DesyncManager.getInstance().getConfig().fragmentDelay) }

    // وضعیت Backup
    val backupStatus by vm.backupStatus.collectAsState()
    val backupFiles = remember { vm.getBackupFiles() }

    // انتخاب فایل برای Restore
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
                                selected = splitMode == V2RayService.SplitMode.INCLUDE,
                                onClick = {
                                    splitMode = V2RayService.SplitMode.INCLUDE
                                    V2RayService.splitMode = splitMode
                                },
                                label = { Text("Include", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = splitMode == V2RayService.SplitMode.EXCLUDE,
                                onClick = {
                                    splitMode = V2RayService.SplitMode.EXCLUDE
                                    V2RayService.splitMode = splitMode
                                },
                                label = { Text("Exclude", fontSize = 12.sp) }
                            )
                        }

                        Text(
                            text = if (splitMode == V2RayService.SplitMode.INCLUDE) {
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

                    backupStatus?.let { status ->
                        Spacer(modifier = Modifier.height(8.dp))
                        when (status) {
                            is com.v2ray.app.viewmodel.BackupStatus.Success -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GreenSuccess
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        status.message,
                                        color = GreenSuccess,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            is com.v2ray.app.viewmodel.BackupStatus.Error -> {
                                Text(
                                    "❌ ${status.message}",
                                    color = RedError,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // لیست فایل‌های پشتیبان موجود
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
                                Text(
                                    text = file.name,
                                    color = WhiteText.copy(0.6f),
                                    fontSize = 10.sp
                                )
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

            // ===== Desync / Fragment =====
            Text(
                text = "🔀 DPI Evasion (Desync)",
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
                            text = "Enable Fragment",
                            color = WhiteText,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = fragmentEnabled,
                            onCheckedChange = {
                                fragmentEnabled = it
                                if (it) {
                                    DesyncManager.getInstance().enable(
                                        DesyncManager.Config(
                                            enableFragment = true,
                                            sniChunk = sniChunk,
                                            fragmentDelay = fragmentDelay
                                        )
                                    )
                                } else {
                                    DesyncManager.getInstance().disable()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SNI Chunk Size: $sniChunk",
                            color = WhiteText.copy(0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Slider(
                        value = sniChunk.toFloat(),
                        onValueChange = { sniChunk = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fragment Delay: ${fragmentDelay}ms",
                            color = WhiteText.copy(0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Slider(
                        value = fragmentDelay.toFloat(),
                        onValueChange = { fragmentDelay = it.toLong() },
                        valueRange = 100f..1000f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (fragmentEnabled) "🟢 Fragment: ON" else "🔴 Fragment: OFF",
                        color = if (fragmentEnabled) GreenSuccess else RedError,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // دکمه‌ی ذخیره
            Button(
                onClick = {
                    // ذخیره تنظیمات
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save Settings", color = WhiteText)
            }
        }
    }
}
