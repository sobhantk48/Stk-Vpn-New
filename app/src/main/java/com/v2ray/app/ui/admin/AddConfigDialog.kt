package com.v2ray.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConfigDialog(onDismiss: () -> Unit, onAdd: (Profile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var addr by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var type by remember { mutableStateOf("VLESS") }
    var uuid by remember { mutableStateOf("") }
    var jsonInput by remember { mutableStateOf("") }
    var parseError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Config", color = WhiteText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { 
                        jsonInput = it
                        parseError = null
                    },
                    label = { Text("Paste JSON or Link", color = WhiteText.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                if (parseError != null) {
                    Text(
                        text = parseError!!,
                        color = RedError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            try {
                                if (jsonInput.isBlank()) {
                                    parseError = "Please paste JSON or link"
                                    return@Button
                                }
                                Profile.fromLink(jsonInput)?.let { p ->
                                    name = p.name
                                    addr = p.address
                                    port = p.port.toString()
                                    type = p.type
                                    uuid = p.uuid
                                    parseError = null
                                } ?: run {
                                    Profile.fromJson(jsonInput)?.let { p ->
                                        name = p.name
                                        addr = p.address
                                        port = p.port.toString()
                                        type = p.type
                                        uuid = p.uuid
                                        parseError = null
                                    } ?: run {
                                        parseError = "Invalid JSON or link format"
                                    }
                                }
                            } catch (e: Exception) {
                                parseError = "Parse error: ${e.message}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Parse", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            jsonInput = """{"name":"Test Server","address":"example.com","port":443,"type":"VLESS","uuid":"00000000-0000-0000-0000-000000000000"}"""
                            parseError = null
                        },
                        colors = ButtonDefaults.buttonColors(PrimaryBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sample", color = WhiteText, fontSize = 12.sp)
                    }
                }
                Divider(color = WhiteText.copy(0.2f))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = WhiteText.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                OutlinedTextField(
                    value = addr,
                    onValueChange = { addr = it },
                    label = { Text("Address", color = WhiteText.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port", color = WhiteText.copy(0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = WhiteText.copy(0.3f),
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                    OutlinedTextField(
                        value = uuid,
                        onValueChange = { uuid = it },
                        label = { Text("UUID", color = WhiteText.copy(0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = WhiteText.copy(0.3f),
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        )
                    )
                }
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
                            focusedBorderColor = PrimaryBlue,
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && addr.isNotBlank() && port.isNotBlank()) {
                        onAdd(Profile(
                            name = name,
                            type = type,
                            address = addr,
                            port = port.toIntOrNull() ?: 443,
                            uuid = uuid
                        ))
                    } else {
                        parseError = "Please fill all required fields"
                    }
                },
                colors = ButtonDefaults.buttonColors(GreenAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
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
