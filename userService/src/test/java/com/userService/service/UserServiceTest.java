package com.userService.service;

import com.userService.dto.AuthenticationRequestDto;
import com.userService.dto.AuthenticationResponseDto;
import com.userService.dto.RefreshTokenRequestDto;
import com.userService.dto.RegisterRequestDto;
import com.userService.exception.BadRequestException;
import com.userService.exception.UserAlreadyExistsException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private  UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private  RefreshTokenRepository refreshTokenRepository;
    @Mock
    private  JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @InjectMocks
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;

    private User user;
    private RefreshToken refreshToken;
    private AuthenticationRequestDto authenticationRequestDto;
    private AuthenticationResponseDto authenticationResponseDto;
    private RegisterRequestDto registerRequestDto;

    @BeforeEach
    void setUp() {
        registerRequestDto = new RegisterRequestDto("testuser", "test@example.com", "password");
        authenticationRequestDto = new AuthenticationRequestDto("test@example.com", "password");
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");


        refreshToken = new RefreshToken();
        refreshToken.setToken("validRefreshToken");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
    }
    @Test
    @DisplayName("Тест успішної реєстрації")
    void registerUser()  {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("mockJwtToken");
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        AuthenticationResponseDto response = userService.register(registerRequestDto);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getToken()).isEqualTo("mockJwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(refreshTokenService).createRefreshToken(1L);
    }
    @Test
    @DisplayName("Тест неуспішної реєстрації через незнаходження користувача")
    void registerUserFail_whenUserAlreadyExists()  {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequestDto));

        verify(userRepository).findByEmail("test@example.com");
    }
    @Test
    @DisplayName("Тест успішної аутентифікації")
    void authenticateUserSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).
                thenReturn(null);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("mockJwtToken");
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        AuthenticationResponseDto response = userService.authenticate(authenticationRequestDto);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getToken()).isEqualTo("mockJwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(User.class));
        verify(refreshTokenService).createRefreshToken(1L);
    }
    @Test
    @DisplayName("Тест неуспішної аутентифікації")
    void authenticateUserFail() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        assertThrows(BadRequestException.class, () -> userService.authenticate(authenticationRequestDto));

        verify(userRepository, never()).findByEmail(any(String.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }
    @Test
    @DisplayName("Тест на успішне відновлення токену")
    void refreshTokenSuccess() {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtService.generateToken(any(User.class))).thenReturn("newAccessToken");

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("validRefreshToken");
        AuthenticationResponseDto response = userService.refreshToken(request);
        assertThat(response.getToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(refreshTokenService).findByToken("validRefreshToken");
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("Тест на неуспішне відновлення токену через відсутність refreshToken")
    void refreshTokenFail()  {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.empty());
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalidRefreshToken");
        assertThrows(BadRequestException.class, () -> userService.refreshToken(request));
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());

    }
    @Test
    @DisplayName("Тест на успішній logout з системи")
    void logoutSuccess()  {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        userService.logout("validRefreshToken");
        verify(refreshTokenService,times(1)).delete(refreshToken);
    }
    @Test
    @DisplayName("Вихід із системи: нічого не відбувається, якщо токен не знайдено")
    void logoutFail()  {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.empty());
        userService.logout("invalidRefreshToken");
        verify(refreshTokenService,never()).delete(refreshToken);
    }
}
