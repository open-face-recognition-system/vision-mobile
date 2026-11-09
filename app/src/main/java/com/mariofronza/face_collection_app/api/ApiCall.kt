package com.mariofronza.face_collection_app.api

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class ApiError(val status: Int?, message: String) : Exception(message)

suspend fun <T> safeCall(call: suspend () -> Response<T>): Result<T> = try {
    val response = call()
    @Suppress("UNCHECKED_CAST")
    val body = response.body() ?: (if (response.code() == 204) Unit as T else null)
    if (response.isSuccessful && body != null) Result.success(body)
    else Result.failure(ApiError(response.code(), errorMessage(response.code(), response.errorBody()?.string())))
} catch (e: IOException) {
    Result.failure(ApiError(null, "Falha de conexão com o servidor"))
}

private fun errorMessage(status: Int, errorBody: String?): String = try {
    JSONObject(errorBody.orEmpty()).getString("message")
} catch (e: JSONException) {
    when {
        status == 401 -> "Sessão expirada, entre novamente"
        status >= 500 -> "Servidor indisponível, tente novamente mais tarde"
        else -> "Erro na requisição"
    }
}
