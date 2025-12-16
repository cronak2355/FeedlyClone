package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.FeedService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/feeds")
class FeedController(
    private val feedService: FeedService
) {

    @GetMapping("/{feedId}")
    fun feed(
        @PathVariable feedId: Long,
        model: Model
    ): String {
        model.addAttribute("items", feedService.feedItems(feedId))
        return "feed"
    }
}
