package com.mariofronza.face_collection_app.api

import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

private const val AUTHORIZATION = "Authorization"
private val JSON = "application/json".toMediaType()
private val AUTH_PATHS = setOf("/sessions", "/refresh-tokens")

private fun Request.isAuth() = AUTH_PATHS.any { url.encodedPath.endsWith(it) }

class AuthInterceptor(private val store: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = store.current?.token
        val request = chain.request()
        if (token == null || request.isAuth()) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(AUTHORIZATION, "Bearer $token").build())
    }
}

/** Refreshes the session once on a 401 and ends it when the refresh token is rejected. */
class TokenAuthenticator(
    private val baseUrl: String,
    private val store: SessionStore,
    private val refreshClient: OkHttpClient,
) : Authenticator {
    private val gson = Gson()

    override fun authenticate(route: Route?, response: Response): Request? {
        val current = store.current ?: return null
        if (response.request.isAuth() || response.priorResponse != null) return null

        val refresh = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/refresh-tokens")
            .post(gson.toJson(RefreshTokenRequest(current.refreshToken)).toRequestBody(JSON))
            .build()

        val renewed = refreshClient.newCall(refresh).execute().use { reply ->
            if (reply.isSuccessful) gson.fromJson(reply.body.string(), Session::class.java) else null
        }

        store.current = renewed
        return renewed?.let { response.request.newBuilder().header(AUTHORIZATION, "Bearer ${it.token}").build() }
    }
}
