package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.entity.Article
import com.feedly.feedlyclonebackend.repository.ArticleRepository
import org.springframework.stereotype.Service

@Service
class ArticleService(
    private val feedService: FeedService,
    private val articleRepository: ArticleRepository
) {

    fun search(companyId: Long): List<Article> {
        val feeds = feedService.getFeedsByCompany(companyId)
        val feedIds = feeds.map { it.id }
        return articleRepository.findByFeedIds(feedIds)
    }
}
