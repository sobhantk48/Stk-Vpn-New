package com.v2ray.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.security.AdminSession
import com.v2ray.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(onSuccess: () -> Unit, onBack: () -> Unit) {
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter Admin Password", color = WhiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Password", color = WhiteText.copy(0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = WhiteText.copy(0.3f),
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Text(error!!, color = RedError, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (AdminSession.validatePassword(pass)) {
                        AdminSession.login()
                        onSuccess()
                    } else {
                        error = "Invalid password"
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login", color = WhiteText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
