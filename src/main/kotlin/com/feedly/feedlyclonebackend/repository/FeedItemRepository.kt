package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.FeedItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

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
}
