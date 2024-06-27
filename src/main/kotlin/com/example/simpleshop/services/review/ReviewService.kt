package com.example.simpleshop.services.review

import com.example.simpleshop.model.ReviewModel
import com.example.simpleshop.model.ReviewRequest
import com.example.simpleshop.model.ReviewResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReviewService {
    fun getReviveByProductID(productID: Long): Flux<ReviewResponse>
    fun getReviewByReviewID(reviewID: String): Mono<ReviewResponse>
    fun addReview(review: ReviewRequest): Mono<ReviewModel>
    fun updateReview(reviewID: String, review: ReviewRequest): Mono<ReviewModel>
    fun deleteReview(reviewID: String): Mono<Void>
}