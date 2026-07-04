package com.v2ray.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.v2ray.app.data.Profile
import com.v2ray.app.viewmodel.MainViewModel

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    onAddProfile: () -> Unit = {}
) {
    val uiState = viewModel.uiState.value
    val profiles = uiState.profiles

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Admin")

        Button(onClick = onAddProfile) {
            Text("Add Profile")
        }

        LazyColumn {
            items(profiles) { profile ->
                AdminProfileItem(
                    profile = profile,
                    onDelete = { viewModel.delete(profile) },
                    onEdit = { viewModel.update(profile) }
                )
            }
        }
    }
}

@Composable
fun AdminProfileItem(profile: Profile, onDelete: () -> Unit, onEdit: () -> Unit) {
    Text(
        text = "${profile.name} (${profile.server}:${profile.port})",
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
