package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.entity.NewsArticleDto
import com.feedly.feedlyclonebackend.service.NewsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class NewsTestController(
    private val newsService: NewsService
) {

    @GetMapping("/search")
    fun searchNews(
        @RequestParam keyword: String
    ): Any {
        return newsService.searchNews(keyword)
    }
}
