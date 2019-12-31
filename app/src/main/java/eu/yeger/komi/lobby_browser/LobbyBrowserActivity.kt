package eu.yeger.komi.lobby_browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import eu.yeger.komi.BuildConfig
import eu.yeger.komi.network.KomiWebSocketManager

class LobbyBrowserActivity : AppCompatActivity() {

    private val lobbyBrowserModel = LobbyBrowserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KomiWebSocketManager.apply {
            start("ws://${BuildConfig.BACKEND_URL}")
            subscribe("lobby", LobbyBrowserSubscriber(lobbyBrowserModel))
        }
        setContent {
            LobbyPage(lobbyBrowserModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        KomiWebSocketManager.unsubscribe("lobby")
    }
}
