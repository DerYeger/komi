package eu.yeger.komi.network

import okhttp3.WebSocket

interface WebSocketMessageHandler {
    fun onBind(webSocket: WebSocket)
    fun onUnbind()
    fun onMessage(webSocket: WebSocket, message: Message)
}
