package eu.yeger.komi.lobby

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import eu.yeger.komi.network.WebSocketManager

class LobbyBrowserActivity : AppCompatActivity() {

    private val lobbyModel = LobbyBrowserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebSocketManager.start().bind("lobby", LobbyBrowserMessageHandler(lobbyModel))
        setContent {
            LobbyPage(lobbyModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.unbind("lobby")
    }
}
