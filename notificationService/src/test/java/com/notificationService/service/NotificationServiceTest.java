package com.notificationService.service;

import com.notificationService.config.UserServiceClient;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.dto.UserDto;
import com.notificationService.model.Channel;
import com.notificationService.model.Notification;
import com.notificationService.model.Notification_status;
import com.notificationService.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private TelegramService telegramService;
    @Mock
    private UserServiceClient userServiceClient;
    @InjectMocks
    private NotificationService notificationService;


    private Notification notification;
    private NotificationServiceRequest emailRequest;
    private NotificationServiceRequest telegramRequest;
    private UserDto userDto;
    private Long userId;
    @BeforeEach
    void setUp() {
        emailRequest = NotificationServiceRequest.builder()
                .userId(1L)
                .recipient("recipient@example.com")
                .subject("subject2")
                .message("message2")
                .channel(Channel.EMAIL)
                .status(Notification_status.SENT)
                .atDate(LocalDateTime.now())
                .build();
        telegramRequest = NotificationServiceRequest.builder()
                .userId(2L)
                .recipientTelegramId(1L)
                .subject("subject2")
                .message("message2")
                .channel(Channel.TELEGRAM)
                .status(Notification_status.SENT)
                .atDate(LocalDateTime.now())
                .build();
        userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("recipient@example.com")
                .telegramChatId(1L)
                .build();
    }

    @Test
    @DisplayName("Success - sendNotification for email")
    void successSendNotification() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(Optional.of(userDto));

        notificationService.sendNotification(emailRequest);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendSimpleEmail("recipient@example.com", "subject2", "message2");

        assertEquals(Notification_status.SENT, emailRequest.getStatus());
    }
    @Test
    @DisplayName("Success - sendNotification for telegram")
    void successSendNotificationTelegram() {
       when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(2L)).thenReturn(Optional.of(userDto));

        notificationService.sendNotification(telegramRequest);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(telegramService).sendMessage(1L, "message2");

        assertEquals(Notification_status.SENT, telegramRequest.getStatus());
    }
    @Test
    @DisplayName("RuntimeException - sendNotification")
    void runtimeExceptionSendNotification() {
        when(userServiceClient.getUserById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> notificationService.sendNotification(emailRequest));
        verify(notificationRepository,never()).save(any(Notification.class));
        verify(telegramService,never()).sendMessage(anyLong(), anyString());
    }

    @Test
    @DisplayName("Failure - email service throws exception")
    void failureEmailServiceException() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(1L)).thenReturn(Optional.of(userDto));
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        notificationService.sendNotification(emailRequest);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        Notification secondSave = captor.getAllValues().get(1);
        assertEquals(Notification_status.FAILED, secondSave.getStatus());
        assertTrue(secondSave.getError_message().contains("SMTP error"));
    }

    @Test
    @DisplayName("Failure - telegram service throws exception")
    void failureTelegramServiceException() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userServiceClient.getUserById(2L)).thenReturn(Optional.of(userDto));
        doThrow(new RuntimeException("Telegram API down"))
                .when(telegramService).sendMessage(anyLong(), anyString());

        notificationService.sendNotification(telegramRequest);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        Notification secondSave = captor.getAllValues().get(1);
        assertEquals(Notification_status.FAILED, secondSave.getStatus());
        assertTrue(secondSave.getError_message().contains("Telegram API down"));
    }
}
