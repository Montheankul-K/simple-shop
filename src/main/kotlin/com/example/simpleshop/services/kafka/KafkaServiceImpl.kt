package com.example.simpleshop.services.kafka


import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaServiceImpl(
    private val kafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, String>,
    private val kafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, String>
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun produceMessage(topic: String, message: String): Mono<Void> {
        return kafkaProducerTemplate.send(topic, message)
            .then()
    }

    fun produceMessage(topic: String, message: Any): Mono<Void> {
        val jsonMessage = objectMapper.writeValueAsString(message)
        return kafkaProducerTemplate.send(topic, jsonMessage)
            .then()
    }
}