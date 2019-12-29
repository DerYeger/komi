package eu.yeger.komi.lobby

import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.TextField
import androidx.ui.core.dp
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.text.TextStyle
import eu.yeger.komi.common.*
import eu.yeger.komi.network.Message
import eu.yeger.komi.network.WebSocketManager

@Composable
fun LobbyPage(lobbyModel: LobbyBrowserModel) {
    ThemedPage {
        Column(modifier = ExpandedHeight) {
            CenteredRow {
                Text(
                    text = "Lobby Browser",
                    style = (+MaterialTheme.typography()).h3.merge(TextStyle(color = secondaryColor))
                )
            }
            HeightSpacer(height = 8.dp)
            if (WebSocketManager.error === null) {
                CreateLobbyRow(lobbyModel)
                HeightSpacer(height = 8.dp)
                LobbyList(lobbyModel)
            } else {
                CenteredRow {
                    Text(
                        text = "An error occurred: ${WebSocketManager.error}",
                        style = TextStyle(color = errorColor)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateLobbyRow(lobbyModel: LobbyBrowserModel) {
    KomiCard {
        ExpandedRow(arrangement = Arrangement.SpaceBetween) {
            TextField(
                value = lobbyModel.lobbyNameInput,
                onValueChange = { lobbyModel.lobbyNameInput = it },
                modifier = Width(150.dp)
            )
            Button(
                text = "Create",
                onClick = {
                    WebSocketManager.send(
                        Message(
                            "createLobby",
                            lobbyModel.lobbyNameInput
                        )
                    )
                },
                modifier = Width(75.dp)
            )
        }
    }
}

@Composable
fun LobbyList(lobbyModel: LobbyBrowserModel) {
    VerticalScroller {
        Column {
            for (lobby in lobbyModel.lobbies) {
                CenteredRow {
                    LobbyView(lobby = lobby)
                }
                HeightSpacer(height = 4.dp)
            }
        }
    }
}

@Composable
fun LobbyView(lobby: Lobby) {
    KomiCard {
        ExpandedRow(arrangement = Arrangement.SpaceBetween) {
            Text(text = lobby.name, modifier = Width(150.dp))
            Text("${lobby.players.size}/2")
            Button(
                text = "Join",
                onClick = {
                    WebSocketManager.send(
                        Message(
                            "joinLobby",
                            lobby.id.toString()
                        )
                    )
                },
                modifier = Width(75.dp)
            )
        }
    }
}
