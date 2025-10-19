package com.taskService.service;

import com.taskService.config.UserServiceClient;
import by.info_microservice.core.TaskEventDto;
import com.taskService.dto.UserDto;
import com.taskService.exception.TaskEventPublishException;
import com.taskService.exception.TaskNotificationException;
import com.taskService.model.Frequency_repeat;
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
    private final TaskEventProducer taskEventProducer;
    @Scheduled(fixedRate = 60000, initialDelay = 60000)
//    @Transactional
    public void checkAllTaskStatuses() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime fifteenMinutesFromNow = now.plusMinutes(15);

        List<Task> overdueTasks = taskRepository.findByDueDateBeforeAndStatus(now, Status.NOT_COMPLETED);
        if (!overdueTasks.isEmpty()) {
            List<Task> updatedOverdueTasks = overdueTasks.stream()
                    .peek(task -> {
                                if (task.isRepeat()) {
                                    try {
                                        createRepeatTask(task);
                                    } catch (Exception e) {
                                        log.error("Failed to create next repeat task for ID {}. Error: {}", task.getId(), e.getMessage());
                                    }
                                }
                        sendNotification(task, "The task is overdue!",
                                "You have not completed the task " + task.getTitle() + ". Please complete it as soon as possible!");
                        task.setStatus(Status.OVERDUE);
                    })
                    .collect(Collectors.toList());
            taskRepository.saveAll(updatedOverdueTasks);
            log.info("Updated {} tasks to OVERDUE status.", updatedOverdueTasks.size());
        }
        List<Task> completedRepeatableTasks = taskRepository.findByDueDateBeforeAndStatusAndIsRepeat(
                now, Status.COMPLETED, true
        );

        if (!completedRepeatableTasks.isEmpty()) {
            completedRepeatableTasks.forEach(task -> {
                try {
                    createRepeatTask(task);
                    log.debug("Created repeat for completed task ID: {}", task.getId());
                } catch (Exception e) {
                    log.error("Failed to create repeat for COMPLETED task ID {}. Error: {}", task.getId(), e.getMessage());
                }
            });
            log.info("Created {} new tasks from completed repeating tasks.", completedRepeatableTasks.size());
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
                throw new TaskNotificationException("User " + task.getUserId() + " not found, notification not sent");
            }
            String eventType;
            if (subject.contains("soon be overdue")) {
                eventType = "TASK_SOON_OVERDUE";
            } else if (subject.contains("is overdue")) {
                eventType = "TASK_OVERDUE";
            } else {
                eventType = "TASK_REMINDER";
            }
            log.info("telegramchatId {}",user.getTelegramChatId());
            if (user.getTelegramChatId() != null) {
                TaskEventDto telegramRequest = TaskEventDto.builder()
                        .taskId(task.getId())
                        .userId(user.getId())
                        .subject(subject)
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .dueDate(task.getDueDate())
                        .eventType(eventType)
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .status("PENDING")
                        .channel("TELEGRAM")
                        .recipientTelegramId(user.getTelegramChatId())
                        .build();

                taskEventProducer.sendTaskEvent(telegramRequest);
                log.info("Telegram notification sent: {} → {}", user.getTelegramChatId(), subject);

            }
            if(user.getEmail() != null) {
                TaskEventDto telegramRequest = TaskEventDto.builder()
                        .taskId(task.getId())
                        .userId(user.getId())
                        .subject(subject)
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .dueDate(task.getDueDate())
                        .eventType(eventType)
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .status("PENDING")
                        .channel("EMAIL")
                        .recipient(user.getEmail())
                        .build();

                taskEventProducer.sendTaskEvent(telegramRequest);
            log.info("Notification sending: {} → {}", user.getEmail(), subject);
            }
        }catch (TaskEventPublishException e) {
            throw e; // пробрасываем дальше
        } catch (Exception e) {
            throw new TaskNotificationException("Failed to send notification for taskId=" + task.getId(), e);
        }
    }

    private Task createRepeatTask(Task overdueTask) {
        Long parentIdToUse = overdueTask.getParentTaskId() != null
                ? overdueTask.getParentTaskId()
                : overdueTask.getId();
        LocalDateTime newDueDate = calculateDueDate(overdueTask.getDueDate(),overdueTask.getFrequencyRepeat());
        Task task = Task.builder()
                .userId(overdueTask.getUserId())
                .title(overdueTask.getTitle())
                .description(overdueTask.getDescription())
                .date(LocalDateTime.now())
                .status(Status.NOT_COMPLETED)
                .dueDate(newDueDate)
                .priority(overdueTask.getPriority())
                .nearlyOverdueNotified(false)
                .isRepeat(true)
                .frequencyRepeat(overdueTask.getFrequencyRepeat())
                .parentTaskId(parentIdToUse)
                .build();
        return taskRepository.save(task);

    }
    private LocalDateTime calculateDueDate(LocalDateTime currentDueDate, Frequency_repeat frequency) {
        LocalDateTime nextDate = currentDueDate;
        while (nextDate.isBefore(LocalDateTime.now())) {
            switch (frequency) {
                case HOUR -> nextDate = nextDate.plusHours(1);
                case DAY -> nextDate = nextDate.plusDays(1);
                case WEEK -> nextDate = nextDate.plusWeeks(1);
                case MONTH -> nextDate = nextDate.plusMonths(1);
                case YEAR -> nextDate = nextDate.plusYears(1);
                default -> nextDate = nextDate.plusDays(1);
            }
        }
        return nextDate;
    }
}
