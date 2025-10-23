package com.notificationService.service;

import by.info_microservice.core.UserVerificationEventDto;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification_status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserEventConsumer consumer;

    private UserVerificationEventDto event;
    private final Long USER_ID = 50L;
    private final String EMAIL = "newuser@test.com";
    private final String NAME = "Alexey";
    private final String TOKEN = "unique-verification-token-123";
    private final Long TELEGRAM_ID = 111222333L;

    @BeforeEach
    void setUp() {
        event = UserVerificationEventDto.builder()
                .userId(USER_ID)
                .name(NAME)
                .email(EMAIL)
                .telegramChatId(TELEGRAM_ID)
                .verificationToken(TOKEN)
                .eventType("USER_VERIFICATION_REQUESTED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should correctly transform UserVerificationEvent to NotificationServiceRequest and call sendNotification")
    void consume_VerificationEvent_Success() {
        ArgumentCaptor<NotificationServiceRequest> captor = ArgumentCaptor.forClass(NotificationServiceRequest.class);
        consumer.consume(event);
        verify(notificationService, times(1)).sendNotification(captor.capture());

        NotificationServiceRequest request = captor.getValue();

        assertEquals(USER_ID, request.getUserId(), "UserId must match the event.");
        assertEquals(EMAIL, request.getRecipient(), "Recipient email must be set from the event.");
        assertEquals(TELEGRAM_ID, request.getRecipientTelegramId(), "Telegram ID must be correctly passed.");
        assertEquals("Email verification", request.getSubject(), "Subject is fixed and must be correct.");
        assertEquals(Channel.EMAIL, request.getChannel(), "Channel must be set to EMAIL for verification.");
        assertEquals(Notification_status.PENDING, request.getStatus(), "Status must be PENDING.");

        String expectedLinkPart = "http://localhost:8080/api/auth/verify-email?token=" + TOKEN;
        assertTrue(request.getMessage().contains("Hello, " + NAME + "!"), "Message must contain the user's name.");
        assertTrue(request.getMessage().contains(expectedLinkPart), "Message must contain the correct verification link with the token.");
    }
}