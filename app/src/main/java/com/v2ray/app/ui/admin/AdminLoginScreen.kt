package com.v2ray.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Login", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, tint = WhiteText, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter Admin Password",
                color = WhiteText,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = false
                },
                label = { Text("Password", color = WhiteText.copy(0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = error,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = WhiteText.copy(0.3f),
                    focusedLabelColor = CyanAccent,
                    unfocusedLabelColor = WhiteText.copy(0.5f),
                    textColor = WhiteText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (error) {
                Text(
                    text = "Incorrect password. Try again.",
                    color = RedError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password == "admin") {
                        onSuccess()
                    } else {
                        error = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Login", color = WhiteText, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Cancel", color = WhiteText.copy(0.5f))
            }
        }
    }
}
