package eu.yeger.komi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import eu.yeger.komi.model.Game
import eu.yeger.komi.model.Player
import java.io.Serializable
import kotlin.reflect.KClass

fun AppCompatActivity.startActivity(newActivityKClass: KClass<out AppCompatActivity>) =
    this.startActivity(Intent(this, newActivityKClass.java))

internal fun AppCompatActivity.startGameWithConfiguration(configuration: Game.Configuration) {
    val intent = Intent(this, GameActivity::class.java)
    intent.putKomiExtra("game_configuration", configuration)
    this.startActivity(intent)
}

internal fun <T : Serializable> Intent.putKomiExtra(key: String, value: T) {
    putExtra("eu.yeger.komi.$key", value)
}

internal inline fun <reified T : Serializable> Intent.getKomiExtra(key: String): T? =
    extras?.get("eu.yeger.komi.$key") as T?

//
// Configuration generators
//

fun Pair<Player.Configuration, Player.Configuration>.generatePlayers() =
    Pair(
        Player(first.name, primaryColor, first.isComputer),
        Player(second.name, secondaryColor, second.isComputer)
    )

fun Game.generateConfiguration() =
    Game.Configuration(
        width = width,
        height = height,
        scoreLimit = scoreLimit,
        versusComputer = players.second.isComputer
    )

fun Game.Configuration.generateGame() =
    Game(
        players = playerConfigurations.generatePlayers(),
        width = width,
        height = height,
        scoreLimit = scoreLimit
    )
