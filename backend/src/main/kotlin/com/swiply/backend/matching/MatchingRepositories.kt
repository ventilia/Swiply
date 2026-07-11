package com.swiply.backend.matching

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SwipeRepository : JpaRepository<Swipe, UUID> {

    fun findByFromUserIdAndToUserId(fromUserId: UUID, toUserId: UUID): Swipe?

    fun findTopByFromUserIdOrderByCreatedAtDesc(fromUserId: UUID): Swipe?


    @Query(
        """
        select s from Swipe s
        where s.fromUserId = :from and s.toUserId = :to and s.action in ('LIKE', 'SUPERLIKE')
        """,
    )
    fun findReverseLike(@Param("from") from: UUID, @Param("to") to: UUID): Swipe?

    fun deleteByFromUserIdAndToUserId(fromUserId: UUID, toUserId: UUID)
}

interface MatchRepository : JpaRepository<Match, UUID> {

    @Query(
        """
        select m from Match m
        where (m.userAId = :userId or m.userBId = :userId)
          and m.unmatchedAt is null
          and (cast(:cursorTs as instant) is null or m.matchedAt < :cursorTs)
        order by m.matchedAt desc
        """,
    )
    fun findActiveFor(
        @Param("userId") userId: UUID,
        @Param("cursorTs") cursorTs: Instant?,
        pageable: Pageable,
    ): List<Match>

    @Query(
        """
        select m from Match m
        where m.userAId = :a and m.userBId = :b
        """,
    )
    fun findByNormalizedPair(@Param("a") a: UUID, @Param("b") b: UUID): Match?

    @Query(
        """
        select count(m) from Match m
        where (m.userAId = :userId or m.userBId = :userId) and m.unmatchedAt is null
        """,
    )
    fun countActiveFor(@Param("userId") userId: UUID): Long
}

interface BlockedUserRepository : JpaRepository<BlockedUser, BlockedUserId> {

    @Query(
        """
        select count(b) > 0 from BlockedUser b
        where (b.id.blockerId = :a and b.id.blockedId = :b)
           or (b.id.blockerId = :b and b.id.blockedId = :a)
        """,
    )
    fun existsEitherWay(@Param("a") a: UUID, @Param("b") b: UUID): Boolean

    fun findAllByIdBlockerId(blockerId: UUID): List<BlockedUser>
}
