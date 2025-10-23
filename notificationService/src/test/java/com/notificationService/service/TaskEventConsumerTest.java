package com.notificationService.service;

import by.info_microservice.core.TaskEventDto;
import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.model.Channel;
import okhttp3.internal.concurrent.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class TaskEventConsumerTest {
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private TaskEventConsumer consumer;

    private TaskEventDto event;
    @BeforeEach
    public void setUp() {
        event = TaskEventDto.builder()
                .userId(100L)
                .title("Test Task")
                .recipient("test@example.com")
                .recipientTelegramId(123456L)
                .build();
    }
    @Test
    @DisplayName("TASK_OVERDUE - sends email and telegram notifications if both recipients present")
    void consume_taskOverdue_sendsBothNotifications() {
        event.setEventType("TASK_OVERDUE");
        consumer.consume(event);
        ArgumentCaptor<NotificationServiceRequest> captor = ArgumentCaptor.forClass(NotificationServiceRequest.class);
        verify(notificationService, times(2)).sendNotification(captor.capture());

        assertEquals(2, captor.getAllValues().size());
        NotificationServiceRequest emailReq = captor.getAllValues().get(0);
        NotificationServiceRequest tgReq = captor.getAllValues().get(1);

        assertEquals(event.getUserId(), emailReq.getUserId());
        assertEquals("The task is overdue!", emailReq.getSubject());
        assertEquals(Channel.EMAIL, emailReq.getChannel());

        assertEquals(event.getUserId(), tgReq.getUserId());
        assertEquals("The task is overdue!", tgReq.getSubject());
        assertEquals(Channel.TELEGRAM, tgReq.getChannel());

    }
    @Test
    @DisplayName("TASK_SOON_OVERDUE - sends only email if telegram id is null")
    void consume_taskSoonOverdue_emailOnly() {
        event.setEventType("TASK_SOON_OVERDUE");
        event.setRecipientTelegramId(null);
        consumer.consume(event);
        ArgumentCaptor<NotificationServiceRequest> captor = ArgumentCaptor.forClass(NotificationServiceRequest.class);
        verify(notificationService, times(1)).sendNotification(captor.capture());
        NotificationServiceRequest req = captor.getValue();
        assertEquals(event.getRecipient(), req.getRecipient());
        assertEquals("The task will soon be overdue!", req.getSubject());
        assertEquals(Channel.EMAIL, req.getChannel());
    }
    @Test
    @DisplayName("TASK_OVERDUE - sends only telegram if email is null")
    void consume_taskOverdue_telegramOnly() {
        event.setEventType("TASK_OVERDUE");
        event.setRecipient(null);
        consumer.consume(event);
        ArgumentCaptor<NotificationServiceRequest> captor = ArgumentCaptor.forClass(NotificationServiceRequest.class);
        verify(notificationService, times(1)).sendNotification(captor.capture());
        NotificationServiceRequest req = captor.getValue();
        assertEquals(event.getRecipient(), req.getRecipient());
        assertEquals("The task is overdue!", req.getSubject());
        assertEquals(Channel.TELEGRAM, req.getChannel());
    }

}
