package com.notificationService.service;


import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private final com.pengrad.telegrambot.TelegramBot telegramBot;

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        telegramBot.execute(request);
    }
}