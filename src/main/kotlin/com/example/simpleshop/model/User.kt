package com.example.simpleshop.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "users")
data class User(
    @Id
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String,
    @Column(value = "created_at")
    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @Column(value = "updated_at")
    @LastModifiedDate
    val updatedAt: LocalDateTime? = null
)
