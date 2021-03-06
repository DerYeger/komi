package eu.yeger.komi.lobby_browser

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import eu.yeger.komi.network.Message
import eu.yeger.komi.network.WebSocketClient
import eu.yeger.komi.network.moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LobbyBrowserSubscriber(private val lobbyBrowserModel: LobbyBrowserModel) :
    WebSocketClient.Subscriber<Message, Message> {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val lobbyListAdapter: JsonAdapter<List<Lobby>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, Lobby::class.java))

    override fun onSubscribe() = listOf(Message("join", "Placeholder"))

    override fun onUnsubscribe(): Collection<Message>? = null

    override fun onMessage(message: Message): Collection<Message>? {
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

    private fun setLobbies(lobbies: List<Lobby>): Collection<Message>? {
        scope.launch {
            lobbyBrowserModel.lobbies = lobbies
        }
        return null
    }
}
