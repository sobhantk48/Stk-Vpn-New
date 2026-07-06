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
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
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

    var showAdd by remember { mutableStateOf(false) }
    var editProfile by remember { mutableStateOf<Profile?>(null) }
    var showChangePass by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showChangePass = true }) {
                        Icon(Icons.Default.Lock, tint = WhiteText, contentDescription = "Change Password")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = PrimaryBlue
            ) {
                Text("+", color = WhiteText, fontSize = 20.sp)
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding) // فقط یک بار padding استفاده شود
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (profiles.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No profiles added yet.\nTap + to add one.",
                            color = WhiteText.copy(0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(profiles, key = { it.id }) { p ->
                    AdminProfileCard(
                        profile = p,
                        onEdit = { editProfile = p },
                        onDelete = { vm.delete(p.id) }
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddConfigDialog(
            onDismiss = { showAdd = false },
            onAdd = {
                vm.add(it)
                showAdd = false
            }
        )
    }

    editProfile?.let { profile ->
        EditProfileDialog(
            profile = profile,
            onDismiss = { editProfile = null },
            onSave = {
                vm.update(it)
                editProfile = null
            }
        )
    }

    if (showChangePass) {
        ChangePasswordDialog(
            onDismiss = { showChangePass = false },
            onSuccess = { showChangePass = false }
        )
    }
}

// ====== دیالوگ‌ها (قبلاً تعریف شده‌اند، اینجا فقط برای تکمیل) ======

@Composable
fun AdminPasswordDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Admin Password") },
        text = {
            var password by remember { mutableStateOf("") }
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
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
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

@Composable
fun AddConfigDialog(onDismiss: () -> Unit, onAdd: (Profile) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile") },
        text = {
            var link by remember { mutableStateOf("") }
            Column {
                Text("Paste share link (vless://, vmess://, ...)")
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Share Link") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                Profile.fromLink(link)?.let { onAdd(it) } ?: onDismiss()
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditProfileDialog(profile: Profile, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var name by remember { mutableStateOf(profile.name) }
    var address by remember { mutableStateOf(profile.address) }
    var port by remember { mutableStateOf(profile.port.toString()) }
    var uuid by remember { mutableStateOf(profile.uuid) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uuid,
                    onValueChange = { uuid = it },
                    label = { Text("UUID / Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(profile.copy(
                    name = name,
                    address = address,
                    port = port.toIntOrNull() ?: profile.port,
                    uuid = uuid
                ))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Admin Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (oldPass == "admin" && newPass.isNotBlank()) {
                    // ذخیره رمز جدید (در اینجا فقط نمونه)
                    onSuccess()
                }
            }) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
