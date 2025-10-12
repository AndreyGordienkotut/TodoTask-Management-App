package com.taskService.service;

import by.info_microservice.core.TaskEventDto;
import com.taskService.config.UserServiceClient;
import com.taskService.dto.UserDto;
import com.taskService.exception.TaskEventPublishException;
import com.taskService.exception.TaskNotificationException;
import com.taskService.model.Frequency_repeat;
import com.taskService.model.Status;
import com.taskService.model.Task;
import com.taskService.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskCheckSchedulerTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private TaskEventProducer taskEventProducer;
    @InjectMocks
    private TaskCheckScheduler taskCheckScheduler;
    private Task task;
    private UserDto user;
    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .userId(100L)
                .title("Test Task")
                .description("desc")
                .dueDate(LocalDateTime.now().minusMinutes(5))
                .status(Status.NOT_COMPLETED)
                .isRepeat(false)
                .build();
        user = new UserDto();
        user.setId(100L);
        user.setEmail("user@example.com");
        user.setTelegramChatId(12345L);
    }
    @Test
    @DisplayName("Succeed overdue- checkAllTaskStatuses - updates the status and sends a notification")
    void overdueTasks_shouldUpdateAndNotify() {
        when(taskRepository.findByDueDateBeforeAndStatus(any(), eq(Status.NOT_COMPLETED)))
                .thenReturn(List.of(task));
        when(userServiceClient.getUserById(100L)).thenReturn(user);
        taskCheckScheduler.checkAllTaskStatuses();
        assertEquals(Status.OVERDUE,task.getStatus());
        verify(taskRepository).saveAll(anyList());
        verify(taskEventProducer, atLeastOnce()).sendTaskEvent(any(TaskEventDto.class));
    }
    @Test
    @DisplayName("Succeed Nearly overdue - checkAllTaskStatuses - updates the flag and sends a notification")
    void nearlyOverdueTasks_shouldNotify() {
        Task nearly = Task.builder()
                .id(2L)
                .userId(100L)
                .title("Soon overdue")
                .dueDate(LocalDateTime.now().plusMinutes(10))
                .status(Status.NOT_COMPLETED)
                .nearlyOverdueNotified(false)
                .build();
        when(taskRepository.findByDueDateBetweenAndStatusAndNearlyOverdueNotified(any(),any(), eq(Status.NOT_COMPLETED),eq(false)))
                .thenReturn(List.of(nearly));
        when(userServiceClient.getUserById(100L)).thenReturn(user);
        taskCheckScheduler.checkAllTaskStatuses();
        assertTrue(nearly.isNearlyOverdueNotified());
        verify(taskRepository).saveAll(anyList());
        verify(taskEventProducer, atLeastOnce()).sendTaskEvent(any(TaskEventDto.class));
    }
    @Test
    @DisplayName("TaskNotificationException - checkAllTaskStatuses")
    void userNotFound_shouldThrow() {
        when(taskRepository.findByDueDateBeforeAndStatus(any(), eq(Status.NOT_COMPLETED)))
                .thenReturn(List.of(task));
        when(userServiceClient.getUserById(100L)).thenReturn(null);

        assertThrows(TaskNotificationException.class, () -> taskCheckScheduler.checkAllTaskStatuses());
    }

    @Test
    @DisplayName("TaskEventPublishException - checkAllTaskStatuses")
    void taskEventProducerFails_shouldThrow() {
        when(taskRepository.findByDueDateBeforeAndStatus(any(), eq(Status.NOT_COMPLETED)))
                .thenReturn(List.of(task));
        when(userServiceClient.getUserById(100L)).thenReturn(user);
        doThrow(new TaskEventPublishException("fail")).when(taskEventProducer).sendTaskEvent(any());

        assertThrows(TaskEventPublishException.class, () -> taskCheckScheduler.checkAllTaskStatuses());
    }
    @Test
    @DisplayName("Succeed - createRepeatTask - new repeat task is created and a notification is sent")
    void createRepeatTask_shouldCreateAndNotify() {
        Task repeatTask = Task.builder()
                .id(3L)
                .userId(100L)
                .title("Repeatable")
                .description("repeat desc")
                .dueDate(LocalDateTime.now().minusMinutes(1))
                .status(Status.NOT_COMPLETED)
                .isRepeat(true)
                .frequencyRepeat(Frequency_repeat.DAY)
                .build();

        when(taskRepository.findByDueDateBeforeAndStatus(any(), eq(Status.NOT_COMPLETED)))
                .thenReturn(List.of(repeatTask));
        when(userServiceClient.getUserById(100L)).thenReturn(user);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(999L);
            }
            return t;
        });

        taskCheckScheduler.checkAllTaskStatuses();

        assertEquals(Status.OVERDUE, repeatTask.getStatus());
        verify(taskRepository).saveAll(argThat(iterable -> {
            List<Task> list = (List<Task>) iterable;
            return list.stream()
                    .anyMatch(t -> t.getId().equals(repeatTask.getId())
                            && t.getStatus() == Status.OVERDUE);
        }));
        verify(taskRepository).save(argThat(t ->
                !t.getId().equals(repeatTask.getId())
                        && t.getTitle().equals("Repeatable")
                        && t.getStatus() == Status.NOT_COMPLETED
                        && t.isRepeat()
        ));
        verify(taskEventProducer, atLeastOnce()).sendTaskEvent(any(TaskEventDto.class));
    }
}
