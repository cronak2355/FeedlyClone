package com.feedly.feedlyclonebackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_article_interactions",
    // 한 사용자가 같은 기사(URL)에 대해 중복 레코드를 갖지 않도록 유니크 제약 조건 설정
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "article_url"])]
)
class UserArticleInteraction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    // 외부 기사이므로 URL이 식별자 역할을 합니다.
    // 일부 URL은 매우 길 수 있으므로 넉넉하게 잡거나 TEXT 타입을 고려해야 합니다.
    @Column(name = "article_url", nullable = false, length = 2048)
    val articleUrl: String,

    // URL 해시 (인덱싱용)
    @Column(name = "article_url_hash", columnDefinition = "CHAR(64)", nullable = false)
    var articleUrlHash: String = "",

    // === 기사 정보 스냅샷 (목록 보여주기용) ===
    @Column(name = "title", length = 1000)
    var title: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    var thumbnailUrl: String? = null,

    @Column(name = "site_name")
    var siteName: String? = null,

    // === 사용자 상호작용 상태 ===
    
    // 1. 나중에 보기 (Saved)
    @Column(name = "is_saved")
    var isSaved: Boolean = false,

    @Column(name = "saved_at")
    var savedAt: LocalDateTime? = null,

    // 2. 읽음 표시 (Read) & 최근 읽은 목록 (History)
    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,

    // 3. 메모 (Memo)
    @Column(name = "memo", columnDefinition = "TEXT")
    var memo: String? = null,

    // 데이터 생성 및 최종 수정 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
        // URL 해시 생성 (SHA-256)
        if (articleUrl.isNotEmpty()) {
            articleUrlHash = java.security.MessageDigest.getInstance("SHA-256")
                .digest(articleUrl.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
    }
}