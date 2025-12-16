package com.feedly.feedlyclonebackend.dto

import java.time.LocalDateTime

/**
 * RSS/Atom 피드의 개별 아이템(기사)을 나타내는 DTO
 */
data class FeedItem(
    val title: String,
    val link: String,
    val description: String? = null,
    val author: String? = null,
    val publishedDate: LocalDateTime? = null,
    val thumbnailUrl: String? = null,
    val categories: List<String> = emptyList(),
    val sourceName: String? = null  // 추가
)

/**
 * 탐지/검색된 피드 소스 정보
 */
data class DiscoveredFeed(
    val feedUrl: String,
    val siteUrl: String,
    val title: String,
    val description: String? = null,
    val faviconUrl: String? = null,
    val category: String? = null,
    val feedType: String = "RSS",
    val subscriberCount: Int = 0,
    val items: List<FeedItem> = emptyList(),
    val isFollowed: Boolean = false
)

/**
 * Reddit 포스트 정보
 */
data class RedditPost(
    val title: String,
    val link: String,
    val author: String,
    val subreddit: String,
    val score: Int = 0,
    val commentCount: Int = 0,
    val publishedDate: LocalDateTime? = null,
    val thumbnailUrl: String? = null,
    val selfText: String? = null,
    val isNsfw: Boolean = false
)

/**
 * Reddit 서브레딧 피드 정보
 */
data class SubredditFeed(
    val subreddit: String,
    val feedUrl: String,
    val title: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val subscriberCount: Int = 0,
    val posts: List<RedditPost> = emptyList(),
    val isFollowed: Boolean = false
)

/**
 * 검색 결과 응답
 */
data class DiscoverResult(
    val query: String,
    val feeds: List<DiscoveredFeed> = emptyList(),
    val totalCount: Int = 0,
    val message: String? = null
)

/**
 * Follow 요청 DTO
 */
data class FollowRequest(
    val feedUrl: String,
    val title: String? = null,
    val description: String? = null,
    val faviconUrl: String? = null,
    val category: String? = null,
    val feedType: String = "RSS"
)

/**
 * API 응답 DTO
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
