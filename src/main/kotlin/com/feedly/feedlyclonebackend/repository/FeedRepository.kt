package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.Feed
import org.springframework.stereotype.Repository
import org.springframework.jdbc.core.JdbcTemplate

@Repository
class FeedRepository(
    private val jdbcTemplate: JdbcTemplate
) {

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