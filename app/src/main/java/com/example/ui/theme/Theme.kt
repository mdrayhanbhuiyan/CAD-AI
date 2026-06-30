package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SophisticatedColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedTextPrimary,
    secondary = SophisticatedSecondary,
    onSecondary = SophisticatedTextPrimary,
    tertiary = SophisticatedAccent,
    background = SophisticatedBackground,
    onBackground = SophisticatedTextSecondary,
    surface = SophisticatedSurface,
    onSurface = SophisticatedTextSecondary,
    surfaceVariant = SophisticatedSurfaceAlt,
    onSurfaceVariant = SophisticatedTextMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Force Sophisticated Dark for a uniform high-fidelity premium experience
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SophisticatedColorScheme,
        typography = Typography,
        content = content
    )
}
