package com.example.simpleshop.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "order_items")
data class OrderItem(
    @Id
    val id: Long? = null,
    @Column(value = "order_id")
    val orderId: Long,
    @Column(value = "product_id")
    val productId: Long,
    val quantity: Int,
    val price: Double,
    @Column(value = "created_at")
    @CreatedDate
    val createdDate: LocalDateTime? = null,
    @Column(value = "updated_at")
    @LastModifiedDate
    val updatedDate: LocalDateTime? = null
)
