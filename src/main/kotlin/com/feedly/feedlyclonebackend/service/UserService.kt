package com.feedly.feedlyclonebackend.service

import com.feedly.feedlyclonebackend.entity.Account
import com.feedly.feedlyclonebackend.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult
import com.feedly.feedlyclonebackend.dto.SignupResult
import org.springframework.dao.DataIntegrityViolationException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    fun signup(email: String, password: String): SignupResult {
        var result = SignupResult(true, "")

        if (userRepository.existsByEmail(email)) {
            result.result = false
            result.message = "이미 존재하는 이메일입니다."
            return result
        }

        val account = Account(
            email = email,
            password = passwordEncoder.encode(password)!!
        )

        try{
            userRepository.save(account)
            result.result = true
            result.message = "회원가입이 성공적으로 완료했습니다."
        }
        catch (e: DataIntegrityViolationException)
        {
            result.result = false
            result.message = "이미 존재하는 이메일입니다."
        }

        return result
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
