package com.swiply.app.core.network.dto

import com.swiply.app.core.model.Page
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
    val fieldErrors: Map<String, String>? = null,
)

@Serializable
data class OkDto(val ok: Boolean = true)

@Serializable
data class PageDto<T>(
    val items: List<T> = emptyList(),
    val nextCursor: String? = null,
)

fun <T, R> PageDto<T>.toDomain(transform: (T) -> R): Page<R> =
    Page(items = items.map(transform), nextCursor = nextCursor)
