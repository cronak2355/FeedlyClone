package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.FeedItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import com.feedly.feedlyclonebackend.entity.Feed
import org.springframework.stereotype.Repository
import org.springframework.jdbc.core.JdbcTemplate

@Repository
interface FeedItemRepository : JpaRepository<FeedItem, Long> {

    fun findByFeedIdOrderByPublishedAtDesc(feedId: Long): List<FeedItem>

    @Query(
        """
        SELECT fi
        FROM FeedItem fi
        WHERE fi.userId = :userId
          AND fi.isRead = false
          AND fi.publishedAt >= :threshold
        ORDER BY fi.publishedAt DESC
        """
    )
    fun findTodayItems(
        @Param("userId") userId: Long,
        @Param("threshold") threshold: LocalDateTime
    ): List<FeedItem>

    fun findUnreadRecentByUser(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<com.feedly.feedlyclonebackend.dto.FeedItem>

    fun findByCompanyId(companyId: Long): List<Feed>
}


