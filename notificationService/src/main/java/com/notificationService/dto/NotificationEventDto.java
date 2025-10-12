package com.notificationService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "A generic Kafka event DTO containing basic information required for notification processing.")
public class NotificationEventDto {
    @Schema(description = "Unique identifier of the user.", example = "99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(description = "User's email address.", example = "test@example.com")
    private String email;
    @Schema(description = "User's Telegram Chat ID.", example = "123456789")
    private Long telegramChatId;
    @Schema(description = "The subject line of the notification.", example = "System Alert: Maintenance Required")
    private String subject;
    @Schema(description = "The main content/body of the notification message.", example = "The system will undergo maintenance tonight at 02:00 UTC.")
    private String message;
    @Schema(description = "The specific type of event being sent (e.g., SYSTEM_ALERT).", example = "SYSTEM_ALERT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventType;
    @Schema(description = "Timestamp indicating when the event was generated.", example = "2025-10-10 18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
}