package eu.yeger.komi.network

import okhttp3.*
import java.util.concurrent.ConcurrentHashMap

abstract class WebSocketClient<InboundMessage, OutboundMessage>(
    url: String,
    private val webSocketFactory: WebSocket.Factory = OkHttpClient()
) {
    //
    // Lifecycle
    //

    sealed class State<out InboundMessage, out OutboundMessage> {
        object Inactive : State<Nothing, Nothing>()

        class Active<InboundMessage, OutboundMessage>(
            val webSocket: WebSocket
        ) : State<InboundMessage, OutboundMessage>() {
            internal val subscribers =
                ConcurrentHashMap<String, Subscriber<InboundMessage, OutboundMessage>>()
        }
    }

    @Volatile
    var state: State<InboundMessage, OutboundMessage> = State.Inactive

    private val request: Request = Request.Builder().url(url).build()

    @Synchronized
    fun start() {
        when (state) {
            is State.Inactive -> {
                val webSocket = webSocketFactory.newWebSocket(request, listener)
                state = State.Active(webSocket = webSocket)
            }
            is State.Active -> throw IllegalStateException("WebSocketManager is active, unable to start")
        }
    }

    @Synchronized
    fun startIfInactive() {
        when (state) {
            is State.Active -> start()
        }
    }

    @Synchronized
    fun terminate(code: Int = 1000, reason: String = "Shutdown") {
        when (val state = state) {
            is State.Inactive -> throw IllegalStateException("WebSocketManager is inactive, unable to terminate")
            is State.Active -> {
                this.state = State.Inactive
                state.subscribers.keys.forEach { unsubscribe(it) }
                state.webSocket.close(code, reason)
            }
        }
    }

    @Synchronized
    fun restart() {
        when (state) {
            is State.Inactive -> throw IllegalStateException("WebSocketManager is inactive, unable to restart")
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
    // Un/Subscribing
    //

    interface Subscriber<in InboundMessage, out OutboundMessage> {
        /**
         * Invoked after the handler has been bound.
         * @return Optional list of Messages to be sent to the active WebSocket, if one is active.
         */
        fun onBind(): Collection<OutboundMessage>?

        /**
         * Invoked after the handles has been unbound.
         * @return Optional list of Messages to be sent to the active WebSocket, if one is active.
         */
        fun onUnbind(): Collection<OutboundMessage>?

        /**
         * Invoked when the active WebSocket receives a message.
         * @param message Received message.
         * @return Optional list of response Messages.
         */
        fun onMessage(message: InboundMessage): Collection<OutboundMessage>?

        /**
         * Invoked when the active WebSocket receives a message.
         * @param error Error message.
         */
        fun onError(error: String)
    }

    @Synchronized
    fun subscribe(key: String, subscriber: Subscriber<InboundMessage, OutboundMessage>) {
        when (val state = state) {
            is State.Active -> {
                state.subscribers[key]?.let { throw IllegalStateException("A WebSocketSubscriber with that key is already bound") }
                state.subscribers[key] = subscriber
                subscriber.onBind()?.let { send(it) }
            }
        }
    }

    @Synchronized
    fun unsubscribe(key: String) {
        when (val state = state) {
            is State.Active -> {
                when (val subscriber = state.subscribers.remove(key)) {
                    null -> throw IllegalStateException("No WebSocketSubscriber with that key is bound")
                    else -> subscriber.onUnbind()?.let { send(it) }
                }
                if (state.subscribers.isEmpty()) terminate()
            }
        }
    }

    //
    // Message sending
    //

    private fun WebSocket.send(message: OutboundMessage) {
        send(message.toText())
    }

    private fun WebSocket.send(messages: Collection<OutboundMessage>) {
        messages.map { it.toText() }.forEach { send(it) }
    }

    fun send(message: OutboundMessage) {
        when (val state = state) {
            is State.Inactive -> throw IllegalStateException("WebSocketManager is inactive, unable to send message")
            is State.Active -> state.webSocket.send(message)
        }
    }

    fun send(messages: Collection<OutboundMessage>) {
        when (val state = state) {
            is State.Inactive -> throw IllegalStateException("WebSocketManager is inactive, unable to send messages")
            is State.Active -> state.webSocket.send(messages)
        }
    }

    //
    // Listener
    //

    private val listener = Listener()

    private inner class Listener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            when (val state = state) {
                is State.Active -> {
                    val message: InboundMessage? = text.toInboundMessage()
                    if (message !== null) {
                        state.subscribers.values.forEach {
                            it.onMessage(message)?.let { responses -> webSocket.send(responses) }
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
                onError(
                    error = "Connection closed $code",
                    code = code,
                    reason = reason
                )
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            onError(
                error = t.message ?: "Unknown error",
                code = 1011,
                reason = t.message ?: "Failure"
            )
        }
    }

    //
    // Message transformations
    //

    protected abstract fun String.toInboundMessage(): InboundMessage?

    protected abstract fun OutboundMessage.toText(): String
}
