package com.feedly.feedlyclonebackend.entity

data class NewsApiResponse(
    val articles: List<NewsArticleDto>
)

data class NewsArticleDto(
    val title: String,
    val url: String,
    val source: NewsSourceDto
)

data class NewsSourceDto(
    val name: String
)

data class NewsDto(
    val title: String,
    val url: String,
    val source: String
)
