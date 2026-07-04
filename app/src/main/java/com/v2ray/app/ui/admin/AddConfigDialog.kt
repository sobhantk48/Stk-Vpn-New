package com.v2ray.app.ui.admin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.v2ray.app.data.Profile
import java.util.UUID

@Composable
fun AddConfigDialog(
    onDismiss: () -> Unit,
    onAdd: (Profile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var uuid by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile") },
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
                val profile = Profile(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    server = server,
                    port = port.toIntOrNull() ?: 443,
                    uuid = uuid
                )
                onAdd(profile)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
