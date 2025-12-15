package com.feedly.feedlyclonebackend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController {

    @GetMapping("/layout")
    fun layout(): String = "layout"

    @GetMapping("/createAIfeed")
    fun createAIfeed(): String = "createAIfeed"
}