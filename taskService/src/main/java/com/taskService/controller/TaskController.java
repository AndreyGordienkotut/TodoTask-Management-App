package com.taskService.controller;

import com.taskService.dto.*;
import com.taskService.model.Priority;
import com.taskService.model.Status;
import com.taskService.model.Task;
import com.taskService.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping()
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        return ResponseEntity.ok(tasks);

    }
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto task = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(task);

    }
//    public List<TaskResponseDto> filterTasks(Long userId, Status status, LocalDate fromDate, LocalDate toDate, Priority priority) {
//        Specification<Task> spec = Specification.where(byUserId(userId));
    @GetMapping("/filter")
    public ResponseEntity<List<TaskResponseDto>> filterTask(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal){
        Long userId = Long.parseLong(principal.getName());
        List<TaskResponseDto> tasks = taskService.filterTasks(userId,status,fromDate,toDate,priority);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto requestDto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto createdTask = taskService.createTask(requestDto, userId);
        return ResponseEntity.ok(createdTask);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id, @RequestBody UpdateTaskRequestDto dto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updateTask(id, dto, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequestDto dto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updateStatus(id, dto, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @PatchMapping("/{id}/priority")
    public ResponseEntity<TaskResponseDto> updatePriority(@PathVariable Long id, @RequestBody UpdatePriorityRequestDto dto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updatePriority(id, dto, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponseDto> deleteTask(@PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto deleteTask = taskService.deleteTask(id,userId);
        return ResponseEntity.ok(deleteTask);
    }

}
