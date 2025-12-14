package com.feedly.feedlyclonebackend.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * NewsAPI 응답 DTO들
 * https://newsapi.org/docs/endpoints
 */

// === Sources 엔드포인트 응답 ===

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiSourcesResponse(
    val status: String,
    val sources: List<NewsApiSource> = emptyList(),
    val code: String? = null,
    val message: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiSource(
    val id: String?,
    val name: String,
    val description: String?,
    val url: String?,
    val category: String?,
    val language: String?,
    val country: String?
)

// === Everything / Top Headlines 엔드포인트 응답 ===

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiArticlesResponse(
    val status: String,
    val totalResults: Int = 0,
    val articles: List<NewsApiArticle> = emptyList(),
    val code: String? = null,
    val message: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiArticle(
    val source: NewsApiArticleSource?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiArticleSource(
    val id: String?,
    val name: String?
)

// === 내부 변환용 DTO ===

/**
 * NewsAPI 소스를 내부 DiscoveredFeed로 변환
 */
fun NewsApiSource.toDiscoveredFeed(isFollowed: Boolean = false): DiscoveredFeed {
    return DiscoveredFeed(
        feedUrl = this.url ?: "",
        siteUrl = this.url ?: "",
        title = this.name,
        description = this.description,
        faviconUrl = this.url?.let { extractFaviconUrl(it) },
        category = this.category?.replaceFirstChar { it.uppercase() },
        feedType = "NewsAPI",
        subscriberCount = 0,
        isFollowed = isFollowed
    )
}

/**
 * NewsAPI 기사를 내부 FeedItem으로 변환
 */
fun NewsApiArticle.toFeedItem(): FeedItem {
    return FeedItem(
        title = this.title ?: "제목 없음",
        link = this.url ?: "",
        description = this.description,
        author = this.author,
        publishedDate = this.publishedAt?.let { parseNewsApiDate(it) },
        thumbnailUrl = this.urlToImage,
        categories = emptyList()
    )
}

/**
 * URL에서 favicon URL 추출
 */
private fun extractFaviconUrl(url: String): String {
    return try {
        val uri = java.net.URI(url)
        "https://www.google.com/s2/favicons?domain=${uri.host}&sz=64"
    } catch (e: Exception) {
        ""
    }
}

/**
 * NewsAPI 날짜 문자열 파싱 (ISO 8601)
 */
private fun parseNewsApiDate(dateStr: String): java.time.LocalDateTime? {
    return try {
        java.time.ZonedDateTime.parse(dateStr).toLocalDateTime()
    } catch (e: Exception) {
        try {
            java.time.LocalDateTime.parse(dateStr.replace("Z", ""))
        } catch (e2: Exception) {
            null
        }
    }
}
