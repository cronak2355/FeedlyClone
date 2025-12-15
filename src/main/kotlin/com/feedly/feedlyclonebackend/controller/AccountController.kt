package com.feedly.feedlyclonebackend.controller

import com.feedly.feedlyclonebackend.form.SignupForm
import com.feedly.feedlyclonebackend.service.UserService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/account")
class AccountController
(
    private var accountService: UserService
)
{

    @GetMapping("/signup")
    fun signupPage(@ModelAttribute("signupForm") signupForm: SignupForm, model: Model): String
    {
        println("=== GET /account/signup 호출됨 ===")
        return "signup"
    }

    @PostMapping("/signup")
    fun signupSubmit(
        @ModelAttribute @Valid signupForm: SignupForm,
        bindingResult: BindingResult
    ): String {
        println("=== POST /account/signup 호출됨 ===")
        println("이메일: ${signupForm.email}, 비밀번호 길이: ${signupForm.password.length}")

        // 검증 실패 시
        if (bindingResult.hasErrors()) {
            println("검증 실패: ${bindingResult.allErrors}")
            return "signup"
        }

        // 회원가입 처리
        val result = accountService.signup(signupForm.email, signupForm.password, bindingResult)
        if (result == null) {
            println("회원가입 실패")
            return "signup"
        }

        println("회원가입 성공: ${result.email}")
        return "redirect:/login"
    }
    @GetMapping("/signin")
    fun signinPage(): String {
        return "signin"
    }
}