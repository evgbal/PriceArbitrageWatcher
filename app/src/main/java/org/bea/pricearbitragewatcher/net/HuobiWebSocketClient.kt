package org.bea.pricearbitragewatcher.net

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val TAG = "HuobiWebSocket"

@Singleton
class HuobiWebSocketClient @Inject constructor(
    @Named("NoAuthOkHttpClient") private val client: OkHttpClient
) {

    companion object {
        private val _messageFlow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 10)
        val messageFlow: SharedFlow<String> get() = _messageFlow
    }

    private var webSocket: WebSocket? = null
    private var pingJob: Job? = null
    private var resubscribeJob: Job? = null
    private val retryIntervalMillis: Long = 5000
    private var isConnecting = AtomicBoolean(false) // Флаг состояния подключения

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
            .url("wss://api.huobi.pro/ws")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket открыт")
                this@HuobiWebSocketClient.webSocket = webSocket
                startPing()
                subscribeToSymbols(symbols)
                startResubscribeLoop(symbols)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                when {
                    text.contains("\"ping\"") -> {
                        val pong = JSONObject().apply { put("pong", json.getLong("ping")) }.toString()
                        webSocket.send(pong)
                        Log.d(TAG, "Отправлен PONG: $pong")
                    }
                    json.has("subbed") -> {
                        Log.d(TAG, "Подписка подтверждена: $text")
                    }
                    json.has("unsubbed") -> {
                        Log.w(TAG, "Подписка отменена сервером: $text")
                    }
                    json.has("err-code") -> {
                        Log.e(TAG, "Ошибка от сервера: ${json.optString("err-msg")}")
                    }
                    else -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            _messageFlow.emit(text)
                        }
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                try {
                    val decompressed = GZIPInputStream(bytes.toByteArray().inputStream())
                        .bufferedReader()
                        .use { it.readText() }
                    onMessage(webSocket, decompressed)
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка разархивирования: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Ошибка WebSocket: ${t.localizedMessage}")
                isConnecting.set(false)
                closeWebSocketIfNeeded()
                stopPing()
                stopResubscribeLoop()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(retryIntervalMillis)
                    connect(symbols).collect { trySend(it) }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "Соединение закрыто: $reason")
                isConnecting.set(false)
                closeWebSocketIfNeeded()
                stopPing()
                stopResubscribeLoop()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(retryIntervalMillis)
                    connect(symbols).collect { trySend(it) }
                }
            }
        }

        webSocket = client.newWebSocket(request, listener)

        awaitClose {
            isConnecting.set(false)
            closeWebSocketIfNeeded()
            stopPing()
            stopResubscribeLoop()
        }
    }.shareIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        replay = 1
    )

    private fun startPing() {
        pingJob?.cancel()
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(30000)
                webSocket?.send("{\"ping\": ${System.currentTimeMillis()}}")
                Log.d(TAG, "Ping отправлен")
            }
        }
    }

    private fun stopPing() {
        pingJob?.cancel()
        pingJob = null
        Log.d(TAG, "Ping остановлен")
    }

    private fun subscribeToSymbols(symbols: List<String>) {
        symbols.forEach { symbol ->
            val subscriptionMessage = HuobiSubscriptionMessage(
                sub = "market.$symbol.ticker",
                id = System.currentTimeMillis().toString()
            )
            val message = Gson().toJson(subscriptionMessage)
            webSocket?.send(message)
            Log.d(TAG, "Подписка отправлена: $message")
        }
    }

    private fun startResubscribeLoop(symbols: List<String>) {
        resubscribeJob?.cancel()
        resubscribeJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(300000) // Раз в 5 минут
                Log.d(TAG, "Обновление подписки...")
                subscribeToSymbols(symbols)
            }
        }
    }

    private fun stopResubscribeLoop() {
        resubscribeJob?.cancel()
        resubscribeJob = null
        Log.d(TAG, "Цикл обновления подписок остановлен")
    }

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

data class HuobiSubscriptionMessage(
    val sub: String,
    val id: String
)