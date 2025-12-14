package com.feedly.feedlyclonebackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val requestHandler = CsrfTokenRequestAttributeHandler()
        requestHandler.setCsrfRequestAttributeName("_csrf")

        http
            // 모든 경로 허용 (로그인 없이 사용)
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            // CSRF 설정 - 쿠키 기반으로 AJAX에서 사용 가능하게
            .csrf { csrf ->
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(requestHandler)
            }
            // 폼 로그인 비활성화
            .formLogin { it.disable() }
            // HTTP Basic 비활성화
            .httpBasic { it.disable() }

        return http.build()
    }
}
