package com.taskService.service;

import by.info_microservice.core.TaskEventDto;
import com.sun.source.util.TaskEvent;
import com.taskService.exception.TaskEventPublishException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TaskEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TaskEventProducer producer;
    private TaskEventDto event;
    private final String TOPIC = "task-events-topic";
    private final Long USER_ID = 100L;
    private final String KEY = USER_ID.toString();
    @BeforeEach
    void setUp() {
        event = TaskEventDto.builder()
                .userId(USER_ID)
                .taskId(100L)
                .recipient("recipient")
                .recipientTelegramId(100L)
                .subject("subject")
                .title("title")
                .description("description")
                .createdAt(LocalDateTime.now())
                .channel("TELEGRAM")
                .message("message")
                .status("SEND")
                .build();
    }
    @Test
    @DisplayName("Success - sendTaskEvent publishes event")
    void sendTaskEvent() {
        SendResult<String, Object> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), eq(event))).thenReturn(future);
        producer.sendTaskEvent(event);
        verify(kafkaTemplate,times(1)).send(eq(TOPIC), eq(KEY), eq(event));
    }
    @Test
    @DisplayName("Failure - sendAccountVerificationEvent throws EventPublishException when Kafka fails")
    void sendSendAccountVerificationEvent_failureThrowsException() {
        CompletableFuture<SendResult<String, Object>> fail = new CompletableFuture<>();
        fail.completeExceptionally(new RuntimeException("Kafka fails"));
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), eq(event))).thenReturn(fail);
        TaskEventPublishException exception = assertThrows(
                TaskEventPublishException.class,
                () -> producer.sendTaskEvent(event)
        );

        assertTrue(exception.getMessage().contains("Failed to publish task event"));
        verify(kafkaTemplate,times(1)).send(eq(TOPIC), eq(KEY), eq(event));

    }
}
