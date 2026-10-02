package ca.tommysanterre.snackloop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF376A20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F397),
    onPrimaryContainer = Color(0xFF082100),
    secondary = Color(0xFF55624C),
    background = Color(0xFFFDFDF5),
    surface = Color(0xFFFDFDF5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD67E),
    onPrimary = Color(0xFF123800),
    primaryContainer = Color(0xFF285111),
    onPrimaryContainer = Color(0xFFB8F397),
    secondary = Color(0xFFBDCBB1),
    background = Color(0xFF1A1C18),
    surface = Color(0xFF1A1C18)
)

@Composable
fun HyperbolicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
