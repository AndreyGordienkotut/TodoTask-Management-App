package com.userService.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for user authentication")
public class AuthenticationRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "The user's unique email address", example = "andrey@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "The user's password ", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}