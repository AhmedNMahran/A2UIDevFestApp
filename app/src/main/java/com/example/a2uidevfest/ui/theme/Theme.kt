package com.example.a2uidevfest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DevFestBlue = Color(0xFF1A73E8)
private val DevFestIndigo = Color(0xFF3949AB)
private val DevFestTeal = Color(0xFF00897B)
private val DevFestAmber = Color(0xFFF9AB00)

private val LightColors = lightColorScheme(
    primary = DevFestBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = DevFestTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00251A),
    tertiary = DevFestIndigo,
    tertiaryContainer = Color(0xFFC5CAE9),
    background = Color(0xFFF8FAFD),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEF2F8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF00332C),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFF9FA8DA),
    background = Color(0xFF0E131B),
    surface = Color(0xFF171D26),
    surfaceVariant = Color(0xFF222A35),
)

@Composable
fun A2UIDevFestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
