package com.swiply.backend.discovery

import java.util.UUID

/** Задача пересчёта кэша кандидатов пользователя (профиль/локация/настройки изменились). */
data class DiscoveryRecomputeTask(val userId: UUID)
