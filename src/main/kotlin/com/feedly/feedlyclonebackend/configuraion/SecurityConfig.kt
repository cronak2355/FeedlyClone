package com.feedly.feedlyclonebackend.configuraion

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }

            .authorizeHttpRequests { auth ->
                auth
                    // 로그인 페이지 & 로그인 요청은 허용
                    .requestMatchers(
                        "/account/signin",
                        "/account/signup",
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/"
                    ).permitAll()

                    // 나머지는 로그인 필요
                    .anyRequest().authenticated()
            }

            .formLogin { form ->
                form
                    .loginPage("/account/signin")   // GET 로그인 페이지
                    .loginProcessingUrl("/login")   // POST 처리 URL
                    .usernameParameter("email")     // form input name
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            }

            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
            }

        return http.build()
    }
}
