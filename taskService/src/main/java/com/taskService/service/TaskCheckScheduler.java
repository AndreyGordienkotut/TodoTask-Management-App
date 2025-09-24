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
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCheckScheduler {
    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkOverdueTasks() {
        List<Task> tasks = taskRepository.findAllByStatus(Status.NOT_COMPLETED);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(now))
                .forEach(task -> {
                    task.setStatus(Status.OVERDUE);
                    taskRepository.save(task);
                });
        log.info("Launched checkOverdueTasks в {}", LocalDateTime.now());

    }
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkNearlyOverdueAndOverdueTasks() {

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Task> nearlyOverdue = taskRepository.findByDueDateBetweenAndStatus(
                now.minusMinutes(15), now, Status.NOT_COMPLETED
        );
        for(Task task : nearlyOverdue) {
            sendNotification(task,"Task nearly overdue!",
                    "Hello, you have less than 15 minutes left to complete the task "+task.getTitle()+ " hurry!");
        }

        List<Task> overdue = taskRepository.findByDueDateBetweenAndStatus(
                now, now.plusMinutes(5),Status.NOT_COMPLETED
        );
        for(Task task : overdue) {
            sendNotification(task,"Task overdue",
                    "You haven't completed the task " + task.getTitle() + ". Try to complete it as quickly as possible!");
            task.setStatus(Status.OVERDUE);
            taskRepository.save(task);
        }
        log.info("Launched checkNearlyOverdueAndOverdueTasks в {}", LocalDateTime.now());
        log.info("Search nearlyOverdue: {}", nearlyOverdue.size());
        log.info("Search overdue: {}", overdue.size());
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
            NotificationServiceRequest request2 = new NotificationServiceRequest();
                 request2 = NotificationServiceRequest.builder()
                        .userId(task.getUserId())
                        .recipient(user.getEmail())
                        .subject(subject)
                        .message(message)
                        .channel("EMAIL")
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build();


            notificationServiceClient.sendNotification(request2);
            log.info("Notification sending: {} → {}", user.getEmail(), subject);
            }
        } catch (Exception e) {
            log.error("Error sending task notification {}: {}", task.getId(), e.getMessage(), e);
        }
    }
}
