package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.repository.UserArticleInteractionRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SavedArticlesController(
    private val userArticleInteractionRepository: UserArticleInteractionRepository
) {

    // 임시 사용자 ID (나중에 Security Context에서 가져와야 함)
    private val TEMPORARY_USER_ID = 1L

    @GetMapping("/saved")
    fun savedPage(model: Model): String {
        val savedArticles = userArticleInteractionRepository.findByUserIdAndIsSavedTrueOrderBySavedAtDesc(TEMPORARY_USER_ID)
        
        // 읽은 기사 수 (Reviewed count용)
        val readCount = userArticleInteractionRepository.findByUserIdAndIsReadTrueOrderByReadAtDesc(TEMPORARY_USER_ID).size

        model.addAttribute("savedArticles", savedArticles)
        model.addAttribute("readCount", readCount)
        model.addAttribute("selectedCategory", "saved") // 사이드바 활성화용 (필요시)
        
        return "saved"
    }
}
