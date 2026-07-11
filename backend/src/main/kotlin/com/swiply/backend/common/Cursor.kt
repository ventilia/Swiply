package com.swiply.backend.common

import java.util.Base64


object Cursor {

    fun encode(raw: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray(Charsets.UTF_8))

    fun decode(cursor: String?): String? {
        if (cursor.isNullOrBlank()) return null
        return try {
            String(Base64.getUrlDecoder().decode(cursor), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("BAD_CURSOR", "Некорректный курсор пагинации")
        }
    }
}

data class PageResponse<T>(
    val items: List<T>,
    val nextCursor: String? = null,
)
