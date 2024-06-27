package com.example.simpleshop.services.product

import com.example.simpleshop.exception.*
import com.example.simpleshop.model.Product
import com.example.simpleshop.model.ProductRequest
import com.example.simpleshop.model.ProductResponse
import com.example.simpleshop.repository.ProductRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, String>
) : ProductService {
    val mapper = jacksonObjectMapper()

    override fun getAllProducts(): Flux<ProductResponse> =
        // Flux represents a stream of 0 to N elements
        reactiveRedisTemplate.opsForValue().get("allProducts")
            .cast(List::class.java)
            .flatMapMany { Flux.fromIterable(it.filterIsInstance<ProductResponse>()) }
            // filterIsInstance is used to filter elements of a specific type
            .switchIfEmpty(
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
                }.collectList().flatMap {
                    // collectList is used to collect all elements emitted by Flux into a single list
                    reactiveRedisTemplate.opsForValue().set("allProducts", it)
                        .then(Mono.just(it)) // create a Mono that emits the specified item
                }.flatMapMany {
                    Flux.fromIterable(it) // create a Flux that emits the items contained in the provided iterable
                }
            )

    override fun getProductById(productId: Long): Mono<ProductResponse> =
        reactiveRedisTemplate.opsForValue().get("product::$productId")
            .cast(ProductResponse::class.java)
            .switchIfEmpty(
                productRepository.findById(productId).flatMap {
                    if (it != null) {
                        val productResponse = ProductResponse(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            price = it.price,
                            quantity = it.quantity
                        )
                        reactiveRedisTemplate.opsForValue().set("product::$productId", productResponse)
                            .then(Mono.just(productResponse))
                    } else {
                        Mono.error(
                            NotFoundException("product id: $productId was not found")
                        )
                    }
                }
            )

    @Transactional
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
            ).flatMap { product ->
                reactiveRedisTemplate.opsForValue().delete("allProducts")
                    .flatMap {
                        val messageMap = mapOf(
                            "topic" to "new product added",
                            "details" to product.toString()
                        )
                        val messageJson = mapper.writeValueAsString(messageMap)
                        reactiveKafkaProducerTemplate.send("product", messageJson)
                    }
                    .then(Mono.just(product))
            }
        )

    @Transactional
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
                ).flatMap { product ->
                    reactiveRedisTemplate.opsForValue().delete("allProducts")
                        .then(
                            reactiveRedisTemplate.opsForValue().delete("product::$productId")
                                .flatMap {
                                    val messageMap = mapOf(
                                        "topic" to "product updated",
                                        "details" to product.toString()
                                    )
                                    val messageJson = mapper.writeValueAsString(messageMap)
                                    reactiveKafkaProducerTemplate.send("product", messageJson)
                                }
                        ).then(Mono.just(product))
                }
        }.onErrorResume {
            Mono.error(
                UpdateException("failed to update product id: $productId")
            )
        }

    @Transactional
    override fun deleteProduct(productId: Long): Mono<Void> =
        getProductById(productId).switchIfEmpty(
            Mono.error(
                BadRequestException("product id: $productId was not exists")
            )
        ).flatMap {
            productRepository.deleteById(productId)
                .then(
                    reactiveRedisTemplate.opsForValue().delete("allProducts")
                ).then(
                    reactiveRedisTemplate.opsForValue().delete("product::$productId")
                        .then(reactiveRedisTemplate.opsForValue().delete("product::$productId"))
                        .flatMap {
                            val messageMap = mapOf(
                                "topic" to "product deleted",
                                "details" to it.toString()
                            )
                            val messageJson = mapper.writeValueAsString(messageMap)
                            reactiveKafkaProducerTemplate.send("product", messageJson)
                        }.then()
                )
        }.onErrorResume {
            Mono.error(
                DeleteException("failed to delete product id: $productId")
            )
        }

    private fun checkProductIsExistsByName(productRequest: ProductRequest) =
        productRepository.findByName(productRequest.name)
            .flatMap {
                Mono.error<Product>(
                    BadRequestException("product with name: ${productRequest.name} is exists")
                )
            }
}