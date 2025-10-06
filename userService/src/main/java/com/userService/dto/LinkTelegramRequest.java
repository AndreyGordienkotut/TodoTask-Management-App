package com.userService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body containing to link a user's account to Telegram")
public class LinkTelegramRequest {
    @Schema(description = "Token for ling a user's account to telegram", example = "f14fde3a-e170-41a7-a1e1-73bcfdfe6e26", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
    @Schema(description = "Identification chat telegram", example = "1513562345", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chatId;
}

