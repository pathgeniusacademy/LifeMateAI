package com.pathgeniusacademy.lifemate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF007F73),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCEF5E9),
    onPrimaryContainer = Color(0xFF004E46),
    secondary = Color(0xFF687D32),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF3D3),
    onSecondaryContainer = Color(0xFF34451C),
    tertiary = Color(0xFFC47B42),
    background = Color(0xFFF5F7F4),
    onBackground = Color(0xFF172C29),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172C29),
    surfaceVariant = Color(0xFFEAF0EB),
    onSurfaceVariant = Color(0xFF60736C),
    outline = Color(0xFFD3DFD8),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7EE0C6),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF125447),
    onPrimaryContainer = Color(0xFFC7F9E9),
    secondary = Color(0xFFD4E899),
    secondaryContainer = Color(0xFF39451C),
    tertiary = Color(0xFFFFB870),
    background = Color(0xFF0B1715),
    onBackground = Color(0xFFE4EFE8),
    surface = Color(0xFF12231F),
    onSurface = Color(0xFFE4EFE8),
    surfaceVariant = Color(0xFF20332D),
    onSurfaceVariant = Color(0xFFADC4B9),
    outline = Color(0xFF354D43)
)

private val LifeMateTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp)
)

private val LifeMateShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = LifeMateTypography,
        shapes = LifeMateShapes,
        content = content
    )
}
