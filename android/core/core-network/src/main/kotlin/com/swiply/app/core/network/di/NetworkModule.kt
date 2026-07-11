package com.swiply.app.core.network.di

import com.swiply.app.core.network.BuildConfig
import com.swiply.app.core.network.api.AuthApi
import com.swiply.app.core.network.api.ChatApi
import com.swiply.app.core.network.api.DiscoveryApi
import com.swiply.app.core.network.api.MatchingApi
import com.swiply.app.core.network.api.MediaApi
import com.swiply.app.core.network.api.NotificationApi
import com.swiply.app.core.network.api.ProfileApi
import com.swiply.app.core.network.api.ReportApi
import com.swiply.app.core.network.api.TokenRefreshApi
import com.swiply.app.core.network.auth.AuthInterceptor
import com.swiply.app.core.network.auth.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Provides
    @Singleton
    @Named("baseOkHttp")
    fun baseOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                // BASIC: без тел запросов — в логи не утекают токены и личные данные
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides
    @Singleton
    @Named("apiOkHttp")
    fun apiOkHttp(
        @Named("baseOkHttp") base: OkHttpClient,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = base.newBuilder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    /** Для WebSocket: без Authenticator (токен идёт query-параметром), с ping'ом */
    @Provides
    @Singleton
    @Named("wsOkHttp")
    fun wsOkHttp(@Named("baseOkHttp") base: OkHttpClient): OkHttpClient = base.newBuilder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("apiBaseUrl")
    fun apiBaseUrl(): String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"

    @Provides
    @Singleton
    @Named("wsUrl")
    fun wsUrl(@Named("apiBaseUrl") baseUrl: String): String =
        baseUrl.replaceFirst("http", "ws").trimEnd('/') + "/ws"

    @Provides
    @Singleton
    fun retrofit(
        @Named("apiOkHttp") okHttpClient: OkHttpClient,
        @Named("apiBaseUrl") baseUrl: String,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("refreshRetrofit")
    fun refreshRetrofit(
        @Named("baseOkHttp") okHttpClient: OkHttpClient,
        @Named("apiBaseUrl") baseUrl: String,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun authApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun tokenRefreshApi(@Named("refreshRetrofit") retrofit: Retrofit): TokenRefreshApi =
        retrofit.create(TokenRefreshApi::class.java)

    @Provides
    @Singleton
    fun profileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun mediaApi(retrofit: Retrofit): MediaApi = retrofit.create(MediaApi::class.java)

    @Provides
    @Singleton
    fun discoveryApi(retrofit: Retrofit): DiscoveryApi = retrofit.create(DiscoveryApi::class.java)

    @Provides
    @Singleton
    fun matchingApi(retrofit: Retrofit): MatchingApi = retrofit.create(MatchingApi::class.java)

    @Provides
    @Singleton
    fun chatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun notificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun reportApi(retrofit: Retrofit): ReportApi = retrofit.create(ReportApi::class.java)
}
