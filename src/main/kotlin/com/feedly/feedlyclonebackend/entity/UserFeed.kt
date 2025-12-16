package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_feeds")
class UserFeed(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 1L,

    // 수정: length를 1000에서 500으로 변경 (MySQL utf8mb4 인덱스 제한 때문)
    @Column(name = "feed_url", nullable = false, length = 500)
    var feedUrl: String,

    @Column(name = "feed_title", length = 500)
    var feedTitle: String? = null,

    @Column(name = "feed_description", columnDefinition = "TEXT")
    var feedDescription: String? = null,

    @Column(name = "feed_type", length = 50)
    var feedType: String = "RSS",

    @Column(name = "favicon_url", length = 500)
    var faviconUrl: String? = null,

    @Column(name = "category", length = 100)
    var category: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_fetched_at")
    var lastFetchedAt: LocalDateTime? = null
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun toString(): String {
        return "UserFeed(id=$id, userId=$userId, feedUrl='$feedUrl', feedTitle=$feedTitle)"
    }
}