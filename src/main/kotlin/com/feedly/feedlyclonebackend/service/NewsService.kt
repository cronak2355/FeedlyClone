package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.entity.NewsApiResponse
import com.feedly.feedlyclonebackend.entity.NewsArticleDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class NewsService(
    private val restTemplate: RestTemplate,
    @Value("\${newsapi.key}")
    private val apiKey: String
) {

    fun searchNews(keyword: String): List<NewsArticleDto> {
        val url = "https://newsapi.org/v2/everything" +
                "?q=$keyword" +
                "&language=ko" +
                "&apiKey=$apiKey"

        val response = restTemplate.getForObject(
            url,
            NewsApiResponse::class.java
        )

        return response?.articles ?: emptyList()
    }
}