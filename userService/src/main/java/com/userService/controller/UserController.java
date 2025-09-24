package com.userService.controller;

import com.userService.dto.*;
import com.userService.model.User;
import com.userService.repository.UserRepository;
import com.userService.service.JwtService;
import com.userService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto dto = userService.findUserById(String.valueOf(id));
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto> getUserForInternal(@PathVariable Long id) {
        UserDto dto = userService.findUserById(String.valueOf(id));
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@Valid @RequestBody AuthenticationRequestDto request) {
        return new ResponseEntity<>(userService.authenticate(request), HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return new ResponseEntity<>(userService.refreshToken(request), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponseDto> logout(@RequestBody RefreshTokenRequestDto request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/telegram")
    public void updateTelegramChatId(@PathVariable Long id, @RequestParam Long chatId) {
        userService.updateTelegramChatId(id, chatId);
    }
    @GetMapping("/by-email")
    public UserDto getByEmail(@RequestParam String email) {
        return userService.getByEmail(email);
    }

    @PostMapping("/generate-telegram-token")
    public ResponseEntity<String> generateTelegramToken(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String token = userService.generateTelegramToken(user);
        return ResponseEntity.ok(token);
    }
    @PostMapping("/link-by-token")
    public ResponseEntity<?> linkTelegram(@RequestBody LinkTelegramRequest request) {
        Optional<User> user = userRepository.findByTelegramLinkToken(request.getToken());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid token");
        }
        User u = user.get();
        u.setTelegramChatId(request.getChatId());
        userRepository.save(u);
        return ResponseEntity.ok("Linked");
    }


}
