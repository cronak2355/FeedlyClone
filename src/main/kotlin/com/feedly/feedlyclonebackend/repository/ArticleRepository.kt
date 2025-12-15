package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.Article
import org.springframework.stereotype.Repository
import org.springframework.jdbc.core.JdbcTemplate

@Repository
class ArticleRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun findByFeedIds(feedIds: List<Long>): List<Article> {
        if (feedIds.isEmpty()) return emptyList()

        val inSql = feedIds.joinToString(",")
        val sql = """
            SELECT id, feed_id, title, url, published_at
            FROM article
            WHERE feed_id IN ($inSql)
            ORDER BY published_at DESC
        """

        return jdbcTemplate.query(sql) { rs, _ ->
            Article(
                rs.getLong("id"),
                rs.getLong("feed_id"),
                rs.getString("title"),
                rs.getString("url"),
                rs.getTimestamp("published_at")?.toLocalDateTime()
            )
        }
    }
}
