package com.example.simpleshop.services.product

import com.example.simpleshop.model.Product
import com.example.simpleshop.model.ProductRequest
import com.example.simpleshop.model.ProductResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ProductService {
    fun getAllProducts(): Flux<ProductResponse>
    fun getProductById(productId: Long): Mono<ProductResponse>
    fun addProduct(productRequest: ProductRequest): Mono<Product>
    fun updateProduct(productId: Long, productRequest: ProductRequest): Mono<Product>
    fun deleteProduct(productId: Long): Mono<Void>
}