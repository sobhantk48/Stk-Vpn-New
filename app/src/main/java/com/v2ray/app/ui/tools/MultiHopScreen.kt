package com.v2ray.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.model.MultiHopConfig
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.ui.theme.CyanAccent
import com.v2ray.app.ui.theme.GreenSuccess
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiHopScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val configs by vm.multiHopConfigs.collectAsStateWithLifecycle()
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔗 Multi-Hop", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhiteText)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = WhiteText)
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
            if (configs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No multi-hop configs. Add one to chain proxies.", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(configs, key = { it.id }) { config ->
                        MultiHopItem(
                            config = config,
                            onToggle = { vm.enableMultiHop(config.id) },
                            onDelete = { vm.removeMultiHopConfig(config.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog && profiles.size >= 2) {
        var name by remember { mutableStateOf("") }
        var entryId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
        var exitId by remember { mutableStateOf(profiles.getOrNull(1)?.id ?: "") }
        var strategy by remember { mutableStateOf(MultiHopConfig.Strategy.RANDOM) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Multi-Hop", color = WhiteText) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", color = WhiteText.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Entry Proxy Dropdown
                    var expanded1 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded1,
                        onExpandedChange = { expanded1 = it }
                    ) {
                        OutlinedTextField(
                            value = profiles.find { it.id == entryId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Entry Proxy", color = WhiteText.copy(0.5f)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                        )
                        ExposedDropdownMenu(expanded = expanded1, onDismissRequest = { expanded1 = false }) {
                            profiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile.name, color = WhiteText) },
                                    onClick = {
                                        entryId = profile.id
                                        expanded1 = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Exit Proxy Dropdown
                    var expanded2 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded2,
                        onExpandedChange = { expanded2 = it }
                    ) {
                        OutlinedTextField(
                            value = profiles.find { it.id == exitId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Exit Proxy", color = WhiteText.copy(0.5f)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                        )
                        ExposedDropdownMenu(expanded = expanded2, onDismissRequest = { expanded2 = false }) {
                            profiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile.name, color = WhiteText) },
                                    onClick = {
                                        exitId = profile.id
                                        expanded2 = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Strategy Dropdown
                    var expanded3 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded3,
                        onExpandedChange = { expanded3 = it }
                    ) {
                        OutlinedTextField(
                            value = strategy.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Strategy", color = WhiteText.copy(0.5f)) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                        )
                        ExposedDropdownMenu(expanded = expanded3, onDismissRequest = { expanded3 = false }) {
                            MultiHopConfig.Strategy.values().forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name, color = WhiteText) },
                                    onClick = {
                                        strategy = s
                                        expanded3 = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && entryId.isNotBlank() && exitId.isNotBlank()) {
                            val config = MultiHopConfig(
                                name = name,
                                entryProxyId = entryId,
                                exitProxyId = exitId,
                                strategy = strategy
                            )
                            vm.addMultiHopConfig(config)
                            showAddDialog = false
                        }
                    }
                ) { Text("Add", color = WhiteText) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = WhiteText.copy(0.5f))
                }
            },
            containerColor = DarkBackground
        )
    } else if (showAddDialog && profiles.size < 2) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Error", color = WhiteText) },
            text = { Text("You need at least 2 profiles to create a multi-hop config.", color = WhiteText) },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("OK", color = WhiteText)
                }
            },
            containerColor = DarkBackground
        )
    }
}

@Composable
fun MultiHopItem(
    config: MultiHopConfig,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (config.enabled) CyanAccent.copy(alpha = 0.2f) else DarkBackground
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
                    text = config.name.ifEmpty { "Multi-Hop" },
                    color = if (config.enabled) CyanAccent else WhiteText,
                    fontSize = 16.sp
                )
                Text(
                    text = "Entry → Exit | Strategy: ${config.strategy.name}",
                    color = WhiteText.copy(0.5f),
                    fontSize = 12.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { if (!config.enabled) onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedError)
                }
            }
        }
    }
}
