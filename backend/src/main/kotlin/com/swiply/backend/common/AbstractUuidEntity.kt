package com.swiply.backend.common

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable
import java.util.UUID


@MappedSuperclass
abstract class AbstractUuidEntity(
    @Id
    private val id: UUID = UUID.randomUUID(),
) : Persistable<UUID> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    @PostPersist
    @PostLoad
    protected fun markNotNew() {
        isNewEntity = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractUuidEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
