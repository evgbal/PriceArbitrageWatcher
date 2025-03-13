package org.bea.pricearbitragewatcher.net

import okhttp3.Interceptor
import okhttp3.Response
//import org.bea.pricearbitragewatcher.BuildConfig

import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    //private val token = BuildConfig.DADATA_API_KEY
    //private val secret = BuildConfig.DADATA_SECRET_KEY

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            //.addHeader("Authorization", token)
//            .apply {
//                // Добавляем X-Secret только для запроса очистки адреса
//                if (originalRequest.url.encodedPath.contains("/clean/address")) {
//                    addHeader("X-Secret", secret)
//                }
//            }
            .build()
        return chain.proceed(newRequest)
    }
}