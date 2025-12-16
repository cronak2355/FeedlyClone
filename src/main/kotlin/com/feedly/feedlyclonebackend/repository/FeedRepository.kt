package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.Feed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedRepository : JpaRepository<Feed, Long> {

    fun findByCompanyId(companyId: Long): List<Feed>

    fun existsByUrl(url: String): Boolean
}
