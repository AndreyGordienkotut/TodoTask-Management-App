package com.taskService.service;

import com.taskService.model.Status;
import com.taskService.model.Task;
import com.taskService.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskScheduler {
    private final TaskRepository taskRepository;
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkOverdueTasks() {
        List<Task> tasks = taskRepository.findAllByStatus(Status.NOT_COMPLETED);
        LocalDateTime now = LocalDateTime.now();

        tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(now))
                .forEach(task -> {
                    task.setStatus(Status.OVERDUE);
                    taskRepository.save(task);
                });
    }
}
