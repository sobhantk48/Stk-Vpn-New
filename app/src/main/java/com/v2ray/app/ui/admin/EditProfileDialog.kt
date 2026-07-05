package com.v2ray.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.v2ray.app.data.Profile

@Composable
fun EditProfileDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var server by remember { mutableStateOf(profile.server) }
    var port by remember { mutableStateOf(profile.port.toString()) }
    var uuid by remember { mutableStateOf(profile.uuid) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = server, onValueChange = { server = it }, label = { Text("Server") })
                TextField(value = port, onValueChange = { port = it }, label = { Text("Port") })
                TextField(value = uuid, onValueChange = { uuid = it }, label = { Text("UUID") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = profile.copy(
                    name = name,
                    server = server,
                    port = port.toIntOrNull() ?: 443,
                    uuid = uuid
                )
                onSave(updated)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
