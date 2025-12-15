package com.feedly.feedlyclonebackend.repository

import com.feedly.feedlyclonebackend.entity.Account
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<Account, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): Account?;
}