package com.swiply.backend.notification

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface NotificationRepository : JpaRepository<Notification, UUID> {

    @Query(
        """
        select n from Notification n
        where n.userId = :userId
          and (:unreadOnly = false or n.isRead = false)
          and (
            cast(:cursorTs as instant) is null
            or n.createdAt < :cursorTs
            or (n.createdAt = :cursorTs and n.id < :cursorId)
          )
        order by n.createdAt desc, n.id desc
        """,
    )
    fun findPage(
        @Param("userId") userId: UUID,
        @Param("unreadOnly") unreadOnly: Boolean,
        @Param("cursorTs") cursorTs: Instant?,
        @Param("cursorId") cursorId: UUID?,
        pageable: Pageable,
    ): List<Notification>

    fun countByUserIdAndIsReadFalse(userId: UUID): Long

    @Modifying
    @Query("update Notification n set n.isRead = true where n.id = :id and n.userId = :userId")
    fun markRead(@Param("id") id: UUID, @Param("userId") userId: UUID): Int

    @Modifying
    @Query("update Notification n set n.isRead = true where n.userId = :userId and n.isRead = false")
    fun markAllRead(@Param("userId") userId: UUID): Int
}
