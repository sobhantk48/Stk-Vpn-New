package com.v2ray.app.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.v2ray.app.data.Profile
import com.v2ray.app.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLocations: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val uiState = viewModel.uiState.value
    val connectionState = uiState.connectionState

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "V2RAY STK Dashboard")
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Status: ${connectionState.status}")
        connectionState.errorMessage?.let {
            Text(text = "Error: $it", color = androidx.compose.ui.graphics.Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.connect(Profile(name = "Test Server")) },
            enabled = connectionState.status != com.v2ray.app.data.ConnectionStatus.CONNECTED
        ) {
            Text("Connect")
        }

        Button(
            onClick = { viewModel.disconnect() },
            enabled = connectionState.status == com.v2ray.app.data.ConnectionStatus.CONNECTED
        ) {
            Text("Disconnect")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToLocations) { Text("Locations") }
        Button(onClick = onNavigateToSettings) { Text("Settings") }
        Button(onClick = onNavigateToLogs) { Text("Logs") }
    }
}
