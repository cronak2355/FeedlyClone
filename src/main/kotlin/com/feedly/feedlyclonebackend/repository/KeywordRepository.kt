package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.Keyword
import org.springframework.stereotype.Repository
import org.springframework.jdbc.core.JdbcTemplate

@Repository
class KeywordRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun findByType(type: String): List<Keyword> =
        jdbcTemplate.query(
            "SELECT id, type, name FROM keyword WHERE type = ?",
            arrayOf(type)
        ) { rs, _ ->
            Keyword(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("name")
            )
        }
}