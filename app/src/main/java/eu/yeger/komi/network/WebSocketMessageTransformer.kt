package eu.yeger.komi.network

interface WebSocketMessageTransformer<out Incoming, in Outgoing> {
    fun stringToIncoming(message: String): Incoming?
    fun outgoingToString(message: Outgoing): String
}
