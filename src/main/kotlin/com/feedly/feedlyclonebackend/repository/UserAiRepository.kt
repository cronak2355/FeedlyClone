package com.feedly.feedlyclonebackend.repository
import com.feedly.feedlyclonebackend.entity.UserArticleInteraction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
@Repository
interface UserArticleInteractionRepository : JpaRepository<UserArticleInteraction, Long> {
    fun findByUserIdAndArticleUrl(userId: Long, articleUrl: String): UserArticleInteraction?
    fun findByUserIdAndIsSavedTrueOrderBySavedAtDesc(userId: Long): List<UserArticleInteraction>
    fun findByUserIdAndIsReadTrueOrderByReadAtDesc(userId: Long): List<UserArticleInteraction>
}