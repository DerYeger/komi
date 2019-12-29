package eu.yeger.komi.game

import eu.yeger.komi.common.primaryColor
import eu.yeger.komi.common.secondaryColor

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
