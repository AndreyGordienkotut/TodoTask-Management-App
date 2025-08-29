package com.userService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDto {
//    private String token;
//    private String refreshToken;
    private Long userId;
    private String username;
    private String email;
}