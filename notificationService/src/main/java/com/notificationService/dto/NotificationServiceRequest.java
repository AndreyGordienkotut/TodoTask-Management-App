package com.notificationService.dto;

import com.notificationService.model.Channel;
import com.notificationService.model.Notification_status;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationServiceRequest {
    private Long userId;
    private String recipient;
    private Long recipientTelegramId;
    private String subject;
    @NotBlank(message = "Message is required")
    private String message;
    private Channel channel;
    private Notification_status status;
    private LocalDateTime atDate;
}