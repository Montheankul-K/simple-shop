package com.example.simpleshop.repository

import com.example.simpleshop.model.Order
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OrderRepository : ReactiveCrudRepository<Order, Long>