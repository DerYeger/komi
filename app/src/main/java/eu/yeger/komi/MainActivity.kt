package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.Dialog
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.text.TextStyle
import eu.yeger.komi.model.Game

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainPage(this)
        }
    }
}

@Composable
fun MainPage(activity: AppCompatActivity) {
    val state = MainPageState()
    ThemedPage {
        Column(modifier = ExpandedHeight) {
            CenteredRow {
                Text(
                    text = "Komi",
                    style = (+MaterialTheme.typography()).h1.merge(TextStyle(color = secondaryColor))
                )
            }
            HeightSpacer(height = 16.dp)
            Column {
                CenteredRow {
                    Button(
                        text = "Play",
                        onClick = { activity.startGameWithConfiguration(Game.Configuration.Default) },
                        modifier = Width(100.dp)
                    )
                }
                HeightSpacer(height = 16.dp)
                CenteredRow {
                    Button(
                        text = "Local",
                        onClick = { activity.startGameWithConfiguration(Game.Configuration.Local) },
                        modifier = Width(100.dp)
                    )
                }
                HeightSpacer(height = 16.dp)
                CenteredRow {
                    Button(
                        text = "Online",
                        onClick = { activity.startActivity(LobbyBrowserActivity::class) },
                        modifier = Width(100.dp)
                    )
                }
                HeightSpacer(height = 16.dp)
                CenteredRow {
                    Button(
                        text = "Custom",
                        onClick = { state.dialogVisible.value = true },
                        modifier = Width(100.dp)
                    )
                }
            }
        }
        GameConfigurationDialog(activity = activity, state = state)
    }
}

@Composable
fun GameConfigurationDialog(activity: AppCompatActivity, state: MainPageState) {
    if (state.dialogVisible.value) {
        Dialog(onCloseRequest = { state.dialogVisible.value = false }) {
            AppTheme {
                KomiCard(modifier = Width(400.dp)) {
                    Column(modifier = Spacing(8.dp)) {
                        Slider(
                            text = "Width",
                            sliderState = state.gameWidth,
                            range = Game.Configuration.WIDTH_RANGE,
                            sideEffect = { state.coerceGameScoreLimit() }
                        )
                        HeightSpacer(height = 8.dp)
                        Slider(
                            text = "Height",
                            sliderState = state.gameHeight,
                            range = Game.Configuration.HEIGHT_RANGE,
                            sideEffect = { state.coerceGameScoreLimit() }
                        )
                        HeightSpacer(height = 8.dp)
                        Slider(
                            text = "Score Limit",
                            sliderState = state.gameScoreLimit,
                            range = 1F..Game.Configuration.maxScoreLimit(
                                state.gameWidth.value,
                                state.gameHeight.value
                            ).toFloat()
                        )
                        HeightSpacer(height = 8.dp)
                        Checkbox(text = "Computer Opponent", checkboxState = state.versusComputer)
                        HeightSpacer(height = 24.dp)
                        ExpandedRow(arrangement = Arrangement.End) {
                            Button(
                                text = "Play",
                                onClick = {
                                    state.storeToPreferences()
                                    activity.startGameWithConfiguration(state.generateGameConfiguration())
                                    state.dialogVisible.value = false
                                },
                                modifier = Width(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Slider(
    text: String,
    sliderState: State<Float>,
    range: ClosedFloatingPointRange<Float>,
    sideEffect: () -> Unit = {}
) {
    Column {
        Text(text = "$text: ${sliderState.value.toInt()}")
        Slider(
            modifier = ExpandedWidth,
            position = SliderPosition(
                initial = sliderState.value,
                valueRange = range
            ),
            onValueChange = {
                sliderState.value = it
                sideEffect()
            }
        )
    }
}

@Composable
fun Checkbox(text: String, checkboxState: State<Boolean>) {
    ExpandedRow(arrangement = Arrangement.SpaceBetween) {
        Text(text = text)
        WidthSpacer(width = 8.dp)
        Checkbox(
            checked = checkboxState.value,
            onCheckedChange = { checkboxState.value = it })
    }
}

class MainPageState {
    val dialogVisible = +state { false }

    val gameWidth = +state {
        Preferences.retrieveInt("game_width", Game.Configuration.DEFAULT_WIDTH).toFloat()
    }

    val gameHeight = +state {
        Preferences.retrieveInt("game_height", Game.Configuration.DEFAULT_HEIGHT).toFloat()
    }

    val gameScoreLimit = +state {
        Preferences.retrieveInt("game_score_limit", Game.Configuration.DEFAULT_SCORE_LIMIT)
            .toFloat()
    }

    val versusComputer = +state {
        Preferences.retrieveBoolean("computer_opponent", true)
    }

    fun coerceGameScoreLimit() {
        gameScoreLimit.value = gameScoreLimit.value.coerceAtMost(
            Game.Configuration.maxScoreLimit(
                gameWidth.value,
                gameHeight.value
            ).toFloat()
        )
    }

    fun storeToPreferences() {
        Preferences.apply {
            storeInt("game_width", gameWidth.value.toInt())
            storeInt("game_height", gameHeight.value.toInt())
            storeInt("game_score_limit", gameScoreLimit.value.toInt())
            storeBoolean("computer_opponent", versusComputer.value)
        }
    }

    fun generateGameConfiguration() =
        Game.Configuration(
            width = gameWidth.value.toInt(),
            height = gameHeight.value.toInt(),
            scoreLimit = gameScoreLimit.value.toInt(),
            versusComputer = versusComputer.value
        )
}
