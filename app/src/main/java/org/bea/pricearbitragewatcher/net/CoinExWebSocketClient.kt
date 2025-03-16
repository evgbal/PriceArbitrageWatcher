package org.bea.pricearbitragewatcher.net

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CoinExWebSocketClient @Inject constructor(
    @Named("NoAuthOkHttpClient") private val client: OkHttpClient
) {

    companion object {
        private val _messageFlow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 10)
        val messageFlow: SharedFlow<String> get() = _messageFlow
    }

    private var webSocket: WebSocket? = null
    private val retryIntervalMillis: Long = 5000
    private var isConnecting =
        java.util.concurrent.atomic.AtomicBoolean(false) // Флаг состояния подключения
    private var lastSymbols: List<String> = listOf()

    @Synchronized
    fun connect(symbols: List<String>): Flow<String> = callbackFlow {
//        if (isConnecting.get()) {
//            // Если уже идет подключение, просто возвращаем существующий Flow
//            messageFlow.collect { trySend(it) }
//            return@callbackFlow
//        }

        closeWebSocketIfNeeded()
        isConnecting.set(true)

        val request = Request.Builder()
            .url("wss://socket.coinex.com/")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@CoinExWebSocketClient.webSocket = webSocket
                val symbolsSet = HashSet(symbols)
                val symbolsDiff = lastSymbols.filter { !symbolsSet.contains(it) }
                if (symbolsDiff.isNotEmpty()) {
                    val subscriptionMessage = CoinExSubscriptionMessage(
                        method = "bbo.unsubscribe",
                        params = symbolsDiff,
                        id = System.currentTimeMillis().toInt()
                    )
                    val message = Gson().toJson(subscriptionMessage)
                    webSocket.send(message)
                }

                val subscriptionMessage = CoinExSubscriptionMessage(
                    method = "bbo.subscribe",
                    params = symbols,
                    id = System.currentTimeMillis().toInt()
                )
                val message = Gson().toJson(subscriptionMessage)
                webSocket.send(message)

                lastSymbols = symbols
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                CoroutineScope(Dispatchers.IO).launch {
                    _messageFlow.emit(text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnecting.set(false)
                closeWebSocketIfNeeded()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(retryIntervalMillis) // Фиксированная задержка
                    connect(symbols).collect { trySend(it) }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnecting.set(false)
                closeWebSocketIfNeeded()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(retryIntervalMillis) // Переподключение после закрытия
                    connect(symbols).collect { trySend(it) }
                }
            }
        }

        webSocket = client.newWebSocket(request, listener)

        awaitClose {
            isConnecting.set(false)
            closeWebSocketIfNeeded()
        }
    }.shareIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        replay = 1
    )

    @Synchronized
    private fun closeWebSocketIfNeeded() {
        webSocket?.close(1000, "Closed by client") ?: webSocket?.cancel()
        webSocket = null
    }

    fun close() {
        closeWebSocketIfNeeded()
        isConnecting.set(false)
    }
}

data class CoinExSubscriptionMessage(
    val method: String,
    val params: List<String>,
    val id: Int
)