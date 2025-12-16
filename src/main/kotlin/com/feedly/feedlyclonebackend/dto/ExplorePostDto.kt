package com.feedly.feedlyclonebackend.dto

import java.time.LocalDateTime

data class ExplorePostDto(
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val source: String,
    val publishedAt: LocalDateTime
)
