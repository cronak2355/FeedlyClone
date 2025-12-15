package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.http.MediaType
import org.springframework.cache.annotation.Cacheable

/**
 * NewsAPI 연동 서비스
 * https://newsapi.org/docs
 */
@Service
class NewsApiService(
    @Value("\${newsapi.key}") private val apiKey: String,
    @Value("\${newsapi.base-url}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(NewsApiService::class.java)
    
    private val restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("X-Api-Key", apiKey)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build()

    companion object {
        // NewsAPI 카테고리 목록
        val CATEGORIES = listOf(
            "business", "entertainment", "general", 
            "health", "science", "sports", "technology"
        )
        
        // 지원 국가 (일부)
        val COUNTRIES = mapOf(
            "us" to "미국",
            "gb" to "영국",
            "kr" to "한국",
            "jp" to "일본",
            "de" to "독일",
            "fr" to "프랑스"
        )
    }

    /**
     * 뉴스 소스 목록 조회
     * GET /v2/sources
     */
    fun getSources(
        category: String? = null,
        language: String? = null,
        country: String? = null
    ): List<NewsApiSource> {
        logger.debug("Fetching news sources - category: $category, language: $language, country: $country")
        
        return try {
            val response = restClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/sources")
                    category?.let { uriBuilder.queryParam("category", it) }
                    language?.let { uriBuilder.queryParam("language", it) }
                    country?.let { uriBuilder.queryParam("country", it) }
                    uriBuilder.build()
                }
                .retrieve()
                .body(NewsApiSourcesResponse::class.java)

            if (response?.status == "ok") {
                logger.info("Found ${response.sources.size} sources")
                response.sources
            } else {
                logger.warn("NewsAPI error: ${response?.message}")
                emptyList()
            }
        } catch (e: RestClientException) {
            logger.error("Failed to fetch sources: ${e.message}")
            emptyList()
        }
    }

    /**
     * 소스 검색 (이름/설명으로 필터링)
     */
    fun searchSources(query: String, category: String? = null): List<NewsApiSource> {
        val allSources = getSources(category = category)
        
        if (query.isBlank()) return allSources
        
        val lowerQuery = query.lowercase()
        return allSources.filter { source ->
            source.name.lowercase().contains(lowerQuery) ||
            source.description?.lowercase()?.contains(lowerQuery) == true ||
            source.category?.lowercase()?.contains(lowerQuery) == true
        }
    }

    /**
     * 헤드라인 뉴스 조회
     * GET /v2/top-headlines
     */
    @Cacheable(value = ["newsHeadlines"], key = "{#country, #category, #sources, #query, #pageSize}", unless = "#result.isEmpty()")
    fun getTopHeadlines(
        country: String? = "us",
        category: String? = null,
        sources: String? = null,
        query: String? = null,
        pageSize: Int = 10
    ): List<NewsApiArticle> {
        logger.debug("Fetching top headlines - country: $country, category: $category, query: $query")
        
        return try {
            val response = restClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/top-headlines")
                    // sources와 country/category는 함께 사용 불가
                    if (sources != null) {
                        uriBuilder.queryParam("sources", sources)
                    } else {
                        country?.let { uriBuilder.queryParam("country", it) }
                        category?.let { uriBuilder.queryParam("category", it) }
                    }
                    query?.let { uriBuilder.queryParam("q", it) }
                    uriBuilder.queryParam("pageSize", pageSize)
                    uriBuilder.build()
                }
                .retrieve()
                .body(NewsApiArticlesResponse::class.java)

            if (response?.status == "ok") {
                logger.info("Found ${response.articles.size} headlines")
                response.articles
            } else {
                logger.warn("NewsAPI error: ${response?.message}")
                emptyList()
            }
        } catch (e: RestClientException) {
            logger.error("Failed to fetch headlines: ${e.message}")
            emptyList()
        }
    }

    /**
     * 기사 검색
     * GET /v2/everything
     */
    @Cacheable(value = ["newsSearch"], key = "{#query, #sources, #domains, #language, #sortBy, #pageSize}", unless = "#result.isEmpty()")
    fun searchArticles(
        query: String,
        sources: String? = null,
        domains: String? = null,
        from: String? = null,
        to: String? = null,
        language: String? = "en",
        sortBy: String? = "publishedAt",
        pageSize: Int = 10
    ): List<NewsApiArticle> {
        logger.debug("Searching articles - query: $query, sources: $sources")
        
        return try {
            val response = restClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/everything")
                    uriBuilder.queryParam("q", query)
                    sources?.let { uriBuilder.queryParam("sources", it) }
                    domains?.let { uriBuilder.queryParam("domains", it) }
                    from?.let { uriBuilder.queryParam("from", it) }
                    to?.let { uriBuilder.queryParam("to", it) }
                    language?.let { uriBuilder.queryParam("language", it) }
                    sortBy?.let { uriBuilder.queryParam("sortBy", it) }
                    uriBuilder.queryParam("pageSize", pageSize)
                    uriBuilder.build()
                }
                .retrieve()
                .body(NewsApiArticlesResponse::class.java)

            if (response?.status == "ok") {
                logger.info("Found ${response.articles.size} articles for query: $query")
                response.articles
            } else {
                logger.warn("NewsAPI error: ${response?.message}")
                emptyList()
            }
        } catch (e: RestClientException) {
            logger.error("Failed to search articles: ${e.message}")
            emptyList()
        }
    }

    /**
     * 특정 소스의 최신 기사 조회
     */
    fun getArticlesBySource(sourceId: String, pageSize: Int = 5): List<NewsApiArticle> {
        return getTopHeadlines(sources = sourceId, pageSize = pageSize)
    }

    /**
     * 카테고리별 뉴스 소스 조회
     */
    fun getSourcesByCategory(category: String): List<NewsApiSource> {
        return getSources(category = category.lowercase())
    }

    /**
     * 모든 카테고리 조회
     */
    fun getAllCategories(): List<String> {
        return CATEGORIES.map { it.replaceFirstChar { c -> c.uppercase() } }
    }
}
