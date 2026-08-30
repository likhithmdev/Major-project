package com.smartambulance.driver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Design System Colors
val Background = Color(0xFF070B12)      // Dark background
val CardBackground = Color(0xFF0F1623)  // Card background
val ElevatedCard = Color(0xFF162030)    // Elevated card
val PrimaryRed = Color(0xFFEF233C)       // Emergency Red
val SecondaryAmber = Color(0xFFF59E0B)  // Amber
val SuccessGreen = Color(0xFF10B981)    // Success
val Muted = Color(0xFF1E2940)           // Muted
val Border = Color(0xFF1E2D42)          // Border
val TextPrimary = Color(0xFFF0F4FF)    // Primary text
val TextMuted = Color(0xFF6B7A99)       // Muted text
val TextDim = Color(0xFF3D4F6E)         // Dim text

// Role colors
val DriverRed = Color(0xFFEF233C)
val PoliceBlue = Color(0xFF3B82F6)
val HospitalGreen = Color(0xFF10B981)
val AdminAmber = Color(0xFFF59E0B)

// Legacy colors (for backward compatibility)
val Crimson = PrimaryRed
val Teal = SuccessGreen
val Amber = SecondaryAmber
val Blue = PoliceBlue
val Ink = Background
val Panel = CardBackground
val PanelAlt = ElevatedCard
val Paper = TextPrimary
val Mute = TextMuted

private val Scheme = darkColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    secondary = SecondaryAmber,
    onSecondary = Background,
    tertiary = SuccessGreen,
    background = Background,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedCard,
    onSurfaceVariant = TextMuted,
    outline = Border,
    error = PrimaryRed
)

private val Type = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = TextMuted
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
        color = TextDim
    )
)

@Composable
fun SmartAmbulanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}
