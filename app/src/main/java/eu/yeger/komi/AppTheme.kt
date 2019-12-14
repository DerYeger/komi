package eu.yeger.komi

import androidx.compose.Composable
import androidx.ui.graphics.Color
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface

val primaryColor = Color(0xBB86FC)
val primaryVariantColor = Color(0x3700B3)
val secondaryColor = Color(0x03DAC6)
val backgroundColor = Color.DarkGray // Color(0x121212) breaks the theme for some reason
val errorColor = Color(0xCF6679)

val DarkTheme = ColorPalette(
    background = backgroundColor,
    error = errorColor,
    onBackground = Color.White,
    onError = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onSurface = Color.White,
    primary = primaryColor,
    primaryVariant = primaryVariantColor,
    secondary = secondaryColor,
    secondaryVariant = secondaryColor,
    surface = backgroundColor
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
        Surface {
            children()
        }
    }
}