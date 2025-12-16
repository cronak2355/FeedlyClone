package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.dto.*
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.net.URL

@Service
class GoogleSearchService {

    private val logger = LoggerFactory.getLogger(GoogleSearchService::class.java)
    private val restTemplate = RestTemplate()

    @Value("\${google.search.api-key}") private lateinit var apiKey: String
    @Value("\${google.search.cx}") private lateinit var searchEngineId: String
    @Value("\${google.search.base-url}") private lateinit var baseUrl: String

    private val feedPaths = listOf("/feed", "/rss", "/rss.xml", "/feed.xml", "/atom.xml", "/index.xml")

    fun searchByKeyword(keyword: String): KeywordSearchResponse {
        logger.info("=== 검색 시작: $keyword ===")

        val url = "$baseUrl?key=$apiKey&cx=$searchEngineId&q=${keyword.encodeUrl()}&num=10"

        return try {
            val response = restTemplate.getForObject(url, GoogleSearchResponse::class.java)

            logger.info("Google API 응답: items=${response?.items?.size ?: 0}개")

            val searchItems = response?.items

            if (searchItems.isNullOrEmpty()) {
                return KeywordSearchResponse(keyword, 0, emptyList(), "검색 결과가 없습니다.")
            }

            val sites = response.items
                .distinctBy {
                    try { URI(it.link ?: "").host } catch (e: Exception) { it.link }
                }
                .take(8)
                .map { item -> processItem(item) }

            logger.info("최종 결과: ${sites.size}개 사이트")
            KeywordSearchResponse(keyword, sites.size, sites)

        } catch (e: Exception) {
            logger.error("Google API 호출 실패: ${e.message}", e)
            KeywordSearchResponse(keyword, 0, emptyList(), "검색 중 오류: ${e.message}")
        }
    }

    private fun processItem(item: GoogleSearchItem): RecommendedSiteDto {
        val siteUrl = item.link ?: ""
        val siteBaseUrl = try { val uri = URI(siteUrl); "${uri.scheme}://${uri.host}" } catch (e: Exception) { siteUrl }
        val domain = try { URI(siteUrl).host ?: "" } catch (e: Exception) { "" }

        val feedResult = discoverFeed(siteBaseUrl)

        return RecommendedSiteDto(
            title = item.title ?: domain,
            url = siteUrl,
            feedUrl = feedResult?.first,
            description = item.snippet?.take(150),
            faviconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=64",
            previewArticles = feedResult?.second ?: emptyList(),
            hasFeed = feedResult?.first != null
        )
    }

    private fun discoverFeed(baseUrl: String): Pair<String, List<PreviewArticleDto>>? {
        try {
            val doc = Jsoup.connect(baseUrl).timeout(5000).get()
            doc.select("link[rel=alternate][type*=rss], link[rel=alternate][type*=atom]").forEach {
                val href = it.attr("abs:href")
                if (href.isNotBlank()) {
                    tryParseFeed(href)?.let { articles -> return Pair(href, articles) }
                }
            }
        } catch (e: Exception) {}

        for (path in feedPaths) {
            val feedUrl = baseUrl + path
            tryParseFeed(feedUrl)?.let { return Pair(feedUrl, it) }
        }
        return null
    }

    private fun tryParseFeed(feedUrl: String): List<PreviewArticleDto>? {
        return try {
            val conn = URL(feedUrl).openConnection().apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            val feed = SyndFeedInput().build(XmlReader(conn))
            feed.entries.take(3).map { PreviewArticleDto(it.title ?: "", it.link ?: "") }
        } catch (e: Exception) { null }
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")
}