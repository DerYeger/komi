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
// Extensions
//

fun WebSocket.send(message: Message) {
    send(moshi.adapter(Message::class.java).toJson(message))
}

//
// Exceptions
//

class WebSocketException(message: String) : Exception(message)