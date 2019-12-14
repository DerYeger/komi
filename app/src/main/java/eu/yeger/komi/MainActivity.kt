package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.shape.border.Border
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.ButtonStyle
import androidx.ui.material.Divider
import androidx.ui.material.surface.Surface
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainPage {
                Board()
            }
        }
    }
}

@Composable
fun MainPage(children: @Composable() () -> Unit) {
    AppTheme {
        Surface {
            children()
        }
    }
}

@Preview("MyScreen preview")
@Composable
fun DefaultPreview() {
    MainPage {
        Board()
    }
}

@Composable
fun CellView(game: Game, cell: Cell) {
    Container(modifier = Size(50.dp, 50.dp)) {
        Button(
            text = "",
            onClick = { game.turn(cell) },
            style = ButtonStyle(
                color = cell.state.color,
                shape = CircleShape,
                border = Border(brush = SolidColor(Color.Black), width = 1.dp)
            )
        )
    }
}

@Composable
fun Board(game: Game = Game()) {
    Column(modifier = ExpandedHeight) {
        PlayerCard(game.players.first)
        Divider(height = 5.dp)
        PlayerCard(game.players.second)
        Divider(height = 5.dp)
        Column {
            for (row in game.cellArray) {
                Row(modifier = ExpandedWidth) {
                    for (cell in row) {
                        CellView(game = game, cell = cell)
                    }
                }
            }
        }
        Divider(height = 5.dp)
        CurrentPlayerCard(game = game)
    }
}

@Composable
fun PlayerCard(player: Player) {
    Row {
        Text(text = player.name)
        Text(text = " Score: ${player.score}")
    }
}

@Composable
fun CurrentPlayerCard(game: Game) {
    Row {
        Text(text = "Current Player: ")
        Text(text = game.currentPlayer.name, style = TextStyle(color = game.currentPlayer.color))
    }
}
