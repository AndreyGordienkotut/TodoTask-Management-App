package com.userService.dto;

import com.userService.model.RefreshToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object containing JWT and Refresh Token after successful authentication or registration")
public class AuthenticationResponseDto {
    @Schema(description = "The JWT Access Token for subsequent authenticated requests", example = "eyJhbGciOiJIUzI1NiI...")
    private String token;

    @Schema(description = "The Refresh Token used to obtain a new Access Token", example = "f8b9e6a0-...")
    private String refreshToken;

    @Schema(description = "The unique ID of the registered/authenticated user", example = "101")
    private Long userId;

    @Schema(description = "The username of the user", example = "andrey")
    private String username;

    @Schema(description = "The email of the user", example = "andrey.com")
    private String email;
}