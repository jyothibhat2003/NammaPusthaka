package com.example.nammapustaka.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LibraryGreenDark,
    onPrimary = Color(0xFF073A2D),
    secondary = Color(0xFFD9BEE7),
    tertiary = Color(0xFFFFC5A9),
    background = Night,
    onBackground = Color(0xFFE8EEE8),
    surface = NightPanel,
    onSurface = Color(0xFFE8EEE8),
    surfaceVariant = Color(0xFF2A332F),
    onSurfaceVariant = Color(0xFFC5CEC7),
    outline = Color(0xFF66736C),
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = LibraryGreen,
    onPrimary = Color.White,
    secondary = Plum,
    onSecondary = Color.White,
    tertiary = Copper,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Panel,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF4D5651),
    outline = Line,
    error = Color(0xFFB3261E)
)

@Composable
fun NammaPustakaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
