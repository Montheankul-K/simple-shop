package com.example.simpleshop.model

import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "review")
data class ReviewModel(
    @Id
    @Field(value = "review_id")
    val reviewID: String? = null,
    @Field(value = "product_id")
    val productID: Long,
    @Field(value = "user_id")
    val userID: Long,
    val rating: Int,
    val comment: String? = null,
    @Field(value = "created_at")
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    @Field(value = "updated_at")
    val updatedAt: LocalDateTime? = LocalDateTime.now()
)

data class ReviewRequest(
    @field:NotNull
    val productID: Long,
    @field:NotNull
    val userID: Long,
    @field:NotNull
    val rating: Int,
    val comment: String? = null
)

data class ReviewResponse(
    val reviewID: String? = null,
    val productID: Long,
    val userID: Long,
    val rating: Int,
    val comment: String? = null,
)

