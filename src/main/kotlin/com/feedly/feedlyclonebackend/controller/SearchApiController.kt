package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.dto.*
import com.feedly.feedlyclonebackend.service.FeedService
import com.feedly.feedlyclonebackend.service.GoogleSearchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class SearchApiController(
    private val googleSearchService: GoogleSearchService,
    private val feedService: FeedService
) {
    @GetMapping("/keyword")
    fun searchByKeyword(@RequestParam keyword: String): ResponseEntity<KeywordSearchResponse> {
        if (keyword.isBlank()) return ResponseEntity.badRequest().body(
            KeywordSearchResponse("", 0, emptyList(), "키워드를 입력해주세요.")
        )
        return ResponseEntity.ok(googleSearchService.searchByKeyword(keyword.trim()))
    }

    @PostMapping("/follow")
    fun followSource(@RequestBody request: FollowSourceRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val userFeed = feedService.followFeed(FollowRequest(
                feedUrl = request.feedUrl,
                title = request.title,
                description = request.description,
                faviconUrl = request.faviconUrl,
                feedType = "RSS",
                category = null
            ))
            ResponseEntity.ok(mapOf("success" to true, "message" to "'${userFeed.feedTitle}' 팔로우 완료!"))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf("success" to false, "message" to (e.message ?: "팔로우 실패")))
        }
    }
}