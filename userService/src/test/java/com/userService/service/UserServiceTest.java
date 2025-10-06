package com.userService.service;

import com.userService.config.NotificationServiceClient;
import com.userService.dto.AuthenticationRequestDto;
import com.userService.dto.AuthenticationResponseDto;
import com.userService.dto.RefreshTokenRequestDto;
import com.userService.dto.RegisterRequestDto;
import com.userService.exception.BadRequestException;
import com.userService.exception.UserAlreadyExistsException;
import com.userService.model.EmailVerificationTokens;
import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.repository.EmailVerificationTokensRepository;
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
import java.time.LocalDateTime;
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
    @Mock
    private NotificationServiceClient notificationServiceClient;
    @Mock
    private EmailVerificationTokensRepository emailVerificationTokensRepository;

    private User user;
    private RefreshToken refreshToken;
    private EmailVerificationTokens tokenEntity = new EmailVerificationTokens();

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
        user.setVerified(true);


        refreshToken = new RefreshToken();
        refreshToken.setToken("validRefreshToken");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        tokenEntity.setToken("validToken");
        tokenEntity.setExpiryAt(LocalDateTime.now().plusHours(1));
        tokenEntity.setUsed(false);
        tokenEntity.setUser(user);

    }
    @Test
    @DisplayName("Success - register")
    void registerUser()  {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("mockJwtToken");
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);
        when(emailVerificationTokensRepository.save(any(EmailVerificationTokens.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthenticationResponseDto response = userService.register(registerRequestDto);
        assertThat(response.getUsername()).isEqualTo("test@example.com");
        assertThat(response.getToken()).isEqualTo("mockJwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(anyMap(), any(User.class));
        verify(refreshTokenService).createRefreshToken(1L);
        verify(emailVerificationTokensRepository).save(any(EmailVerificationTokens.class));
    }
    @Test
    @DisplayName("BadRequestException - register - User with this email already exists")
    void registerUserFail_whenUserAlreadyExists()  {
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequestDto));

        verify(userRepository).findByEmail("test@example.com");
    }
    @Test
    @DisplayName("Success - authenticate")
    void authenticateUserSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).
                thenReturn(null);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("mockJwtToken");
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        AuthenticationResponseDto response = userService.authenticate(authenticationRequestDto);
        assertThat(response.getUsername()).isEqualTo("test@example.com");
        assertThat(response.getToken()).isEqualTo("mockJwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(anyMap(), any(User.class));
        verify(refreshTokenService).createRefreshToken(1L);
    }
    @Test
    @DisplayName("BadRequestException - authenticate - Invalid email or password")
    void authenticateUserFail() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        assertThrows(BadRequestException.class, () -> userService.authenticate(authenticationRequestDto));

        verify(userRepository, never()).findByEmail(any(String.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }
    @Test
    @DisplayName("BadRequestException - authenticate - Email is not verified")
    void authenticateUserFail_whenEmailIsNotVerified() {
        user.setVerified(false);
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        AuthenticationRequestDto request = new AuthenticationRequestDto("test@example.com", "password");

        assertThrows(BadRequestException.class, () -> userService.authenticate(request));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService, never()).generateToken(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @Test
    @DisplayName("Success - refreshToken")
    void refreshTokenSuccess() {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtService.generateToken(any(User.class))).thenReturn("newAccessToken");

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("validRefreshToken");
        AuthenticationResponseDto response = userService.refreshToken(request);
        assertThat(response.getToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("validRefreshToken");
        assertThat(response.getUsername()).isEqualTo("test@example.com");

        verify(refreshTokenService).findByToken("validRefreshToken");
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("BadRequestException - refreshToken - Refresh token is not in database")
    void refreshTokenFail()  {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.empty());
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalidRefreshToken");
        assertThrows(BadRequestException.class, () -> userService.refreshToken(request));
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());

    }
    @Test
    @DisplayName("Success - verifyEmail")
    void verifyEmailSuccess() {
        when(emailVerificationTokensRepository.findByToken("validToken")).thenReturn(Optional.of(tokenEntity));
        userService.verifyEmail("validToken");

        assertThat(user.isVerified()).isTrue();
        assertThat(tokenEntity.isUsed()).isTrue();

        verify(userRepository).save(user);
        verify(emailVerificationTokensRepository).save(tokenEntity);
    }
    @Test
    @DisplayName("BadRequestException - verifyEmail - Invalid or expired token")
    void verifyEmailInvalidToken() {
        when(emailVerificationTokensRepository.findByToken("badToken"))
                .thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> userService.verifyEmail("badToken"));
        verify(emailVerificationTokensRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
    @Test
    @DisplayName("BadRequestException - verifyEmail - Email verification expired")
    void verifyEmailExpiredToken() {
        tokenEntity.setExpiryAt(LocalDateTime.now().minusDays(1));
        when(emailVerificationTokensRepository.findByToken("expiredToken")).thenReturn(Optional.of(tokenEntity));
        assertThrows(BadRequestException.class, () -> userService.verifyEmail("expiredToken"));
        verify(emailVerificationTokensRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
    @Test
    @DisplayName("BadRequestException - verifyEmail - Email verification token already used")
    void verifyEmailTokenAlreadyUsed() {
        tokenEntity.setUsed(true);
        when(emailVerificationTokensRepository.findByToken("usedToken")).thenReturn(Optional.of(tokenEntity));
        assertThrows(BadRequestException.class, () -> userService.verifyEmail("usedToken"));
        verify(emailVerificationTokensRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Success - logout")
    void logoutSuccess()  {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        userService.logout("validRefreshToken");
        verify(refreshTokenService,times(1)).delete(refreshToken);
    }
    @Test
    @DisplayName("Failed - logout - token is not found")
    void logoutFail()  {
        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.empty());
        userService.logout("invalidRefreshToken");
        verify(refreshTokenService,never()).delete(refreshToken);
    }
}
