package com.v2ray.app.ui.subscription

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
import com.v2ray.app.model.Subscription
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val subscriptions by vm.subscriptions.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📡 Subscriptions", color = WhiteText) },
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
            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subscriptions yet.\nTap + to add one.",
                        color = WhiteText.copy(0.5f),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subscriptions, key = { it.id }) { sub ->
                        SubscriptionItem(
                            subscription = sub,
                            onImport = { vm.importFromSubscription(sub.url) },
                            onRemove = { vm.removeSubscription(sub.id) },
                            onUpdate = { vm.updateSubscription(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Subscription", color = WhiteText) },
            text = {
                Column {
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("Subscription URL", color = WhiteText.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Name (optional)", color = WhiteText.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (urlText.isNotBlank()) {
                            vm.addSubscription(urlText, nameText)
                            vm.importFromSubscription(urlText)
                            urlText = ""
                            nameText = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add", color = WhiteText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = WhiteText.copy(0.5f))
                }
            },
            containerColor = DarkBackground
        )
    }
}

@Composable
fun SubscriptionItem(
    subscription: Subscription,
    onImport: () -> Unit,
    onRemove: () -> Unit,
    onUpdate: (Subscription) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBackground)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = subscription.name.ifEmpty { "Unnamed" },
                        color = WhiteText,
                        fontSize = 16.sp
                    )
                    Text(
                        text = subscription.url.take(50),
                        color = WhiteText.copy(0.5f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Nodes: ${subscription.nodeCount} | Last update: ${if (subscription.lastUpdate > 0) "recent" else "never"}",
                        color = WhiteText.copy(0.4f),
                        fontSize = 11.sp
                    )
                }
                Row {
                    IconButton(onClick = onImport) {
                        Icon(Icons.Default.Download, contentDescription = "Import", tint = GreenSuccess)
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = RedError)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = subscription.autoUpdate,
                    onCheckedChange = {
                        onUpdate(subscription.copy(autoUpdate = it))
                    }
                )
                Text(
                    text = if (subscription.autoUpdate) "Auto-update every ${subscription.updateInterval}h" else "Manual update",
                    color = WhiteText.copy(0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
