package com.userService.service;

import com.userService.dto.*;

import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.exception.*;
import com.userService.repository.RefreshTokenRepository;
import com.userService.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    @Transactional
    public AuthenticationResponseDto register(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User savedUser = userRepository.save(user);
        Map<String, Object> extra = new HashMap<>();
        extra.put("userId", user.getId());
        String jwtAccessToken = jwtService.generateToken(extra, user);
//        String jwtAccessToken = jwtService.generateToken(savedUser);
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
            throw new BadRequestException("Invalid email or password.");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " +request.getEmail()));
//        String jwtAccessToken = jwtService.generateToken(user);
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
    public void logout(String refreshToken) {
        Optional<RefreshToken> tokenOptional = refreshTokenService.findByToken(refreshToken);
        if (tokenOptional.isPresent()) {
            refreshTokenService.delete(tokenOptional.get());
        }
    }

    public UserDto findUserById(String userId) {
        return userRepository.findById(Long.valueOf(userId))
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .name(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .orElse(null);
    }

}
