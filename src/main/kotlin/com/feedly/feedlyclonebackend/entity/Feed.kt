package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "feeds")
class Feed(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val companyId: Long,

    @Column(nullable = false, length = 500, unique = true)
    val url: String,

    @Column(nullable = true, length = 300)
    val title: String? = null,

    @Column(nullable = true, length = 500)
    val description: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
