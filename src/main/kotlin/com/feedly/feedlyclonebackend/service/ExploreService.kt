package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.dto.ExplorePostDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExploreService {

    fun getExplorePosts(): List<ExplorePostDto> {
        return listOf(
            ExplorePostDto(
                title = "Sample Tech News",
                summary = "This is a sample article from NewsAPI.",
                imageUrl = null,
                source = "NewsAPI",
                publishedAt = LocalDateTime.now().minusDays(1)
            )
        )
    }
}
