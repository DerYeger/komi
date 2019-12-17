package eu.yeger.komi.model

import androidx.compose.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

@Model
class Game(
    val players: Pair<Player, Player>,
    val width: Int,
    val height: Int,
    val scoreLimit: Int
) {
    val cellArray by lazy {
        Array(height) { y ->
            Array(width) { x ->
                Cell(
                    x = x,
                    y = y,
                    state = Cell.State.Empty
                )
            }
        }
    }

    private val cells by lazy { cellArray.flatten() }
    private val neighborMap = HashMap<Cell, List<Cell>>()

    var winner: Player? = null
    val gameOver: Boolean
        get() = winner !== null

    var currentPlayer: Player

    init {
        currentPlayer = players.first
    }

    fun occupy(cell: Cell) {
        if (!currentPlayer.isComputer) {
            CoroutineScope(Dispatchers.Main).launch {
                turn(cell)
            }
        }
    }

    private suspend fun turn(cell: Cell) {
        if (turnIsValid(cell)) {
            cell.state = Cell.State.Occupied(currentPlayer)
            turnForPlayer(currentPlayer)
            if (gameOver) return
            turnForPlayer(currentPlayer.opponent)
            if (gameOver) return
            currentPlayer = currentPlayer.opponent
            if (currentPlayer.isComputer) {
                CoroutineScope(Dispatchers.Main).launch {
                    computerTurn(currentPlayer)
                }
            }
        }
    }

    private suspend fun computerTurn(player: Player) {
        cells
            .shuffled()
            .filter { it.isEmpty() }
            .map { Turn(player = player, cell = it) }
            .max()
            ?.also { turn(it.cell) }
    }

    private fun turnIsValid(cell: Cell): Boolean {
        if (cell.isOccupied() || gameOver) return false
        return true
    }

    private fun turnForPlayer(player: Player) {
        player.opponent
            .cellsWithoutLiberties()
            .forEach {
                player.score++
                it.state = Cell.State.Empty
            }
        if (player.score >= scoreLimit) {
            winner = player
        }
    }

    //
    // Player-Extensions
    //

    private val Player.opponent
        get() = when (this) {
            players.first -> players.second
            players.second -> players.first
            else -> Player.None
        }

    // turn can be used to calculate cellsWithoutLiberties for the state after that turn took place
    private fun Player.cellsWithoutLiberties(turn: Turn? = null): List<Cell> {
        val safeCells = cells
            .filter { it.state.player === this && it.hasEmptyNeighbor(except = turn?.cell) }
            .toMutableSet() // collect occupied cells with empty neighbors, as those are inherently safe

        val uncheckedCells = cells
            .filter { it.state.player === this && !it.hasEmptyNeighbor(except = turn?.cell) }
            .toMutableSet() // collect occupied cells without empty neighbors

        while (true) {
            uncheckedCells
                .filter {
                    it.neighbors
                        .any { neighbor -> neighbor in safeCells && neighbor.liberates(it, turn) }
                }
                .also {
                    if (it.isEmpty()) {
                        turn
                            ?.takeIf {
                                this === turn.player && turn.cell.neighbors.none { neighbor -> neighbor.isEmpty() || neighbor in safeCells }
                            }?.also { turn ->
                                uncheckedCells.add(turn.cell)
                            }
                        return uncheckedCells.toList()
                    } // no more liberties granted, remaining unchecked cells can not have liberties
                }
                .forEach {
                    safeCells.add(it)
                    uncheckedCells.remove(it)
                }
        }
    }

    //
    // Cell-Extensions
    //

    private val Cell.neighbors
        get() = neighborMap.computeIfAbsent(this) {
            cells.filter { it.isNeighborOf(this) }
        }

    private fun Cell.hasEmptyNeighbor(except: Cell? = null) =
        neighbors.any { it.isEmpty() && it !== except }

    private fun Cell.liberates(other: Cell, turn: Turn? = null) =
        if (turn !== null && this === turn.cell) { // if turn is present and it occupies this cell
            neighbors.contains(other) && turn.player === other.state.player
        } else {
            state !== Cell.State.Empty && neighbors.contains(other) && state.player === other.state.player
        }

    //
    // Game.Configuration
    //

    open class Configuration(
        val width: Int,
        val height: Int,
        val scoreLimit: Int,
        versusComputer: Boolean
    ) : Serializable {
        val playerConfigurations: Pair<Player.Configuration, Player.Configuration>

        init {
            val secondPlayerConfiguration =
                if (versusComputer) Player.Configuration.Computer else Player.Configuration.SecondHuman
            playerConfigurations = Player.Configuration.FirstHuman to secondPlayerConfiguration
        }

        object Default : Configuration(
            width = 5,
            height = 5,
            scoreLimit = 5,
            versusComputer = true
        )

        object Local : Configuration(
            width = 5,
            height = 5,
            scoreLimit = 5,
            versusComputer = false
        )
    }

    //
    // Game.Turn
    //

    private inner class Turn(val player: Player, val cell: Cell) : Comparable<Turn> {

        // return value is negative if this turn is worse than other
        // return value is positive if this turn is better than other
        // return value is 0 if both turns are equal or not comparable
        override fun compareTo(other: Turn): Int {
            if (this.player !== other.player) return 0

            return losses(other).smallerOr {
                gains(other).biggerOr {
                    potentialLosses(other).smallerOr {
                        inherentlySafeCells(other).biggerOr {
                            nonEmptyNeighbors(other).biggerOr {
                                neighbors(other).biggerOr {
                                    0
                                }
                            }
                        }
                    }
                }
            }
        }

        val losses by lazy { this.player.cellsWithoutLiberties(this).size }

        private fun losses(other: Turn) = Pair(
            this.losses,
            other.losses
        )

        val gains by lazy { this.player.opponent.cellsWithoutLiberties(this).size }
        private fun gains(other: Turn) = this.gains to other.gains

        val potentialLosses by lazy { this.player.cellsWithoutLiberties(Turn(this.player.opponent, this.cell)).size }
        private fun potentialLosses(other: Turn) = this.potentialLosses to other.potentialLosses

        val inherentlySafeCells by lazy { cells.filter { it.state.player === this.player && it.hasEmptyNeighbor(except = this.cell) }.size }
        private fun inherentlySafeCells(other: Turn) = this.inherentlySafeCells to other.inherentlySafeCells

        private fun neighbors(other: Turn) = this.cell.neighbors.size to other.cell.neighbors.size

        val emptyNeighbors by lazy { this.cell.neighbors.count { it.isOccupied() } }
        private fun nonEmptyNeighbors(other: Turn) = this.emptyNeighbors to other.emptyNeighbors

        private inline fun Pair<Int, Int>.biggerOr(block: () -> Int): Int {
            return if (first == second) {
                block()
            } else {
                first - second
            }
        }

        private inline fun Pair<Int, Int>.smallerOr(block: () -> Int): Int {
            return if (first == second) {
                block()
            } else {
                second - first
            }
        }
    }
}
