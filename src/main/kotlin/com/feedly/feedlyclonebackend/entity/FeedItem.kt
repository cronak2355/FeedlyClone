package com.feedly.feedlyclonebackend.entity

import java.time.LocalDateTime

data class Article(
    val id: Long,
    val feedId: Long,
    val title: String,
    val url: String,
    val publishedAt: LocalDateTime?
)