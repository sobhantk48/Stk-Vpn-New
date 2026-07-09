package com.v2ray.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.app.ui.theme.DarkBackground
import com.v2ray.app.ui.theme.WhiteText
import com.v2ray.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoIPScreen(
    vm: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    val geoCache by vm.geoIPCache.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌍 GeoIP Locations", color = WhiteText) },
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
        ) {
            if (profiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No proxies to show GeoIP for", color = WhiteText.copy(0.5f), fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profiles, key = { it.id }) { profile ->
                        val geo = geoCache[profile.id]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkBackground)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (geo != null) {
                                    Text(geo.flagEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(profile.name, color = WhiteText, fontSize = 16.sp)
                                        Text("${geo.country} - ${geo.city}", color = WhiteText.copy(0.7f), fontSize = 14.sp)
                                        Text("${geo.asn} - ${geo.org}", color = WhiteText.copy(0.5f), fontSize = 12.sp)
                                        Text("${geo.ip}", color = WhiteText.copy(0.4f), fontSize = 11.sp)
                                    }
                                } else {
                                    Text("🌐", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(profile.name, color = WhiteText.copy(0.5f), fontSize = 14.sp)
                                    Text("Loading GeoIP...", color = WhiteText.copy(0.3f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
