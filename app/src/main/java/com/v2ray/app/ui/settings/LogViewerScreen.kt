package com.v2ray.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.viewmodel.LogEntry
import com.v2ray.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    onBack: () -> Unit
) {
    val vm: MainViewModel = hiltViewModel()
    val filteredLogs by vm.filteredLogs.collectAsStateWithLifecycle()
    val logFilter by vm.logFilter.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var filterText by remember { mutableStateOf(logFilter) }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Logs", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhiteText)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.clearLogs() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = WhiteText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    scrolledContainerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // نوار جستجو
            OutlinedTextField(
                value = filterText,
                onValueChange = { 
                    filterText = it
                    vm.setLogFilter(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search logs...", color = WhiteText.copy(0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = WhiteText.copy(0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhiteText.copy(0.3f),
                    unfocusedBorderColor = WhiteText.copy(0.2f),
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                ),
                singleLine = true
            )

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (logFilter.isNotEmpty()) "No logs match your search." else "No logs yet.\nConnect to VPN to see logs.",
                        color = WhiteText.copy(0.5f),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = listState,
                    reverseLayout = false
                ) {
                    items(
                        items = filteredLogs,
                        key = { it.timestamp }
                    ) { entry ->
                        LogItem(entry = entry)
                        Divider(color = WhiteText.copy(0.1f))
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(entry: LogEntry) {
    val (color, prefix) = when (entry.level) {
        "ERROR" -> Pair(androidx.compose.ui.graphics.Color.Red, "❌")
        "WARN" -> Pair(androidx.compose.ui.graphics.Color.Yellow, "⚠️")
        "SUCCESS" -> Pair(androidx.compose.ui.graphics.Color.Green, "✅")
        else -> Pair(androidx.compose.ui.graphics.Color.Cyan, "ℹ️")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$prefix ${entry.message}",
            color = color,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
