package by.info_microservice.core;

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
@Schema(description = "Kafka event DTO containing detailed task and recipient information for notification reminders.")
public class TaskEventDto {

    @Schema(description = "Identifier of the task that triggered the event.", example = "99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @Schema(description = "Identifier of the task owner.", example = "40", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "Recipient's email address.", example = "andrey@example.com")
    private String recipient;

    @Schema(description = "Recipient's Telegram Chat ID.", example = "987654321")
    private Long recipientTelegramId;

    @Schema(description = "The subject line for the task alert.", example = "Task Overdue!")
    private String subject;

    @Schema(description = "Title of the task.", example = "Implement Kafka DLT")
    private String title;

    @Schema(description = "Full description of the task.", example = "Ensure reliable message handling using the Dead Letter Topic mechanism.")
    private String description;

    @Schema(description = "The task's deadline date and time.", example = "2025-10-15 17:00:00")
    private LocalDateTime dueDate;

    @Schema(description = "The specific channel this message is intended for (EMAIL/TELEGRAM).", example = "EMAIL")
    private String channel;

    @Schema(description = "Specifies the exact type of task event (e.g., TASK_OVERDUE).", example = "TASK_OVERDUE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventType;

    @Schema(description = "The generated notification message content.", example = "Your task 'Implement Kafka DLT' is now overdue.")
    private String message;

    @Schema(description = "Timestamp when the event was generated.", example = "2025-10-10 18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Status of the notification request (usually PENDING).", example = "NOT_COMPLETED")
    private String status;
}
