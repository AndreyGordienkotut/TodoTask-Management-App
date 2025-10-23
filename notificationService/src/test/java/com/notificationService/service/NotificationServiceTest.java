package com.notificationService.service;

import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.dto.UserDto;
import com.notificationService.exception.NotificationProcessingException;
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
    @InjectMocks
    private NotificationService notificationService;


    private NotificationServiceRequest emailRequest;
    private NotificationServiceRequest telegramRequest;
    private Notification pendingNotification;
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
        pendingNotification = Notification.builder()
                .id(100L)
                .userId(1L)
                .channel(Channel.EMAIL)
                .status(Notification_status.PENDING)
                .build();
        notificationService = spy(new NotificationService(notificationRepository, emailService, telegramService));
    }
@Test
@DisplayName("Success - sendNotification saves PENDING and calls async processor")
void successSendNotification() throws Exception {
    when(notificationRepository.saveAndFlush(any(Notification.class)))
            .thenReturn(pendingNotification);
    doNothing().when(notificationService).processNotificationAsync(anyLong(), any(NotificationServiceRequest.class));

    notificationService.sendNotification(emailRequest);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, times(1)).saveAndFlush(captor.capture());

    Notification savedNotification = captor.getValue();
    assertEquals(Notification_status.PENDING, savedNotification.getStatus());
    verify(notificationService, times(1))
            .processNotificationAsync(eq(pendingNotification.getId()), eq(emailRequest));
    verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
}
    @Test
    @DisplayName("Success - processNotificationAsync sends email and updates to SENT")
    void successProcessNotificationAsyncEmail() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(pendingNotification));
        notificationService.processNotificationAsync(pendingNotification.getId(), emailRequest);
        verify(emailService, times(1)).sendSimpleEmail(
                eq(emailRequest.getRecipient()),
                eq(emailRequest.getSubject()),
                eq(emailRequest.getMessage())
        );
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification finalNotification = captor.getValue();
        assertEquals(Notification_status.SENT, finalNotification.getStatus());
        assertNotNull(finalNotification.getSentAt());
    }

    @Test
    @DisplayName("Success - processNotificationAsync sends telegram and updates to SENT")
    void successProcessNotificationAsyncTelegram() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(pendingNotification));
        notificationService.processNotificationAsync(pendingNotification.getId(), telegramRequest);
        verify(telegramService, times(1)).sendMessage(
                eq(telegramRequest.getRecipientTelegramId()),
                eq(telegramRequest.getMessage())
        );
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification finalNotification = captor.getValue();
        assertEquals(Notification_status.SENT, finalNotification.getStatus());
    }

    @Test
    @DisplayName("Failure - processNotificationAsync handles email exception and updates to FAILED")
    void failureProcessNotificationAsyncEmailException() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(pendingNotification));
        String errorMessage = "SMTP server failed";
        doThrow(new RuntimeException(errorMessage))
                .when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        notificationService.processNotificationAsync(pendingNotification.getId(), emailRequest);

        verify(emailService, times(1)).sendSimpleEmail(anyString(), anyString(), anyString());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification finalNotification = captor.getValue();
        assertEquals(Notification_status.FAILED, finalNotification.getStatus());
        assertTrue(finalNotification.getError_message().contains(errorMessage));
    }

    @Test
    @DisplayName("Failure - processNotificationAsync throws if notification not found")
    void failureProcessNotificationAsyncNotFound() {
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> notificationService.processNotificationAsync(999L, emailRequest));
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
    }
}
