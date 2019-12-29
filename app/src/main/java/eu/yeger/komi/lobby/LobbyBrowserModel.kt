package eu.yeger.komi.lobby

import androidx.compose.Model

data class User(
    val id: Long,
    val name: String
)

data class Lobby(
    val id: Long,
    val name: String,
    val players: List<User>
)

@Model
class LobbyBrowserModel {
    var lobbies: List<Lobby> = listOf()
    var lobbyNameInput: String = "MyLobby"
}
