package com.example.simpleshop.config

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailSenderConfig {
    @Bean
    fun mailSender(): JavaMailSender {
        val dotenv = Dotenv.load()
        val mailSender = JavaMailSenderImpl()

        mailSender.host = dotenv["SMTP_HOST"]
        mailSender.port = dotenv["SMTP_PORT"]?.toInt() ?: 587
        mailSender.username = dotenv["SMTP_USERNAME"]
        mailSender.password = dotenv["SMTP_PASSWORD"]

        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp" // protocol for sending email
        props["mail.smtp.auth"] = "true" // enable smtp authentication
        props["mail.smtp.starttls.enable"] = "true" // upgrade the connection to a secure connection
        props["mail.debug"] = "true" // enable debug mode
        return mailSender
    }
}