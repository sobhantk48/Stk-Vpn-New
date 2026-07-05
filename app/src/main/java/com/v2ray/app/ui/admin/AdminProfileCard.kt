package com.v2ray.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.app.data.Profile
import com.v2ray.app.ui.theme.*

@Composable
fun AdminProfileCard(
    profile: Profile,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    profile.name.ifEmpty { "Unnamed" },
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    "${profile.type} • ${profile.address}:${profile.port}",
                    color = CyanAccent,
                    fontSize = 12.sp
                )

                if (profile.selected) {
                    Text(
                        "★ SELECTED",
                        color = GreenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row {

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        tint = CyanAccent,
                        contentDescription = "Edit"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        tint = RedError,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}
