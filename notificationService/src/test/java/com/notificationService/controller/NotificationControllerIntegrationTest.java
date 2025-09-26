package com.notificationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationService.config.TestSecurityConfig;
import com.notificationService.config.UserServiceClient;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.dto.UserDto;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification;
import com.notificationService.model.Notification_status;
import com.notificationService.repository.NotificationRepository;
import com.notificationService.service.EmailService;
import com.notificationService.service.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig.class)
public class NotificationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotificationRepository notificationRepository;
    @MockBean
    private UserServiceClient userServiceClient;
    @MockBean
    private EmailService emailService;
    @MockBean
    private TelegramService telegramService;
    private UserDto userDto;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.8-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("application.security.jwt.secret-key", () -> "0c1o5t7vPfsdf42f8yQ9z3a5b6c7d8eF0g1H2i3J7k5L6m7N8o9p0q1R2s3T4u5V6w7X8y9Z0A1B2C3D1E5F6G7H8I9J0K1L2M3N4O5P6Q5R8S9T0U1V2W3X4Y1Z");
        registry.add("application.security.jwt.expiration", () -> "3600000");
        registry.add("application.security.jwt.refresh-token.expiration", () -> "604800000");
        registry.add("application.security.jwt.cookie-name", () -> "jwt_token");

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

        registry.add("TELEG_USERNAME", () -> "testName");
        registry.add("TELEG_TOKEN", () -> "8128255555:BDE6xV-NoeqUa6XzdrZaQkSlAluTb__IyaQ");
    }

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userDto = UserDto.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .telegramChatId(1234L)
                .build();
    }
    @Test
    @DisplayName("Success - sendNotification")
    void successSendNotification() throws Exception {
        when(userServiceClient.getUserById(anyLong())).thenReturn(Optional.of(userDto));
        doNothing().when(telegramService).sendMessage(anyLong(), anyString());
        NotificationServiceRequest notificationServiceRequest = NotificationServiceRequest
                .builder()
                .userId(1L)
                .channel(Channel.TELEGRAM)
                .recipient(null)
                .recipientTelegramId(1234L)
                .subject("test subject")
                .message("test message")
                .build();
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(() -> "1")
                        .content(objectMapper.writeValueAsString(notificationServiceRequest)))
                .andExpect(status().isOk());
        assertThat(notificationRepository.count()).isEqualTo(1);
        Notification notification = notificationRepository.findAll().get(0);
        assertThat(notification.getStatus()).isEqualTo(Notification_status.SENT);
        assertThat(notification.getSentAt()).isNotNull();
    }
    @Test
    @DisplayName("Failed - sendNotification, notification failed")
    void failedSendNotification() throws Exception {
        when(userServiceClient.getUserById(anyLong())).thenReturn(Optional.of(userDto));
        doThrow(new RuntimeException("Email server is down")).when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        NotificationServiceRequest notificationServiceRequest = NotificationServiceRequest
                .builder()
                .userId(1L)
                .channel(Channel.EMAIL)
                .recipient("email@test.com")
                .recipientTelegramId(null)
                .subject("test subject")
                .message("test message")
                .build();
        mockMvc.perform(post("/api/notifications/send")
                        .with(csrf())
                        .principal(() -> "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationServiceRequest)))
                .andExpect(status().isOk());
        assertThat(notificationRepository.count()).isEqualTo(1);
        Notification notification = notificationRepository.findAll().get(0);
        assertThat(notification.getStatus()).isEqualTo(Notification_status.FAILED);
        assertThat(notification.getError_message()).isEqualTo("Email server is down");
        assertThat(notification.getSentAt()).isNull();
    }
}