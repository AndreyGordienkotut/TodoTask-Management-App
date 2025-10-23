package com.notificationService.service;

import by.info_microservice.core.UserVerificationEventDto;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification_status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {
    private final NotificationService notificationService;
    @KafkaListener(topics = "account-verification-events", groupId = "notification-service",containerFactory = "kafkaListenerContainerFactory")
    public void consume(UserVerificationEventDto event) {
        log.info("Received user verification event: {}", event);
        NotificationServiceRequest request = NotificationServiceRequest.builder()
                .userId(event.getUserId())
                .recipient(event.getEmail())
                .recipientTelegramId(event.getTelegramChatId())
                .subject("Email verification")
                .message("Hello, " + event.getName() + "! \nFollow the link: http://localhost:8080/api/auth/verify-email?token=" + event.getVerificationToken())
                .channel(Channel.EMAIL)
                .status(Notification_status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        notificationService.sendNotification(request);
    }
}