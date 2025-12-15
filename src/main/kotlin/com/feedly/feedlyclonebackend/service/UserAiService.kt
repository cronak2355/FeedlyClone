package com.feedly.feedlyclonebackend.service
import com.feedly.feedlyclonebackend.entity.UserArticleInteraction
import com.feedly.feedlyclonebackend.repository.UserArticleInteractionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
@Service
@Transactional
class UserArticleInteractionService(
    private val repository: UserArticleInteractionRepository
) {
    // 저장(나중에 보기) 토글
    fun toggleSave(userId: Long, url: String, title: String?, description: String?, thumbnailUrl: String?, siteName: String?): Boolean {
        val interaction = getOrCreateInteraction(userId, url)
        
        interaction.isSaved = !interaction.isSaved
        interaction.savedAt = if (interaction.isSaved) LocalDateTime.now() else null
        
        // 스냅샷 정보 업데이트 (이미 있는 경우에도 최신 정보로 갱신)
        updateSnapshot(interaction, title, description, thumbnailUrl, siteName)
        
        repository.save(interaction)
        return interaction.isSaved
    }
    // 읽음 처리
    fun markAsRead(userId: Long, url: String, title: String?, description: String?, thumbnailUrl: String?, siteName: String?) {
        val interaction = getOrCreateInteraction(userId, url)
        if (!interaction.isRead) {
            interaction.isRead = true
            interaction.readAt = LocalDateTime.now()
            updateSnapshot(interaction, title, description, thumbnailUrl, siteName)
            repository.save(interaction)
        }
    }
    // 메모 저장
    fun updateMemo(userId: Long, url: String, memoContent: String, title: String?, description: String?, thumbnailUrl: String?, siteName: String?) {
        val interaction = getOrCreateInteraction(userId, url)
        interaction.memo = memoContent
        updateSnapshot(interaction, title, description, thumbnailUrl, siteName)
        repository.save(interaction)
    }
    private fun getOrCreateInteraction(userId: Long, url: String): UserArticleInteraction {
        return repository.findByUserIdAndArticleUrl(userId, url)
            ?: UserArticleInteraction(userId = userId, articleUrl = url)
    }
    private fun updateSnapshot(interaction: UserArticleInteraction, title: String?, description: String?, thumbnailUrl: String?, siteName: String?) {
        if (title != null) interaction.title = title
        if (description != null) interaction.description = description
        if (thumbnailUrl != null) interaction.thumbnailUrl = thumbnailUrl
        if (siteName != null) interaction.siteName = siteName
    }
}