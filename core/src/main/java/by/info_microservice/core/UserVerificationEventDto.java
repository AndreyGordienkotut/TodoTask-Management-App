package by.info_microservice.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Kafka event DTO for user verification requests, sent after registration.")
public class UserVerificationEventDto {

    @Schema(description = "Identifier of the user requiring verification.", example = "55", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "User's display name.", example = "Alex")
    private String name;

    @Schema(description = "User's email address for sending the verification link.", example = "newuser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "User's Telegram Chat ID (optional).", example = "987654321")
    private Long telegramChatId;

    @Schema(description = "The unique token required to complete the verification process.", example = "xyz789token456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationToken;

    @Schema(description = "Specifies the type of user event (e.g., USER_REGISTERED).", example = "USER_REGISTERED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventType;

    @Schema(description = "Timestamp when the event was generated.", example = "2025-10-10 18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
}
