package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.shape.border.Border
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.ButtonStyle
import androidx.ui.material.surface.Card
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamePage()
        }
    }
}

@Composable
fun GamePage() {
    ThemedPage {
        Board()
    }
}

@Preview("Game Screen")
@Composable
fun DefaultPreview() {
    GamePage()
}

@Composable
fun Board(game: Game = Game()) {
    Column(modifier = ExpandedHeight) {
        Row {
            PlayerCard(game.players.first)
            WidthSpacer(width = 8.dp)
            PlayerCard(game.players.second)
        }
        HeightSpacer(height = 8.dp)
        Column {
            for (row in game.cellArray) {
                Row(modifier = ExpandedWidth) {
                    for (cell in row) {
                        CellView(game = game, cell = cell)
                    }
                }
            }
        }
        HeightSpacer(height = 8.dp)
        CurrentPlayerCard(game = game)
    }
}

@Composable
fun PlayerCard(player: Player) {
    val style = TextStyle(color = player.color)
    Card(shape = RoundedCornerShape(4.dp), elevation = 8.dp, modifier = Spacing(4.dp)) {
        Padding(padding = 4.dp) {
            Text(text = "${player.name} Score: ${player.score}", style = style)
        }
    }
}

@Composable
fun CurrentPlayerCard(game: Game) {
    Row {
        Text(text = "Current Player: ")
        Text(text = game.currentPlayer.name, style = TextStyle(color = game.currentPlayer.color))
    }
}

@Composable
fun CellView(game: Game, cell: Cell) {
    Card {
        Container(modifier = Size(50.dp, 50.dp), expanded = true) {
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
}
