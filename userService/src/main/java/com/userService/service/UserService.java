package com.userService.service;

import com.userService.dto.AuthenticationRequestDto;
import com.userService.dto.AuthenticationResponseDto;
import com.userService.dto.RegisterRequestDto;

import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
//    private final RefreshTokenService refreshTokenService;
    @Transactional
    public AuthenticationResponseDto register(RegisterRequestDto requestDto) {
//        if(userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
//            throw new BadRequestException("User with this email already exists.");
//        }
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User savedUser = userRepository.save(user);
        //створити токен по користувачу
//        String jwtToken = jwtService
        //створити рефреш токену
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
        return AuthenticationResponseDto.builder()
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
//            throw new BadRequestException("Invalid email or password.");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " +request.getEmail()));
//        token
        return AuthenticationResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

    }

}
