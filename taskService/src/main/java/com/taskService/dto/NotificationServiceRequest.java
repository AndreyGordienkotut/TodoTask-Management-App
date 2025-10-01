package com.taskService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String message;
    @Schema(description = "Сhannel where the notification is sent", example = "EMAIL",requiredMode = Schema.RequiredMode.REQUIRED)
    private String channel;
    @Schema(description = "Status notification", example = "SENT",requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
    @Schema(description = "Sending date notification", example = "2025-09-29 19:36:19", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;



}
