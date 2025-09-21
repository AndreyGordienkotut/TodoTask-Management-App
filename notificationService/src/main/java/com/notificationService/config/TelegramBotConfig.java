package com.notificationService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    @Value("${TELEG_TOKEN}")
    private String botToken;

    @Bean
    public com.pengrad.telegrambot.TelegramBot telegramBot() {
        return new com.pengrad.telegrambot.TelegramBot(botToken);
    }
}