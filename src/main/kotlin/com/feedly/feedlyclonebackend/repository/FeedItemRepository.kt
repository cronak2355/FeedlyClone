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
interface FeedItemRepository : JpaRepository<FeedItem, Long> (
    private val jdbcTemplate: JdbcTemplate
){

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

    fun findByCompany(companyId: Long): List<Feed> =
        jdbcTemplate.query(
            "SELECT id, company_id, url FROM feed WHERE company_id = ?",
            arrayOf(companyId)
        ) { rs, _ ->
            Feed(
                rs.getLong("id"),
                rs.getLong("company_id"),
                rs.getString("url")
            )
        }
}


