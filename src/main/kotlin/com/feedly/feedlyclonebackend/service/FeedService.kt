package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.repository.FeedRepository
import org.springframework.stereotype.Service

@Service
class FeedService(
    private val feedRepository: FeedRepository
) {

    fun getFeedsByCompany(companyId: Long) =
        feedRepository.findByCompany(companyId)
}