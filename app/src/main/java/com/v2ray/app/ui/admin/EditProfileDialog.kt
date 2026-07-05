package com.v2ray.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {

    var name by remember { mutableStateOf(profile.name) }
    var addr by remember { mutableStateOf(profile.address) }
    var port by remember { mutableStateOf(profile.port.toString()) }
    var type by remember { mutableStateOf(profile.type) }
    var uuid by remember { mutableStateOf(profile.uuid) }

    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = WhiteText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // NAME
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Name", color = WhiteText.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )

                // ADDRESS
                OutlinedTextField(
                    value = addr,
                    onValueChange = {
                        addr = it
                        error = null
                    },
                    label = { Text("Address", color = WhiteText.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )

                // PORT + UUID
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedTextField(
                        value = port,
                        onValueChange = {
                            port = it
                            error = null
                        },
                        label = { Text("Port", color = WhiteText.copy(0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = WhiteText.copy(0.3f),
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )

                    OutlinedTextField(
                        value = uuid,
                        onValueChange = {
                            uuid = it
                            error = null
                        },
                        label = { Text("UUID", color = WhiteText.copy(0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = WhiteText.copy(0.3f),
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                }

                // PROTOCOL DROPDOWN
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {

                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Protocol", color = WhiteText.copy(0.7f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = WhiteText.copy(0.3f),
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("VLESS", "VMESS", "Trojan", "Shadowsocks").forEach {
                            DropdownMenuItem(
                                text = { Text(it, color = WhiteText) },
                                onClick = {
                                    type = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = RedError, fontSize = 12.sp)
                }
            }
        },

        // SAVE BUTTON
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || addr.isBlank() || port.isBlank()) {
                        error = "Please fill all required fields"
                        return@Button
                    }

                    val updated = profile.copy(
                        name = name,
                        address = addr,
                        port = port.toIntOrNull() ?: 443,
                        type = type,
                        uuid = uuid
                    )

                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", color = WhiteText, fontWeight = FontWeight.Bold)
            }
        },

        // CANCEL BUTTON
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = WhiteText)
            }
        },

        containerColor = DarkSurface,
        titleContentColor = WhiteText,
        textContentColor = WhiteText
    )
}
