package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.layout.ExpandedHeight
import androidx.ui.layout.HeightSpacer
import androidx.ui.material.Button
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*

class LobbyActivity : AppCompatActivity() {

    private val lobbyModel = LobbyModel()
    private val request: Request = Request.Builder().url("ws://${BuildConfig.BACKEND_URL}/lobby").build()
    private val webSocket: WebSocket

    init {
        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, LobbySocketListener(lobbyModel))
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
                Button(text = "Create", onClick = { webSocket.send("{\"type\":\"createLobby\"}") })
            }
            HeightSpacer(height = 8.dp)
            LobbyList(lobbyModel = lobbyModel)
        }
    }
}

@Composable
fun LobbyList(lobbyModel: LobbyModel) {
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
    Text(text = "ID: ${lobby.id} Players: ${lobby.players.size}/2")
}

data class Player(val id: Long)
data class Lobby(val id: Long, val players: List<Player>)

@Model
class LobbyModel {
    var lobbies: List<Lobby> = listOf()
}

class LobbySocketListener(private val lobbyModel: LobbyModel) : WebSocketListener() {

    data class Message(val type: String, val data: List<Lobby>)

    private val moshi = Moshi.Builder().build()

    override fun onMessage(webSocket: WebSocket, text: String) {
        println(text)
        moshi.adapter(Message::class.java).fromJson(text)?.also {
            CoroutineScope(Dispatchers.Main).launch {
                lobbyModel.lobbies = it.data
            }
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println(t)
    }
}