package com.example.simpleshop.services.mail

import com.example.simpleshop.model.KafkaProductConsume
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.cdimascio.dotenv.Dotenv
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class MailServiceImpl(
    private val emailSender: JavaMailSender,
) {
    private val dotenv = Dotenv.load()
    private val mapper = jacksonObjectMapper()
    private val mailReceiver = dotenv["MAIL_RECEIVER"]

    fun sendMail(to: String, subject: String, text: String): Mono<Unit> {
        return Mono.fromCallable {
            val message = SimpleMailMessage()
            message.setTo(to)
            message.subject = subject
            message.text = text
            emailSender.send(message)
        }.subscribeOn(Schedulers.boundedElastic())
    }

    @KafkaListener(topics = ["product"])
    fun consumeMessage(message: String) {
        val consumeMessage: KafkaProductConsume = mapper.readValue(message)
        val topic = consumeMessage.topic
        val details = consumeMessage.details
        if (mailReceiver != null) {
            if (topic != null && details != null) {
                sendMail(mailReceiver, topic, details)
                    .subscribe()
            }
        }
    }
}
