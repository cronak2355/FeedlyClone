package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.feedly.feedlyclonebackend.dto.SignupResult
@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class AccountApiController(
    private val userService: UserService
) {

    data class SignupRequest(val email: String, val password: String)
    data class SignupResponse(val success: Boolean, val message: String)

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<SignupResponse> {
        println("=== API POST /api/account/signup 호출됨 ===")
        println("이메일: ${request.email}")
        var result = userService.signup(request.email, request.password)

        if (result.result)
            return ResponseEntity.ok(SignupResponse(true, result.message))
        else
            return ResponseEntity.badRequest().body(SignupResponse(false, result.message))

    }
}
