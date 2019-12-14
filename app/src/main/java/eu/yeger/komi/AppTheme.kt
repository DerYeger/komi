package eu.yeger.komi

import androidx.compose.Composable
import androidx.ui.core.dp
import androidx.ui.graphics.Color
import androidx.ui.layout.Padding
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface

val primaryColor = Color(0xFFBB86FC)
val primaryVariantColor = Color(0xFF3700B3)
val secondaryColor = Color(0xFF03DAC6)
val backgroundColor = Color(0xFF121212)
val errorColor = Color(0xFFCF6679)

val DarkTheme = ColorPalette(
    background = backgroundColor,
    error = errorColor,
    onBackground = secondaryColor,
    onError = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onSurface = secondaryColor,
    primary = primaryColor,
    primaryVariant = primaryVariantColor,
    secondary = secondaryColor,
    secondaryVariant = secondaryColor,
    surface = Color.DarkGray
)

@Composable
fun AppTheme(children: @Composable() () -> Unit) {
    MaterialTheme(colors = DarkTheme) {
        children()
    }
}

@Composable
fun ThemedPage(children: @Composable() () -> Unit) {
    AppTheme {
        Surface(color = backgroundColor) {
            Padding(padding = 8.dp) {
                children()
            }
        }
    }
}
