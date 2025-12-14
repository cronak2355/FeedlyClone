package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.dto.ApiResponse
import com.feedly.feedlyclonebackend.dto.FollowRequest
import com.feedly.feedlyclonebackend.service.FeedService
import com.feedly.feedlyclonebackend.service.NewsApiService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/discover")
class DiscoverController(
    private val feedService: FeedService,
    private val newsApiService: NewsApiService
) {
    private val logger = LoggerFactory.getLogger(DiscoverController::class.java)

    /**
     * Discover 메인 페이지 (Explore 탭)
     */
    @GetMapping
    fun discoverPage(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) source: String?,
        model: Model
    ): String {
        logger.debug("Discover page - query: $query, category: $category, source: $source")

        // 카테고리 목록 (NewsAPI + DB 통합)
        val categories = feedService.getAllCategories()
        model.addAttribute("categories", categories)
        model.addAttribute("selectedCategory", category ?: "")
        model.addAttribute("query", query ?: "")
        model.addAttribute("source", source ?: "all")

        // 검색 또는 카테고리 필터
        val feeds = when {
            !query.isNullOrBlank() -> {
                val result = feedService.searchFeeds(query)
                model.addAttribute("searchResult", result)
                result.feeds
            }
            source == "newsapi" -> {
                // NewsAPI만 조회
                feedService.getNewsApiSourcesByCategory(category?.lowercase())
            }
            else -> {
                // 전체 (NewsAPI + DB)
                feedService.getFeedsByCategory(category)
            }
        }

        model.addAttribute("feeds", feeds)
        model.addAttribute("feedCount", feeds.size)

        // 헤드라인 뉴스 (메인 페이지에 표시)
        if (query.isNullOrBlank()) {
            val headlines = feedService.getTopHeadlines(category = category?.lowercase())
            model.addAttribute("headlines", headlines.take(5))
        }

        return "discover"
    }

    /**
     * 뉴스 페이지 (NewsAPI 기사 검색)
     */
    @GetMapping("/news")
    fun newsPage(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false, defaultValue = "us") country: String,
        model: Model
    ): String {
        logger.debug("News page - query: $query, category: $category, country: $country")

        model.addAttribute("query", query ?: "")
        model.addAttribute("selectedCategory", category ?: "")
        model.addAttribute("selectedCountry", country)
        model.addAttribute("categories", newsApiService.getAllCategories())
        model.addAttribute("countries", NewsApiService.COUNTRIES)

        // 기사 조회
        val articles = when {
            !query.isNullOrBlank() -> feedService.searchArticles(query)
            else -> feedService.getTopHeadlines(country = country, category = category?.lowercase())
        }

        model.addAttribute("articles", articles)
        model.addAttribute("articleCount", articles.size)

        return "news"
    }

    /**
     * Reddit 서브레딧 검색 페이지
     */
    @GetMapping("/reddit")
    fun redditPage(
        @RequestParam(required = false) query: String?,
        model: Model
    ): String {
        logger.debug("Reddit search page - query: $query")

        model.addAttribute("query", query ?: "")

        if (!query.isNullOrBlank()) {
            val subredditFeed = feedService.parseRedditFeed(query)
            
            if (subredditFeed != null) {
                model.addAttribute("subreddit", subredditFeed)
                model.addAttribute("found", true)
            } else {
                model.addAttribute("found", false)
                model.addAttribute("errorMessage", "서브레딧 'r/$query'를 찾을 수 없습니다.")
            }

            // 추천 서브레딧
            val suggestions = feedService.searchSubreddits(query)
                .filter { it.subreddit != query }
                .take(4)
            model.addAttribute("suggestions", suggestions)
        }

        return "reddit-search"
    }

    /**
     * 피드 팔로우 (AJAX)
     */
    @PostMapping("/follow")
    @ResponseBody
    fun followFeed(@RequestBody request: FollowRequest): ResponseEntity<ApiResponse<Any>> {
        logger.debug("Follow request: ${request.feedUrl}")

        return try {
            val userFeed = feedService.followFeed(request)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "'${userFeed.feedTitle ?: request.title}'을(를) 팔로우했습니다!",
                    data = mapOf("id" to userFeed.id)
                )
            )
        } catch (e: IllegalStateException) {
            ResponseEntity.ok(
                ApiResponse(
                    success = false,
                    message = e.message ?: "팔로우에 실패했습니다."
                )
            )
        } catch (e: Exception) {
            logger.error("Follow error: ${e.message}", e)
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = "오류가 발생했습니다: ${e.message}"
                )
            )
        }
    }

    /**
     * 피드 언팔로우 (AJAX)
     */
    @PostMapping("/unfollow")
    @ResponseBody
    fun unfollowFeed(@RequestBody request: Map<String, String>): ResponseEntity<ApiResponse<Any>> {
        val feedUrl = request["feedUrl"] ?: return ResponseEntity.badRequest().body(
            ApiResponse(success = false, message = "feedUrl이 필요합니다.")
        )

        logger.debug("Unfollow request: $feedUrl")

        return try {
            val success = feedService.unfollowFeed(feedUrl)
            if (success) {
                ResponseEntity.ok(ApiResponse(success = true, message = "언팔로우했습니다."))
            } else {
                ResponseEntity.ok(ApiResponse(success = false, message = "팔로우 중인 피드가 아닙니다."))
            }
        } catch (e: Exception) {
            logger.error("Unfollow error: ${e.message}", e)
            ResponseEntity.badRequest().body(
                ApiResponse(success = false, message = "오류가 발생했습니다: ${e.message}")
            )
        }
    }

    /**
     * 피드 미리보기 (AJAX)
     */
    @GetMapping("/preview")
    @ResponseBody
    fun previewFeed(@RequestParam feedUrl: String): ResponseEntity<ApiResponse<Any>> {
        logger.debug("Preview request: $feedUrl")

        return try {
            val feed = feedService.parseFeed(feedUrl)
            if (feed != null) {
                ResponseEntity.ok(ApiResponse(success = true, message = "성공", data = feed))
            } else {
                ResponseEntity.ok(ApiResponse(success = false, message = "피드를 파싱할 수 없습니다."))
            }
        } catch (e: Exception) {
            logger.error("Preview error: ${e.message}", e)
            ResponseEntity.badRequest().body(
                ApiResponse(success = false, message = "오류: ${e.message}")
            )
        }
    }

    /**
     * 특정 소스의 기사 조회 (AJAX)
     */
    @GetMapping("/articles")
    @ResponseBody
    fun getArticles(@RequestParam sourceId: String): ResponseEntity<ApiResponse<Any>> {
        logger.debug("Get articles for source: $sourceId")

        return try {
            val articles = feedService.getArticlesBySource(sourceId)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "${articles.size}개의 기사를 찾았습니다.",
                    data = articles
                )
            )
        } catch (e: Exception) {
            logger.error("Get articles error: ${e.message}", e)
            ResponseEntity.badRequest().body(
                ApiResponse(success = false, message = "오류: ${e.message}")
            )
        }
    }

    /**
     * 팔로우한 피드 목록 조회 (AJAX)
     */
    @GetMapping("/following")
    @ResponseBody
    fun getFollowingFeeds(): ResponseEntity<ApiResponse<Any>> {
        return try {
            val feeds = feedService.getFollowedFeeds()
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "${feeds.size}개의 피드를 팔로우 중",
                    data = feeds
                )
            )
        } catch (e: Exception) {
            logger.error("Get following error: ${e.message}", e)
            ResponseEntity.badRequest().body(
                ApiResponse(success = false, message = "오류: ${e.message}")
            )
        }
    }
}
