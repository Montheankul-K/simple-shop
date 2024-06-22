package com.example.simpleshop.repository

import com.example.simpleshop.model.Product
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ProductRepository : ReactiveCrudRepository<Product, Long> {
    fun findByName(name: String): Mono<Product>
}