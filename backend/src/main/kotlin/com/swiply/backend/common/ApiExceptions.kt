package com.swiply.backend.common

import org.springframework.http.HttpStatus


open class ApiException(
    val status: HttpStatus,
    val code: String,
    override val message: String,
) : RuntimeException(message)

class BadRequestException(code: String, message: String) :
    ApiException(HttpStatus.BAD_REQUEST, code, message)

class UnauthorizedException(message: String = "Требуется аутентификация") :
    ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message)

class ForbiddenException(code: String = "FORBIDDEN", message: String = "Доступ запрещён") :
    ApiException(HttpStatus.FORBIDDEN, code, message)

class NotFoundException(code: String, message: String) :
    ApiException(HttpStatus.NOT_FOUND, code, message)

class ConflictException(code: String, message: String) :
    ApiException(HttpStatus.CONFLICT, code, message)

class RateLimitedException(val retryAfterSeconds: Long) :
    ApiException(
        HttpStatus.TOO_MANY_REQUESTS,
        "RATE_LIMITED",
        "Слишком много запросов, попробуйте позже",
    )
