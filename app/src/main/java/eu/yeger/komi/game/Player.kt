package eu.yeger.komi.game

import androidx.compose.Model
import androidx.ui.graphics.Color
import java.io.Serializable

@Model
class Player(
    val name: String,
    val color: Color,
    val isComputer: Boolean = false,
    var score: Int = 0
) {
    companion object {
        val None = Player(
            name = "None",
            color = Color(0xFF121212),
            isComputer = false
        )
    }

    //
    // Player.Configuration
    //

    open class Configuration(
        val name: String,
        val isComputer: Boolean
    ) : Serializable {
        object FirstHuman : Configuration("First player", false)
        object SecondHuman : Configuration("Second player", false)
        object Computer : Configuration("Computer", true)
    }
}
