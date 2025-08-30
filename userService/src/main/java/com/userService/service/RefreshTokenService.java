package com.userService.service;

import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.repository.RefreshTokenRepository;
import com.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new BadRequestException("User not found with ID: " + userId));
        User user = userRepository.findById(userId).orElse(null);
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
    @Transactional
    public void deleteRefreshTokenForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
