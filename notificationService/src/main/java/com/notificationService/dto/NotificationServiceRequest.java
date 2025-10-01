package com.notificationService.dto;

import com.notificationService.model.Channel;
import com.notificationService.model.Notification_status;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body containing information about notification")
public class NotificationServiceRequest {
    @Schema(description = "User id", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(description = "Recipient notification for email", example = "andrey@gmail.com")
    private String recipient;
    @Schema(description = "Recipient notification for telegram", example = "123412421")
    private Long recipientTelegramId;
    @Schema(description = "Subject notification", example = "The task will soon be overdue!")
    private String subject;
    @Schema(description = "Message notification", example = "Less than 15 minutes left until the deadline for the task. Hurry up!",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Message is required")
    private String message;
    @Schema(description = "Сhannel where the notification is sent", example = "EMAIL",requiredMode = Schema.RequiredMode.REQUIRED)
    private Channel channel;
    @Schema(description = "Status notification", example = "SENT",requiredMode = Schema.RequiredMode.REQUIRED)
    private Notification_status status;
    @Schema(description = "Date when the task needs to be completed in notification", example = "2025-09-29 20:00:00")
    private LocalDateTime atDate;
    @Schema(description = "Sending date notification", example = "2025-09-29 19:36:19", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
}
