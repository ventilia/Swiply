package com.swiply.backend.common

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

data class ApiErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val code: String,
    val message: String,
    val path: String? = null,
    val fieldErrors: Map<String, String>? = null,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException, request: HttpServletRequest): ResponseEntity<ApiErrorResponse> {
        val body = ApiErrorResponse(
            status = ex.status.value(),
            code = ex.code,
            message = ex.message,
            path = request.requestURI,
        )
        val builder = ResponseEntity.status(ex.status)
        if (ex is RateLimitedException) {
            builder.header("Retry-After", ex.retryAfterSeconds.toString())
        }
        return builder.body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        val fields = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity.badRequest().body(
            ApiErrorResponse(
                status = 400,
                code = "VALIDATION_ERROR",
                message = "Некорректные данные запроса",
                path = request.requestURI,
                fieldErrors = fields,
            ),
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.badRequest().body(
            ApiErrorResponse(
                status = 400,
                code = "MALFORMED_REQUEST",
                message = "Тело запроса не читается",
                path = request.requestURI,
            ),
        )

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleUploadSize(
        ex: MaxUploadSizeExceededException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
            ApiErrorResponse(
                status = 413,
                code = "FILE_TOO_LARGE",
                message = "Файл слишком большой",
                path = request.requestURI,
            ),
        )


    @ExceptionHandler(AuthenticationException::class, AccessDeniedException::class)
    fun rethrowSecurity(ex: Exception): Nothing = throw ex

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(
        ex: NoResourceFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(status = 404, code = "NOT_FOUND", message = "Ресурс не найден", path = request.requestURI),
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiErrorResponse> {
        log.error("Необработанное исключение на {}", request.requestURI, ex)
        return ResponseEntity.internalServerError().body(
            ApiErrorResponse(
                status = 500,
                code = "INTERNAL_ERROR",
                message = "Внутренняя ошибка сервера",
                path = request.requestURI,
            ),
        )
    }
}
