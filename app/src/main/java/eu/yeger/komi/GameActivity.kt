package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.layout.*
import androidx.ui.material.AlertDialog
import androidx.ui.material.Button
import androidx.ui.material.FloatingActionButton
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamePage(this)
        }
    }
}

@Preview("Game Screen")
@Composable
fun DefaultPreview() {
    GamePage(GameActivity())
}

@Composable
fun GamePage(activity: AppCompatActivity, game: Game = Game()) {
    ThemedPage {
        Center {
            Column(modifier = ExpandedHeight) {
                PlayerCards(game)
                HeightSpacer(height = 8.dp)
                Board(game)
                HeightSpacer(height = 8.dp)
                CurrentPlayerCard(game = game)
                HeightSpacer(height = 8.dp)
                if (game.gameOver) {
                    GameOverDialog(activity = activity, game = game)
                }
            }
        }
    }
}

@Composable
fun PlayerCards(game: Game) {
    Row(
        modifier = ExpandedWidth,
        arrangement = Arrangement.SpaceBetween
    ) {
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
fun GameOverDialog(activity: AppCompatActivity, game: Game) {
    AlertDialog(
        onCloseRequest = { },
        title = { Text(text = "Game Over") },
        text = { Text(text = "${game.winner!!.name} has won!") },
        confirmButton = {
            Button(
                text = "Retry",
                onClick = {
                    activity.startActivity(GameActivity::class)
                    activity.finish()
                }
            )
        },
        dismissButton = {
            Button(
                text = "Quit",
                onClick = {
                    activity.finish()
                }
            )
        }
    )
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
        FloatingActionButton(
            text = "",
            onClick = { game.turn(cell) },
            color = cell.state.color,
            elevation = 1.dp
        )
    }
}
