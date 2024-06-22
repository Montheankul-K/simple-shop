package com.example.simpleshop.service

import com.example.simpleshop.exception.BadRequestException
import com.example.simpleshop.exception.CreateException
import com.example.simpleshop.exception.NotFoundException
import com.example.simpleshop.model.Product
import com.example.simpleshop.model.ProductRequest
import com.example.simpleshop.model.ProductResponse
import com.example.simpleshop.repository.ProductRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository
) : ProductService {
    override fun getAllProducts(): Flux<ProductResponse> =
        productRepository.findAll().switchIfEmpty(
            Flux.error(
                NotFoundException("products was not found")
            )
        ).map {
            ProductResponse(
                id = it.id,
                name = it.name,
                description = it.description,
                price = it.price,
                quantity = it.quantity
            )
        }

    override fun getProductById(productId: Long): Mono<ProductResponse> =
        productRepository.findById(productId).flatMap {
            if (it != null) {
                Mono.just(
                    ProductResponse(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        price = it.price,
                        quantity = it.quantity
                    )
                )
            } else {
                Mono.error(
                    NotFoundException("product id: $productId was not found")
                )
            }
        }

    override fun addProduct(productRequest: ProductRequest): Mono<Product> =
        checkProductIsExistsByName(productRequest).switchIfEmpty(
            productRepository.save(
                Product(
                    name = productRequest.name,
                    description = productRequest.description,
                    price = productRequest.price,
                    quantity = productRequest.quantity
                )
            ).switchIfEmpty(
                Mono.error(
                    CreateException("failed to add product")
                )
            )
        )

    override fun updateProduct(productId: Long, productRequest: ProductRequest): Mono<Product> =
        getProductById(productId).flatMap {
            checkProductIsExistsByName(productRequest)
                .switchIfEmpty(
                    productRepository.save(
                        Product(
                            name = productRequest.name,
                            description = productRequest.description,
                            price = productRequest.price,
                            quantity = productRequest.quantity
                        )
                    )
                )
        }

    override fun deleteProduct(productId: Long): Mono<Void> =
        getProductById(productId).flatMap {
            productRepository.deleteById(productId)
        }

    private fun checkProductIsExistsByName(productRequest: ProductRequest) =
        productRepository.findByName(productRequest.name)
            .flatMap {
                Mono.error<Product>(
                    BadRequestException("product with name: ${productRequest.name} is exists")
                )
            }
}