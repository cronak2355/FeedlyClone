package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.repository.KeywordRepository
import org.springframework.stereotype.Service

@Service
class KeywordService {

    fun getCompanies(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to 1, "name" to "Google"),
            mapOf("id" to 2, "name" to "Apple"),
            mapOf("id" to 3, "name" to "NVIDIA")
        )
    }

    fun getTopics(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to 1, "name" to "AI"),
            mapOf("id" to 2, "name" to "Stock"),
            mapOf("id" to 3, "name" to "Earnings")
        )
    }
}
