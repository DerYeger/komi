package eu.yeger.komi.game

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
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
import eu.yeger.komi.common.CenteredRow
import eu.yeger.komi.common.ExpandedRow
import eu.yeger.komi.common.KomiCard
import eu.yeger.komi.common.ThemedPage
import eu.yeger.komi.common.startGameWithConfiguration

@Preview("GamePage")
@Composable
fun DefaultPreview() {
    GamePage(
        activity = GameActivity(),
        game = Game.Configuration.Default.generateGame()
    )
}

@Composable
fun GamePage(activity: AppCompatActivity, game: Game) {
    ThemedPage {
        Align(alignment = Alignment.TopCenter) {
            Column(modifier = ExpandedHeight) {
                if (game.scoreLimit in 8..15) {
                    VerticalPlayerCards(game = game)
                } else {
                    HorizontalPlayerCards(
                        game = game,
                        useIcons = game.scoreLimit <= 7
                    )
                }
                Center {
                    Board(game)
                }
            }
        }
        GameOverDialog(activity = activity, game = game)
    }
}

@Composable
fun HorizontalPlayerCards(game: Game, useIcons: Boolean) {
    ExpandedRow(arrangement = Arrangement.SpaceBetween) {
        PlayerCard(
            game = game,
            player = game.players.first,
            useIcons = useIcons
        )
        PlayerCard(
            game = game,
            player = game.players.second,
            useIcons = useIcons
        )
    }
}

@Composable
fun VerticalPlayerCards(game: Game) {
    Column {
        CenteredRow {
            PlayerCard(
                game = game,
                player = game.players.first,
                useIcons = true
            )
        }
        CenteredRow {
            PlayerCard(
                game = game,
                player = game.players.second,
                useIcons = true
            )
        }
    }
}

@Composable
fun PlayerCard(game: Game, player: Player, useIcons: Boolean) {
    KomiCard(
        border = if (game.currentPlayer === player) Border(player.color, 2.dp) else null,
        color = Color.Transparent,
        elevation = 0.dp,
        modifier = Spacing(8.dp)
    ) {
        if (useIcons) {
            PlayerCardIcons(game = game, player = player)
        } else {
            PlayerCardText(player = player)
        }
    }
}

@Composable
fun PlayerCardIcons(game: Game, player: Player) {
    Row {
        for (i in 1..game.scoreLimit) {
            Padding(padding = 2.dp) {
                FloatingActionButton(
                    modifier = Size(20.dp, 20.dp),
                    color = if (player.score >= i) player.color else Color.DarkGray,
                    elevation = 0.dp,
                    children = { }
                )
            }
        }
    }
}

@Composable
fun PlayerCardText(player: Player) {
    Text(
        modifier = Spacing(4.dp),
        text = "Score: ${player.score}",
        style = TextStyle(color = player.color)
    )
}

@Composable
fun GameOverDialog(activity: AppCompatActivity, game: Game) {
    if (game.gameOver) {
        AlertDialog(
            onCloseRequest = { },
            title = { Text(text = "Game Over") },
            text = { Text(text = "${game.winner?.name} has won!") },
            confirmButton = {
                Padding(bottom = 4.dp, right = 4.dp) {
                    Button(
                        text = "Restart",
                        onClick = {
                            activity.startGameWithConfiguration(game.generateConfiguration())
                            activity.finish()
                        },
                        modifier = Width(100.dp)
                    )
                }
            },
            dismissButton = {
                Padding(bottom = 4.dp) {
                    Button(
                        text = "Quit",
                        onClick = {
                            activity.finish()
                        },
                        modifier = Width(100.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun Board(game: Game) {
    CenteredRow(modifier = Spacing(8.dp)) {
        HorizontalScroller {
            VerticalScroller {
                KomiCard {
                    Column(modifier = ExpandedHeight) {
                        for (row in game.cellArray) {
                            ExpandedRow {
                                for (cell in row) {
                                    CellView(
                                        game = game,
                                        cell = cell
                                    )
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
