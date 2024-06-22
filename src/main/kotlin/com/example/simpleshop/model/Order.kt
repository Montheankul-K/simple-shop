package com.example.simpleshop.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(value = "orders")
data class Order(
    @Id
    val id: Long? = null,
    @Column(value = "user_id")
    val userId: Long? = null,
    @Column(value = "total_amount")
    val totalAmount: Double,
    val status: String,
    @Column(value = "created_at")
    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @Column(value = "updated_at")
    @LastModifiedDate
    val updatedAt: LocalDateTime? = null
)