package com.smartambulance.driver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Crimson = Color(0xFFE11D48)
val Teal = Color(0xFF14B8A6)
val Amber = Color(0xFFF59E0B)
val Blue = Color(0xFF3B82F6)
val Ink = Color(0xFF070B14)
val Panel = Color(0xFF121826)
val PanelAlt = Color(0xFF1A2333)
val Paper = Color(0xFFF4F7FB)
val Mute = Color(0xFF8B97A8)

private val Scheme = darkColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    secondary = Teal,
    onSecondary = Ink,
    tertiary = Amber,
    background = Ink,
    onBackground = Paper,
    surface = Panel,
    onSurface = Paper,
    surfaceVariant = PanelAlt,
    onSurfaceVariant = Mute,
    outline = Color(0xFF2A3548),
    error = Crimson
)

private val Type = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.2.sp)
)

@Composable
fun SmartAmbulanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}
