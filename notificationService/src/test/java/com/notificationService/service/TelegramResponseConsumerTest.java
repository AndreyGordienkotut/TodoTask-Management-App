package com.notificationService.service;

import by.info_microservice.core.LinkTelegramResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class TelegramResponseConsumerTest {
    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TelegramResponseConsumer consumer;

    private final Long CHAT_ID = 123456789L;
    private LinkTelegramResponse successResponse;
    private LinkTelegramResponse failureResponse;

    @BeforeEach
    void setUp() {
        successResponse = LinkTelegramResponse.builder()
                .chatId(CHAT_ID)
                .success(true)
                .build();
        failureResponse = LinkTelegramResponse.builder()
                .chatId(CHAT_ID)
                .success(false)
                .build();
    }
    @Test
    @DisplayName("Success consume - Should send success message when linking is successful")
    void consume_Success() {
        final String expectedMessage = "Your account has been successfully linked!";
        consumer.consume(successResponse);
        verify(telegramService, times(1)).sendMessage(CHAT_ID, expectedMessage);
        verify(telegramService, never()).sendMessage(eq(CHAT_ID), eq("Invalid token. Please generate a new one in your personal account."));
    }
    @Test
    @DisplayName("Failed consume -  Should send failure message when linking token is invalid")
    void consume_Failure() {
        final String expectedMessage = "Invalid token. Please generate a new one in your personal account.";
        consumer.consume(failureResponse);

        verify(telegramService, times(1)).sendMessage(CHAT_ID, expectedMessage);
        verify(telegramService, never()).sendMessage(eq(CHAT_ID), eq("Your account has been successfully linked!"));
    }
}
