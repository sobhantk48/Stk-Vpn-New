package com.v2ray.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.*
import com.v2ray.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    nav: NavController,
    drawer: DrawerState,
    vm: MainViewModel,
    onAdminClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLocations: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val state by vm.state.collectAsState()
    val current by vm.current.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerContentColor = WhiteText
            ) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("V2RAY STK", color = PrimaryBlue, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp))
                    DrawerItem(Icons.Default.Settings, "Settings", onNavigateToSettings)
                    DrawerItem(Icons.Default.LocationOn, "Location List", onNavigateToLocations)
                    DrawerItem(Icons.Default.Info, "About Us", onNavigateToAbout)
                    DrawerItem(Icons.Default.List, "View Logs", onNavigateToLogs)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("V2RAY STK", color = WhiteText) },
                    navigationIcon = {
                        IconButton({ scope.launch { if (drawer.isClosed) drawer.open() else drawer.close() } }) {
                            Icon(Icons.Default.Menu, tint = WhiteText, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton({ expanded = true }) { Icon(Icons.Default.MoreVert, tint = WhiteText, contentDescription = "Admin") }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Admin Panel", color = WhiteText) },
                                onClick = {
                                    expanded = false
                                    onAdminClick()
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    )
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(pad)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                ConnectionStatusSection(state)
                ConnectButton(
                    status = state.status,
                    onClick = {
                        when (state.status) {
                            ConnectionStatus.IDLE, ConnectionStatus.DISCONNECTED -> current?.let { vm.connect(it) }
                            ConnectionStatus.CONNECTED -> vm.disconnect()
                            ConnectionStatus.ERROR -> {
                                vm.clearError()
                                current?.let { vm.connect(it) }
                            }
                            else -> {}
                        }
                    }
                )
                CurrentProfileCard(current)
                Text("v1.0.0", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                if (state.errorMessage != null) {
                    Text(
                        state.errorMessage!!,
                        color = RedError,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = WhiteText, fontSize = 16.sp)
    }
}

@Composable
fun ConnectionStatusSection(state: com.v2ray.app.data.ConnectionState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when (state.status) {
                ConnectionStatus.IDLE -> "DISCONNECTED"
                ConnectionStatus.CONNECTING -> "CONNECTING..."
                ConnectionStatus.CONNECTED -> "CONNECTED"
                ConnectionStatus.DISCONNECTED -> "DISCONNECTED"
                ConnectionStatus.ERROR -> "ERROR"
            },
            color = when (state.status) {
                ConnectionStatus.CONNECTED -> GreenAccent
                ConnectionStatus.ERROR -> RedError
                else -> RedError
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        if (state.status == ConnectionStatus.CONNECTED) {
            Text(
                text = "CONNECTED: ${state.connectedTime}",
                color = WhiteText.copy(0.7f),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpeedItem("PING", "${state.ping} ms")
            SpeedItem("DOWNLOAD", "${state.downloadSpeed} Mbps")
            SpeedItem("UPLOAD", "${state.uploadSpeed} Mbps")
        }
    }
}

@Composable
fun SpeedItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = WhiteText.copy(0.6f), fontSize = 12.sp)
    }
}

@Composable
fun ConnectButton(status: ConnectionStatus, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = when (status) {
                ConnectionStatus.CONNECTED -> GreenAccent
                ConnectionStatus.CONNECTING -> CyanAccent
                ConnectionStatus.ERROR -> RedError
                else -> PrimaryBlue
            }
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp),
        enabled = status != ConnectionStatus.CONNECTING
    ) {
        Text(
            text = when (status) {
                ConnectionStatus.IDLE, ConnectionStatus.DISCONNECTED -> "CONNECT"
                ConnectionStatus.CONNECTING -> "CONNECTING"
                ConnectionStatus.CONNECTED -> "DISCONNECT"
                ConnectionStatus.ERROR -> "RETRY"
            },
            color = DarkBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun CurrentProfileCard(profile: Profile?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (profile != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = profile.name.ifEmpty { "No Name" },
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = profile.type + " • TLS" + if (profile.realityPublicKey.isNotBlank()) " • Reality" else "",
                    color = CyanAccent,
                    fontSize = 14.sp
                )
                Text(
                    text = "${profile.address}:${profile.port}",
                    color = WhiteText.copy(0.7f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${profile.ping} ms",
                    color = GreenAccent,
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                text = "No profile selected",
                color = WhiteText.copy(0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
