package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.repository.UserArticleInteractionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saved")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class SavedApiController(
    private val userArticleInteractionRepository: UserArticleInteractionRepository
) {
    
    private val TEMPORARY_USER_ID = 1L
    
    @GetMapping
    fun getSavedArticles(): ResponseEntity<Map<String, Any>> {
        val savedArticles = userArticleInteractionRepository.findByUserIdAndIsSavedTrueOrderBySavedAtDesc(TEMPORARY_USER_ID)
        
        val articles = savedArticles.map { article ->
            mapOf(
                "url" to article.articleUrl,
                "title" to article.title,
                "description" to article.description,
                "thumbnailUrl" to article.thumbnailUrl,
                "siteName" to article.siteName,
                "savedAt" to article.savedAt,
                "memo" to article.memo
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "articles" to articles,
            "count" to articles.size
        ))
    }
}
