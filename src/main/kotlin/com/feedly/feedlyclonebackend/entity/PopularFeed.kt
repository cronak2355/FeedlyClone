package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "popular_feeds")
class PopularFeed(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "feed_url", nullable = false, unique = true, length = 1000)
    var feedUrl: String,

    @Column(name = "site_url", length = 1000)
    var siteUrl: String? = null,

    @Column(name = "title", nullable = false, length = 500)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "category", length = 100)
    var category: String? = null,

    @Column(name = "favicon_url", length = 500)
    var faviconUrl: String? = null,

    @Column(name = "subscriber_count")
    var subscriberCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "PopularFeed(id=$id, title='$title', category=$category)"
    }
}
