package com.userService.controller;

import com.userService.dto.AuthenticationRequestDto;
import com.userService.dto.AuthenticationResponseDto;
import com.userService.dto.RefreshTokenRequestDto;
import com.userService.dto.RegisterRequestDto;
import com.userService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.OK);
    }
    @PostMapping("/authenticate")
    public  ResponseEntity<AuthenticationResponseDto> authenticate(@Valid @RequestBody AuthenticationRequestDto request) {
        return new ResponseEntity<>(userService.authenticate(request), HttpStatus.OK);
    }
    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponseDto> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }














//
//
//    @PostMapping("/authenticate")
//    public ResponseEntity<AuthenticationResponseDto> authenticate(@Valid @RequestBody AuthenticationRequestDto request) {
//        return new ResponseEntity<>(authService.authenticate(request), HttpStatus.OK);
//    }
//    @PostMapping("/refresh-token")
//    public ResponseEntity<AuthenticationResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
//        return new ResponseEntity<>(authService.refreshToken(request), HttpStatus.OK);
//    }
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDto request) {
//        authService.logout(request.getRefreshToken());
//        return ResponseEntity.noContent().build();
//    }
}
