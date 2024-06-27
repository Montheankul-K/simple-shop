package com.example.simpleshop.repository

import com.example.simpleshop.model.ReviewModel
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReviewRepository : ReactiveMongoRepository<ReviewModel, String> {
    fun findByProductID(productID: Long): Flux<ReviewModel>
    fun findByProductIDAndUserID(productID: Long, userID: Long): Mono<ReviewModel>
}