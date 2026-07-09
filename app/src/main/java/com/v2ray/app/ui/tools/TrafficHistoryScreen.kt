package com.v2ray.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.ui.theme.CyanAccent
import com.v2ray.app.ui.theme.RedError
import com.v2ray.app.viewmodel.MainViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficHistoryScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val history by vm.trafficHistory.collectAsStateWithLifecycle()
    val selectedDate by vm.selectedHistoryDate.collectAsStateWithLifecycle()
    val decimalFormat = remember { DecimalFormat("#.#") }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val filteredHistory = if (selectedDate != null) {
        vm.getHistoryForDate(selectedDate!!)
    } else history

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024 * 1024 -> "${decimalFormat.format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
            bytes >= 1024L * 1024 -> "${decimalFormat.format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${decimalFormat.format(bytes / 1024.0)} KB"
            else -> "$bytes B"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Traffic History", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhiteText)
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = WhiteText)
                    }
                    IconButton(onClick = { vm.clearTrafficHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = RedError)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDate != null) "📅 ${dateFormat.format(Date(selectedDate!!))}" else "📅 All time",
                    color = CyanAccent,
                    fontSize = 16.sp
                )
                Text(
                    text = "${filteredHistory.size} records",
                    color = WhiteText.copy(0.5f),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No traffic data available", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                }
            } else {
                val totalDownload = filteredHistory.sumOf { it.download }
                val totalUpload = filteredHistory.sumOf { it.upload }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Download", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            Text(formatBytes(totalDownload), color = CyanAccent, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Upload", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                            Text(formatBytes(totalUpload), color = CyanAccent, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filteredHistory.reversed(), key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dateFormat.format(Date(entry.date)), color = WhiteText.copy(0.5f), fontSize = 11.sp)
                                Text("Download: ${formatBytes(entry.download)}", color = WhiteText, fontSize = 13.sp)
                                Text("Upload: ${formatBytes(entry.upload)}", color = WhiteText.copy(0.7f), fontSize = 12.sp)
                            }
                            Text(formatBytes(entry.total), color = CyanAccent, fontSize = 14.sp)
                        }
                        Divider(color = WhiteText.copy(0.1f))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date", color = WhiteText) },
            text = {
                Column {
                    TextButton(onClick = {
                        vm.setHistoryDate(System.currentTimeMillis())
                        showDatePicker = false
                    }) { Text("Today", color = WhiteText) }
                    TextButton(onClick = {
                        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.let {
                            vm.setHistoryDate(it.timeInMillis)
                        }
                        showDatePicker = false
                    }) { Text("Yesterday", color = WhiteText) }
                    TextButton(onClick = {
                        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.let {
                            vm.setHistoryDate(it.timeInMillis)
                        }
                        showDatePicker = false
                    }) { Text("Last 7 days", color = WhiteText) }
                    TextButton(onClick = {
                        vm.setHistoryDate(null)
                        showDatePicker = false
                    }) { Text("All time", color = WhiteText) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Close", color = WhiteText)
                }
            },
            containerColor = DarkBackground
        )
    }
}
