package com.example.simpleshop.controller.review

import com.example.simpleshop.model.ReviewRequest
import com.example.simpleshop.model.ReviewResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReviewController {
    fun getReviveByProductID(productID: Long): Flux<ReviewResponse>
    fun getReviewByReviewID(reviewID: String): Mono<ReviewResponse>
    fun addReview(reviewRequest: ReviewRequest): Mono<ReviewResponse>
    fun updateReview(reviewID: String, reviewRequest: ReviewRequest): Mono<ReviewResponse>
    fun deleteReview(reviewID: String): Mono<Void>
}