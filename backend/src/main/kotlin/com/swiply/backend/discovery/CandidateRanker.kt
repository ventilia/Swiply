package com.swiply.backend.discovery

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.Instant
import java.util.UUID

data class CandidateRow(
    val userId: UUID,
    val distanceMeters: Double,
    val maxDistanceMeters: Double,
    val lastActiveAt: Instant?,
    val hasBio: Boolean,
    val photoCount: Int,
    val isVerified: Boolean,
)


fun interface CandidateRanker {
    fun score(row: CandidateRow): Double
}

class WeightedCandidateRanker : CandidateRanker {

    companion object {
        private const val W_RECENCY = 0.5
        private const val W_COMPLETENESS = 0.3
        private const val W_DISTANCE = 0.2
        private val RECENCY_HORIZON: Duration = Duration.ofDays(7)
    }

    override fun score(row: CandidateRow): Double {
        val recency = row.lastActiveAt?.let {
            val elapsed = Duration.between(it, Instant.now())
            if (elapsed.isNegative) {
                1.0
            } else {
                (1.0 - elapsed.toMillis().toDouble() / RECENCY_HORIZON.toMillis()).coerceIn(0.0, 1.0)
            }
        } ?: 0.0

        val completeness = listOf(
            if (row.hasBio) 0.3 else 0.0,
            (row.photoCount.coerceAtMost(4) / 4.0) * 0.5,
            if (row.isVerified) 0.2 else 0.0,
        ).sum()

        val distance = if (row.maxDistanceMeters <= 0) {
            0.0
        } else {
            (1.0 - row.distanceMeters / row.maxDistanceMeters).coerceIn(0.0, 1.0)
        }

        return W_RECENCY * recency + W_COMPLETENESS * completeness + W_DISTANCE * distance
    }
}

@Configuration
class RankerConfig {

    @Bean
    @ConditionalOnMissingBean(CandidateRanker::class)
    fun weightedCandidateRanker(): CandidateRanker = WeightedCandidateRanker()
}
