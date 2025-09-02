package com.userService.service;


import com.userService.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @InjectMocks
    @Spy
    private JwtService jwtService;
    private User user;
    private User anotherUser;
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L); // 1 секунда для теста
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 100000L); // 100 секунд

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@test.com");

        anotherUser = new User();
        anotherUser.setEmail("wrong@test.com");
    }
    @Test
    @DisplayName("Перевірка генерації токена та вилучення імені користувача")
    void generateTokenAndExtractUsername() {
        String token = jwtService.generateToken(user);
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getEmail());
    }
    @Test
    @DisplayName("Перевірка валідності токена для правильного користувача")
    void isTokenValidForCorrectUser() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("extractUsername кидає ExpiredJwtException для простроченого токена")
    void extractUsernameThrowsExceptionForExpiredToken() throws InterruptedException {
        // Устанавливаем короткий срок годности ТОЛЬКО для этого теста
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100L);
        String token = jwtService.generateToken(user);
        Thread.sleep(200);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

}
