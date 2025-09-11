package com.taskService.service;

import com.taskService.config.UserServiceClient;
import com.taskService.dto.TaskRequestDto;
import com.taskService.dto.TaskResponseDto;
import com.taskService.dto.UserDto;
import com.taskService.model.Task;
import com.taskService.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    //createTask + abilities add date and time in task
    //update task
    // delete task
    //get all task
    //get task in status
    //post task in another status


    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;

    public String createTestTaskForUser(Long userId) {
        UserDto user = userServiceClient.getUserById(userId);

        if (user != null) {
            return "Task created for user: " + user.getName() + " with email: " + user.getEmail();
        } else {
            return "User not found with ID: " + userId;
        }
    }

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto requestDto, Long userId) {
        Task task = Task.builder()
                .userId(userId)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .date(requestDto.getDate())
                .status(requestDto.getStatus())
                .build();
        Task savedTask = taskRepository.save(task);
        return TaskResponseDto.builder()
                .id(savedTask.getId())
                .userId(savedTask.getUserId())
                .title(savedTask.getTitle())
                .description(savedTask.getDescription())
                .date(savedTask.getDate())
                .status(savedTask.getStatus())
                .build();
    }

    //not work
    public TaskResponseDto updateTask(TaskRequestDto requestDto, Long userId) {
        Task existTask = taskRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Task not found"));

        existTask.setTitle(requestDto.getTitle());
        existTask.setDescription(requestDto.getDescription());
        existTask.setDate(requestDto.getDate());
        existTask.setStatus(requestDto.getStatus());
        Task savedTask = taskRepository.save(existTask);
        return TaskResponseDto.builder()
                .id(savedTask.getId())
                .userId(savedTask.getUserId())
                .title(savedTask.getTitle())
                .description(savedTask.getDescription())
                .date(savedTask.getDate())
                .status(savedTask.getStatus())
                .build();
    }

    //not work
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }
}