package eu.yeger.komi.lobby_browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import eu.yeger.komi.network.KomiWebSocketClient

class LobbyBrowserActivity : AppCompatActivity() {

    private val lobbyBrowserModel = LobbyBrowserModel()
    private val lobbyBrowserSubscriber = LobbyBrowserSubscriber(lobbyBrowserModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KomiWebSocketClient.subscribe(lobbyBrowserSubscriber)
        setContent {
            LobbyPage(lobbyBrowserModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        KomiWebSocketClient.unsubscribe(lobbyBrowserSubscriber)
    }
}
