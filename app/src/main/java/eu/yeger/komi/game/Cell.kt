package eu.yeger.komi.game

import androidx.compose.Model

@Model
data class Cell(
    val x: Int,
    val y: Int,
    var state: State
) {
    fun isEmpty() = state === State.Empty

    fun isOccupied() = !isEmpty()

    fun isNeighborOf(other: Cell) = isVerticalNeighborOf(other) || isHorizontalNeighborOf(other)

    private fun isVerticalNeighborOf(other: Cell) =
        this.x == other.x && (this.y == other.y - 1 || this.y == other.y + 1)

    private fun isHorizontalNeighborOf(other: Cell) =
        this.y == other.y && (this.x == other.x - 1 || this.x == other.x + 1)

    //
    // Cell.State
    //

    sealed class State {
        abstract val player: Player

        val color
            get() = player.color

        object Empty : State() {
            override val player = Player.None
        }

        class Occupied(override val player: Player) : State()
    }
}
