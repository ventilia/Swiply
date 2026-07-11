package com.swiply.app.core.network

import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.network.dto.ApiErrorDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единая точка перевода исключений сети в [AppResult]:
 * HTTP-ошибки разбираются в структурный [AppError] с кодом бэкенда.
 */
@Singleton
class ApiCaller @Inject constructor(private val json: Json) {

    suspend fun <T> call(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: HttpException) {
        AppResult.Failure(e.toAppError())
    } catch (e: IOException) {
        AppResult.Failure(AppError.network())
    } catch (e: Exception) {
        AppResult.Failure(AppError.unknown(e.message))
    }

    private fun HttpException.toAppError(): AppError {
        val parsed = response()?.errorBody()?.string()?.let { body ->
            runCatching { json.decodeFromString<ApiErrorDto>(body) }.getOrNull()
        }
        val fieldSummary = parsed?.fieldErrors?.entries?.joinToString("; ") { "${it.key}: ${it.value}" }
        return AppError(
            code = parsed?.code ?: "HTTP_${code()}",
            message = fieldSummary?.takeIf { it.isNotBlank() }
                ?: parsed?.message
                ?: "Ошибка сервера (${code()})",
            httpStatus = code(),
        )
    }
}
