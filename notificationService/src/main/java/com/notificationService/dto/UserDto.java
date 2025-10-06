package com.notificationService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for basic user information.")
public class UserDto {
    @Schema(description = "Unique ID of the user", example = "101")
    private Long id;
    @Schema(description = "User's full name or username", example = "andrey")
    private String name;
    @Schema(description = "User's email address", example = "andrey@example.com")
    private String email;
    @Schema(description = "Telegram Chat ID for receiving notifications", example = "1234567890")
    private Long telegramChatId;
}