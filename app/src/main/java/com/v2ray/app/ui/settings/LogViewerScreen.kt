package com.v2ray.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.ui.theme.*
import com.v2ray.app.utils.Logger
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(onBack: () -> Unit) {
    var logs by remember { mutableStateOf("Loading logs...") }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // بارگذاری لاگ‌ها
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        val content = Logger.getLogContent()
        logs = if (content.isNullOrBlank()) {
            "No logs available.\n\nLog file path: ${Logger.getLogFilePath() ?: "Unknown"}\n\nMake sure the app has storage permissions."
        } else {
            content
        }
        isLoading = false
    }

    // به‌روزرسانی خودکار هر ۵ ثانیه
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            refreshTrigger++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Viewer", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, tint = WhiteText, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        refreshTrigger++
                    }) {
                        Icon(Icons.Default.Refresh, tint = CyanAccent, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        Logger.clearLogs()
                        refreshTrigger++
                    }) {
                        Icon(Icons.Default.Delete, tint = RedError, contentDescription = "Clear")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
        ) {
            // نمایش مسیر فایل لاگ
            Text(
                text = "Log File: ${Logger.getLogFilePath() ?: "Unknown"}",
                color = WhiteText.copy(0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    item {
                        Text(
                            text = logs,
                            color = WhiteText.copy(0.9f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
