package com.notificationService.service;

import com.notificationService.config.UserServiceClient;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.dto.UserDto;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification;
import com.notificationService.model.Notification_status;
import com.notificationService.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TelegramService telegramService;
    private final UserServiceClient userServiceClient;

    @Transactional
public void sendNotification(NotificationServiceRequest request) {
    UserDto user = userServiceClient.getUserById(request.getUserId());
    if (user == null) {
        throw new RuntimeException("User not found for notification");
    }
    Notification notification = Notification.builder()
            .userId(user.getId())
            .recipient(request.getRecipient())
            .channel(request.getChannel())
            .subject(request.getSubject())
            .message(request.getMessage())
            .status(Notification_status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    notificationRepository.save(notification);

    try {
        if (request.getChannel() == Channel.EMAIL) {
            emailService.sendSimpleEmail(user.getEmail(), request.getSubject(), request.getMessage());
        } else if (request.getChannel() == Channel.TELEGRAM) {
            telegramService.sendMessage(user.getTelegramChatId(), request.getMessage());
        } else {
            throw new RuntimeException("No valid channel for user");
        }

        notification.setStatus(Notification_status.SENT);
        notification.setSentAt(LocalDateTime.now());
    } catch (Exception e) {
        notification.setStatus(Notification_status.FAILED);
        notification.setError_message(e.getMessage());
    }

    notificationRepository.save(notification);
}
}
