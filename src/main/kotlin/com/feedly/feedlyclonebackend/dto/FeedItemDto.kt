package com.feedly.feedlyclonebackend.dto

import com.feedly.feedlyclonebackend.entity.FeedItem
import java.time.LocalDateTime

data class FeedItemDto(
    val id: Long,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val source: String,
    val publishedAt: LocalDateTime,
    val isRead: Boolean
) {
    companion object {
        fun from(entity: FeedItem): FeedItemDto =
            FeedItemDto(
                id = entity.id,
                title = entity.title,
                summary = entity.summary,
                imageUrl = entity.imageUrl,
                source = entity.source,
                publishedAt = entity.publishedAt,
                isRead = entity.isRead
            )
    }
}
