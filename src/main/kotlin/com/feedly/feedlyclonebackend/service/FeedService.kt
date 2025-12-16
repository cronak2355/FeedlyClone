package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.dto.*
import com.feedly.feedlyclonebackend.entity.Feed
import com.feedly.feedlyclonebackend.entity.UserFeed
import com.feedly.feedlyclonebackend.repository.FeedItemRepository
import com.feedly.feedlyclonebackend.repository.FeedRepository
import com.feedly.feedlyclonebackend.repository.PopularFeedRepository
import com.feedly.feedlyclonebackend.repository.UserFeedRepository
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class FeedService(
    private val userFeedRepository: UserFeedRepository,
    private val popularFeedRepository: PopularFeedRepository,
    private val newsApiService: NewsApiService,
    private val feedRepository: FeedRepository,
    private val feedItemRepository: FeedItemRepository
) {
    private val logger = LoggerFactory.getLogger(FeedService::class.java)

    companion object {
        private const val DEFAULT_USER_ID = 1L
        private const val CONNECTION_TIMEOUT = 10000
        private const val READ_TIMEOUT = 15000
        private const val MAX_ITEMS_PREVIEW = 5
        private const val USER_AGENT = "Mozilla/5.0 (compatible; FeedlyClone/1.0; +https://feedly.clone)"

        // Reddit RSS URL 패턴
        private const val REDDIT_RSS_TEMPLATE = "https://www.reddit.com/r/%s/.rss"
        private const val REDDIT_ICON_URL = "https://www.redditstatic.com/desktop2x/img/favicon/android-icon-192x192.png"
    }
    fun getFeedsByCompany(companyId: Long): List<Feed> {
        return feedRepository.findByCompanyId(companyId)
    }
    fun feedItems(feedId: Long): List<FeedItemDto> {
        return feedItemRepository
            .findByFeedIdOrderByPublishedAtDesc(feedId)
            .map { it.toDto() }
    }

    fun getTodayMePosts(userId: Long): List<FeedItemDto> {
        val since = LocalDateTime.now().minusDays(30)
        return feedItemRepository
            .findTodayItems(userId, since)
            .map { it.toDto() }
    }

    fun markAsRead(postId: Long) {
        val item = feedItemRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found") }

        item.isRead = true
        feedItemRepository.save(item)
    }

    /**
     * 주제/키워드로 피드 검색 (NewsAPI + DB 통합)
     */
    fun searchFeeds(query: String): DiscoverResult {
        logger.debug("Searching feeds for query: $query")

        return try {
            val followedUrls = userFeedRepository.findByUserId(DEFAULT_USER_ID)
                .map { it.feedUrl }
                .toSet()

            // 1. NewsAPI에서 소스 검색
            val newsApiSources = newsApiService.searchSources(query)
            val newsApiFeeds = newsApiSources.map { source ->
                source.toDiscoveredFeed(followedUrls.contains(source.url))
            }
            logger.debug("Found ${newsApiFeeds.size} sources from NewsAPI")

            // 2. DB에서 인기 피드 검색
            val dbFeeds = popularFeedRepository.searchByQuery(query)
            val dbDiscoveredFeeds = dbFeeds.map { feed ->
                DiscoveredFeed(
                    feedUrl = feed.feedUrl,
                    siteUrl = feed.siteUrl ?: extractSiteUrl(feed.feedUrl),
                    title = feed.title,
                    description = feed.description,
                    faviconUrl = feed.faviconUrl ?: getFaviconUrl(feed.siteUrl ?: feed.feedUrl),
                    category = feed.category,
                    subscriberCount = feed.subscriberCount,
                    isFollowed = followedUrls.contains(feed.feedUrl)
                )
            }
            logger.debug("Found ${dbDiscoveredFeeds.size} feeds in database")

            // 3. 통합 (중복 제거)
            val allFeeds = (newsApiFeeds + dbDiscoveredFeeds)
                .distinctBy { it.feedUrl }

            DiscoverResult(
                query = query,
                feeds = allFeeds,
                totalCount = allFeeds.size
            )
        } catch (e: Exception) {
            logger.error("Error searching feeds: ${e.message}", e)
            DiscoverResult(query = query, message = "검색 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * NewsAPI 소스만 검색
     */
    fun searchNewsApiSources(query: String, category: String? = null): List<DiscoveredFeed> {
        val followedUrls = userFeedRepository.findByUserId(DEFAULT_USER_ID)
            .map { it.feedUrl }
            .toSet()

        val sources = newsApiService.searchSources(query, category)
        return sources.map { it.toDiscoveredFeed(followedUrls.contains(it.url)) }
    }

    /**
     * NewsAPI 카테고리별 소스 조회
     */
    fun getNewsApiSourcesByCategory(category: String?): List<DiscoveredFeed> {
        val followedUrls = userFeedRepository.findByUserId(DEFAULT_USER_ID)
            .map { it.feedUrl }
            .toSet()

        val sources = if (category.isNullOrBlank()) {
            newsApiService.getSources()
        } else {
            newsApiService.getSourcesByCategory(category)
        }

        return sources.map { it.toDiscoveredFeed(followedUrls.contains(it.url)) }
    }

    /**
     * NewsAPI 헤드라인 뉴스 조회
     */
    fun getTopHeadlines(
        country: String? = "us",
        category: String? = null,
        query: String? = null
    ): List<FeedItem> {
        val articles = newsApiService.getTopHeadlines(
            country = country,
            category = category,
            query = query
        )
        return articles.map { it.toFeedItem() }
    }

    /**
     * 특정 소스의 최신 기사 조회
     */
    fun getArticlesBySource(sourceId: String): List<FeedItem> {
        val articles = newsApiService.getArticlesBySource(sourceId)
        return articles.map { it.toFeedItem() }
    }

    /**
     * NewsAPI 기사 검색
     */
    fun searchArticles(query: String): List<FeedItem> {
        val articles = newsApiService.searchArticles(query)
        return articles.map { it.toFeedItem() }
    }

    /**
     * 카테고리별 피드 조회 (NewsAPI + DB 통합)
     */
    fun getFeedsByCategory(category: String?): List<DiscoveredFeed> {
        val followedUrls = userFeedRepository.findByUserId(DEFAULT_USER_ID)
            .map { it.feedUrl }
            .toSet()

        // 1. NewsAPI 소스
        val newsApiFeeds = getNewsApiSourcesByCategory(category?.lowercase())

        // 2. DB 피드
        val dbFeeds = if (category.isNullOrBlank()) {
            popularFeedRepository.findAllByOrderBySubscriberCountDesc()
        } else {
            popularFeedRepository.findByCategoryOrderBySubscriberCountDesc(category)
        }

        val dbDiscoveredFeeds = dbFeeds.map { feed ->
            DiscoveredFeed(
                feedUrl = feed.feedUrl,
                siteUrl = feed.siteUrl ?: extractSiteUrl(feed.feedUrl),
                title = feed.title,
                description = feed.description,
                faviconUrl = feed.faviconUrl ?: getFaviconUrl(feed.siteUrl ?: feed.feedUrl),
                category = feed.category,
                subscriberCount = feed.subscriberCount,
                isFollowed = followedUrls.contains(feed.feedUrl)
            )
        }

        // 통합 (NewsAPI 우선, 중복 제거)
        return (newsApiFeeds + dbDiscoveredFeeds).distinctBy { it.feedUrl }
    }

    /**
     * 모든 카테고리 조회 (NewsAPI + DB 통합)
     */
    fun getAllCategories(): List<String> {
        val newsApiCategories = newsApiService.getAllCategories()
        val dbCategories = popularFeedRepository.findAllCategories()

        return (newsApiCategories + dbCategories)
            .map { it.replaceFirstChar { c -> c.uppercase() } }
            .distinct()
            .sorted()
    }

    /**
     * RSS/Atom 피드 파싱
     */
    fun parseFeed(feedUrl: String): DiscoveredFeed? {
        logger.debug("Parsing feed: $feedUrl")

        return try {
            val url = URL(feedUrl)
            val connection = url.openConnection().apply {
                connectTimeout = CONNECTION_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
            }

            val input = SyndFeedInput()
            val feed: SyndFeed = input.build(XmlReader(connection))

            val items = feed.entries.take(MAX_ITEMS_PREVIEW).map { entry ->
                FeedItem(
                    title = entry.title ?: "제목 없음",
                    link = entry.link ?: "",
                    description = entry.description?.value?.let { stripHtml(it).take(200) },
                    author = entry.author,
                    publishedDate = entry.publishedDate?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.toLocalDateTime(),
                    thumbnailUrl = extractThumbnail(entry),
                    categories = entry.categories?.mapNotNull { it.name } ?: emptyList()
                )
            }

            val siteUrl = feed.link ?: extractSiteUrl(feedUrl)
            val isFollowed = userFeedRepository.existsByUserIdAndFeedUrl(DEFAULT_USER_ID, feedUrl)

            DiscoveredFeed(
                feedUrl = feedUrl,
                siteUrl = siteUrl,
                title = feed.title ?: "Unknown Feed",
                description = feed.description?.take(300),
                faviconUrl = getFaviconUrl(siteUrl),
                feedType = if (feed.feedType?.contains("atom", ignoreCase = true) == true) "Atom" else "RSS",
                items = items,
                isFollowed = isFollowed
            ).also {
                logger.info("Successfully parsed feed: ${it.title}")
            }
        } catch (e: Exception) {
            logger.error("Failed to parse feed $feedUrl: ${e.message}")
            null
        }
    }

    /**
     * Reddit 서브레딧 RSS 파싱
     */
    fun parseRedditFeed(subreddit: String): SubredditFeed? {
        val cleanSubreddit = subreddit.removePrefix("r/").trim()
        val feedUrl = REDDIT_RSS_TEMPLATE.format(cleanSubreddit)

        logger.debug("Parsing Reddit feed: $feedUrl")

        return try {
            val url = URL(feedUrl)
            val connection = url.openConnection().apply {
                connectTimeout = CONNECTION_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("User-Agent", USER_AGENT)
            }

            val input = SyndFeedInput()
            val feed: SyndFeed = input.build(XmlReader(connection))

            val posts = feed.entries.take(MAX_ITEMS_PREVIEW).map { entry ->
                RedditPost(
                    title = entry.title ?: "제목 없음",
                    link = entry.link ?: "",
                    author = entry.author ?: "unknown",
                    subreddit = cleanSubreddit,
                    publishedDate = entry.publishedDate?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.toLocalDateTime(),
                    selfText = entry.description?.value?.let { stripHtml(it).take(300) }
                )
            }

            val isFollowed = userFeedRepository.existsByUserIdAndFeedUrl(DEFAULT_USER_ID, feedUrl)

            SubredditFeed(
                subreddit = cleanSubreddit,
                feedUrl = feedUrl,
                title = feed.title ?: "r/$cleanSubreddit",
                description = feed.description,
                iconUrl = REDDIT_ICON_URL,
                posts = posts,
                isFollowed = isFollowed
            ).also {
                logger.info("Successfully parsed Reddit feed: r/$cleanSubreddit")
            }
        } catch (e: Exception) {
            logger.error("Failed to parse Reddit feed r/$subreddit: ${e.message}")
            null
        }
    }

    /**
     * 여러 서브레딧 검색
     */
    fun searchSubreddits(query: String): List<SubredditFeed> {
        // 인기 프로그래밍 관련 서브레딧 목록
        val popularSubreddits = listOf(
            "programming", "kotlin", "java", "javascript", "python",
            "webdev", "android", "ios", "devops", "linux",
            "technology", "tech", "coding", "learnprogramming"
        )

        return popularSubreddits
            .filter { it.contains(query, ignoreCase = true) }
            .mapNotNull { parseRedditFeed(it) }
    }

    /**
     * 피드 팔로우
     */
    @Transactional
    fun followFeed(request: FollowRequest, userId: Long = DEFAULT_USER_ID): UserFeed {
        logger.debug("Following feed: ${request.feedUrl} for user: $userId")

        // 이미 팔로우 중인지 확인
        if (userFeedRepository.existsByUserIdAndFeedUrl(userId, request.feedUrl)) {
            throw IllegalStateException("이미 팔로우 중인 피드입니다")
        }

        val userFeed = UserFeed(
            userId = userId,
            feedUrl = request.feedUrl,
            feedTitle = request.title,
            feedDescription = request.description,
            feedType = request.feedType,
            faviconUrl = request.faviconUrl,
            category = request.category
        )

        return userFeedRepository.save(userFeed).also {
            logger.info("Successfully followed feed: ${it.feedTitle}")

            // TODO: 주기적 피드 업데이트 스케줄러에 등록
            // scheduleFeedUpdate(it)
        }
    }

    /**
     * 피드 언팔로우
     */
    @Transactional
    fun unfollowFeed(feedUrl: String, userId: Long = DEFAULT_USER_ID): Boolean {
        logger.debug("Unfollowing feed: $feedUrl for user: $userId")

        val deleted = userFeedRepository.deleteByUserIdAndFeedUrl(userId, feedUrl)

        return (deleted > 0).also {
            if (it) logger.info("Successfully unfollowed feed: $feedUrl")
            else logger.warn("Feed not found for unfollow: $feedUrl")
        }
    }

    /**
     * 사용자가 팔로우한 피드 목록 조회
     */
    @Transactional(readOnly = true)
    fun getFollowedFeeds(userId: Long = DEFAULT_USER_ID): List<UserFeed> {
        return userFeedRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    /**
     * 피드 팔로우 여부 확인
     */
    @Transactional(readOnly = true)
    fun isFollowing(feedUrl: String, userId: Long = DEFAULT_USER_ID): Boolean {
        return userFeedRepository.existsByUserIdAndFeedUrl(userId, feedUrl)
    }

    // === Private Helper Methods ===

    private fun extractSiteUrl(feedUrl: String): String {
        return try {
            val uri = URI(feedUrl)
            "${uri.scheme}://${uri.host}"
        } catch (e: Exception) {
            feedUrl
        }
    }

    private fun getFaviconUrl(siteUrl: String): String {
        return try {
            val uri = URI(siteUrl)
            "https://www.google.com/s2/favicons?domain=${uri.host}&sz=64"
        } catch (e: Exception) {
            ""
        }
    }

    private fun stripHtml(html: String): String {
        return try {
            Jsoup.parse(html).text()
        } catch (e: Exception) {
            html.replace(Regex("<[^>]*>"), "")
        }
    }

    private fun extractThumbnail(entry: com.rometools.rome.feed.synd.SyndEntry): String? {
        // 미디어 모듈에서 썸네일 추출 시도
        return entry.enclosures?.firstOrNull {
            it.type?.startsWith("image") == true
        }?.url
    }

    // === 주기적 피드 업데이트 (주석 처리 - 추후 구현) ===

    /**
     * TODO: 피드 어그리게이터 - 주기적 업데이트 로직
     *
     * @Scheduled(fixedRate = 300000) // 5분마다
     * fun updateAllFeeds() {
     *     val activeFeeds = userFeedRepository.findAllByIsActiveTrue()
     *     activeFeeds.forEach { feed ->
     *         try {
     *             val parsed = parseFeed(feed.feedUrl)
     *             // 새 아이템 저장 로직
     *             feed.lastFetchedAt = LocalDateTime.now()
     *             userFeedRepository.save(feed)
     *         } catch (e: Exception) {
     *             logger.error("Failed to update feed: ${feed.feedUrl}", e)
     *         }
     *     }
     * }
     */
}