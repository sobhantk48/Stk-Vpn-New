package com.v2ray.app.ui.location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.v2ray.app.data.Profile
import com.v2ray.app.viewmodel.MainViewModel

@Composable
fun LocationListScreen(
    viewModel: MainViewModel,
    onProfileSelected: (Profile) -> Unit = {}
) {
    val uiState = viewModel.uiState.value
    val profiles = uiState.profiles

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Locations")

        LazyColumn {
            items(profiles) { profile ->
                LocationItem(
                    profile = profile,
                    onClick = { onProfileSelected(profile) }
                )
            }
        }
    }
}

@Composable
fun LocationItem(profile: Profile, onClick: () -> Unit) {
    Text(
        text = profile.name,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
