package eu.yeger.komi

import androidx.compose.Model
import androidx.ui.graphics.Color

@Model
class Game(
    val players: Pair<Player, Player> = Pair(Player.Black, Player.White),
    val cellArray: Array<Array<Cell>> = Array(5) { y ->
        Array(5) { x -> Cell(x = x, y = y, state = CellState.Empty) }
    }

) {
    private val cells = cellArray.flatten()
    var currentPlayer: Player? = null

    private val neighborMap = HashMap<Cell, List<Cell>>()

    init {
        // workaround for compiler backend exception
        currentPlayer = players.first
    }

    fun turn(cell: Cell) {
        if (turnIsValid(cell)) {
            cell.state = CellState.Occupied(currentPlayer!!)
            val cellsWithoutLiberties = cellsWithoutLiberties()
            cells
                .filter { it in cellsWithoutLiberties }
                .forEach {
                    it.state.player.opponent().score++
                    it.state = CellState.Empty
                }

            currentPlayer = currentPlayer?.opponent()
        }
    }

    private fun cellsWithoutLiberties(): List<Cell> {
        val safeCells = cells
            .filter { it.isOccupied() && it.hasEmptyNeighbor() }
            .toMutableSet() // collect occupied cells with empty neighbors, as those are inherently safe

        val uncheckedCells = cells
            .filter { it.isOccupied() && !it.hasEmptyNeighbor() }
            .toMutableSet() // collect occupied cells without empty neighbors

        while (true) {
            val transitiveSafeCells = uncheckedCells
                .filter { safeCells.any { safeCell -> safeCell.grantsLibertyTo(it) } }
                .toSet()

            if (transitiveSafeCells.isEmpty()) {
                return uncheckedCells.toList() // no more liberties granted, remaining unchecked cells can not have liberties
            }

            safeCells.addAll(transitiveSafeCells)
            uncheckedCells.removeAll(transitiveSafeCells)
        }
    }

    private fun Cell.grantsLibertyTo(other: Cell) = when (state) {
        CellState.Empty -> false
        else -> neighbors().contains(other) && state.player === other.state.player
    }

    private fun Cell.hasEmptyNeighbor() = neighbors().any { it.isEmpty() }

    private fun turnIsValid(cell: Cell): Boolean {
        if (cell.isOccupied()) return false

        val neighbors = cell.neighbors()
        if (neighbors.all { it.state.player === this.currentPlayer?.opponent() }) return false

        return true
    }

    private fun Player.opponent() = when (this) {
        players.first -> players.second
        players.second -> players.first
        else -> Player.None
    }

    private fun Cell.neighbors() = neighborMap.computeIfAbsent(this) {
        cells.filter { it.isHorizontalNeighborOf(this) || it.isVerticalNeighborOf(this) }
    }
}

@Model
class Player(
    val name: String,
    val color: Color,
    var score: Int = 0
) {
    companion object {
        val None = Player(name = "None", color = Color.Transparent, score = 0)
        val Black = Player(name = "Black", color = Color.Black, score = 0)
        val White = Player(name = "White", color = Color.White, score = 0)
    }
}

@Model
data class Cell(
    val x: Int,
    val y: Int,
    var state: CellState
) {
    fun isEmpty() = state === CellState.Empty

    fun isOccupied() = !isEmpty()

    fun isVerticalNeighborOf(other: Cell) =
        this.x == other.x && (this.y == other.y - 1 || this.y == other.y + 1)

    fun isHorizontalNeighborOf(other: Cell) =
        this.y == other.y && (this.x == other.x - 1 || this.x == other.x + 1)
}

sealed class CellState {
    abstract val player: Player

    val color
        get() = player.color

    object Empty : CellState() {
        override val player = Player.None
    }

    class Occupied(override val player: Player) : CellState()
}
