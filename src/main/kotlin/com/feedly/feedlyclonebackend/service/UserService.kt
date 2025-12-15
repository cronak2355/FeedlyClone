package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.entity.Account
import com.feedly.feedlyclonebackend.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    fun signup(email: String, password: String, bindingResult: BindingResult): Account? {
        if (userRepository.existsByEmail(email)) {
            bindingResult.rejectValue("email", "duplicate", "이미 존재하는 이메일")
            return null
        }

        val account = Account(
            email = email,
            password = passwordEncoder.encode(password)!!
        )
        return userRepository.save(account)
    }

    fun emailExists(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun createUser(email: String, password: String): Account {
        val account = Account(
            email = email,
            password = passwordEncoder.encode(password)!!
        )
        return userRepository.save(account)
    }

    override fun loadUserByUsername(email: String): UserDetails {
        val account = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("No user found with email: $email")

        // 권한은 일단 USER 하나 박아두는 게 제일 무난함
        return User.withUsername(account.email)
            .password(account.password)
            .roles("USER")
            .build()
    }
}
