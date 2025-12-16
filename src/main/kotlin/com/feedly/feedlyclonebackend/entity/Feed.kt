package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.*
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import java.time.LocalDateTime

@Entity
@Table(name = "feeds")
class Feed(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT용, 필요 시 조정
    var id: Long = 0,  // var로 변경 (JPA가 값을 설정할 수 있도록)

    @Column(name = "companyId")  // 필요 시 컬럼명 지정
    var companyId: Long = 0,

    @Column(nullable = false, length = 500, unique = true)
    val url: String,

    @Column(nullable = true, length = 300)
    val title: String? = null,

    @Column(nullable = true, length = 500)
    val description: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

