package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.FeedService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/feeds")
class FeedController(
    private val feedService: FeedService
) {

    @GetMapping("/{companyId}")
    fun feeds(@PathVariable companyId: Long) =
        feedService.getFeedsByCompany(companyId)
}