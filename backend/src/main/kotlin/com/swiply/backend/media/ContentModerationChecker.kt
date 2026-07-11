package com.swiply.backend.media

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

enum class ModerationVerdict { APPROVE, REJECT, MANUAL_REVIEW }

/**
 * Плагин авто-модерации контента. Реальный сервис (Rekognition, Vision API, self-hosted NSFW-модель)
 * подключается заменой бина — пайплайн об этом не знает.
 */
fun interface ContentModerationChecker {
    fun check(imageBytes: ByteArray): ModerationVerdict
}

@Configuration
class ContentModerationConfig {

    /** Дефолт: авто-аппрув. Ручная модерация всё равно доступна админам. */
    @Bean
    @ConditionalOnMissingBean(ContentModerationChecker::class)
    fun autoApproveChecker(): ContentModerationChecker = ContentModerationChecker { ModerationVerdict.APPROVE }
}
