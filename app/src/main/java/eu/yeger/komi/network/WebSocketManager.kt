package eu.yeger.komi.network

import androidx.compose.Model
import eu.yeger.komi.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*

interface WebSocketMessageHandler {
    fun onBind(webSocket: WebSocket)
    fun onUnbind()
    fun onMessage(webSocket: WebSocket, message: Message)
}

@Model
object WebSocketManager : CoroutineScope {
    override val coroutineContext = Dispatchers.Main

    private var webSocket: WebSocket? = null
    private val handlers = HashMap<String, WebSocketMessageHandler>()

    var error: String? = null
        set(value) {
            launch {
                field = value
            }
        }

    //
    // Lifecycle
    //

    fun start(): WebSocketManager {
        terminate()

        error = null
        val request: Request = Request.Builder().url("ws://${BuildConfig.BACKEND_URL}").build()
        val client = OkHttpClient()
        webSocket = client.newWebSocket(
            request,
            ManagedWebSocketListener
        )
        client.dispatcher().executorService().shutdown()
        return this
    }

    fun terminate(code: Int = 1000, reason: String = "Shutdown") {
        handlers.keys.forEach {
            unbind(
                it
            )
        }
        webSocket?.close(code, reason)
    }

    //
    // Handler binding
    //

    fun bind(identifier: String, handler: WebSocketMessageHandler) {
        val webSocket = webSocket
        if (webSocket === null) throw WebSocketException("Not yet started")
        handlers[identifier] = handler
        handler.onBind(webSocket = webSocket)
    }

    fun unbind(identifier: String) {
        handlers.remove(identifier)?.onUnbind()
        if (handlers.isEmpty()) terminate()
    }

    //
    // Message sending
    //

    fun send(message: Message) = webSocket?.send(message)

    //
    // Listener
    //

    private object ManagedWebSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            val message: Message? = moshi.adapter(
                Message::class.java
            ).fromJson(text)
            if (message !== null) {
                handlers.values.forEach { it.onMessage(webSocket = webSocket, message = message) }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1002, "Connection closing")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (code != 1000) {
                error = "Connection closed $code"
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            error = t.message ?: "Unknown error"
        }
    }
}
