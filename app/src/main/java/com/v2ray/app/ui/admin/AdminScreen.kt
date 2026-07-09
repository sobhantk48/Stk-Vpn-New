package com.v2ray.app.ui.admin

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.ui.theme.CyanAccent
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhiteText)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Profile", tint = WhiteText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
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
            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No profiles. Add one using + button.", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profiles, key = { it.id }) { profile ->
                        ProfileAdminItem(
                            profile = profile,
                            isSelected = vm.selectedId.value == profile.id,
                            onSelect = { vm.selectProfile(profile) },
                            onEdit = { editingProfile = profile },
                            onDelete = { vm.deleteProfile(profile.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Profile Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Profile", color = WhiteText) },
            text = {
                ProfileForm(
                    onSave = { profile ->
                        vm.addProfile(profile)
                        showAddDialog = false
                    },
                    onCancel = { showAddDialog = false }
                )
            },
            containerColor = DarkBackground
        )
    }

    // Edit Profile Dialog
    editingProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { editingProfile = null },
            title = { Text("Edit Profile: ${profile.name}", color = WhiteText) },
            text = {
                ProfileForm(
                    initialProfile = profile,
                    onSave = { updated ->
                        vm.addProfile(updated)
                        editingProfile = null
                    },
                    onCancel = { editingProfile = null }
                )
            },
            containerColor = DarkBackground
        )
    }
}

@Composable
fun ProfileAdminItem(
    profile: Profile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyanAccent.copy(alpha = 0.2f) else DarkBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = profile.name,
                    color = if (isSelected) CyanAccent else WhiteText,
                    fontSize = 16.sp
                )
                Text(
                    text = "${profile.type.name} - ${profile.address}:${profile.port}",
                    color = WhiteText.copy(0.5f),
                    fontSize = 12.sp
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WhiteText)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedError)
                }
            }
        }
    }
}

@Composable
fun ProfileForm(
    initialProfile: Profile? = null,
    onSave: (Profile) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var type by remember { mutableStateOf(initialProfile?.type ?: Profile.ProtocolType.VLESS) }
    var address by remember { mutableStateOf(initialProfile?.address ?: "") }
    var port by remember { mutableStateOf(initialProfile?.port?.toString() ?: "") }
    var uuid by remember { mutableStateOf(initialProfile?.uuid ?: java.util.UUID.randomUUID().toString()) }
    var password by remember { mutableStateOf(initialProfile?.password ?: "") }
    var customSni by remember { mutableStateOf(initialProfile?.customSni ?: "") }
    
    // Reality specific
    var realityPublicKey by remember { mutableStateOf(initialProfile?.realityPublicKey ?: "") }
    var realityShortId by remember { mutableStateOf(initialProfile?.realityShortId ?: "") }
    var realityServerName by remember { mutableStateOf(initialProfile?.realityServerName ?: "") }
    var realityFingerprint by remember { mutableStateOf(initialProfile?.realityFingerprint ?: "chrome") }
    
    // AmneziaWG specific
    var awgPrivateKey by remember { mutableStateOf(initialProfile?.awgPrivateKey ?: "") }
    var awgPublicKey by remember { mutableStateOf(initialProfile?.awgPublicKey ?: "") }
    var awgEndpoint by remember { mutableStateOf(initialProfile?.awgEndpoint ?: "") }
    
    // NaïveProxy specific
    var naiveUsername by remember { mutableStateOf(initialProfile?.naiveUsername ?: "") }
    var naivePassword by remember { mutableStateOf(initialProfile?.naivePassword ?: "") }
    
    // SSH specific
    var sshUsername by remember { mutableStateOf(initialProfile?.sshUsername ?: "") }
    var sshPassword by remember { mutableStateOf(initialProfile?.sshPassword ?: "") }
    
    // SOCKS5 specific
    var socks5Username by remember { mutableStateOf(initialProfile?.socks5Username ?: "") }
    var socks5Password by remember { mutableStateOf(initialProfile?.socks5Password ?: "") }
    
    // HTTP specific
    var httpUsername by remember { mutableStateOf(initialProfile?.httpUsername ?: "") }
    var httpPassword by remember { mutableStateOf(initialProfile?.httpPassword ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = WhiteText.copy(0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Protocol Type Dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = type.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Protocol Type", color = WhiteText.copy(0.5f)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Profile.ProtocolType.values().forEach { protocol ->
                    DropdownMenuItem(
                        text = { Text(protocol.name, color = WhiteText) },
                        onClick = {
                            type = protocol
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address", color = WhiteText.copy(0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port", color = WhiteText.copy(0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Protocol-specific fields
        when (type) {
            Profile.ProtocolType.VLESS, 
            Profile.ProtocolType.VLESS_REALITY,
            Profile.ProtocolType.VMESS,
            Profile.ProtocolType.TUIC -> {
                OutlinedTextField(
                    value = uuid,
                    onValueChange = { uuid = it },
                    label = { Text("UUID", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.TROJAN,
            Profile.ProtocolType.TROJAN_GO -> {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.SHADOWSOCKS,
            Profile.ProtocolType.SHADOWSOCKS_R -> {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.HYSTERIA2 -> {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.AMNEZIA_WG -> {
                OutlinedTextField(
                    value = awgPrivateKey,
                    onValueChange = { awgPrivateKey = it },
                    label = { Text("Private Key", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = awgPublicKey,
                    onValueChange = { awgPublicKey = it },
                    label = { Text("Public Key", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = awgEndpoint,
                    onValueChange = { awgEndpoint = it },
                    label = { Text("Endpoint", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.NAIVE_PROXY -> {
                OutlinedTextField(
                    value = naiveUsername,
                    onValueChange = { naiveUsername = it },
                    label = { Text("Username", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = naivePassword,
                    onValueChange = { naivePassword = it },
                    label = { Text("Password", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.SSH -> {
                OutlinedTextField(
                    value = sshUsername,
                    onValueChange = { sshUsername = it },
                    label = { Text("Username", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = sshPassword,
                    onValueChange = { sshPassword = it },
                    label = { Text("Password", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.SOCKS5 -> {
                OutlinedTextField(
                    value = socks5Username,
                    onValueChange = { socks5Username = it },
                    label = { Text("Username (optional)", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = socks5Password,
                    onValueChange = { socks5Password = it },
                    label = { Text("Password (optional)", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Profile.ProtocolType.HTTP -> {
                OutlinedTextField(
                    value = httpUsername,
                    onValueChange = { httpUsername = it },
                    label = { Text("Username (optional)", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = httpPassword,
                    onValueChange = { httpPassword = it },
                    label = { Text("Password (optional)", color = WhiteText.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            else -> {}
        }
        
        // Common fields: SNI
        if (type !in listOf(
            Profile.ProtocolType.WIREGUARD,
            Profile.ProtocolType.AMNEZIA_WG,
            Profile.ProtocolType.SOCKS5,
            Profile.ProtocolType.HTTP
        )) {
            OutlinedTextField(
                value = customSni,
                onValueChange = { customSni = it },
                label = { Text("SNI (optional)", color = WhiteText.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Reality-specific fields
        if (type == Profile.ProtocolType.VLESS_REALITY) {
            OutlinedTextField(
                value = realityPublicKey,
                onValueChange = { realityPublicKey = it },
                label = { Text("Reality Public Key", color = WhiteText.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = realityShortId,
                onValueChange = { realityShortId = it },
                label = { Text("Reality Short ID", color = WhiteText.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = realityServerName,
                onValueChange = { realityServerName = it },
                label = { Text("Reality Server Name", color = WhiteText.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val portInt = port.toIntOrNull() ?: 443
                    val profile = Profile(
                        id = initialProfile?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name.ifEmpty { "New Profile" },
                        type = type,
                        address = address.ifEmpty { "127.0.0.1" },
                        port = portInt,
                        uuid = uuid,
                        password = password,
                        customSni = customSni,
                        // Reality
                        realityPublicKey = realityPublicKey,
                        realityShortId = realityShortId,
                        realityServerName = realityServerName,
                        realityFingerprint = realityFingerprint,
                        // AmneziaWG
                        awgPrivateKey = awgPrivateKey,
                        awgPublicKey = awgPublicKey,
                        awgEndpoint = awgEndpoint,
                        // NaïveProxy
                        naiveUsername = naiveUsername,
                        naivePassword = naivePassword,
                        // SSH
                        sshUsername = sshUsername,
                        sshPassword = sshPassword,
                        // SOCKS5
                        socks5Username = socks5Username,
                        socks5Password = socks5Password,
                        // HTTP
                        httpUsername = httpUsername,
                        httpPassword = httpPassword
                    )
                    onSave(profile)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Text("Save", color = WhiteText)
            }
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = WhiteText)
            }
        }
    }
}
