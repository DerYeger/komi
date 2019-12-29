package eu.yeger.komi.lobby

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.yeger.komi.network.Message
import eu.yeger.komi.network.WebSocketMessageHandler
import eu.yeger.komi.network.moshi
import eu.yeger.komi.network.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket

class LobbyBrowserMessageHandler(private val lobbyBrowserModel: LobbyBrowserModel) :
    WebSocketMessageHandler {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val lobbyListAdapter: JsonAdapter<List<Lobby>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, Lobby::class.java))

    override fun onBind(webSocket: WebSocket) {
        webSocket.send(Message("join", "Placeholder"))
    }

    override fun onUnbind() = Unit

    override fun onMessage(webSocket: WebSocket, message: Message) {
        when (message.type) {
            "lobbies" -> lobbyListAdapter.fromJson(message.data)?.let { setLobbies(it) }
//            "error" -> scope.launch{
//                Toast.makeText(
//                    +ambient(ContextAmbient),
//                    message.data,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        }
    }

    private fun setLobbies(lobbies: List<Lobby>) {
        scope.launch {
            lobbyBrowserModel.lobbies = lobbies
        }
    }
}
