package com.taskService.controller;

import com.taskService.dto.TaskRequestDto;
import com.taskService.dto.TaskResponseDto;
import com.taskService.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/create-test-task/{userId}")
    public String createTestTask(@PathVariable Long userId) {
        return taskService.createTestTaskForUser(userId);
    }
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto requestDto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto createdTask = taskService.createTask(requestDto, userId);
        return ResponseEntity.ok(createdTask);
    }

    //not work
    @PostMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id, @RequestBody TaskRequestDto requestDto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updateTask(requestDto,userId);
        return ResponseEntity.ok(taskResponseDto);
    }
}
