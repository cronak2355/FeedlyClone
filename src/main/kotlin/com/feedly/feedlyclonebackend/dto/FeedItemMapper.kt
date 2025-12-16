package com.feedly.feedlyclonebackend.dto

import com.feedly.feedlyclonebackend.entity.FeedItem

fun FeedItem.toDto(): FeedItemDto =
    FeedItemDto(
        id = this.id,
        title = this.title,
        summary = this.summary,
        imageUrl = this.imageUrl,
        source = this.source,
        publishedAt = this.publishedAt,
        isRead = this.isRead
    )
