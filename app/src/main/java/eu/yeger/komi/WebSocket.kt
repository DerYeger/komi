package eu.yeger.komi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

data class Message(val type: String, val data: String)

private val moshi by lazy { Moshi.Builder().build() }

fun WebSocket.send(message: Message) {
    send(moshi.adapter(Message::class.java).toJson(message))
}

class KomiWebSocketListener(private val lobbyModel: LobbyModel) : WebSocketListener() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val lobbyListAdapter: JsonAdapter<List<Lobby>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, Lobby::class.java))

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send(Message("join", "Placeholder"))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println(text)
        val message: Message? = moshi.adapter(Message::class.java).fromJson(text)
        when (message?.type) {
            "lobbies" -> lobbyListAdapter.fromJson(message.data)?.let { setLobbies(it) }
            "error" -> "Connection closed ${message.data}"
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1002, "Connection closing")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        if (code != 1000) {
            showError("Connection closed $code")
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        showError(t.message ?: "Unknown error")
    }

    private fun setLobbies(lobbies: List<Lobby>) {
        scope.launch {
            lobbyModel.lobbies = lobbies
        }
    }

    private fun showError(errorMessage: String) {
        scope.launch {
            lobbyModel.error = errorMessage
        }
    }
}
