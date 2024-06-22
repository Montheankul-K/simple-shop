package com.example.simpleshop.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ProductRequest(
    @field:NotBlank val name: String,
    val description: String,
    @field:NotNull val price: Double,
    @field:NotNull val quantity: Int,
)
