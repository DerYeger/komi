package eu.yeger.komi.model

import androidx.compose.Model

@Model
data class Cell(
    val x: Int,
    val y: Int,
    var state: CellState
) {
    fun isEmpty() = state === CellState.Empty

    fun isOccupied() = !isEmpty()

    fun isNeighborOf(other: Cell) = isVerticalNeighborOf(other) || isHorizontalNeighborOf(other)

    private fun isVerticalNeighborOf(other: Cell) =
        this.x == other.x && (this.y == other.y - 1 || this.y == other.y + 1)

    private fun isHorizontalNeighborOf(other: Cell) =
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
