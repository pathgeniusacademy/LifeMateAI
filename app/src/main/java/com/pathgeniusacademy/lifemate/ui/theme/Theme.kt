package com.pathgeniusacademy.lifemate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B5FEF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E4FF),
    onPrimaryContainer = Color(0xFF171858),
    secondary = Color(0xFF00A7A5),
    secondaryContainer = Color(0xFFD1F7F3),
    tertiary = Color(0xFFFF8A4C),
    background = Color(0xFFF7F8FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F1F7),
    outline = Color(0xFFD8D9E3),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBEC2FF),
    onPrimary = Color(0xFF272A8B),
    primaryContainer = Color(0xFF3F43B5),
    secondary = Color(0xFF77DAD5),
    tertiary = Color(0xFFFFB68F),
    background = Color(0xFF111218),
    surface = Color(0xFF191A21),
    surfaceVariant = Color(0xFF24252E)
)

@Composable
fun LifeMateTheme(themeMode: String, content: @Composable () -> Unit) {
    val dark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
