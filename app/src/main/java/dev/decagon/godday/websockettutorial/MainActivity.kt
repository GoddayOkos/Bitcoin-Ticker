package dev.decagon.godday.websockettutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class MainActivity : AppCompatActivity() {
    private lateinit var webSocketClient: WebSocketClient

    companion object {
        const val WEB_SOCKET_URL = "wss://ws-feed.pro.coinbase.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        initWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }

    private fun initWebSocket() {
        val coinBaseUri: URI? = URI(WEB_SOCKET_URL)
        createWebSocket(coinBaseUri)
        val socketFactory: SSLSocketFactory =
            SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    private fun createWebSocket(coinBaseUri: URI?) {
        webSocketClient = object : WebSocketClient(coinBaseUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                logIt("onOpen")
                subscribe()
            }

            override fun onMessage(message: String?) {
                logIt("onMessage: $message")
                setUpBtcPriceText(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                logIt("onClose")
                unsubscribe()
            }

            override fun onError(ex: Exception?) {
                logIt("onError: ${ex?.message}")
            }

        }
    }

    private fun unsubscribe() {
        webSocketClient.send(
            "{\n" + "    \"type\": \"unsubscribe\",\n" +"    \"channels\": [\"ticker\"]\n}"
        )
    }

    private fun setUpBtcPriceText(message: String?) {
        message?.let {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<BitcoinTicker> = moshi.adapter(BitcoinTicker::class.java)
            val bitcoin = adapter.fromJson(message)
            runOnUiThread { findViewById<TextView>(R.id.btc_price_tv).text = "1 BTC: ${bitcoin?.price} $" }
        }
    }

    private fun subscribe() {
        webSocketClient.send(
            "{\n" + "    \"type\": \"subscribe\",\n" +
                    "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-USD\"] }]\n" +
                    "}"
        )
    }

    private fun logIt(message: String) {
        Log.d("Coinbase", message)
    }
}