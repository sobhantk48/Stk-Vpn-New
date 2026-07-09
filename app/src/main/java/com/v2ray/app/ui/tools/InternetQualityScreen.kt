package com.v2ray.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.v2ray.app.ui.theme.GreenSuccess
import com.v2ray.app.ui.theme.CyanAccent
import com.v2ray.app.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternetQualityScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val quality by vm.internetQuality.collectAsStateWithLifecycle()
    val decimalFormat = remember { DecimalFormat("#.#") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Internet Quality", color = WhiteText) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (quality == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Run speed test to check your internet quality", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { vm.startInternetQualityTest() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = WhiteText)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Test", color = WhiteText)
                        }
                    }
                }
            } else {
                quality?.let { q ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkBackground)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📶 Speed", color = CyanAccent, fontSize = 18.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Download", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${decimalFormat.format(q.downloadSpeed)} Mbps", color = GreenSuccess, fontSize = 20.sp)
                                }
                                Column {
                                    Text("Upload", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${decimalFormat.format(q.uploadSpeed)} Mbps", color = GreenSuccess, fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Ping", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${q.ping} ms", color = if (q.ping < 50) GreenSuccess else CyanAccent, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Jitter", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${q.jitter} ms", color = if (q.jitter < 20) GreenSuccess else CyanAccent, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Packet Loss", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${decimalFormat.format(q.packetLoss)}%", color = if (q.packetLoss < 1) GreenSuccess else CyanAccent, fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("🎮 Quality Scores", color = CyanAccent, fontSize = 18.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Gaming", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${q.gamingScore}", color = if (q.gamingScore > 70) GreenSuccess else CyanAccent, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Browsing", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${q.browsingScore}", color = if (q.browsingScore > 70) GreenSuccess else CyanAccent, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Streaming", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${q.streamingScore}", color = if (q.streamingScore > 60) GreenSuccess else CyanAccent, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Video Call", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                    Text("${q.videoCallScore}", color = if (q.videoCallScore > 60) GreenSuccess else CyanAccent, fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { vm.startInternetQualityTest() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                            ) { Text("Retest", color = WhiteText) }
                        }
                    }
                }
            }
        }
    }
}
