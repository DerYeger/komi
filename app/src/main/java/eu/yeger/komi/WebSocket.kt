package eu.yeger.komi

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

data class Message(val type: String, val data: String)

private val moshi by lazy { Moshi.Builder().build() }

fun WebSocket.send(message: Message) {
    val json = moshi.adapter(Message::class.java).toJson(message)
    send(json)
}

class KomiWebSocketListener(private val lobbyModel: LobbyModel) : WebSocketListener() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val lobbyListMessageAdapter = moshi.adapter(LobbyListMessage::class.java)

    data class LobbyListMessage(val type: String, val data: List<Lobby>)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send(Message("join", "Placeholder"))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        lobbyListMessageAdapter.fromJson(text)?.also {
            scope.launch {
                lobbyModel.lobbies = it.data
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1002, "Connection closing")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        if (code != 1000) {
            scope.launch {
                lobbyModel.error = "Connection closed $code"
            }
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        scope.launch {
            lobbyModel.error = t.message
        }
    }
}