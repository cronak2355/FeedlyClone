package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.FeedService
import com.feedly.feedlyclonebackend.repository.UserArticleInteractionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reddit")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class RedditApiController(
    private val feedService: FeedService
) {
    
    @GetMapping
    fun getSubreddit(@RequestParam(required = false) subreddit: String?): ResponseEntity<Map<String, Any?>> {
        val response = mutableMapOf<String, Any?>()
        
        if (!subreddit.isNullOrBlank()) {
            val feed = feedService.parseRedditFeed(subreddit)
            response["subreddit"] = feed
        }
        
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/search")
    fun searchSubreddits(@RequestParam query: String): ResponseEntity<Map<String, Any>> {
        val suggestions = feedService.searchSubreddits(query)
        return ResponseEntity.ok(mapOf(
            "suggestions" to suggestions,
            "count" to suggestions.size
        ))
    }
    
    @GetMapping("/popular")
    fun getPopularSubreddits(): ResponseEntity<Map<String, Any>> {
        val popularSubreddits = listOf(
            "programming", "kotlin", "java", "javascript", "python",
            "webdev", "android", "technology", "news", "worldnews"
        )
        
        val feeds = popularSubreddits.mapNotNull { feedService.parseRedditFeed(it) }
        
        return ResponseEntity.ok(mapOf(
            "subreddits" to feeds,
            "count" to feeds.size
        ))
    }
}
