package eu.yeger.komi

import androidx.compose.Composable
import androidx.ui.graphics.Color
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface

val green = Color(0xFF1EB980)
private val themeColors = ColorPalette(
    primary = green,
    surface = Color.DarkGray,
    onSurface = green
)

@Composable
fun AppTheme(children: @Composable() () -> Unit) {
    MaterialTheme(colors = themeColors) {
        children()
    }
}

@Composable
fun ThemedPage(children: @Composable() () -> Unit) {
    AppTheme {
        Surface {
            children()
        }
    }
}