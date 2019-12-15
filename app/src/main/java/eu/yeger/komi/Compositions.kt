package eu.yeger.komi

import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Modifier
import androidx.ui.core.dp
import androidx.ui.foundation.shape.border.Border
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Card

@Composable
fun CenteredRow(children: @Composable() () -> Unit) {
    Row(modifier = ExpandedWidth, arrangement = Arrangement.Center) {
        children()
    }
}

@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier.None,
    border: Border? = null,
    color: Color = (+MaterialTheme.colors()).surface,
    children: @Composable() () -> Unit
) {
    Card(
        modifier = Spacing(8.dp).wraps(modifier),
        border = border,
        color = color,
        shape = RoundedCornerShape(4.dp),
        elevation = 3.dp
    ) {
        Padding(padding = 8.dp) {
            children()
        }
    }
}
