package com.v2ray.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    primary = PrimaryBlue,
    secondary = CyanAccent,
    onBackground = WhiteText,
    onSurface = WhiteText,
    error = RedError
)

@Composable
fun V2rayAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = V2rayTypography,
        shapes = V2rayShapes,
        content = content
    )
}
