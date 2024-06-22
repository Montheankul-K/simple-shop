package com.example.simpleshop.model

data class ProductResponse(
    val id: Long?,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
)
