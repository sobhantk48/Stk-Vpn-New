package com.v2ray.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.ui.theme.*
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    var protocol by remember { mutableStateOf("VLESS") }
    val protocols = listOf("VLESS", "VMESS", "Trojan", "Shadowsocks")

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Default Protocol", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        protocols.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { protocol = p }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(p, color = WhiteText)
                                if (protocol == p) {
                                    Icon(Icons.Default.Check, tint = CyanAccent, contentDescription = null)
                                }
                            }
                            Divider(color = WhiteText.copy(0.1f))
                        }
                    }
                }
            }
            listOf("Auto Connect", "Stay Connected", "Show Notification").forEach { title ->
                item {
                    SwitchSetting(title, false, {})
                }
            }
        }
    }
}

@Composable
fun SwitchSetting(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
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
            Text(title, color = WhiteText)
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    uncheckedThumbColor = Color.Gray
                )
            )
        }
    }
}
