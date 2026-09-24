package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = ElectricIndigoDark,
    onPrimaryContainer = Color.White,
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    secondaryContainer = DarkCard,
    onSecondaryContainer = NeonCyan,
    tertiary = VibrantAmber,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFF0F6FC),
    surface = DarkSurface,
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFF8B949E),
    outline = DarkBorder,
    error = CoralCrimson
)

private val LightColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = NeonCyan,
    onPrimaryContainer = Color.Black,
    secondary = NeonPurple,
    onSecondary = Color.White,
    tertiary = RadiantEmerald,
    background = DarkBackground,
    surface = DarkSurface,
    outline = DarkBorder
)

@Composable
fun BlockWorldsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Gaming apps shine brightest in an immersive dark cyberpunk / neon theme
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    BlockWorldsTheme(darkTheme = darkTheme, content = content)
}
