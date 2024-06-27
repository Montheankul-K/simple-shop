package com.example.simpleshop.config

import io.github.cdimascio.dotenv.Dotenv
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class KafkaConsumerConfig {
    private val dotenv = Dotenv.load()
    private val bootstrapServers = dotenv["KAFKA_BOOTSTRAP_SERVERS"]
    private val groupId = dotenv["KAFKA_GROUP_ID"]
    private val topic = dotenv["KAFKA_TOPIC"]

    @Bean
    fun reactiveKafkaConsumerTemplate(): ReactiveKafkaConsumerTemplate<String, String> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer"
        )
        val receiverOptions = ReceiverOptions.create<String, String>(props)
            .subscription(listOf(topic))
        return ReactiveKafkaConsumerTemplate(receiverOptions)
    }
}