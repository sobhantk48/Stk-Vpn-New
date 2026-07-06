package com.v2ray.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.ui.theme.*
import com.v2ray.app.utils.Logger
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var logs by rememberSaveable { mutableStateOf("Loading logs...") }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var refreshTrigger by rememberSaveable { mutableStateOf(0) }

    // بارگذاری لاگ‌ها
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        // اطمینان از مقداردهی Logger
        Logger.init(context)
        val content = Logger.getLogContent()
        logs = if (content.isNullOrBlank()) {
            "No logs available.\n\nLog file path: ${Logger.getLogFilePath() ?: "Unknown"}\n\nMake sure the app has storage permissions."
        } else {
            content
        }
        isLoading = false
    }

    // رفرش خودکار هر ۵ ثانیه
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
                    IconButton(onClick = { refreshTrigger++ }) {
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
            Text(
                text = "Log File: ${Logger.getLogFilePath() ?: "Unknown"}",
                color = WhiteText.copy(0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(16.dp)
            )
            Divider(color = WhiteText.copy(0.1f))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else {
                // نمایش لاگ‌ها با شماره خط
                val lines = logs.split("\n")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(lines) { line ->
                        if (line.isNotBlank()) {
                            Text(
                                text = line,
                                color = when {
                                    line.contains("[ERROR]") -> RedError
                                    line.contains("[WARN]") -> CyanAccent.copy(0.8f)
                                    else -> WhiteText.copy(0.9f)
                                },
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
