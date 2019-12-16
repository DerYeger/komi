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
            CoroutineScope(Dispatchers.Unconfined).launch {
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
                CoroutineScope(Dispatchers.Unconfined).launch {
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

        // hierarchy of decision
        // unequal should return it, if larger is better or -it, if smaller is better
        // equal should return the next lower decision result of the hierarchy
        override fun compareTo(other: Turn): Int {
            gains(other).unequal { return it }.equal {
                potentialLosses(other).unequal { return -it }.equal {
                    suicides(other).unequal { return -it }.equal {
                        nonOpposedNeighbors(other).unequal { return it }
                    }
                }
            }
            return 0
        }

        private fun gains(other: Turn) = Pair(
            this.player.opponent.cellsWithoutLiberties(this).size,
            other.player.opponent.cellsWithoutLiberties(other).size
        )

        private fun suicides(other: Turn) = Pair(
            this.player.cellsWithoutLiberties(this).size,
            other.player.cellsWithoutLiberties(other).size
        )

        private fun potentialLosses(other: Turn) = Pair(
            this.player.cellsWithoutLiberties(
                Turn(this.player.opponent, this.cell)
            ).size,
            this.player.cellsWithoutLiberties(
                Turn(other.player.opponent, other.cell)
            ).size
        )

        private fun nonOpposedNeighbors(other: Turn) = Pair(
            this.cell.neighbors.count { it.isEmpty() || it.state.player === this.player },
            other.cell.neighbors.count { it.isEmpty() || it.state.player === other.player }
        )

        private inline fun Pair<Int, Int>.equal(block: () -> Unit): Pair<Int, Int> {
            if (first == second) {
                block()
            }
            return this
        }

        private inline fun Pair<Int, Int>.unequal(block: (Int) -> Unit): Pair<Int, Int> {
            val difference = first - second
            if (difference != 0) {
                block(difference)
            }
            return this
        }
    }
}
