package com.notificationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationService.config.TestSecurityConfig;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.dto.UserDto;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification;
import com.notificationService.model.Notification_status;
import com.notificationService.repository.NotificationRepository;
import com.notificationService.service.EmailService;
import com.notificationService.service.NotificationService;
import com.notificationService.service.TelegramService;
import jakarta.persistence.PrePersist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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
    private EmailService emailService;
    @MockBean
    private TelegramService telegramService;
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;
    @MockBean
    private ProducerFactory<?, ?> producerFactory;
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
        registry.add("application.security.jwt.secret-key", () -> "12345t7vPfsdf42f8yQ9z3a5b6c7d8eF0g1H2i3J4k5L6m7N8o9p0q1R2s3T4u5V6w7X8y9Z0A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z");
        registry.add("application.security.jwt.expiration", () -> "3600000");
        registry.add("application.security.jwt.refresh-token.expiration", () -> "604800000");
        registry.add("application.security.jwt.cookie-name", () -> "jwt_token");

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

        registry.add("TELEG_USERNAME", () -> "testName");
        registry.add("TELEG_TOKEN", () -> "8128255555:BDE6xV-NoeqUa6XzdrZaQkSlAluTb__IyaQ");
//        registry.add("KAFKA_BOOTSTRAP_SERVERS", () -> "localhost:9092");
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
    void controllerSavesPending() throws Exception {
        NotificationServiceRequest request = NotificationServiceRequest.builder()
            .userId(1L)
            .channel(Channel.EMAIL)
            .recipient("test@test.com")
            .subject("subject")
            .message("message")
            .createdAt(LocalDateTime.now())
            .build();
        Notification notification = Notification.builder()
            .userId(request.getUserId())
            .recipient(request.getRecipient())
            .recipientTelegramId(request.getRecipientTelegramId())
            .channel(request.getChannel())
            .subject(request.getSubject())
            .message(request.getMessage())
            .status(Notification_status.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    mockMvc.perform(post("/api/notifications/send")
                    .with(csrf())
                    .principal(() -> "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notification)))
            .andExpect(status().isOk());

    assertThat(notificationRepository.count()).isEqualTo(1);


}

    @Test
    @DisplayName("Success - processNotificationAsync")
    void workerSetsSentOnSuccessEmail() throws Exception {
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        NotificationService service = new NotificationService(notificationRepository, emailService, telegramService);

        Notification pending = notificationRepository.save(Notification.builder()
                .userId(1L)
                .channel(Channel.EMAIL)
                .status(Notification_status.PENDING)
                .recipient("test@test.com")
                .subject("subj")
                .message("msg")
                        .createdAt(LocalDateTime.now())
                .build());
        service.processNotificationAsync(pending.getId(), NotificationServiceRequest.builder()
                .userId(1L)
                .channel(Channel.EMAIL)
                .recipient("test@test.com")
                .subject("subj")
                .message("msg")
                .build());
        TimeUnit.MILLISECONDS.sleep(200);

        Notification updated = notificationRepository.findById(pending.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Notification_status.SENT);
        assertThat(updated.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Failed - processNotificationAsync")
    void workerSetsFailedOnEmailException() throws Exception {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        NotificationService service = new NotificationService(notificationRepository, emailService, telegramService);

        Notification pending = notificationRepository.save(Notification.builder()
                .userId(1L)
                .channel(Channel.EMAIL)
                .status(Notification_status.PENDING)
                .recipient("test@test.com")
                .subject("subj")
                .message("msg")
                .createdAt(LocalDateTime.now())
                .build());

        service.processNotificationAsync(pending.getId(), NotificationServiceRequest.builder()
                .userId(1L)
                .channel(Channel.EMAIL)
                .recipient("test@test.com")
                .subject("subj")
                .message("msg")
                .build());

        TimeUnit.MILLISECONDS.sleep(200);

        Notification updated = notificationRepository.findById(pending.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Notification_status.FAILED);
        assertThat(updated.getError_message()).contains("SMTP error");
    }

}