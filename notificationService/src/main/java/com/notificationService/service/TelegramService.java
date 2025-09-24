package com.notificationService.service;


import com.notificationService.config.UserServiceClient;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private final com.pengrad.telegrambot.TelegramBot telegramBot;
    private final UserServiceClient userServiceClient;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    String text = update.message().text();
                    Long chatId = update.message().chat().id();

                    if ("/start".equalsIgnoreCase(text)) {
                        sendMessage(chatId, "Hello! Please enter the token from your personal account to link it.");
                    } else if (text.length() > 10) {
                        boolean linked = userServiceClient.linkTelegramChatByToken(text, chatId);
                        if (linked) {
                            sendMessage(chatId, "✅ Your account has been successfully linked!");
                        } else {
                            sendMessage(chatId, "❌ Invalid token. Generate a new one in your personal account.");
                        }
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        telegramBot.execute(request);
    }
}