package eu.yeger.komi.network

import com.squareup.moshi.Moshi
import okhttp3.WebSocket

//
// Serialization
//

val moshi: Moshi by lazy { Moshi.Builder().build() }

//
// Classes
//

data class Message(val type: String, val data: String)

//
// Komi
//

object KomiMessageParser : WebSocketMessageParser<Message, Message> {
    override fun parseString(message: String): Message? =
        moshi.adapter(Message::class.java).fromJson(message)

    override fun parseOutgoing(outgoing: Message): String =
        moshi.adapter(Message::class.java).toJson(outgoing)
}

object KomiWebSocketManager : WebSocketManager<Message, Message>(KomiMessageParser)
