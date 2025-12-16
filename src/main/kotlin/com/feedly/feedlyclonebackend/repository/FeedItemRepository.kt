package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.FeedItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
interface FeedItemRepository : JpaRepository<FeedItem, Long> {

    // 엔티티를 조회 (DTO 변환은 서비스 레이어에서 처리)
    @Query("""
        SELECT f FROM FeedItem f
        WHERE f.userId = :userId
          AND f.isRead = false
          AND f.publishedAt >= :since
        ORDER BY f.publishedAt DESC
    """)
    fun findUnreadRecentEntitiesByUser(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<FeedItem>
    fun findByFeedIdOrderByPublishedAtDesc (id:Long): List<FeedItem>
    // companyId로 FeedItem 조회
    fun findByCompanyId(companyId: Long): List<FeedItem>
}