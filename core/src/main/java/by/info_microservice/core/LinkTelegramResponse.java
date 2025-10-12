package by.info_microservice.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response object for linking a user account with a Telegram chat via a token.")
public class LinkTelegramResponse {
    @Schema(description = "The Telegram Chat ID provided by the messaging platform", example = "1234567890",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chatId;
    @Schema(description = "Success/failure", example = "1234567890",requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;
}