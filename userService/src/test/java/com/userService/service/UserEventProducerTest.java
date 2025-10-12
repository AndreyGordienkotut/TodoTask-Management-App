package com.userService.service;

import by.info_microservice.core.UserVerificationEventDto;
import com.userService.exception.EventPublishException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserEventProducer producer;

    private UserVerificationEventDto event;
    private final String TOPIC = "account-verification-events";
    private final Long USER_ID = 100L;
    private final String KEY = USER_ID.toString();

    @BeforeEach
    void setUp() {
        event = UserVerificationEventDto.builder()
                .userId(USER_ID)
                .name("TestUser")
                .email("test@example.com")
                .verificationToken("token123")
                .eventType("USER_REGISTERED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Success - sendAccountVerificationEvent publishes event")
    void sendAccountVerificationEvent_successful() {
        // Создаём фейковый SendResult, чтобы симулировать успешную отправку
        SendResult<String, Object> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), eq(event))).thenReturn(future);


        producer.sendAccountVerificationEvent(event);
        verify(kafkaTemplate,times(1)).send(eq(TOPIC), eq(KEY), eq(event));

    }

    @Test
    @DisplayName("Failure - sendAccountVerificationEvent throws EventPublishException when Kafka fails")
    void sendAccountVerificationEvent_failureThrowsException() {
        CompletableFuture<SendResult<String, Object>> fail = new CompletableFuture<>();
        fail.completeExceptionally(new RuntimeException("Kafka fails"));
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), eq(event))).thenReturn(fail);
        EventPublishException exception = assertThrows(
                EventPublishException.class,
                () -> producer.sendAccountVerificationEvent(event)
        );

        assertTrue(exception.getMessage().contains("Failed to publish verification event"));
        verify(kafkaTemplate,times(1)).send(eq(TOPIC), eq(KEY), eq(event));

    }
}
