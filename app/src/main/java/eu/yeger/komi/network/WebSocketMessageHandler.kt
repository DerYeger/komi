package eu.yeger.komi.network

interface WebSocketMessageHandler {
    /**
     * Invoked after the handler has been bound.
     * @return Optional Message to be sent to the active WebSocket, if one is active.
     */
    fun onBind(): Message?

    /**
     * Invoked after the handles has been unbound.
     * @return Optional Message to be sent to the active WebSocket, if one is active.
     */
    fun onUnbind(): Message?

    /**
     * Invoked when the active WebSocket receives a message.
     * @param message Received message.
     * @return Optional response Message.
     */
    fun onMessage(message: Message): Message?

    /**
     * Invoked when the active WebSocket receives a message.
     * @param error Error message.
     */
    fun onError(error: String)
}
