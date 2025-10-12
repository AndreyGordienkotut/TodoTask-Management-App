package com.notificationService.service;


import by.info_microservice.core.LinkTelegramRequest;
import com.notificationService.exception.TelegramSendException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final com.pengrad.telegrambot.TelegramBot telegramBot;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    String text = update.message().text();
                    Long chatId = update.message().chat().id();

                    if ("/start".equalsIgnoreCase(text)) {
                        sendMessage(chatId, "Hello! Please enter the token from your personal account to link it.");
                    } else {
                        LinkTelegramRequest request = new LinkTelegramRequest(text, chatId);
                        kafkaTemplate.send("telegram-link-requests", chatId.toString(), request);
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
    public void sendMessage(Long chatId, String text) {
        try {
            telegramBot.execute(new SendMessage(chatId, text));
            log.debug("Telegram sent to {}: {}", chatId, text);
        } catch (Exception e) {
            throw new TelegramSendException("Failed to send Telegram message to " + chatId, e);
        }
    }
}