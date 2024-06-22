package com.example.simpleshop.repository

import org.springframework.boot.autoconfigure.security.SecurityProperties.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface UserRepository : ReactiveCrudRepository<User, Long>