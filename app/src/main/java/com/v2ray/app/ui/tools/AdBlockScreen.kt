package com.v2ray.app.ui.tools

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
import com.v2ray.app.model.AdBlockRule
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.ui.theme.GreenSuccess
import com.v2ray.app.ui.theme.RedError
import com.v2ray.app.ui.theme.PrimaryBlue
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdBlockScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val enabled by vm.adBlockEnabled.collectAsStateWithLifecycle()
    val rules by vm.adBlockRules.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var domainText by remember { mutableStateOf("") }
    var ruleType by remember { mutableStateOf(AdBlockRule.RuleType.AD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛡️ Ad Block", color = WhiteText) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ad Block", color = WhiteText, fontSize = 18.sp)
                Switch(
                    checked = enabled,
                    onCheckedChange = { vm.toggleAdBlock() }
                )
            }
            Text(
                text = if (enabled) "Active - Blocking ads and trackers" else "Inactive",
                color = if (enabled) GreenSuccess else WhiteText.copy(0.5f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Rules: ${rules.size}", color = WhiteText.copy(0.7f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (rules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No rules added yet", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(rules, key = { it.id }) { rule ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rule.domain, color = WhiteText, fontSize = 14.sp)
                                Text(rule.type.name, color = WhiteText.copy(0.5f), fontSize = 11.sp)
                            }
                            IconButton(onClick = { vm.removeAdBlockRule(rule.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = RedError)
                            }
                        }
                        Divider(color = WhiteText.copy(0.1f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Rule", color = WhiteText) },
            text = {
                Column {
                    OutlinedTextField(
                        value = domainText,
                        onValueChange = { domainText = it },
                        label = { Text("Domain", color = WhiteText.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdBlockRule.RuleType.values().forEach { type ->
                            FilterChip(
                                selected = ruleType == type,
                                onClick = { ruleType = type },
                                label = { Text(type.name, color = WhiteText, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (domainText.isNotBlank()) {
                            vm.addAdBlockRule(domainText, ruleType)
                            domainText = ""
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
    }
}
