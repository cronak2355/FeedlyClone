package com.feedly.feedlyclonebackend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    /**
     * 루트 경로 -> /discover 리다이렉트
     * (로그아웃 상태 가정, 로그인 구현 X)
     */
    @GetMapping("/")
    fun home(): String {
        return "redirect:/discover"
    }
}
