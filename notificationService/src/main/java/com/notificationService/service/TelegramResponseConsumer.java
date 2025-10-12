package com.notificationService.service;

import by.info_microservice.core.LinkTelegramResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramResponseConsumer {

    private final TelegramService telegramService;

    @KafkaListener(topics = "telegram-link-responses", groupId = "notification-service-group")
    public void consume(LinkTelegramResponse response) {
        if (response.isSuccess()) {
            telegramService.sendMessage(response.getChatId(), "Your account has been successfully linked!");
        } else {
            telegramService.sendMessage(response.getChatId(), "Invalid token. Please generate a new one in your personal account.");
        }
    }
}
