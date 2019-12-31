package eu.yeger.komi.lobby_browser

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
import eu.yeger.komi.network.KomiWebSocketClient
import eu.yeger.komi.network.Message

@Composable
fun LobbyPage(lobbyBrowserModel: LobbyBrowserModel) {
    ThemedPage {
        Column(modifier = ExpandedHeight) {
            CenteredRow {
                Text(
                    text = "Lobby Browser",
                    style = (+MaterialTheme.typography()).h3.merge(TextStyle(color = secondaryColor))
                )
            }
            HeightSpacer(height = 8.dp)
            if (lobbyBrowserModel.error === null) {
                CreateLobbyRow(lobbyBrowserModel)
                HeightSpacer(height = 24.dp)
                LobbyList(lobbyBrowserModel)
            } else {
                CenteredRow {
                    Text(
                        text = "An error occurred: ${lobbyBrowserModel.error}",
                        style = TextStyle(color = errorColor)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateLobbyRow(lobbyBrowserModel: LobbyBrowserModel) {
    KomiCard {
        ExpandedRow(arrangement = Arrangement.SpaceBetween) {
            TextField(
                value = lobbyBrowserModel.lobbyNameInput,
                onValueChange = { lobbyBrowserModel.lobbyNameInput = it },
                modifier = Width(150.dp)
            )
            Button(
                text = "Create",
                onClick = {
                    KomiWebSocketClient.send(
                        Message(
                            "createLobby",
                            lobbyBrowserModel.lobbyNameInput
                        )
                    )
                },
                modifier = Width(75.dp)
            )
        }
    }
}

@Composable
fun LobbyList(lobbyBrowserModel: LobbyBrowserModel) {
    VerticalScroller {
        Column {
            for (lobby in lobbyBrowserModel.lobbies) {
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
                    KomiWebSocketClient.send(
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
