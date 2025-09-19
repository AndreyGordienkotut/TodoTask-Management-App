package com.notificationService.service;

import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.model.Notification;
import com.notificationService.model.Notification_status;
import com.notificationService.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void sendNotification(NotificationServiceRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .recipient(request.getRecipient())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(Notification_status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        try {
            emailService.sendSimpleEmail(
                    request.getRecipient(),
                    request.getSubject(),
                    request.getMessage()
            );
            notification.setStatus(Notification_status.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            notification.setStatus(Notification_status.FAILED);
            notification.setError_message(e.getMessage());
        }

        notificationRepository.save(notification);
    }
}
