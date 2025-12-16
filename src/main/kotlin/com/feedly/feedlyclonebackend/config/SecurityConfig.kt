package com.feedly.feedlyclonebackend.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:5173")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val requestHandler = CsrfTokenRequestAttributeHandler()
        requestHandler.setCsrfRequestAttributeName("_csrf")

        http
            // 0. CORS 설정 활성화
            .cors { it.configurationSource(corsConfigurationSource()) }

            // 1. CSRF 비활성화 (개발 테스트용)
            .csrf { it.disable() }

            // 2. HTTP Basic 비활성화 (폼 로그인만 사용할 경우 권장)
            .httpBasic { it.disable() }

            // 3. 경로별 인가(Authorization) 설정 (하나로 통합)
            .authorizeHttpRequests { auth ->
                auth
                    // 정적 자원 및 로그인/회원가입 페이지는 누구나 접근 가능
                    .requestMatchers(
                        "/signup",
                        "/login",
                        "/api/**",  // React API 엔드포인트
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/"
                    ).permitAll()

                    // 그 외 모든 요청은 인증(로그인) 필요
                    .anyRequest().authenticated()
            }

            // 4. 폼 로그인 설정 (SPA용 - 리다이렉트 대신 JSON 응답)
            .formLogin { form ->
                form
                    .loginPage("/login")   // 커스텀 로그인 페이지 경로
                    .loginProcessingUrl("/login")   // HTML Form의 action 경로
                    .usernameParameter("username")     // Form의 input name="username"
                    .passwordParameter("password")  // Form의 input name="password"
                    .successHandler { _, response, _ ->
                        response.status = HttpServletResponse.SC_OK
                        response.contentType = "application/json"
                        response.writer.write("""{"success": true, "message": "Login successful"}""")
                    }
                    .failureHandler { _, response, _ ->
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.contentType = "application/json"
                        response.writer.write("""{"success": false, "message": "Invalid credentials"}""")
                    }
                    .permitAll()                    // 로그인 페이지 자체는 접근 허용
            }

            // 5. 로그아웃 설정
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler { _, response, _ ->
                        response.status = HttpServletResponse.SC_OK
                        response.contentType = "application/json"
                        response.writer.write("""{"success": true, "message": "Logout successful"}""")
                    }
                    .invalidateHttpSession(true) // 세션 무효화 추가 권장
                    .deleteCookies("JSESSIONID") // 쿠키 삭제 추가 권장
            }

        return http.build()
    }
}