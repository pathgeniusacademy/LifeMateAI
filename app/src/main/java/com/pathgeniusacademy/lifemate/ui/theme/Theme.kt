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
    primary = Color(0xFF5357E7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E9FF),
    onPrimaryContainer = Color(0xFF20236D),
    secondary = Color(0xFF0B9F8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9F6EF),
    onSecondaryContainer = Color(0xFF0A5047),
    tertiary = Color(0xFFF39B42),
    background = Color(0xFFF7F8FD),
    onBackground = Color(0xFF181A24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181A24),
    surfaceVariant = Color(0xFFF0F2F8),
    onSurfaceVariant = Color(0xFF676B7C),
    outline = Color(0xFFD8DBE7),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC3C5FF),
    onPrimary = Color(0xFF282B8B),
    primaryContainer = Color(0xFF3C40A8),
    onPrimaryContainer = Color(0xFFE5E6FF),
    secondary = Color(0xFF70DDC8),
    secondaryContainer = Color(0xFF104F46),
    tertiary = Color(0xFFFFB870),
    background = Color(0xFF101117),
    onBackground = Color(0xFFE7E8EF),
    surface = Color(0xFF181A21),
    onSurface = Color(0xFFE7E8EF),
    surfaceVariant = Color(0xFF232631),
    onSurfaceVariant = Color(0xFFC3C5D0),
    outline = Color(0xFF3A3D49)
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
