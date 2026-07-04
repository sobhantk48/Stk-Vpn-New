package com.v2ray.app.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.v2ray.app.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onConnect: () -> Unit = {},
    onDisconnect: () -> Unit = {}
) {
    val uiState = viewModel.uiState.value
    val connectionState = uiState.connectionState

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Dashboard")
        Text(text = "Status: ${connectionState.status}")

        Button(
            onClick = onConnect,
            enabled = connectionState.status.isConnected().not()
        ) {
            Text(text = "Connect")
        }

        Button(
            onClick = onDisconnect,
            enabled = connectionState.status.isConnected()
        ) {
            Text(text = "Disconnect")
        }

        // نمایش خطا (اگه وجود داشته باشه)
        connectionState.errorMessage?.let { error ->
            Text(text = "Error: $error")
        }
    }
}

// تابع extension برای سهولت
fun com.v2ray.app.data.ConnectionStatus.isConnected(): Boolean =
    this == com.v2ray.app.data.ConnectionStatus.CONNECTED
