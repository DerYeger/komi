package eu.yeger.komi.network

interface WebSocketMessageParser<out Incoming, in Outgoing> {
    fun parseString(message: String): Incoming?
    fun parseOutgoing(outgoing: Outgoing): String
}
