package com.swiply.backend.media

import com.swiply.backend.common.AbstractUuidEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

enum class PhotoStatus { PENDING, APPROVED, REJECTED }

@Entity
@Table(name = "photos")
class Photo(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(nullable = false)
    var position: Int,

    @Column(name = "storage_key", nullable = false, length = 256)
    var storageKey: String,

    @Column(name = "thumb_key", length = 256)
    var thumbKey: String? = null,

    @Column(name = "thumb_small_key", length = 256)
    var thumbSmallKey: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: PhotoStatus = PhotoStatus.PENDING,
) : AbstractUuidEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}
