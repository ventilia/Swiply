package com.swiply.app.core.network.auth

import com.swiply.app.core.datastore.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Подставляет Bearer-токен во все запросы, кроме auth-эндпоинтов. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isAuthEndpoint = request.url.encodedPath.startsWith("/api/v1/auth/")
        val token = tokenStorage.accessToken()

        val newRequest = if (!isAuthEndpoint && token != null) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
