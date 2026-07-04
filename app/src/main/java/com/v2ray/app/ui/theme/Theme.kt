package com.v2ray.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF081020)
val DarkSurface = Color(0xFF141B2D)
val PrimaryBlue = Color(0xFF4F7CFF)
val CyanAccent = Color(0xFF00D4FF)
val GreenAccent = Color(0xFF00E676)
val RedError = Color(0xFFFF2522)
val WhiteText = Color(0xFFFFFFFF)

private val DarkScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    primary = PrimaryBlue,
    secondary = CyanAccent,
    onBackground = WhiteText,
    onSurface = WhiteText
)

@Composable
fun V2rayAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
