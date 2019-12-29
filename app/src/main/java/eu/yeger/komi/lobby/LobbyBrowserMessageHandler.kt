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

    override fun onBind() = Message("join", "Placeholder")

    override fun onUnbind(): Message? = null

    override fun onMessage(message: Message): Message? {
        return when (message.type) {
            "lobbies" -> lobbyListAdapter.fromJson(message.data)?.let { setLobbies(it) }
//            "error" -> scope.launch{
//                Toast.makeText(
//                    +ambient(ContextAmbient),
//                    message.data,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
            else -> null
        }

    }

    override fun onError(error: String) {
        scope.launch {
            lobbyBrowserModel.error = error
        }
    }

    private fun setLobbies(lobbies: List<Lobby>): Message? {
        scope.launch {
            lobbyBrowserModel.lobbies = lobbies
        }
        return null
    }
}
