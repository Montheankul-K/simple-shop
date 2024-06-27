package com.example.simpleshop.controller.product

import com.example.simpleshop.model.ProductRequest
import com.example.simpleshop.model.ProductResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ProductController {
    fun getAllProducts(): Flux<ProductResponse>
    fun getProductById(productId: Long): Mono<ProductResponse>
    fun addProduct(productRequest: ProductRequest): Mono<ProductResponse>
    fun updateProduct(productId: Long, productRequest: ProductRequest): Mono<ProductResponse>
    fun deleteProduct(productId: Long): Mono<Void>
}