package org.softwareanvil.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF6C63FF)
val PurpleDark = Color(0xFF5A52D5)
val Pink = Color(0xFFFF6584)
val Green = Color(0xFF4CAF50)
val Red = Color(0xFFF44336)
val Orange = Color(0xFFFF9800)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    secondary = Pink,
    onSecondary = Color.White,
    tertiary = Green,
    error = Red,
    background = Color(0xFFFAFAFA),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    secondary = Pink,
    onSecondary = Color.White,
    tertiary = Green,
    error = Red
)

@Composable
fun MusaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}