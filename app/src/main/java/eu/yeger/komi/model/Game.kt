package eu.yeger.komi.model

import androidx.compose.Model

@Model
class Game(
    val players: Pair<Player, Player> = Pair(
        Player.firstPlayer(),
        Player.secondPlayer()
    ),
    val width: Int = 5,
    val height: Int = 5,
    val scoreLimit: Int = 9
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
        val cellsToClear = player.opponent().cellsWithoutLiberties()
        cells
            .filter { it in cellsToClear }
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
            val transitiveSafeCells = uncheckedCells
                .filter { safeCells.any { safeCell -> safeCell.liberates(it) } }
                .toSet()

            if (transitiveSafeCells.isEmpty()) {
                return uncheckedCells.toList() // no more liberties granted, remaining unchecked cells can not have liberties
            }

            safeCells.addAll(transitiveSafeCells)
            uncheckedCells.removeAll(transitiveSafeCells)
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
}