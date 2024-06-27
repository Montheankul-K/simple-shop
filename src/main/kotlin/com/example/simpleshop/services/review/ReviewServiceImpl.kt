package com.example.simpleshop.services.review

import com.example.simpleshop.exception.BadRequestException
import com.example.simpleshop.exception.CreateException
import com.example.simpleshop.exception.NotFoundException
import com.example.simpleshop.exception.UpdateException
import com.example.simpleshop.model.ReviewModel
import com.example.simpleshop.model.ReviewRequest
import com.example.simpleshop.model.ReviewResponse
import com.example.simpleshop.repository.ReviewRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ReviewServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, String>,
) : ReviewService {
    val mapper = jacksonObjectMapper()

    override fun getReviveByProductID(productID: Long): Flux<ReviewResponse> =
        reactiveRedisTemplate.opsForValue().get("review::$productID")
            .cast(List::class.java)
            .flatMapMany { Flux.fromIterable(it.filterIsInstance<ReviewResponse>()) }
            .switchIfEmpty(
                reviewRepository.findByProductID(productID)
                    .switchIfEmpty(
                        Flux.error(
                            NotFoundException("reviews was not found")
                        )
                    ).map {
                        ReviewResponse(
                            reviewID = it.reviewID,
                            productID = it.productID,
                            userID = it.userID,
                            rating = it.rating,
                            comment = it.comment
                        )
                    }.collectList().flatMap {
                        reactiveRedisTemplate.opsForValue().set("review::$productID", it)
                            .then(Mono.just(it))
                    }.flatMapMany {
                        Flux.fromIterable(it)
                    }
            )

    override fun getReviewByReviewID(reviewID: String): Mono<ReviewResponse> =
        reactiveRedisTemplate.opsForValue().get("review::$reviewID")
            .cast(ReviewResponse::class.java)
            .switchIfEmpty(
                reviewRepository.findById(reviewID).flatMap {
                    if (it != null) {
                        val reviewResponse = ReviewResponse(
                            reviewID = it.reviewID,
                            productID = it.productID,
                            userID = it.userID,
                            rating = it.rating,
                            comment = it.comment
                        )
                        reactiveRedisTemplate.opsForValue().set("review::$reviewID", reviewResponse)
                            .then(Mono.just(reviewResponse))
                    } else {
                        Mono.error(
                            NotFoundException("review id: $reviewID was not found")
                        )
                    }
                }
            )

    @Transactional
    override fun addReview(review: ReviewRequest): Mono<ReviewModel> =
        checkReviewIsExists(review).switchIfEmpty(
            reviewRepository.save(
                ReviewModel(
                    productID = review.productID,
                    userID = review.userID,
                    rating = review.rating,
                    comment = review.comment
                )
            ).switchIfEmpty(
                Mono.error(
                    CreateException("failed to add review of product id: ${review.productID}")
                )
            ).flatMap { savedReview ->
                reactiveRedisTemplate.opsForValue().delete("review::${savedReview.productID}")
                    .flatMap {
                        val messageMap = mapOf(
                            "topic" to "add review on product id: ${savedReview.productID} by customer id: ${savedReview.userID}",
                            "details" to savedReview.toString()
                        )
                        val messageJson = mapper.writeValueAsString(messageMap)
                        reactiveKafkaProducerTemplate.send("review", messageJson)
                    }.then(Mono.just(savedReview))
            }
        )

    @Transactional
    override fun updateReview(reviewID: String, review: ReviewRequest): Mono<ReviewModel> =
        getReviewByReviewID(reviewID).switchIfEmpty(
            Mono.error(
                BadRequestException("review id: $reviewID was not exists")
            )
        ).flatMap {
            reviewRepository.save(
                ReviewModel(
                    productID = review.productID,
                    userID = review.userID,
                    rating = review.rating,
                    comment = review.comment
                )
            )
        }.flatMap {
            reactiveRedisTemplate.opsForValue().delete("review::$reviewID")
                .then(
                    reactiveRedisTemplate.opsForValue().delete("review::${it.productID}")
                        .flatMap {
                            val messageMap = mapOf(
                                "topic" to "update review on review id: $reviewID",
                                "details" to it.toString()
                            )
                            val messageJson = mapper.writeValueAsString(messageMap)
                            reactiveKafkaProducerTemplate.send("review", messageJson)
                        }
                ).then(Mono.just(it))
        }.onErrorResume {
            Mono.error(
                UpdateException("failed to update review id: $reviewID")
            )
        }

    @Transactional
    override fun deleteReview(reviewID: String): Mono<Void> =
        getReviewByReviewID(reviewID).switchIfEmpty(
            Mono.error(
                BadRequestException("review id: $reviewID was not exists")
            )
        ).flatMap { review ->
            reviewRepository.deleteById(reviewID)
                .then(
                    reactiveRedisTemplate.opsForValue().delete("review::$reviewID")
                ).then(
                    reactiveRedisTemplate.opsForValue().delete("review::${review.productID}")
                        .flatMap {
                            val messageMap = mapOf(
                                "topic" to "delete review on product id: $reviewID",
                                "details" to review.toString()
                            )
                            val messageJson = mapper.writeValueAsString(messageMap)
                            reactiveKafkaProducerTemplate.send("review", messageJson)
                        }.then()
                )
        }


    private fun checkReviewIsExists(review: ReviewRequest) =
        reviewRepository.findByProductIDAndUserID(review.productID, review.userID)
            .flatMap {
                Mono.error<ReviewModel>(
                    BadRequestException("review was already exists")
                )
            }

    private fun checkReviewIsExistsByReviewID(reviewID: String) =
        reviewRepository.findById(reviewID)
            .flatMap {
                Mono.error<ReviewModel>(
                    BadRequestException("review was already exists")
                )
            }
}