package eu.yeger.komi.network

import eu.yeger.komi.BuildConfig
import okhttp3.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object WebSocketManager : WebSocketListener() {

    private val client = OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build()

    sealed class State {
        object Inactive : State()

        class Active(val webSocket: WebSocket) : State() {
            val subscribers = ConcurrentHashMap<String, WebSocketSubscriber>()

            fun terminate(code: Int, reason: String) {
                subscribers.keys.forEach { unbind(it) }
                webSocket.close(code, reason)
            }
        }
    }

    @Volatile
    var state: State = State.Inactive
        private set

    //
    // Lifecycle
    //

    @Synchronized
    fun start() {
        when (state) {
            is State.Inactive -> {
                val request: Request =
                    Request.Builder().url("ws://${BuildConfig.BACKEND_URL}").build()
                val webSocket = client.newWebSocket(request, this)
                state = State.Active(webSocket = webSocket)
            }
            is State.Active -> throw WebSocketManagerException("WebSocketManager is active, unable to start")
        }
    }

    @Synchronized
    fun terminate(code: Int = 1000, reason: String = "Shutdown") {
        when (val state = state) {
            is State.Inactive -> throw WebSocketManagerException("WebSocketManager is inactive, unable to terminate")
            is State.Active -> {
                this.state = State.Inactive
                state.terminate(code = code, reason = reason)
            }
        }
    }

    @Synchronized
    fun restart() {
        when (state) {
            is State.Inactive -> throw WebSocketManagerException("WebSocketManager is inactive, unable to restart")
            is State.Active -> {
                terminate()
                start()
            }
        }
    }

    private fun onError(error: String, code: Int, reason: String) {
        when (val state = state) {
            is State.Active -> {
                state.subscribers.values.forEach { it.onError(error) }
                terminate(code, reason)
            }
        }
    }

    //
    // Subscriber binding
    //

    @Synchronized
    fun bind(key: String, subscriber: WebSocketSubscriber) {
        when (val state = state) {
            is State.Active -> {
                state.subscribers[key]?.also { throw WebSocketManagerException("A WebSocketSubscriber with that key is already bound") }
                state.subscribers[key] = subscriber
                subscriber.onBind()?.also { send(it) }
            }
        }
    }

    @Synchronized
    fun unbind(key: String) {
        when (val state = state) {
            is State.Active -> {
                when (val subscriber = state.subscribers.remove(key)) {
                    null -> throw WebSocketManagerException("No WebSocketSubscriber with that key is bound")
                    else -> subscriber.onUnbind()?.also { send(it) }
                }
                if (state.subscribers.isEmpty()) terminate()
            }
        }
    }

    //
    // Message sending
    //

    fun send(message: Message) {
        when (val state = state) {
            is State.Inactive -> throw WebSocketManagerException("WebSocketManager is inactive, unable to send message")
            is State.Active -> state.webSocket.send(message)
        }
    }

    fun send(messages: List<Message>) {
        when (val state = state) {
            is State.Inactive -> throw WebSocketManagerException("WebSocketManager is inactive, unable to send messages")
            is State.Active -> state.webSocket.send(messages)
        }
    }

    //
    // Listener
    //

    override fun onMessage(webSocket: WebSocket, text: String) {
        when (val state = state) {
            is State.Active -> {
                val message: Message? = moshi.adapter(Message::class.java).fromJson(text)
                if (message !== null) {
                    state.subscribers.values.forEach {
                        it.onMessage(message = message)?.also { message -> webSocket.send(message) }
                    }
                }
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        when (state) {
            is State.Active -> terminate(code = code, reason = reason)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        if (code != 1000) {
            onError(error = "Connection closed $code", code = code, reason = reason)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onError(error = t.message ?: "Unknown error", code = 1011, reason = t.message ?: "Failure")
    }
}
