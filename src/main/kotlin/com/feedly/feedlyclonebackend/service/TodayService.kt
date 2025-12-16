package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.dto.FeedItem  // DTO import
import com.feedly.feedlyclonebackend.repository.FeedItemRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TodayService(
    private val repository: FeedItemRepository
) {

    fun meItems(userId: Long): List<FeedItem> {
        val threshold = LocalDateTime.now().minusDays(30)

        // 1. 올바른 메서드 이름 사용
        val entities = repository.findUnreadRecentEntitiesByUser(userId, threshold)

        // 2. Entity를 DTO로 변환
        return entities.map { entity ->
            FeedItem(
                title = entity.title,
                link = entity.url,
                description = entity.summary,
                author = entity.source,
                publishedDate = entity.publishedAt,
                thumbnailUrl = entity.imageUrl,
                categories = emptyList()
            )
        }
    }
}