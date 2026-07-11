package com.swiply.backend.auth

import com.swiply.backend.config.SwiplyProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Абстракция отправки писем. SMTP-провайдер подключается заменой реализации,
 * бизнес-логика об этом не знает.
 */
interface EmailSender {
    fun sendEmailVerification(email: String, token: String)
    fun sendPasswordReset(email: String, token: String)
}

/**
 * Дефолтная реализация: без SMTP. По умолчанию НЕ печатает ни адрес целиком,
 * ни токен (приватность). Токен выводится в лог только если явно включён
 * dev-флаг swiply.auth.log-email-tokens (локальная разработка без почтового
 * сервера).
 */
@Component
class LoggingEmailSender(private val props: SwiplyProperties) : EmailSender {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendEmailVerification(email: String, token: String) =
        emit("подтверждение почты", email, token)

    override fun sendPasswordReset(email: String, token: String) =
        emit("сброс пароля", email, token)

    private fun emit(kind: String, email: String, token: String) {
        if (props.auth.logEmailTokens) {
            // dev-режим: токен нужен, чтобы протестировать флоу без SMTP
            log.info("[EMAIL dev] {} для {}: token={}", kind, mask(email), token)
        } else {
            log.info("[EMAIL] {} поставлено в очередь для {}", kind, mask(email))
        }
    }

    /** j***@example.com — не раскрываем локальную часть адреса */
    private fun mask(email: String): String {
        val at = email.indexOf('@')
        if (at <= 1) return "***"
        return email.first() + "***" + email.substring(at)
    }
}
