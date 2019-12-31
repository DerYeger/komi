package eu.yeger.komi.network

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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

object KomiMessageParser : WebSocketMessageTransformer<Message, Message> {
    override fun stringToIncoming(message: String): Message? =
        moshi.adapter(Message::class.java).fromJson(message)

    override fun outgoingToString(message: Message): String =
        moshi.adapter(Message::class.java).toJson(message)
}

object KomiWebSocketManager : WebSocketManager<Message, Message>(
    client = OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build(),
    parser = KomiMessageParser
)
