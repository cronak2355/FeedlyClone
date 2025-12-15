package com.feedly.feedlyclonebackend.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleSearchResponse(
    val items: List<GoogleSearchItem>? = null,
    val searchInformation: GoogleSearchInfo? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleSearchItem(
    val title: String? = null,
    val link: String? = null,
    val snippet: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleSearchInfo(
    val searchTime: Double? = null
)

data class PreviewArticleDto(
    val title: String,
    val link: String
)

data class RecommendedSiteDto(
    val title: String,
    val url: String,
    val feedUrl: String?,
    val description: String?,
    val faviconUrl: String?,
    val previewArticles: List<PreviewArticleDto> = emptyList(),
    val hasFeed: Boolean = false
)

data class KeywordSearchResponse(
    val keyword: String,
    val totalResults: Int,
    val sites: List<RecommendedSiteDto>,
    val message: String? = null
)

data class FollowSourceRequest(
    val websiteUrl: String,
    val feedUrl: String,
    val title: String,
    val description: String? = null,
    val faviconUrl: String? = null
)