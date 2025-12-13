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
        return "signup"
    }

    @PostMapping("/signup")
    fun signupSubmit(
        @ModelAttribute @Valid signupForm: SignupForm,
        bindingResult: BindingResult
    ): String {
        println("${signupForm.email} ${signupForm.password}")

        // 검증 실패 시
        if (bindingResult.hasErrors()) {
            return "signup"
        }

        // TODO: 회원가입 처리 로직
        if (accountService.signup(signupForm.email, signupForm.password, bindingResult) == null)
        {
            return "signup"
        }

        return "redirect:/account/signin"
    }
}