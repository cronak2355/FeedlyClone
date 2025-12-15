package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column

@Entity
data class Feed(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT용, 필요 시 조정
    var id: Long = 0,  // var로 변경 (JPA가 값을 설정할 수 있도록)

    @Column(name = "companyId")  // 필요 시 컬럼명 지정
    var companyId: Long = 0,

    @Column(nullable = false, length = 1000)  // 이전 경고 고려 (길이 제한)
    var url: String = ""
)