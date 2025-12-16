package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.FeedService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/discover")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class DiscoverApiController(
    private val feedService: FeedService
) {
    private val logger = LoggerFactory.getLogger(DiscoverApiController::class.java)

    @GetMapping
    fun getDiscoverData(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false, defaultValue = "headlines") view: String
    ): ResponseEntity<Map<String, Any>> {
        logger.debug("API Discover - query: $query, category: $category, view: $view")

        val response = mutableMapOf<String, Any>()

        // Categories
        val categories = feedService.getAllCategories()
        response["categories"] = categories
        response["selectedCategory"] = category ?: ""
        response["query"] = query ?: ""
        response["view"] = view

        // My Feed (팔로우한 피드 아이템)
        if (view == "myfeed") {
            val items = feedService.getFollowedFeedItems()
            response["items"] = items
            response["count"] = items.size
            return ResponseEntity.ok(response)
        }

        // 검색어가 있으면 기사 검색
        if (!query.isNullOrBlank()) {
            val articles = feedService.searchArticles(query)
            response["articles"] = articles
            response["articleCount"] = articles.size
            return ResponseEntity.ok(response)
        }

        // Feeds view
        if (view == "feeds" || view == "all") {
            val feeds = when {
                source == "newsapi" -> feedService.getNewsApiSourcesByCategory(category?.lowercase())
                else -> feedService.getFeedsByCategory(category)
            }
            response["feeds"] = feeds
            response["feedCount"] = feeds.size
        }

        // Headlines view
        if (view == "headlines" || view == "all") {
            val headlines = feedService.getTopHeadlines(category = category?.lowercase())
            response["headlines"] = headlines.take(20)
        }

        return ResponseEntity.ok(response)
    }
}