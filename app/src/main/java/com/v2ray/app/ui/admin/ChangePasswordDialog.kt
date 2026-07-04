package com.v2ray.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.security.AdminSession
import com.v2ray.app.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", color = WhiteText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = old,
                    onValueChange = { 
                        old = it
                        error = null
                    },
                    label = { Text("Old Password", color = WhiteText.copy(0.7f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { 
                        new = it
                        error = null
                    },
                    label = { Text("New Password (min 4 chars)", color = WhiteText.copy(0.7f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { 
                        confirm = it
                        error = null
                    },
                    label = { Text("Confirm New Password", color = WhiteText.copy(0.7f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = WhiteText.copy(0.3f),
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                if (error != null) {
                    Text(error!!, color = RedError, fontSize = 14.sp)
                }
                if (success) {
                    Text("Password changed successfully!", color = GreenAccent, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        new.length < 4 -> error = "New password must be at least 4 characters"
                        new != confirm -> error = "Passwords do not match"
                        else -> {
                            if (AdminSession.changePassword(old, new)) {
                                success = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1500)
                                    onSuccess()
                                }
                            } else {
                                error = "Old password is incorrect"
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Change", color = WhiteText, fontWeight = FontWeight.Bold)
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
