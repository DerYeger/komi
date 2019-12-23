package eu.yeger.komi.model

import androidx.compose.Model
import kotlinx.coroutines.*
import java.io.Serializable

@Model
class Game(
    val players: Pair<Player, Player>,
    val width: Int,
    val height: Int,
    val scoreLimit: Int
) {
    private val scope = CoroutineScope(Dispatchers.Main)

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
            scope.launch {
                turn(cell)
            }
        }
    }

    private suspend fun computerTurn(player: Player) {
        coroutineScope {
            delay(100)
            cells
                .filter { it.isEmpty() }
                .shuffled()
                .map { Turn(player = player, cell = it) }
                .max()
                ?.also { turn(it.cell) }
        }
    }

    private fun turn(cell: Cell) {
        if (turnIsValid(cell)) {
            cell.state = Cell.State.Occupied(currentPlayer)
            turnForPlayer(currentPlayer)
            if (gameOver) return
            turnForPlayer(currentPlayer.opponent)
            if (gameOver) return
            currentPlayer = currentPlayer.opponent
            if (currentPlayer.isComputer) {
                val computerPlayer = currentPlayer
                scope.launch {
                    computerTurn(computerPlayer)
                }
            }
        }
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
            .toSet() // collect occupied cells with empty neighbors, as those are inherently safe

        val uncheckedCells = cells
            .filter { it.state.player === this && !it.hasEmptyNeighbor(except = turn?.cell) }
            .toSet() // collect occupied cells without empty neighbors

        return cellsWithoutLiberties(
            safeCells = safeCells,
            uncheckedCells = uncheckedCells,
            turn = turn
        )
    }

    private tailrec fun Player.cellsWithoutLiberties(
        safeCells: Set<Cell>,
        uncheckedCells: Set<Cell>,
        turn: Turn?
    ): List<Cell> {
        uncheckedCells.filter {
            it.neighbors.any { neighbor ->
                neighbor in safeCells && neighbor.liberates(it, turn)
            }
        }.also {
            return when (it.isEmpty()) {
                true -> { // no more liberties granted, remaining unchecked cells can not have liberties
                    turn?.takeIf { turn ->
                        this === turn.player && turn.cell.neighbors.none { neighbor -> neighbor.isEmpty() || neighbor in safeCells }
                    }?.let { turn ->
                        uncheckedCells.toList() + turn.cell
                    } ?: uncheckedCells.toList()
                }
                false -> cellsWithoutLiberties(
                    safeCells = safeCells + it,
                    uncheckedCells = uncheckedCells - it,
                    turn = turn
                )
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

        companion object {
            const val DEFAULT_WIDTH = 5
            const val DEFAULT_HEIGHT = 5
            const val DEFAULT_SCORE_LIMIT = 5

            val WIDTH_RANGE = 4F..32F
            val HEIGHT_RANGE = 4F..32F

            fun maxScoreLimit(width: Number, height: Number) = width.toInt() * height.toInt() / 4
        }

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

            val gains = this.gains to other.gains
            val losses by lazy { this.losses to other.losses }
            val potentialLosses by lazy { this.potentialLosses to other.potentialLosses }
            val neighbors by lazy { this.cell.neighbors.size to other.cell.neighbors.size }
            val friendlyNeighbors by lazy { this.friendlyNeighbors to other.friendlyNeighbors }
            val opposedNeighbors by lazy { this.opposedNeighbors to other.opposedNeighbors }
            val emptyNeighbors by lazy { this.emptyNeighbors to other.emptyNeighbors }
            val nonOpposedNeighbors by lazy { this.occupiedNeighbors to other.occupiedNeighbors }
            val inherentLiberties by lazy { this.inherentLiberties to other.inherentLiberties }
            val opposedInherentLiberties by lazy { this.opposedInherentLiberties to other.opposedInherentLiberties }
            val lowestOpposedInherentLiberties by lazy { this.lowestOpposedInherentLiberties to other.lowestOpposedInherentLiberties }

            return more(gains) or {
                less(losses) or {
                    less(potentialLosses) or {
                        less(opposedInherentLiberties) or {
                            less(lowestOpposedInherentLiberties) or {
                                more(nonOpposedNeighbors) or {
                                    more(inherentLiberties) or {
                                        more(friendlyNeighbors) or {
                                            more(emptyNeighbors) or {
                                                more(opposedNeighbors) or {
                                                    more(neighbors)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private val gains by lazy {
            this.player.opponent.cellsWithoutLiberties(this).size
        }

        private val losses by lazy {
            this.player.cellsWithoutLiberties(this).size
        }

        private val potentialLosses by lazy {
            this.player.cellsWithoutLiberties(Turn(this.player.opponent, this.cell)).size
        }

        private val friendlyNeighbors by lazy {
            this.cell.neighbors.count { it.state.player === this.player }
        }

        private val opposedNeighbors by lazy {
            this.cell.neighbors.count { it.state.player === this.player.opponent }
        }

        private val emptyNeighbors by lazy {
            this.cell.neighbors.count { it.isEmpty() }
        }

        private val occupiedNeighbors by lazy {
            this.cell.neighbors.count { it.state.player !== this.player.opponent }
        }

        private val inherentLiberties by lazy {
            cells
                .filter { it.state.player === this.player }
                .flatMap { it.neighbors.filter { neighbor -> neighbor.isEmpty() && neighbor !== this.cell } }
                .distinct()
                .size
        }

        private val opposedInherentLiberties by lazy {
            cells
                .filter { it.state.player === this.player.opponent }
                .flatMap { it.neighbors.filter { neighbor -> neighbor.isEmpty() && neighbor !== this.cell } }
                .distinct()
                .size
        }

        private val lowestOpposedInherentLiberties by lazy {
            this.cell.neighbors
                .filter { it.state.player === this.player.opponent }
                .map {
                    it.neighbors.filter { neighbor -> neighbor.isEmpty() && neighbor !== this.cell }
                        .size
                }
                .min() ?: Int.MAX_VALUE
        }

        private fun more(pair: Pair<Int, Int>) = pair.first - pair.second

        private fun less(pair: Pair<Int, Int>) = pair.second - pair.first

        private inline infix fun Int.or(block: () -> Int) = if (this != 0) this else block()
    }
}
