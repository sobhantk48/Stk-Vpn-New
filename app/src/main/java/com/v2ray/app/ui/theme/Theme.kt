package com.v2ray.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
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

// تابع اصلی با نام AppTheme (برای استفاده در سایر نقاط)
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// نام مستعار برای سازگاری با MainActivity
@Composable
fun V2rayAppTheme(content: @Composable () -> Unit) {
    AppTheme(content = content)
}
