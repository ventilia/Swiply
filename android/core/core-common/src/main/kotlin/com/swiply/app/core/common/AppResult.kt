package com.swiply.app.core.common

/**
 * Ошибка приложения: машиночитаемый код с бэкенда + человекочитаемое сообщение.
 */
data class AppError(
    val code: String,
    val message: String,
    val isNetwork: Boolean = false,
    val httpStatus: Int? = null,
) {
    companion object {
        const val CODE_NETWORK = "NETWORK"
        const val CODE_UNKNOWN = "UNKNOWN"

        fun network() = AppError(CODE_NETWORK, "Нет соединения с сервером", isNetwork = true)
        fun unknown(message: String? = null) = AppError(CODE_UNKNOWN, message ?: "Что-то пошло не так")
    }
}

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(block: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) block(error)
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

val <T> AppResult<T>.isSuccess: Boolean get() = this is AppResult.Success
