package eu.yeger.komi.network

interface WebSocketSubscriber {
    /**
     * Invoked after the handler has been bound.
     * @return Optional list of Messages to be sent to the active WebSocket, if one is active.
     */
    fun onBind(): List<Message>?

    /**
     * Invoked after the handles has been unbound.
     * @return Optional list of Messages to be sent to the active WebSocket, if one is active.
     */
    fun onUnbind(): List<Message>?

    /**
     * Invoked when the active WebSocket receives a message.
     * @param message Received message.
     * @return Optional list of response Messages.
     */
    fun onMessage(message: Message): List<Message>?

    /**
     * Invoked when the active WebSocket receives a message.
     * @param error Error message.
     */
    fun onError(error: String)
}
