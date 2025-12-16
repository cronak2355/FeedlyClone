package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.ExploreService
import com.feedly.feedlyclonebackend.service.FeedService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/today")
class TodayController(
    private val feedService: FeedService,
    private val exploreService: ExploreService
) {

    @GetMapping
    fun today(model: Model): String {
        model.addAttribute("mePosts", feedService.getTodayMePosts(userId = 1L))
        model.addAttribute("explorePosts", exploreService.getExplorePosts())
        return "today"
    }
}
