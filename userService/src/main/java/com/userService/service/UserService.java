package com.userService.service;

import by.info_microservice.core.UserVerificationEventDto;
import com.userService.dto.*;

import com.userService.model.EmailVerificationTokens;
import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.exception.*;
import com.userService.repository.EmailVerificationTokensRepository;
import com.userService.repository.RefreshTokenRepository;
import com.userService.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokensRepository emailVerificationTokensRepository;
    private final UserEventProducer userEventProducer;

    @Transactional
    public AuthenticationResponseDto register(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setVerified(false);
        User savedUser = userRepository.save(user);
        String token = UUID.randomUUID().toString();
        EmailVerificationTokens emailVerificationTokens = EmailVerificationTokens.builder()
                .user(savedUser)
                .token(token)
                .used(false)
                .expiryAt(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokensRepository.save(emailVerificationTokens);

        UserVerificationEventDto event = UserVerificationEventDto.builder()
                .userId(savedUser.getId())
                .name(savedUser.getUsername())
                .email(savedUser.getEmail())
                .telegramChatId(savedUser.getTelegramChatId())
                .eventType("USER_VERIFICATION_REQUESTED")
                .createdAt(LocalDateTime.now())
                .verificationToken(token)
                .build();
        try {
            userEventProducer.sendAccountVerificationEvent(event);
            log.info("User verification event published for userId={}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to publish verification event for userId={}. Error: {}", savedUser.getId(), e.getMessage(), e);
        }
        Map<String, Object> extra = new HashMap<>();
        extra.put("userId", user.getId());
        String jwtAccessToken = jwtService.generateToken(extra, user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
        return AuthenticationResponseDto.builder()
                .token(jwtAccessToken)
                .refreshToken(refreshToken.getToken())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
    }
    @Transactional
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            System.err.println("Authentication failed for user " + request.getEmail() + ": " + e.getMessage());
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " +request.getEmail()));
        if (!user.isVerified()) {
            throw new BadRequestException("Email is not verified. Please check your inbox.");
        }
        Map<String, Object> extra = new HashMap<>();
        extra.put("userId", user.getId());
        String jwtAccessToken = jwtService.generateToken(extra, user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return AuthenticationResponseDto.builder()
                .token(jwtAccessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

    }
   @Transactional
    public AuthenticationResponseDto refreshToken(RefreshTokenRequestDto request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

       return AuthenticationResponseDto.builder()
               .token(newAccessToken)
               .refreshToken(refreshToken.getToken())
               .userId(user.getId())
               .username(user.getUsername())
               .email(user.getEmail())
               .build();
    }
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationTokens emailVerificationTokens = emailVerificationTokensRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException  ("Invalid or expired token."));
        if(emailVerificationTokens.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Email verification expired.");
        }
        if(emailVerificationTokens.isUsed()){
            throw new BadRequestException("Email verification token already used.");
        }
        emailVerificationTokens.setUsed(true);
        emailVerificationTokensRepository.save(emailVerificationTokens);
        User user = emailVerificationTokens.getUser();
        user.setVerified(true);
        userRepository.save(user);

    }
    @Transactional
    public void logout(String refreshToken) {
        Optional<RefreshToken> tokenOptional = refreshTokenService.findByToken(refreshToken);
        if (tokenOptional.isPresent()) {
            refreshTokenService.delete(tokenOptional.get());
        }
    }

public UserDto findUserById(Long userId) {
    return userRepository.findById(userId)
            .map(user -> UserDto.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .email(user.getEmail())
                    .telegramChatId(user.getTelegramChatId())
                    .build())
            .orElse(null);
}
    public void updateTelegramChatId(Long userId, Long chatId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setTelegramChatId(chatId);
        userRepository.save(user);
    }
    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDto.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .telegramChatId(user.getTelegramChatId())
                .build();
    }

    public String generateTelegramToken(User user) {
        User u = userRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String token = UUID.randomUUID().toString();
        u.setTelegramLinkToken(token);
        userRepository.save(u);
        return token;
    }

}
