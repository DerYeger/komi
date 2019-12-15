package eu.yeger.komi.model

import androidx.compose.Model
import androidx.ui.graphics.Color
import eu.yeger.komi.primaryColor
import eu.yeger.komi.secondaryColor

@Model
class Player(
    val name: String,
    val color: Color,
    var score: Int = 0
) {
    companion object {
        val None = Player(
            name = "None",
            color = Color(0xFF121212),
            score = 0
        )

        fun firstPlayer(name: String = "First player") =
            Player(
                name = name,
                color = primaryColor,
                score = 0
            )

        fun secondPlayer(name: String = "Second player") =
            Player(
                name = name,
                color = secondaryColor,
                score = 0
            )
    }
}
