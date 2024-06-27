package com.example.simpleshop.controller.review

import com.example.simpleshop.model.ReviewRequest
import com.example.simpleshop.model.ReviewResponse
import com.example.simpleshop.services.review.ReviewServiceImpl
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewControllerImpl(
    private val reviewService: ReviewServiceImpl
) : ReviewController {
    @GetMapping("/product/{productID}")
    override fun getReviveByProductID(@PathVariable("productID") productID: Long): Flux<ReviewResponse> =
        reviewService.getReviveByProductID(productID)

    @GetMapping("/review/{reviewID}")
    override fun getReviewByReviewID(@PathVariable("reviewID") reviewID: String): Mono<ReviewResponse> =
        reviewService.getReviewByReviewID(reviewID)

    @PostMapping("")
    override fun addReview(
        @Valid @RequestBody reviewRequest: ReviewRequest
    ): Mono<ReviewResponse> =
        reviewService.addReview(reviewRequest).map {
            ReviewResponse(it.reviewID, it.productID, it.userID, it.rating, it.comment)
        }

    @PutMapping("/{reviewID}")
    override fun updateReview(
        @PathVariable("reviewID") reviewID: String,
        @Valid @RequestBody reviewRequest: ReviewRequest
    ): Mono<ReviewResponse> =
        reviewService.updateReview(reviewID, reviewRequest).map {
            ReviewResponse(it.reviewID, it.productID, it.userID, it.rating, it.comment)
        }

    @DeleteMapping("/{reviewID}")
    override fun deleteReview(@PathVariable("reviewID") reviewID: String): Mono<Void> =
        reviewService.deleteReview(reviewID)
}