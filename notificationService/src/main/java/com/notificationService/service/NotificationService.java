package com.notificationService.service;


//import com.notificationService.config.UserServiceClientF;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.dto.UserDto;
import com.notificationService.exception.*;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification;
import com.notificationService.model.Notification_status;
import com.notificationService.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TelegramService telegramService;
    public void sendNotification(NotificationServiceRequest request) {
        if (request.getChannel() == null || request.getMessage() == null) {
            throw new InvalidNotificationRequestException("Channel and message must not be null");
        }
         Notification notification = Notification.builder()
            .userId(request.getUserId())
            .recipient(request.getRecipient())
            .recipientTelegramId(request.getRecipientTelegramId())
            .channel(request.getChannel())
            .subject(request.getSubject())
            .message(request.getMessage())
            .status(Notification_status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        notification = notificationRepository.saveAndFlush(notification);

        processNotificationAsync(notification.getId(), request);
    }
    @Async("taskExecutor")
    public void processNotificationAsync(Long notificationId, NotificationServiceRequest request) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification with id="  + notificationId + " not found"));
        try {
            switch (request.getChannel()) {
                case EMAIL -> {
                    try {
                        emailService.sendSimpleEmail(request.getRecipient(),
                                request.getSubject(),
                                request.getMessage());
                        notification.setStatus(Notification_status.SENT);
                    } catch (Exception e) {
                        notification.setStatus(Notification_status.FAILED);
                        notification.setError_message(e.getMessage());
                        //throw new EmailSendException("Failed to send email to " + request.getRecipient(), e);
                    }
                }
                case TELEGRAM -> {
                    try {
                        telegramService.sendMessage(request.getRecipientTelegramId(),
                                request.getMessage());
                        notification.setStatus(Notification_status.SENT);
                    } catch (Exception e) {
                        notification.setStatus(Notification_status.FAILED);
                        notification.setSentAt(LocalDateTime.now());
                        //throw new TelegramSendException("Failed to send telegram to " + request.getRecipientTelegramId(), e);
                    }
                }
                default -> throw new InvalidNotificationRequestException("Unsupported channel: " + request.getChannel());
            }

//            notification.setStatus(Notification_status.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (RuntimeException ex) {
            log.error("Notification {} failed: {}", notification.getId(), ex.getMessage(), ex);
            notification.setStatus(Notification_status.FAILED);
            throw new NotificationProcessingException("Notification processing failed for id=" + notification.getId(), ex);
        }finally {
            notificationRepository.save(notification);
        }


    }
}
