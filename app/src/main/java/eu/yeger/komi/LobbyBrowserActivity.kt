package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.TextField
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.text.TextStyle
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class LobbyBrowserActivity : AppCompatActivity() {

    private val lobbyModel = LobbyModel()
    private val request: Request =
        Request.Builder().url("ws://${BuildConfig.BACKEND_URL}/lobby").build()
    private val webSocket: WebSocket

    init {
        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, KomiWebSocketListener(lobbyModel))
        client.dispatcher().executorService().shutdown()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LobbyPage(lobbyModel, webSocket)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.close(1000, "Shutdown")
    }
}

@Composable
fun LobbyPage(lobbyModel: LobbyModel, webSocket: WebSocket) {
    ThemedPage {
        Column(modifier = ExpandedHeight) {
            CenteredRow {
                Text(
                    text = "Lobby Browser",
                    style = (+MaterialTheme.typography()).h3.merge(TextStyle(color = secondaryColor))
                )
            }
            HeightSpacer(height = 8.dp)
            if (lobbyModel.error === null) {
                CreateLobbyRow(lobbyModel = lobbyModel, webSocket = webSocket)
                HeightSpacer(height = 8.dp)
                LobbyList(lobbyModel = lobbyModel, webSocket = webSocket)
            } else {
                CenteredRow {
                    Text(
                        text = "An error occurred: ${lobbyModel.error}",
                        style = TextStyle(color = errorColor)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateLobbyRow(lobbyModel: LobbyModel, webSocket: WebSocket) {
    KomiCard {
        ExpandedRow(arrangement = Arrangement.SpaceBetween) {
            TextField(
                value = lobbyModel.lobbyNameInput,
                onValueChange = { lobbyModel.lobbyNameInput = it },
                modifier = Width(150.dp)
            )
            Button(
                text = "Create",
                onClick = { webSocket.send(Message("createLobby", lobbyModel.lobbyNameInput)) },
                modifier = Width(75.dp)
            )
        }
    }
}

@Composable
fun LobbyList(lobbyModel: LobbyModel, webSocket: WebSocket) {
    VerticalScroller {
        Column {
            for (lobby in lobbyModel.lobbies) {
                CenteredRow {
                    LobbyView(lobby = lobby, webSocket = webSocket)
                }
                HeightSpacer(height = 4.dp)
            }
        }
    }
}

@Composable
fun LobbyView(lobby: Lobby, webSocket: WebSocket) {
    KomiCard {
        ExpandedRow(arrangement = Arrangement.SpaceBetween) {
            Text(text = lobby.name, modifier = Width(150.dp))
            Text("${lobby.players.size}/2")
            Button(
                text = "Join",
                onClick = { webSocket.send(Message("joinLobby", lobby.id.toString())) },
                modifier = Width(75.dp)
            )
        }
    }
}

data class Player(val id: Long, val name: String)
data class Lobby(val id: Long, val name: String, val players: List<Player>)

@Model
class LobbyModel {
    var lobbies: List<Lobby> = listOf()
    var lobbyNameInput: String = "MyLobby"
    var error: String? = null
}
