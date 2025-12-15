package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
        
        return try {
            // 이메일 중복 확인
            if (userService.emailExists(request.email)) {
                println("이메일 중복: ${request.email}")
                ResponseEntity.badRequest().body(SignupResponse(false, "이미 존재하는 이메일입니다."))
            } else {
                // 회원가입 처리
                userService.createUser(request.email, request.password)
                println("회원가입 성공: ${request.email}")
                ResponseEntity.ok(SignupResponse(true, "회원가입 성공"))
            }
        } catch (e: Exception) {
            println("회원가입 에러: ${e.message}")
            ResponseEntity.internalServerError().body(SignupResponse(false, "서버 에러"))
        }
    }
}
