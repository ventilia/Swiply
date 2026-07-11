package com.swiply.backend.moderation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReportRepository : JpaRepository<Report, UUID> {
    fun findByStatusOrderByCreatedAt(status: ReportStatus, pageable: Pageable): Page<Report>
    fun countByStatus(status: ReportStatus): Long
}

interface ModerationActionRepository : JpaRepository<ModerationAction, UUID> {
    fun findByTargetUserIdOrderByCreatedAtDesc(targetUserId: UUID): List<ModerationAction>
}
