package com.userService.service;

import com.userService.exception.BadRequestException;
import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.repository.RefreshTokenRepository;
import com.userService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 100000L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("mockRefreshToken");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(100000L));
    }
    @Test
    @DisplayName("Перевірка створення нового Refresh Token")
    void createRefreshToken_shouldCreateNewToken() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);


        RefreshToken createdToken = refreshTokenService.createRefreshToken(user.getId());
        assertThat(createdToken).isNotNull();
        assertThat(createdToken.getToken()).isEqualTo("mockRefreshToken");
        verify(userRepository).findById(user.getId());
        verify(refreshTokenRepository).findByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));

    }
    @Test
    @DisplayName("Перевірка провалу створення нового Refresh Token через незнаходження користувача")
    void createRefreshToken_shouldCreateNewToken_whenUserIsNotExist() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> refreshTokenService.createRefreshToken(user.getId()));
        verify(userRepository).findById(user.getId());
    }
    @Test
    @DisplayName("Перевірка дійсного токена: повинен повернути токен без помилок")
    void verifyExpiration_shouldReturnTokenWhenValid() {
        refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

        RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(refreshToken);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }
    @Test
    @DisplayName("Перевірка простроченого токена: повинен викинути виняток і видалити токен")
    void verifyExpiration_shouldThrowExceptionAnd_deleteTokenWhenExpired() {
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));
        assertThrows(BadRequestException.class, () -> refreshTokenService.verifyExpiration(refreshToken));

        verify(refreshTokenRepository).delete(refreshToken);
    }


}