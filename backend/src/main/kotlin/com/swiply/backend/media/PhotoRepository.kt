package com.swiply.backend.media

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PhotoRepository : JpaRepository<Photo, UUID> {
    fun findByUserIdOrderByPosition(userId: UUID): List<Photo>
    fun countByUserId(userId: UUID): Long
    fun findByStatusOrderByCreatedAt(status: PhotoStatus, pageable: Pageable): Page<Photo>
    fun deleteByUserId(userId: UUID)
}
