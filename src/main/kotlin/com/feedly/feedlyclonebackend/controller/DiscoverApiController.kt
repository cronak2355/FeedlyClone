package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.dto.ApiResponse
import com.feedly.feedlyclonebackend.service.FeedService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/discover")
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

        // Feeds view or search
        if (view == "feeds" || view == "all" || !query.isNullOrBlank()) {
            val feeds = when {
                !query.isNullOrBlank() -> {
                    val result = feedService.searchFeeds(query)
                    result.feeds
                }
                source == "newsapi" -> {
                    feedService.getNewsApiSourcesByCategory(category?.lowercase())
                }
                else -> {
                    feedService.getFeedsByCategory(category)
                }
            }
            response["feeds"] = feeds
            response["feedCount"] = feeds.size
        }

        // Headlines view
        if ((view == "headlines" || view == "all") && query.isNullOrBlank()) {
            val headlines = feedService.getTopHeadlines(category = category?.lowercase())
            response["headlines"] = headlines.take(20)
        }

        return ResponseEntity.ok(response)
    }
}
