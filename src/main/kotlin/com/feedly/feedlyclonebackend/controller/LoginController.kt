package com.feedly.feedlyclonebackend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController {

    @GetMapping("/login")
    fun login(): String {
        // React 앱으로 리디렉션
        return "redirect:http://localhost:5173/login"
    }
}
