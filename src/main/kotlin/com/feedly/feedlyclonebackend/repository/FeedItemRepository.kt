package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.FeedItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import com.feedly.feedlyclonebackend.entity.Feed
import org.springframework.stereotype.Repository
import org.springframework.jdbc.core.JdbcTemplate

@Repository
interface FeedItemRepository : JpaRepository<FeedItem, Long>{

    @Query("""
        select f from FeedItem f
        where f.userId = :userId
          and f.isRead = false
          and f.publishedAt >= :since
        order by f.publishedAt desc
    """)
    fun findUnreadRecentByUser(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<com.feedly.feedlyclonebackend.dto.FeedItem>

    fun findByCompanyId(companyId: Long): List<Feed>
}


