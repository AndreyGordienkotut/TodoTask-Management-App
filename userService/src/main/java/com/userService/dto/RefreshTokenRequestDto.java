package com.userService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body containing the refresh token for token renewa")
public class RefreshTokenRequestDto {
    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token", example = "2134cds213eqwecv", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
