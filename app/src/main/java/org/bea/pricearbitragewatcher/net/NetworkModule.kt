package org.bea.pricearbitragewatcher.net

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bea.pricearbitragewatcher.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("gateIoApiKey")
    fun provideGateIoApiKey(): String = BuildConfig.GATE_IO_API_KEY

    @Provides
    @Named("gateIoApiSecret")
    fun provideGateIoApiSecret(): String = BuildConfig.GATE_IO_API_SECRET

    @Provides
    @Singleton
    @Named("NoAuthInterceptor")
    fun provideNoAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    @Provides
    @Singleton
    @Named("GateIoAuthInterceptor")
    fun provideGateIoAuthInterceptor(): GateIoAuthInterceptor {
        return GateIoAuthInterceptor(BuildConfig.GATE_IO_API_KEY, BuildConfig.GATE_IO_API_SECRET)
    }

    @Provides
    @Named("NoAuthOkHttpClient")
    fun provideNoAuthOkHttpClient(@Named("NoAuthInterceptor") authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Добавляем наш интерцептор
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }


    @Provides
    @Named("GateIoAuthOkHttpClient")
    fun provideGateIoAuthOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Добавляем наш интерцептор
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
//
//    @Provides
//    @Singleton
//    @Named("cleaner")
//    fun provideCleanerRetrofit(client: OkHttpClient): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://cleaner.dadata.ru")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }

//    @Provides
//    @Singleton
//    @Named("suggestions")
//    fun provideSuggestionsRetrofit(client: OkHttpClient): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://suggestions.dadata.ru")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }

//    @Provides
//    @Singleton
//    fun provideCleanerApi(@Named("cleaner") retrofit: Retrofit): CleanerApi {
//        return retrofit.create(CleanerApi::class.java)
//    }

//    @Provides
//    @Singleton
//    fun provideSuggestionsApi(@Named("suggestions") retrofit: Retrofit): SuggestionsApi {
//        return retrofit.create(SuggestionsApi::class.java)
//    }

//    @Provides
//    @Singleton
//    @Named("api_gateio_ws_api2")
//    fun provideGateIoRetrofit(okHttpClient: OkHttpClient): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://api.gateio.ws/api2/") // Используйте правильный базовый URL
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

//    @Provides
//    @Singleton
//    fun provideGateIoApi(@Named("api_gateio_ws_api2") retrofit: Retrofit): GateIoApi {
//        return retrofit.create(GateIoApi::class.java)
//    }
//
//
//
//    @Provides
//    fun provideGateIoWebSocketClient(@Named("GateIoAuthOkHttpClient") client: OkHttpClient): GateIoWebSocketClient {
//        return GateIoWebSocketClient(client)
//    }

    @Provides
    fun provideGateIoWebSocketClient(@Named("NoAuthOkHttpClient") client: OkHttpClient): GateIoWebSocketClient {
        return GateIoWebSocketClient(client)
    }

//    @Provides
//    @Singleton
//    fun provideSubscribeToGateIoUseCase(webSocketClient: GateIoWebSocketClient): SubscribeToGateIoUseCase {
//        return SubscribeToGateIoUseCase(webSocketClient)
//    }



    @Provides
    @Singleton
    @Named("api_gate_io_ws_api_v4")
    fun provideRetrofitGateIoWsV4Api(@Named("NoAuthOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.gateio.ws/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGateIoWsV4Api(@Named("api_gate_io_ws_api_v4") retrofit: Retrofit): GateIoWsV4Api {
        return retrofit.create(GateIoWsV4Api::class.java)
    }


    @Provides
    @Singleton
    @Named("api_coinex_com_api_v2")
    fun provideRetrofitCoinExV2Api(@Named("NoAuthOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.coinex.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCoinExV2Api(@Named("api_coinex_com_api_v2") retrofit: Retrofit): CoinExV2Api {
        return retrofit.create(CoinExV2Api::class.java)
    }


    @Provides
    @Singleton
    @Named("api_huobi_pro_v1")
    fun provideRetrofitHuobiProV1Api(@Named("NoAuthOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.huobi.pro/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHuobiProV1Api(@Named("api_huobi_pro_v1") retrofit: Retrofit): HuobiProV1Api {
        return retrofit.create(HuobiProV1Api::class.java)
    }

}
