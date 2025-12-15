package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.PopularFeed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PopularFeedRepository : JpaRepository<PopularFeed, Long> {

    fun findByCategory(category: String): List<PopularFeed>

    fun findByCategoryOrderBySubscriberCountDesc(category: String): List<PopularFeed>

    fun findAllByOrderBySubscriberCountDesc(): List<PopularFeed>

    @Query("""
        SELECT pf FROM PopularFeed pf 
        WHERE LOWER(pf.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(pf.description) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(pf.category) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY pf.subscriberCount DESC
    """)
    fun searchByQuery(@Param("query") query: String): List<PopularFeed>

    @Query("SELECT DISTINCT pf.category FROM PopularFeed pf WHERE pf.category IS NOT NULL ORDER BY pf.category")
    fun findAllCategories(): List<String>

    fun findByFeedUrl(feedUrl: String): PopularFeed?
}
