package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.UserFeed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserFeedRepository : JpaRepository<UserFeed, Long> {

    fun findByUserId(userId: Long): List<UserFeed>

    fun findByUserIdAndFeedUrl(userId: Long, feedUrl: String): UserFeed?

    fun existsByUserIdAndFeedUrl(userId: Long, feedUrl: String): Boolean

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<UserFeed>

    fun findByUserIdAndCategory(userId: Long, category: String): List<UserFeed>

    fun findByUserIdAndFeedType(userId: Long, feedType: String): List<UserFeed>

    @Query("SELECT uf FROM UserFeed uf WHERE uf.userId = :userId AND uf.isActive = true ORDER BY uf.createdAt DESC")
    fun findActiveByUserId(@Param("userId") userId: Long): List<UserFeed>

    @Query("SELECT DISTINCT uf.category FROM UserFeed uf WHERE uf.userId = :userId AND uf.category IS NOT NULL")
    fun findDistinctCategoriesByUserId(@Param("userId") userId: Long): List<String>

    fun deleteByUserIdAndFeedUrl(userId: Long, feedUrl: String): Long
}
