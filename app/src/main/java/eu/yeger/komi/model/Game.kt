package eu.yeger.komi.model

import androidx.compose.Model
import java.io.Serializable

@Model
class Game(
    val players: Pair<Player, Player> = Pair(
        Player.firstPlayer(),
        Player.secondPlayer()
    ),
    val width: Int,
    val height: Int,
    val scoreLimit: Int
) {
    val cellArray = Array(height) { y ->
        Array(width) { x ->
            Cell(
                x = x,
                y = y,
                state = CellState.Empty
            )
        }
    }

    private val cells = cellArray.flatten()
    private val neighborMap = HashMap<Cell, List<Cell>>()

    var gameOver = false
    var winner: Player? = null

    var currentPlayer: Player

    init {
        // workaround for compiler backend exception
        currentPlayer = players.first
    }

    fun turn(cell: Cell) {
        if (turnIsValid(cell)) {
            cell.state = CellState.Occupied(currentPlayer)
            turnForPlayer(currentPlayer)
            turnForPlayer(currentPlayer.opponent())
            currentPlayer = currentPlayer.opponent()
        }
    }

    private fun turnIsValid(cell: Cell): Boolean {
        if (cell.isOccupied() || gameOver) return false
        return true
    }

    private fun turnForPlayer(player: Player) {
        player
            .opponent()
            .cellsWithoutLiberties()
            .forEach {
                player.score++
                it.state = CellState.Empty
            }
        if (player.score >= scoreLimit) {
            gameOver = true
            winner = player
        }
    }

    private fun Player.opponent() = when (this) {
        players.first -> players.second
        players.second -> players.first
        else -> Player.None
    }

    private fun Player.cellsWithoutLiberties(): List<Cell> {
        val safeCells = cells
            .filter { it.state.player === this && it.hasEmptyNeighbor() }
            .toMutableSet() // collect occupied cells with empty neighbors, as those are inherently safe

        val uncheckedCells = cells
            .filter { it.state.player === this && !it.hasEmptyNeighbor() }
            .toMutableSet() // collect occupied cells without empty neighbors

        while (true) {
            uncheckedCells
                .filter {
                    it.neighbors()
                        .any { neighbor -> neighbor in safeCells && neighbor.liberates(it) }
                }
                .also {
                    if (it.isEmpty()) return uncheckedCells.toList() // no more liberties granted, remaining unchecked cells can not have liberties
                }
                .forEach {
                    safeCells.add(it)
                    uncheckedCells.remove(it)
                }
        }
    }

    private fun Cell.neighbors() = neighborMap.computeIfAbsent(this) {
        cells.filter { it.isNeighborOf(this) }
    }

    private fun Cell.hasEmptyNeighbor() = neighbors().any { it.isEmpty() }

    private fun Cell.liberates(other: Cell) = when (state) {
        CellState.Empty -> false
        else -> neighbors().contains(other) && state.player === other.state.player
    }

    open class Configuration(
        val width: Int,
        val height: Int,
        val scoreLimit: Int
    ) : Serializable {
        object Default : Configuration(5, 5, 5)
    }
}

fun Game.generateConfiguration() =
    Game.Configuration(
        width = width,
        height = height,
        scoreLimit = scoreLimit
    )

fun Game.Configuration.generateGame() =
    Game(
        width = width,
        height = height,
        scoreLimit = scoreLimit
    )

