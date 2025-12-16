package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "feed_items")
class FeedItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    val feed: Feed,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 500)
    val title: String,

    @Column(nullable = false, length = 2000)
    val summary: String,

    @Column(length = 500)
    val imageUrl: String? = null,

    @Column(nullable = false, length = 200)
    val source: String,

    @Column(nullable = false)
    val publishedAt: LocalDateTime,

    @Column(nullable = false)
    var isRead: Boolean = false
)
