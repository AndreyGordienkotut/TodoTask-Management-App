package com.taskService.controller;

import com.taskService.dto.*;
import com.taskService.model.Priority;
import com.taskService.model.Status;
import com.taskService.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false) List<String> sortParams) {

        Long userId = Long.parseLong(principal.getName());

        if (sortParams == null || sortParams.isEmpty()) {
            sortParams = List.of("dueDate,asc");
        }
        if (sortParams.size() == 2
                && !sortParams.get(0).contains(",")
                && (sortParams.get(1).equalsIgnoreCase("asc") || sortParams.get(1).equalsIgnoreCase("desc"))) {
            sortParams = List.of(sortParams.get(0) + "," + sortParams.get(1));
        }

        List<Sort.Order> orders = sortParams.stream()
                .map(s -> {
                    String[] parts = s.split(",");
                    String property = parts[0].trim();
                    Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                            ? Sort.Direction.DESC : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<TaskResponseDto> tasks = taskService.getAllTasks(userId, pageable);
        return ResponseEntity.ok(tasks);
    }
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDto>> searchTasks(
            Principal principal,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false) List<String> sortParams) {
        Long userId = Long.parseLong(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<TaskResponseDto> tasks = taskService.searchTasks(userId, keyword, pageable);
        return ResponseEntity.ok(tasks);

    }
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto task = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(task);

    }
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
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto createdTask = taskService.createTask(requestDto, userId);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
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
    @DeleteMapping("/{id}/archive")
    public ResponseEntity<TaskResponseDto> archiveTask(@PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.archiveTask(id, userId);
        return ResponseEntity.ok(taskResponseDto);
    }

    @GetMapping("/archived")
    public ResponseEntity<Page<TaskResponseDto>> getArchivedTasks(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false) List<String> sortParams) {

        Long userId = Long.parseLong(principal.getName());

        if (sortParams == null || sortParams.isEmpty()) {
            sortParams = List.of("dueDate,asc");
        }
        if (sortParams.size() == 2
                && !sortParams.get(0).contains(",")
                && (sortParams.get(1).equalsIgnoreCase("asc") || sortParams.get(1).equalsIgnoreCase("desc"))) {
            sortParams = List.of(sortParams.get(0) + "," + sortParams.get(1));
        }

        List<Sort.Order> orders = sortParams.stream()
                .map(s -> {
                    String[] parts = s.split(",");
                    String property = parts[0].trim();
                    Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                            ? Sort.Direction.DESC : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<TaskResponseDto> tasks = taskService.getArchivedTasks(userId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteTasks(@RequestBody List<Long> tasksIds,
                                             Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        taskService.deleteTasksBulk(tasksIds,userId);
        return ResponseEntity.noContent().build();
    }

}
