package eu.yeger.komi.main

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Dialog
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.material.SliderPosition
import androidx.ui.text.TextStyle
import eu.yeger.komi.common.*
import eu.yeger.komi.game.Game
import eu.yeger.komi.lobby.LobbyBrowserActivity
import eu.yeger.komi.common.startActivity
import eu.yeger.komi.common.startGameWithConfiguration

@Composable
fun MainPage(activity: AppCompatActivity) {
    val state = MainActivityModel()
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
        GameConfigurationDialog(
            activity = activity,
            state = state
        )
    }
}

@Composable
fun GameConfigurationDialog(activity: AppCompatActivity, state: MainActivityModel) {
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
                        Checkbox(
                            text = "Computer Opponent",
                            checkboxState = state.versusComputer
                        )
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
        androidx.ui.material.Slider(
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
        androidx.ui.material.Checkbox(
            checked = checkboxState.value,
            onCheckedChange = { checkboxState.value = it })
    }
}
