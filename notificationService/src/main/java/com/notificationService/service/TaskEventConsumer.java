package com.notificationService.service;

import by.info_microservice.core.TaskEventDto;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification_status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventConsumer {
    private final NotificationService notificationService;
    @KafkaListener(topics = "task-events-topic", groupId = "notification-service",containerFactory = "kafkaListenerContainerFactory")
    public void consume(TaskEventDto event) {
        log.info("Received event type: {}", event.getEventType());
        switch (event.getEventType()) {
            case "TASK_OVERDUE" -> {
                if (event.getRecipient() != null) {
                    notificationService.sendNotification(NotificationServiceRequest.builder()
                            .userId(event.getUserId())
                            .recipient(event.getRecipient())
                            .subject("The task is overdue!")
                            .message("You have not completed the task " + event.getTitle() + ". Please complete it as soon as possible!")
                            .channel(Channel.EMAIL)
                            .status(Notification_status.SENT)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
                if (event.getRecipientTelegramId() != null) {
                    notificationService.sendNotification(NotificationServiceRequest.builder()
                            .userId(event.getUserId())
                            .recipientTelegramId(event.getRecipientTelegramId())
                            .subject("The task is overdue!")
                            .message("You have not completed the task " + event.getTitle() + ". Please complete it as soon as possible!")
                            .channel(Channel.TELEGRAM)
                            .status(Notification_status.SENT)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }
            case "TASK_SOON_OVERDUE" -> {
                if (event.getRecipient() != null) {
                    notificationService.sendNotification(NotificationServiceRequest.builder()
                            .userId(event.getUserId())
                            .recipient(event.getRecipient())
                            .subject("The task will soon be overdue!")
                            .message("Less than 15 minutes left until the deadline for the task: " + event.getTitle() + ". Hurry up!")
                            .channel(Channel.EMAIL)
                            .status(Notification_status.SENT)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
                if (event.getRecipientTelegramId() != null) {
                    notificationService.sendNotification(NotificationServiceRequest.builder()
                            .userId(event.getUserId())
                            .recipientTelegramId(event.getRecipientTelegramId())
                            .subject("The task will soon be overdue!")
                            .message("Less than 15 minutes left until the deadline for the task: " + event.getTitle() + ". Hurry up!")
                            .channel(Channel.TELEGRAM)
                            .status(Notification_status.SENT)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }
        }
    }
}
