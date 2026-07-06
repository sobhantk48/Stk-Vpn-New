package com.v2ray.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
        AddConfigDialog(
            onDismiss = { showAddDialog = false },
            onAdd = {
                vm.add(it)
                showAddDialog = false
            }
        )
    }
}

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

@Composable
fun AddConfigDialog(onDismiss: () -> Unit, onAdd: (Profile) -> Unit) {
    var link by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile") },
        text = {
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
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
