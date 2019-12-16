package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.HorizontalScroller
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.border.Border
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.AlertDialog
import androidx.ui.material.Button
import androidx.ui.material.FloatingActionButton
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview
import eu.yeger.komi.model.Cell
import eu.yeger.komi.model.Game
import eu.yeger.komi.model.Player

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configuration: Game.Configuration =
            intent.getKomiExtra("game_configuration") ?: Game.Configuration.Default
        setContent {
            GamePage(activity = this, game = configuration.generateGame())
        }
    }
}

@Preview("GamePage")
@Composable
fun DefaultPreview() {
    GamePage(activity = GameActivity(), game = Game.Configuration.Default.generateGame())
}

@Composable
fun GamePage(activity: AppCompatActivity, game: Game) {
    ThemedPage {
        Align(alignment = Alignment.TopCenter) {
            Column(modifier = ExpandedHeight) {
                PlayerCards(game)
                Center {
                    Board(game)
                }
            }
        }
        GameOverDialog(activity = activity, game = game)
    }
}

@Composable
fun PlayerCards(game: Game) {
    ExpandedRow(arrangement = Arrangement.SpaceBetween) {
        PlayerCard(game = game, player = game.players.first)
        PlayerCard(game = game, player = game.players.second)
    }
}

@Composable
fun PlayerCard(game: Game, player: Player) {
    ElevatedCard(
        border = if (game.currentPlayer === player) Border(player.color, 2.dp) else null,
        color = Color.Transparent,
        elevation = 0.dp
    ) {
        Text(
            text = "Score: ${player.score}",
            style = TextStyle(color = player.color)
        )
    }
}

@Composable
fun GameOverDialog(activity: AppCompatActivity, game: Game) {
    if (game.gameOver) {
        AlertDialog(
            onCloseRequest = { },
            title = { Text(text = "Game Over") },
            text = { Text(text = "${game.winner?.name} has won!") },
            confirmButton = {
                Button(
                    text = "Restart",
                    onClick = {
                        activity.startGameWithConfiguration(game.generateConfiguration())
                        activity.finish()
                    },
                    modifier = Width(100.dp)
                )
            },
            dismissButton = {
                Button(
                    text = "Quit",
                    onClick = {
                        activity.finish()
                    },
                    modifier = Width(100.dp)
                )
            }
        )
    }
}

@Composable
fun Board(game: Game) {
    CenteredRow {
        ElevatedCard {
            HorizontalScroller {
                VerticalScroller {
                    Column(modifier = ExpandedHeight) {
                        for (row in game.cellArray) {
                            ExpandedRow {
                                for (cell in row) {
                                    CellView(game = game, cell = cell)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CellView(game: Game, cell: Cell) {
    Container(modifier = Spacing(4.dp)) {
        FloatingActionButton(
            text = "",
            onClick = { game.occupy(cell) },
            color = cell.state.color,
            elevation = 0.dp
        )
    }
}
