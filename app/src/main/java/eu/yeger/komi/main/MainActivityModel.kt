package eu.yeger.komi.main

import androidx.compose.state
import androidx.compose.unaryPlus
import eu.yeger.komi.common.Preferences
import eu.yeger.komi.game.Game

class MainActivityModel {
    val dialogVisible = +state { false }

    val gameWidth = +state {
        Preferences.retrieveInt(
            "game_width",
            Game.Configuration.DEFAULT_WIDTH
        ).toFloat()
    }

    val gameHeight = +state {
        Preferences.retrieveInt(
            "game_height",
            Game.Configuration.DEFAULT_HEIGHT
        ).toFloat()
    }

    val gameScoreLimit = +state {
        Preferences.retrieveInt(
            "game_score_limit",
            Game.Configuration.DEFAULT_SCORE_LIMIT
        )
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
            storeInt(
                "game_width",
                gameWidth.value.toInt()
            )
            storeInt(
                "game_height",
                gameHeight.value.toInt()
            )
            storeInt(
                "game_score_limit",
                gameScoreLimit.value.toInt()
            )
            storeBoolean(
                "computer_opponent",
                versusComputer.value
            )
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
