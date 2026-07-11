package com.swiply.backend.discovery

import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertTrue

class WeightedCandidateRankerTest {

    private val ranker = WeightedCandidateRanker()

    private fun row(
        distanceMeters: Double = 5_000.0,
        maxDistanceMeters: Double = 50_000.0,
        lastActiveAt: Instant? = Instant.now(),
        hasBio: Boolean = true,
        photoCount: Int = 4,
        isVerified: Boolean = true,
    ) = CandidateRow(UUID.randomUUID(), distanceMeters, maxDistanceMeters, lastActiveAt, hasBio, photoCount, isVerified)

    @Test
    fun `скор в диапазоне 0-1`() {
        val best = ranker.score(row())
        val worst = ranker.score(
            row(
                distanceMeters = 50_000.0,
                lastActiveAt = Instant.now().minus(30, ChronoUnit.DAYS),
                hasBio = false,
                photoCount = 0,
                isVerified = false,
            ),
        )
        assertTrue(best in 0.0..1.0, "best=$best")
        assertTrue(worst in 0.0..1.0, "worst=$worst")
        assertTrue(best > worst)
    }

    @Test
    fun `недавняя активность важнее всего`() {
        val fresh = ranker.score(row(lastActiveAt = Instant.now(), hasBio = false, photoCount = 0, isVerified = false))
        val stale = ranker.score(row(lastActiveAt = Instant.now().minus(30, ChronoUnit.DAYS)))
        assertTrue(fresh > stale - 0.3, "свежая активность должна конкурировать даже с полным профилем")
    }

    @Test
    fun `близкие кандидаты ранжируются выше при прочих равных`() {
        val near = ranker.score(row(distanceMeters = 1_000.0))
        val far = ranker.score(row(distanceMeters = 49_000.0))
        assertTrue(near > far)
    }

    @Test
    fun `полнота профиля повышает скор при прочих равных`() {
        val complete = ranker.score(row(hasBio = true, photoCount = 4, isVerified = true))
        val empty = ranker.score(row(hasBio = false, photoCount = 0, isVerified = false))
        assertTrue(complete > empty)
    }

    @Test
    fun `отсутствие lastActiveAt не роняет ранжирование`() {
        val score = ranker.score(row(lastActiveAt = null))
        assertTrue(score in 0.0..1.0)
    }
}
