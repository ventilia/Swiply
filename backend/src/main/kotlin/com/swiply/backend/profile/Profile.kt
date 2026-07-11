package com.swiply.backend.profile

import com.swiply.backend.common.Gender
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Persistable
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.UUID

@Entity
@Table(name = "profiles")
class Profile(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "display_name", nullable = false, length = 40)
    var displayName: String,

    @Column(name = "birth_date", nullable = false)
    var birthDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var gender: Gender,


    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interested_in", nullable = false, columnDefinition = "text[]")
    var interestedIn: MutableList<String>,

    @Column(columnDefinition = "text")
    var bio: String? = null,


    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interests", nullable = false, columnDefinition = "text[]")
    var interests: MutableList<String> = mutableListOf(),


    @Column(columnDefinition = "geometry(Point,4326)")
    var location: Point? = null,

    @Column(length = 120)
    var city: String? = null,

    @Column(name = "min_age_pref", nullable = false)
    var minAgePref: Int = 18,

    @Column(name = "max_age_pref", nullable = false)
    var maxAgePref: Int = 100,

    @Column(name = "max_distance_km", nullable = false)
    var maxDistanceKm: Int = 50,

    @Column(name = "is_incognito", nullable = false)
    var isIncognito: Boolean = false,

    /** false = профиль скрыт из ленты (настройка видимости/деактивация) */
    @Column(name = "is_discoverable", nullable = false)
    var isDiscoverable: Boolean = true,

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false,
) : Persistable<UUID> {

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = userId

    override fun isNew(): Boolean = isNewEntity

    @PostPersist
    @PostLoad
    protected fun markNotNew() {
        isNewEntity = false
    }

    val age: Int
        get() = Period.between(birthDate, LocalDate.now()).years
}
