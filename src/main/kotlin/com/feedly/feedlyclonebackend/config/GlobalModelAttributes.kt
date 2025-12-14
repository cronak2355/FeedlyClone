package com.feedly.feedlyclonebackend.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * 모든 컨트롤러에 공통 모델 속성을 추가하는 ControllerAdvice
 * Thymeleaf 3.1+에서 #request 객체가 기본 비활성화되어 이 방식 사용
 */
@ControllerAdvice
class GlobalModelAttributes {

    /**
     * 현재 요청 URI를 모델에 추가
     * navbar에서 active 클래스 처리에 사용
     */
    @ModelAttribute("currentUri")
    fun currentUri(request: HttpServletRequest): String {
        return request.requestURI
    }

    /**
     * 현재 페이지 식별자 (더 세밀한 제어가 필요할 때 사용)
     */
    @ModelAttribute("currentPath")
    fun currentPath(request: HttpServletRequest): String {
        val uri = request.requestURI
        return when {
            uri == "/discover" -> "discover"
            uri.startsWith("/discover/news") -> "news"
            uri.startsWith("/discover/reddit") -> "reddit"
            uri.startsWith("/discover") -> "discover"
            else -> ""
        }
    }
}
