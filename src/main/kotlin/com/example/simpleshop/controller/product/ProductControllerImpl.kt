package com.example.simpleshop.controller.product

import com.example.simpleshop.model.ProductRequest
import com.example.simpleshop.model.ProductResponse
import com.example.simpleshop.services.product.ProductServiceImpl
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/products")
class ProductControllerImpl(
    private val productService: ProductServiceImpl
) : ProductController {
    @GetMapping("")
    override fun getAllProducts(): Flux<ProductResponse> =
        productService.getAllProducts()

    @GetMapping("/{id}")
    override fun getProductById(@PathVariable("id") productId: Long): Mono<ProductResponse> =
        productService.getProductById(productId)

    @PostMapping("")
    override fun addProduct(@Valid @RequestBody productRequest: ProductRequest): Mono<ProductResponse> =
        productService.addProduct(productRequest).map {
            ProductResponse(it.id, it.name, it.description, it.price, it.quantity)
        }

    @PutMapping("/{id}")
    override fun updateProduct(
        @PathVariable("id") productId: Long,
        @Valid @RequestBody productRequest: ProductRequest
    ): Mono<ProductResponse> =
        productService.updateProduct(productId, productRequest).map {
            ProductResponse(it.id, it.name, it.description, it.price, it.quantity)
        }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteProduct(@PathVariable("id") productId: Long): Mono<Void> =
        productService.deleteProduct(productId)
}