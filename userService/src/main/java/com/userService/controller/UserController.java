package com.userService.controller;

import by.info_microservice.core.LinkTelegramRequest;
import com.userService.dto.*;
import com.userService.model.User;
import com.userService.repository.UserRepository;
import com.userService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication and Internal Access", description = "API for user registration, authentication, and internal service calls.")
@SecurityRequirement(name = "BearerAuth")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    @Operation(summary = "Get User by ID (Internal Use)",
            description = "Retrieves user details by ID. This endpoint is typically used by other microservices.",
            tags = {"Internal Access"})
    @ApiResponse(responseCode = "200", description = "User successfully found and returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found.",
            content = @Content(schema = @Schema(hidden = true)))

    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto> getUserForInternal(
            @Parameter(description = "Unique ID of the user to retrieve", example = "101")
            @PathVariable Long id) {
        UserDto dto = userService.findUserById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
    @Operation(summary = "Register user",
            description = "Creates a new user account, encrypts the password, sends a welcome/verification notification, and returns JWT tokens.",
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details (username, email, password)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequestDto.class)))
    )
    @ApiResponse(responseCode = "200", description = "Registration successful, returns access and refresh tokens.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request payload (validation errors) or user already exists.",
            content = @Content(schema = @Schema(hidden = true)))

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.OK);
    }
    @Operation(summary = "Authenticate user",
            description = "Authenticate in user account",
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User authenticate credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthenticationRequestDto.class)))
    )
    @ApiResponse(responseCode = "200", description = "Authenticate successful, returns tokens.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid email or password.",
            content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@Valid @RequestBody AuthenticationRequestDto request) {
        return new ResponseEntity<>(userService.authenticate(request), HttpStatus.OK);
    }
    @Operation(summary = "Refresh token",
            description = "Uses a valid refresh token to issue a new JWT access token and refresh token",
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "refresh token",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequestDto.class)))
    )
    @ApiResponse(responseCode = "200", description = "Refresh token successful, returns new tokens.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshTokenRequestDto.class)))
    @ApiResponse(responseCode = "400", description = "Refresh token is not in database",
            content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return new ResponseEntity<>(userService.refreshToken(request), HttpStatus.OK);
    }
    @Operation(summary = "Verify email",
            description = "Validates the user's email using a token sent via a verification link",
            tags = {"Authentication"}
    )
    @ApiResponse(responseCode = "200", description = "Email verified successfully",
            content = @Content( schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400", description = "Email verification expired",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Email verification token already used",
            content = @Content(schema = @Schema(hidden = true)))
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        return new ResponseEntity<>("Email verified successfully!",HttpStatus.OK);
    }
    @Operation(summary = "Logout",
            description = "Deletes the Refresh Token from the database to invalidate the user's session",
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token to be invalidated",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequestDto.class)))
    )
    @ApiResponse(responseCode = "204", description = "Logout successful",
            content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponseDto> logout(@RequestBody RefreshTokenRequestDto request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Update Telegram Chat ID (Internal Use)",
            description = "Updates the stored Telegram Chat ID for a specific user. Intended for use by other microservices.",
            tags = {"Internal Access"})
    @ApiResponse(responseCode = "200", description = "Chat ID updated successfully.")
    @ApiResponse(responseCode = "404", description = "User ID not found.")
    @PatchMapping("/{id}/telegram")
    public void updateTelegramChatId(@PathVariable Long id, @RequestParam Long chatId) {
        userService.updateTelegramChatId(id, chatId);
    }
    @Operation(summary = "Get User by Email (Internal Use)",
            description = "Retrieves user details by email. This endpoint is typically used by other microservices.",
            tags = {"Internal Access"})
    @ApiResponse(responseCode = "200", description = "User successfully found and returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found.",
            content = @Content(schema = @Schema(hidden = true)))
    @GetMapping("/by-email")
    public UserDto getByEmail(@RequestParam String email) {
        return userService.getByEmail(email);
    }

    @Operation(summary = "Generate Telegram Linking Token",
            description = "Generates a temporary, single-use token that the authenticated user must send to the Telegram bot to link their accounts",
            tags = {"Telegram Integration", "Authentication"})
    @ApiResponse(responseCode = "200", description = "Token generated successfully.",
            content = @Content(schema = @Schema(implementation = String.class, example = "xyz123token")))
    @ApiResponse(responseCode = "404", description = "RuntimeException - user not found")
    @PostMapping("/generate-telegram-token")
    public ResponseEntity<String> generateTelegramToken(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String token = userService.generateTelegramToken(user);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Link Telegram Chat ID by Token",
            description = "Endpoint used by the Telegram bot to finalize the linking process by exchanging the token for the user's ID and setting the Chat ID.",
            tags = {"Telegram Integration"})
    @ApiResponse(responseCode = "200", description = "Account linked successfully.")
    @ApiResponse(responseCode = "404", description = "Invalid token provided.")
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
