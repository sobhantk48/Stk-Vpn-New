package com.v2ray.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
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
import com.v2ray.app.ui.theme.GreenSuccess
import com.v2ray.app.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isRunning by vm.speedTestRunning.collectAsStateWithLifecycle()
    val results by vm.speedTestResults.collectAsStateWithLifecycle()
    val decimalFormat = remember { DecimalFormat("#.#") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚀 Speed Test", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhiteText)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                CircularProgressIndicator(color = CyanAccent)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Testing your internet speed...", color = WhiteText.copy(0.7f), fontSize = 16.sp)
            } else if (results != null) {
                results?.let { r ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊 Results", color = CyanAccent, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Download", color = WhiteText.copy(0.5f), fontSize = 14.sp)
                                    Text("${decimalFormat.format(r.downloadSpeed)} Mbps", color = GreenSuccess, fontSize = 24.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Upload", color = WhiteText.copy(0.5f), fontSize = 14.sp)
                                    Text("${decimalFormat.format(r.uploadSpeed)} Mbps", color = GreenSuccess, fontSize = 24.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Ping: ${r.ping} ms", color = WhiteText, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { vm.runSpeedTest() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = WhiteText)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retest", color = WhiteText)
                            }
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Test your internet speed", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { vm.runSpeedTest() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = WhiteText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Test", color = WhiteText)
                    }
                }
            }
        }
    }
}
