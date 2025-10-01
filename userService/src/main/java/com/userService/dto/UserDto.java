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
@Schema(description = "User information returned by the API")
public class UserDto {
    @Schema(description = "Unique user identifier", example = "999")
    private Long id;

    @Schema(description = "Username (login)", example = "Andrey")
    private String name;

    @Schema(description = "Email address", example = "Andrey@gmail.com")
    private String email;

    @Schema(description = "Telegram chat ID for notifications", example = "144", nullable = true)
    private Long telegramChatId;

}
