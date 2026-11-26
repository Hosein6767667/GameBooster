package com.gamebooster.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameBoosterColors = darkColorScheme(
    primary = Color(0xFF00FF88),
    onPrimary = Color(0xFF00140A),
    secondary = Color(0xFF00C878),
    onSecondary = Color(0xFF00140A),
    background = Color(0xFF030805),
    onBackground = Color(0xFFE8FFF3),
    surface = Color(0xFF07110B),
    onSurface = Color(0xFFE8FFF3),
    surfaceVariant = Color(0xFF102019),
    onSurfaceVariant = Color(0xFF9BB8A8),
    outline = Color(0xFF315443),
    error = Color(0xFFFF5252)
)

@Composable
fun GameBoosterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameBoosterColors,
        content = content
    )
}