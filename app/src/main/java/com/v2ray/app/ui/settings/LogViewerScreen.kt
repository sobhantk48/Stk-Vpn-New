package com.v2ray.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.v2ray.app.utils.Logger
import com.v2ray.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun LogViewerScreen(viewModel: MainViewModel) {
    val logs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        while (true) {
            val newLogs = Logger.getLogs()
            logs.clear()
            logs.addAll(newLogs)
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Logs")
        LazyColumn {
            items(logs) { log ->
                Text(text = log, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}
