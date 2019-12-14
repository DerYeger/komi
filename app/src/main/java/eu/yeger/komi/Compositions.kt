package eu.yeger.komi

import androidx.compose.Composable
import androidx.ui.core.dp
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Padding
import androidx.ui.layout.Spacing
import androidx.ui.material.surface.Card

@Composable
fun ElevatedCard(children: @Composable() () -> Unit) {
    Card(shape = RoundedCornerShape(4.dp), elevation = 8.dp, modifier = Spacing(8.dp)) {
        Padding(padding = 8.dp) {
            children()
        }
    }
}
