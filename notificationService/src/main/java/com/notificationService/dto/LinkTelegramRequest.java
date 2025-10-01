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
@Schema(description = "Request object for linking a user account with a Telegram chat via a token.")
public class LinkTelegramRequest {
    @Schema(description = "Unique token generated for the user to link the Telegram account", example = "a1b2c3d4e5f6g7h8")
    private String token;
    @Schema(description = "The Telegram Chat ID provided by the messaging platform", example = "1234567890")
    private Long chatId;
}
