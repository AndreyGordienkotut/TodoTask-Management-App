package com.userService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userService.dto.AuthenticationRequestDto;
import com.userService.dto.RefreshTokenRequestDto;
import com.userService.dto.RegisterRequestDto;
import com.userService.model.RefreshToken;
import com.userService.model.User;
import com.userService.repository.RefreshTokenRepository;
import com.userService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.8-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("application.security.jwt.secret-key", () -> "0c1o5t7vPfsdf42f8yQ9z3a5b6c7d8eF0g1H2i3J4k5L6m7N8o9p0q1R2s3T4u5V6w7X8y9Z0A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z");
        registry.add("application.security.jwt.expiration", () -> "3600000");
        registry.add("application.security.jwt.refresh-token.expiration", () -> "604800000");
        registry.add("application.security.jwt.cookie-name", () -> "jwt_token");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }
    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    @DisplayName("Реєстрація: успішний сценарій")
    void registerSuccesfull() throws Exception {
        RegisterRequestDto requestDto = new RegisterRequestDto("newuser", "newuser@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertThat(userRepository.findByEmail("newuser@example.com")).isPresent();
    }
    @Test
    @DisplayName("Реєстрація: неуспішний сценарій, користувач вже існує")
    void registerFailure_userAlreadyExists() throws Exception {
        userRepository.save(new User("newuser", "newuser@example.com", "password123"));
        RegisterRequestDto requestDto = new RegisterRequestDto("newuser", "newuser@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User with this email already exists.")));
        assertThat(userRepository.count()).isEqualTo(1);
    }
    @Test
    @DisplayName("Тест успішної аутентифікації")
    void authenticateSuccesfull() throws Exception {
        String rawPassword = "password123";
        userRepository.save(new User("testuser", "testuser@example.com", passwordEncoder.encode(rawPassword)));
        AuthenticationRequestDto requestDto = new AuthenticationRequestDto("testuser@example.com", rawPassword);
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
    @Test
    @DisplayName("Тест неуспішної аутентифікації, через неправильні дані")
    void authenticateFailure_userNotFound() throws Exception {
        String rawPassword = "password123";
        userRepository.save(new User("testuser", "testuser@example.com", passwordEncoder.encode(rawPassword)));
        AuthenticationRequestDto requestDto = new AuthenticationRequestDto("test@example.com", "wrongpassword");
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid email or password.")));
    }
    @Test
    @DisplayName("refreshToken - успішно")
    void refreshTokenSuccesfull() throws Exception {
        User user = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .build());
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build());
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(token);
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(token));
        assertThat(refreshTokenRepository.findByToken(token)).isPresent();
    }
    @Test
    @DisplayName("refreshToken - BadRequestException, токен просрочений")
    void refreshTokenFailure_badRequest() throws Exception {
        User user = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .build());
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .expiryDate(Instant.now().minusSeconds(1))
                .user(user)
                .build());
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(token);
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Refresh token was expired. Please make a new sign-in request.")));
    }
    @Test
    @DisplayName("logout - успішний сценарій")
    void logoutSuccesfull() throws Exception {
        User user = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .build());
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build());
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(token);
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent());
        assertThat(refreshTokenRepository.findByToken(token)).isNotPresent();
    }
    @Test
    @DisplayName("logout - успішний сценарій")
    void logoutFailure_userNotFound() throws Exception {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto("non-existent-token");
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent());
    }




}
