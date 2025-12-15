package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.KeywordService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/keywords")
class KeywordController(
    private val keywordService: KeywordService
) {

    @GetMapping("/company")
    fun companies() = keywordService.getCompanies()

    @GetMapping("/topic")
    fun topics() = keywordService.getTopics()
}