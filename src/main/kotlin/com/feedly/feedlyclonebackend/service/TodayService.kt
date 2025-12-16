package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.dto.FeedItemDto
import com.feedly.feedlyclonebackend.repository.FeedItemRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TodayService(
    private val repository: FeedItemRepository
) {

    fun meItems(userId: Long): List<FeedItemDto> {
        val threshold = LocalDateTime.now().minusDays(30)

        return repository.findTodayItems(userId, threshold)
            .map { FeedItemDto.from(it) }
    }
}
