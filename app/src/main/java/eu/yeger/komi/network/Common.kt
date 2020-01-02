package eu.yeger.komi.network

import com.squareup.moshi.Moshi
import eu.yeger.komi.BuildConfig
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

object KomiWebSocketClient : WebSocketClient<Message, Message>(
    url = "ws://${BuildConfig.BACKEND_URL}",
    webSocketFactory = OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build(),
    automatedLifecycle = true
) {
    override fun String.toInboundMessage(): Message? =
        moshi.adapter(Message::class.java).fromJson(this)

    override fun Message.toText(): String = moshi.adapter(Message::class.java).toJson(this)
}
