package eu.yeger.komi.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import eu.yeger.komi.common.getKomiExtra

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configuration: Game.Configuration =
            intent.getKomiExtra("game_configuration") ?: Game.Configuration.Default
        setContent {
            GamePage(
                activity = this,
                game = configuration.generateGame()
            )
        }
    }
}
