package com.feedly.feedlyclonebackend.controller
import com.feedly.feedlyclonebackend.service.UserArticleInteractionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api/articles")
class ArticleInteractionController(
    private val service: UserArticleInteractionService
) {
    // TODO: 실제 구현 시 @AuthenticationPrincipal 등을 통해 userId를 가져와야 함.
    // 현재는 임시로 userId = 1L 고정
    private val TEMPORARY_USER_ID = 1L
    data class ArticleRequest(
        val url: String,
        val title: String? = null,
        val description: String? = null,
        val thumbnailUrl: String? = null,
        val siteName: String? = null
    )
    
    data class MemoRequest(
        val url: String,
        val memo: String,
        val title: String? = null,
        val description: String? = null,
        val thumbnailUrl: String? = null,
        val siteName: String? = null
    )

    @PostMapping("/save")
    fun toggleSave(@RequestBody request: ArticleRequest): ResponseEntity<Map<String, Any>> {
        val isSaved = service.toggleSave(
            TEMPORARY_USER_ID, 
            request.url, 
            request.title, 
            request.description, 
            request.thumbnailUrl, 
            request.siteName
        )
        return ResponseEntity.ok(mapOf("success" to true, "isSaved" to isSaved))
    }
    @PostMapping("/read")
    fun markAsRead(@RequestBody request: ArticleRequest): ResponseEntity<Map<String, Any>> {
        service.markAsRead(
            TEMPORARY_USER_ID, 
            request.url, 
            request.title, 
            request.description, 
            request.thumbnailUrl, 
            request.siteName
        )
        return ResponseEntity.ok(mapOf("success" to true))
    }
    
    @PostMapping("/memo")
    fun updateMemo(@RequestBody request: MemoRequest): ResponseEntity<Map<String, Any>> {
        service.updateMemo(
            TEMPORARY_USER_ID, 
            request.url, 
            request.memo, 
            request.title, 
            request.description, 
            request.thumbnailUrl, 
            request.siteName
        )
        return ResponseEntity.ok(mapOf("success" to true))
    }
}