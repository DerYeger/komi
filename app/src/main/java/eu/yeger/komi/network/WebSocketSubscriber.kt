package eu.yeger.komi.network

interface WebSocketSubscriber<Incoming, Outgoing> {
    /**
     * Invoked after the handler has been bound.
     * @return Optional list of Messages to be sent to the active WebSocket, if one is active.
     */
    fun onBind(): Collection<Outgoing>?

    /**
     * Invoked after the handles has been unbound.
     * @return Optional list of Messages to be sent to the active WebSocket, if one is active.
     */
    fun onUnbind(): Collection<Outgoing>?

    /**
     * Invoked when the active WebSocket receives a message.
     * @param message Received message.
     * @return Optional list of response Messages.
     */
    fun onMessage(message: Incoming): Collection<Outgoing>?

    /**
     * Invoked when the active WebSocket receives a message.
     * @param error Error message.
     */
    fun onError(error: String)
}
