package com.feedly.feedlyclonebackend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController {

    @GetMapping("/")
    fun index(): String = "index"

    @GetMapping("/layout")
    fun layout(): String = "layout"

    @GetMapping("/createAIfeed")
    fun createAIfeed(): String = "createAIfeed"
}