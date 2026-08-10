package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekBlackColorScheme = darkColorScheme(
    primary = CruxIndigo,
    onPrimary = ObsidianBackground,
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = CruxViolet,
    onSecondary = ObsidianBackground,
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = CruxCyan,
    onTertiary = ObsidianBackground,
    tertiaryContainer = Color(0xFF083344),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = ObsidianBackground,
    onBackground = DarkTextPrimary,
    surface = ObsidianSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = CruxIndigo,
    outline = ObsidianBorder,
    outlineVariant = ObsidianSurfaceElevated,
    error = ErrorRed,
    onError = ObsidianBackground
)

private val SleekLightColorScheme = lightColorScheme(
    primary = CruxIndigoDark,
    onPrimary = LightSurface,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = CruxVioletDark,
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFFAF5FF),
    onSecondaryContainer = Color(0xFF581C87),
    tertiary = CruxCyanDark,
    onTertiary = LightSurface,
    tertiaryContainer = Color(0xFFECFEFF),
    onTertiaryContainer = Color(0xFF164E63),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = CruxIndigoDark,
    outline = LightBorder,
    outlineVariant = LightSurfaceElevated,
    error = ErrorRed,
    onError = LightSurface
)

@Composable
fun CruxTutorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature Crux branding by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SleekBlackColorScheme else SleekLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
