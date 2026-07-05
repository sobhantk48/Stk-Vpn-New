package com.v2ray.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
                .padding(padding)
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
                items(profiles.size) { index ->
                    val p = profiles[index]
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
