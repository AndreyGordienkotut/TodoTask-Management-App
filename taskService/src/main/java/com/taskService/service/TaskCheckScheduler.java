package com.taskService.service;

import com.taskService.config.NotificationServiceClient;
import com.taskService.config.UserServiceClient;
import com.taskService.dto.NotificationServiceRequest;
import com.taskService.dto.UserDto;
import com.taskService.model.Status;
import com.taskService.model.Task;
import com.taskService.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCheckScheduler {
    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAllTaskStatuses() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime fifteenMinutesFromNow = now.plusMinutes(15);

        List<Task> overdueTasks = taskRepository.findByDueDateBeforeAndStatus(now, Status.NOT_COMPLETED);
        if (!overdueTasks.isEmpty()) {
            List<Task> updatedOverdueTasks = overdueTasks.stream()
                    .peek(task -> {
                        sendNotification(task, "The task is overdue!",
                                "You have not completed the task " + task.getTitle() + ". Please complete it as soon as possible!");
                        task.setStatus(Status.OVERDUE);
                    })
                    .collect(Collectors.toList());
            taskRepository.saveAll(updatedOverdueTasks);
            log.info("Updated {} tasks to OVERDUE status.", updatedOverdueTasks.size());
        }
        List<Task> nearlyOverdueTasks = taskRepository.findByDueDateBetweenAndStatusAndNearlyOverdueNotified(
                now, fifteenMinutesFromNow, Status.NOT_COMPLETED, false
        );
        if (!nearlyOverdueTasks.isEmpty()) {
            List<Task> updatedNearlyOverdueTasks = nearlyOverdueTasks.stream()
                    .peek(task -> {
                        sendNotification(task, "The task will soon be overdue!",
                                "Less than 15 minutes left until the deadline for the task: " + task.getTitle() + ". Hurry up!");
                        task.setNearlyOverdueNotified(true);
                    })
                    .collect(Collectors.toList());
            taskRepository.saveAll(updatedNearlyOverdueTasks);
            log.info("Updated {} tasks with nearly overdue notification flag.", updatedNearlyOverdueTasks.size());
        }
    }
    private void sendNotification(Task task, String subject, String message) {
        try {
            UserDto user = userServiceClient.getUserById(task.getUserId());

            if (user == null) {
                log.warn("User {} not found, notification not send", task.getUserId());
                return;
            }
            log.info("telegramchatId {}",user.getTelegramChatId());
            NotificationServiceRequest request = new NotificationServiceRequest();
            if (user.getTelegramChatId() != null) {
                 request = NotificationServiceRequest.builder()
                        .userId(task.getUserId())
                        .recipientTelegramId(user.getTelegramChatId())
                        .subject(subject)
                        .message(message)
                        .channel("TELEGRAM")
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build();

                notificationServiceClient.sendNotification(request);
                log.info("Telegram notification sent: {} → {}", user.getTelegramChatId(), subject);

            }
            if(user.getEmail() != null) {
//            NotificationServiceRequest request2 = new NotificationServiceRequest();
//                 request2 = NotificationServiceRequest.builder()
//                        .userId(task.getUserId())
//                        .recipient(user.getEmail())
//                        .subject(subject)
//                        .message(message)
//                        .channel("EMAIL")
//                        .status("PENDING")
//                        .createdAt(LocalDateTime.now())
//                        .build();
//
//
//            notificationServiceClient.sendNotification(request2);
            log.info("Notification sending: {} → {}", user.getEmail(), subject);
            }
        } catch (Exception e) {
            log.error("Error sending task notification {}: {}", task.getId(), e.getMessage(), e);
        }
    }
}
