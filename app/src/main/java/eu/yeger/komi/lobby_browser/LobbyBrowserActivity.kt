package eu.yeger.komi.lobby_browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import eu.yeger.komi.network.WebSocketManager

class LobbyBrowserActivity : AppCompatActivity() {

    private val lobbyBrowserModel = LobbyBrowserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebSocketManager.apply {
            start()
            bind("lobby", LobbyBrowserSubscriber(lobbyBrowserModel))
        }
        setContent {
            LobbyPage(lobbyBrowserModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.unbind("lobby")
    }
}
