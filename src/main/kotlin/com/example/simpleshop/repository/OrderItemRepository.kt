package com.example.simpleshop.repository

import com.example.simpleshop.model.OrderItem
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OrderItemRepository : ReactiveCrudRepository<OrderItem, Long>