package com.v2ray.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(onItemClick: (String) -> Unit) {
    ModalDrawerSheet(
        modifier = Modifier.width(DrawerDefaults.MaximumDrawerWidth),
        drawerContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "V2RAY STK", fontSize = 20.sp)

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            NavigationDrawerItem(
                label = { Text("Settings") },
                selected = false,
                onClick = { onItemClick("settings") }
            )

            NavigationDrawerItem(
                label = { Text("Location List") },
                selected = false,
                onClick = { onItemClick("locations") }
            )

            NavigationDrawerItem(
                label = { Text("About Us") },
                selected = false,
                onClick = { onItemClick("about") }
            )

            NavigationDrawerItem(
                label = { Text("View Logs") },
                selected = false,
                onClick = { onItemClick("logs") }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            NavigationDrawerItem(
                label = { Text("Admin") },
                selected = false,
                onClick = { onItemClick("admin") }
            )
        }
    }
}
