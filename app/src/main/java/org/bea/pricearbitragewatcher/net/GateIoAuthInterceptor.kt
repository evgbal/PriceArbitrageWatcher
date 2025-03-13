package org.bea.pricearbitragewatcher.net

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Named

class GateIoAuthInterceptor @Inject constructor(
    @Named("GateIoApiKey") private val apiKey: String,
    @Named("GateIoApiSecret") private val apiSecret: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Запоминаем тело запроса (для повторного использования)
        val requestBody = originalRequest.body?.let { BufferRequestBody(it) }

        val method = originalRequest.method
        val urlPath = originalRequest.url.encodedPath
        val queryString = originalRequest.url.query ?: ""

        // Для GET/DELETE запросов тело пустое
        val payloadString = if (method == "GET" || method == "DELETE") {
            ""
        } else {
            requestBody?.string() ?: ""
        }

        val timestamp = Instant.now().epochSecond.toString()
        val hashedPayload = sha512(payloadString)
        val signString = "$method\n$urlPath\n$queryString\n$hashedPayload\n$timestamp"
        val signature = hmacSha512(apiSecret, signString)

        // Создаём новый запрос с подписью
        val signedRequest = originalRequest.newBuilder()
            .method(method, requestBody) // Используем кастомное тело
            .addHeader("KEY", apiKey)
            .addHeader("Timestamp", timestamp)
            .addHeader("SIGN", signature)
            .build()

        return chain.proceed(signedRequest)
    }

    // Кастомный RequestBody для повторного использования
    class BufferRequestBody(private val originalBody: RequestBody) : RequestBody() {
        private val buffer = okio.Buffer()

        init {
            // Сохраняем данные в buffer для повторного чтения
            originalBody.writeTo(buffer)
        }

        override fun contentType(): MediaType? = originalBody.contentType()

        override fun contentLength(): Long = originalBody.contentLength()

        override fun writeTo(sink: BufferedSink) {
            // Пишем данные из buffer в sink
            sink.write(buffer, buffer.size)
        }

        fun string(): String {
            // Возвращаем строку из buffered данных
            return buffer.readUtf8()
        }
    }

    private fun sha512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha512(secret: String, data: String): String {
        val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA512")
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}