package org.awaremate.shared.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Platform-specific dynamic color scheme resolver (Android 12+ / API 31+).
 */
@Composable
expect fun rememberDynamicColorScheme(darkTheme: Boolean): ColorScheme?

/**
 * Root theme for AwareMate with dynamic color support, dark mode options,
 * and compassionate, nature-inspired palette.
 */
@Composable
fun AwareMateTheme(
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val dynamicScheme = if (dynamicColor) rememberDynamicColorScheme(isDark) else null
    val colorScheme = dynamicScheme ?: if (isDark) AwareMateDarkColorScheme else AwareMateLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AwareMateTypography,
        shapes = AwareMateShapes,
        content = content
    )
}
