package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.TextField
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.Dialog
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
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
                        onClick = { activity.startActivity(GameActivity::class) },
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
                ElevatedCard {
                    Column {
                        Input(state.gameWidth)
                        HeightSpacer(height = 8.dp)
                        Input(state.gameHeight)
                        HeightSpacer(height = 8.dp)
                        Input(state.gameScoreLimit)
                        HeightSpacer(height = 8.dp)
                        ExpandedRow(arrangement = Arrangement.End) {
                            Button(
                                text = "Play",
                                onClick = {
                                    activity.startGameWithConfiguration(state.generateGameConfiguration())
                                    state.dialogVisible.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Input(inputState: InputState) {
    ExpandedRow(arrangement = Arrangement.SpaceBetween) {
        Text(text = inputState.name)
        WidthSpacer(width = 8.dp)
        TextField(
            value = inputState.value.toString(),
            onValueChange = { inputState.value = if (it.isBlank()) -1 else it.toInt() }
        )
    }
}

class MainPageState {
    val dialogVisible = +state { false }
    val gameWidth = InputState("Width", 5, 4)
    val gameHeight = InputState("Height", 5, 4)
    val gameScoreLimit = InputState("Score limit", 9, 1)

    fun generateGameConfiguration() =
        Game.Configuration(
            width = gameWidth.value,
            height = gameHeight.value,
            scoreLimit = gameScoreLimit.value
        )
}

@Model
class InputState(
    val name: String,
    value: Int,
    private val minValue: Int
) {
    var value = value
        set(value) {
            field = value.coerceAtLeast(minValue)
        }
}
