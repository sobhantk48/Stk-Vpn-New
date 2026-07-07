package com.v2ray.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.*
import com.v2ray.app.utils.SmartParser
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(vm: MainViewModel, onBack: () -> Unit) {
    var authorized by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(true) }

    if (showPasswordDialog) {
        AdminPasswordDialog(
            onDismiss = { onBack() },
            onSuccess = {
                authorized = true
                showPasswordDialog = false
            }
        )
    }

    if (!authorized) return

    val profiles by vm.profiles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, tint = WhiteText, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* change pass */ }) {
                        Icon(Icons.Default.Lock, tint = WhiteText, contentDescription = "Change Password")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue
            ) {
                Text("+", color = WhiteText, fontSize = 20.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (profiles.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No profiles. Tap + to add.",
                        color = WhiteText.copy(0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(profiles, key = { it.id }) { profile ->
                        AdminProfileCard(
                            profile = profile,
                            onEdit = { /* TODO: edit */ },
                            onDelete = { vm.delete(profile.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SmartAddConfigDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { profilesList ->
                profilesList.forEach { vm.add(it) }
                showAddDialog = false
            }
        )
    }
}

// ===== دیالوگ‌های جدید با Smart Paste =====

@Composable
fun SmartAddConfigDialog(onDismiss: () -> Unit, onAdd: (List<Profile>) -> Unit) {
    var input by remember { mutableStateOf("") }
    var parsedProfiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile (Smart Paste)") },
        text = {
            Column {
                Text(
                    text = "Paste any of the following:",
                    color = WhiteText.copy(0.7f),
                    fontSize = 12.sp
                )
                Text(
                    text = "• vless://, vmess://, trojan://, ss://\n• Clash YAML (with proxies)\n• v2rayN JSON (with outbounds)",
                    color = WhiteText.copy(0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        error = null
                        // تلاش برای پارس خودکار
                        if (it.length > 10) {
                            try {
                                val result = SmartParser.detectAndParse(it)
                                if (result.isNotEmpty()) {
                                    parsedProfiles = result
                                } else {
                                    error = "No profiles found"
                                }
                            } catch (e: Exception) {
                                error = e.message
                            }
                        } else {
                            parsedProfiles = emptyList()
                        }
                    },
                    label = { Text("Paste link or config") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3
                )
                if (error != null) {
                    Text(
                        text = "❌ $error",
                        color = RedError,
                        fontSize = 12.sp
                    )
                }
                if (parsedProfiles.isNotEmpty()) {
                    Text(
                        text = "✅ Found ${parsedProfiles.size} profile(s)",
                        color = GreenSuccess,
                        fontSize = 12.sp
                    )
                    // نمایش نام اولین پروفایل
                    parsedProfiles.take(3).forEach { profile ->
                        Text(
                            text = "• ${profile.name} (${profile.type})",
                            color = WhiteText.copy(0.8f),
                            fontSize = 11.sp
                        )
                    }
                    if (parsedProfiles.size > 3) {
                        Text(
                            text = "... and ${parsedProfiles.size - 3} more",
                            color = WhiteText.copy(0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (parsedProfiles.isNotEmpty()) {
                        onAdd(parsedProfiles)
                    } else {
                        // اگر چیزی یافت نشد، سعی کنید مستقیماً از لینک استفاده کنید
                        val single = SmartParser.detectAndParse(input)
                        if (single.isNotEmpty()) {
                            onAdd(single)
                        } else {
                            error = "No valid config found"
                        }
                    }
                },
                enabled = parsedProfiles.isNotEmpty() || input.isNotBlank()
            ) {
                Text("Add All")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ===== دیالوگ‌های قدیمی (AdminPasswordDialog, AdminProfileCard, ChangePasswordDialog) =====
// همان‌طور که بود

@Composable
fun AdminPasswordDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Admin Password") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (password == "admin") onSuccess() else onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminProfileCard(profile: Profile, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(profile.name, color = WhiteText)
                Text("${profile.type} • ${profile.address}:${profile.port}", color = CyanAccent, fontSize = 12.sp)
            }
            Row {
                TextButton(onClick = onEdit) { Text("Edit", color = CyanAccent) }
                TextButton(onClick = onDelete) { Text("Delete", color = RedError) }
            }
        }
    }
}
