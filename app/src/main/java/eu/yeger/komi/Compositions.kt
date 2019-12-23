package eu.yeger.komi

import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Dp
import androidx.ui.core.Modifier
import androidx.ui.core.dp
import androidx.ui.foundation.shape.border.Border
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.Arrangement
import androidx.ui.layout.ExpandedWidth
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Card

@Composable
fun ExpandedRow(
    arrangement: Arrangement = Arrangement.Begin,
    modifier: Modifier = Modifier.None,
    children: @Composable() () -> Unit
) {
    Row(modifier = ExpandedWidth.wraps(modifier), arrangement = arrangement) {
        children()
    }
}

@Composable
fun CenteredRow(
    modifier: Modifier = Modifier.None,
    children: @Composable() () -> Unit
) {
    ExpandedRow(modifier = modifier, arrangement = Arrangement.Center) {
        children()
    }
}

@Composable
fun KomiCard(
    modifier: Modifier = Modifier.None,
    border: Border? = null,
    color: Color = (+MaterialTheme.colors()).surface,
    elevation: Dp = 3.dp,
    children: @Composable() () -> Unit
) {
    Card(
        modifier = modifier,
        border = border,
        color = color,
        shape = RoundedCornerShape(4.dp),
        elevation = elevation
    ) {
        Padding(padding = 4.dp) {
            children()
        }
    }
}
