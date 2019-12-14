package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Alignment
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

@Preview("Game Screen")
@Composable
fun DefaultPreview() {
    GamePage()
}

@Composable
fun GamePage(game: Game = Game()) {
    ThemedPage {
        Center {
            Column(modifier = ExpandedHeight) {
                PlayerCards(game)
                HeightSpacer(height = 8.dp)
                Board(game)
                HeightSpacer(height = 8.dp)
                CurrentPlayerCard(game = game)
                HeightSpacer(height = 8.dp)
                WinnerCard(game = game)
            }
        }
    }
}


@Composable
fun PlayerCards(game: Game) {
    Row {
        PlayerCard(game.players.first)
        WidthSpacer(width = 8.dp)
        PlayerCard(game.players.second)
    }
}

@Composable
fun PlayerCard(player: Player) {
    ElevatedCard {
        Text(
            text = "${player.name} Score: ${player.score}",
            style = TextStyle(color = player.color)
        )
    }
}

@Composable
fun CurrentPlayerCard(game: Game) {
    ElevatedCard {
        Row {
            Text(text = "Current Player: ")
            Text(
                text = game.currentPlayer.name,
                style = TextStyle(color = game.currentPlayer.color)
            )
        }
    }
}

@Composable
fun WinnerCard(game: Game) {
    ElevatedCard {
        Row {
            Text(text = "Winner: ")
            Text(
                text = game.winner?.name ?: "",
                style = TextStyle(color = game.winner?.color ?: Color.Transparent)
            )
        }
    }
}

@Composable
fun Board(game: Game) {
    ElevatedCard {
        Table(columns = game.width, alignment = { Alignment.Center }) {
            for (row in game.cellArray) {
                tableRow {
                    for (cell in row) {
                        CellView(game = game, cell = cell)
                    }
                }
            }
        }
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
