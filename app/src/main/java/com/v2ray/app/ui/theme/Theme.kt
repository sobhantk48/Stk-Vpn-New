package com.v2ray.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = CyanAccent,
    background = DarkBackground,
    surface = DarkSurface,
    error = RedError,
    onPrimary = WhiteText,
    onSecondary = WhiteText,
    onBackground = WhiteText,
    onSurface = WhiteText,
    onError = WhiteText
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = CyanAccent,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    error = RedError,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Black
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun V2rayAppTheme(content: @Composable () -> Unit) {
    AppTheme(content = content)
}
