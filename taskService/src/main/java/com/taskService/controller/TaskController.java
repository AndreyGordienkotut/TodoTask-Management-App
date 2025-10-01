package com.taskService.controller;

import com.taskService.dto.*;
import com.taskService.exception.ResourceNotFoundException;
import com.taskService.model.Priority;
import com.taskService.model.Status;
import com.taskService.model.Task;
import com.taskService.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.media.ArraySchema;
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task management", description = "API for management own tasks.")
@SecurityRequirement(name = "BearerAuth")
public class TaskController {

    private final TaskService taskService;
    @Operation(summary = "Get all task",
            description = "Getting all user tasks using pagination",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Tasks successfully found and returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            Principal principal,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property,(asc|desc). Default is dueDate,asc.", example = "dueDate,asc")
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
    @Operation(summary = "Search tasks",
            description = "Search by words all user tasks using pagination",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Tasks successfully found and returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDto>> searchTasks(
            Principal principal,
            @Parameter(description = "Keyword to search within task titles and descriptions", example = "meeting")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property,(asc|desc). Default is dueDate,asc.", example = "dueDate,asc")
            @RequestParam(value = "sort", required = false) List<String> sortParams) {
        Long userId = Long.parseLong(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<TaskResponseDto> tasks = taskService.searchTasks(userId, keyword, pageable);
        return ResponseEntity.ok(tasks);

    }
    @Operation(summary = "Get task by Id",
            description = "Retrieves task details by ID.",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Tasks successfully found and returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Task not found with id.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "You don't have permission to view this task(Task does not belong to user)",
            content = @Content(schema = @Schema(hidden = true)))
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(description = "Unique ID of the task to retrieve", example = "50")
            @PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto task = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(task);

    }
    @Operation(summary = "Get task by filter",
            description = "Retrieves task details by filter(status, priority, date).",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Tasks successfully found and returned",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
    @GetMapping("/filter")
    public ResponseEntity<List<TaskResponseDto>> filterTask(
            @Parameter(description = "Filter by task status(ARCHIVED, NOT_COMPLETED, COMPLETED, OVERDUE)", example = "NOT_COMPLETED")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Filter by task priority (HIGH, MEDIUM, LOW)", example = "HIGH")
            @RequestParam(required = false) Priority priority,
            @Parameter(description = "Start date for due date range filter", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date for due date range filter", example = "2025-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal){
        Long userId = Long.parseLong(principal.getName());
        List<TaskResponseDto> tasks = taskService.filterTasks(userId,status,fromDate,toDate,priority);
        return ResponseEntity.ok(tasks);
    }
    @Operation(summary = "Create a new task",
            description = "Creates a new task associated with the authenticated user",
            tags = {"Task management"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Details of the task to be created",
                            required = true,
                    content = @Content(schema = @Schema(implementation = TaskRequestDto.class))))
    @ApiResponse(responseCode = "201", description = "Tasks successfully created and returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or validation error.",
            content = @Content(schema = @Schema(hidden = true)))
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto createdTask = taskService.createTask(requestDto, userId);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    @Operation(summary = "Update task details",
            description = "Updates one or more fields of an existing task (partial update is supported). Ensures the task belongs to the user.",
            tags = {"Task management"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the task to be update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateTaskRequestDto.class))))
    @ApiResponse(responseCode = "200", description = "Task successfully updated and returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Task not found with ID",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Task does not belong to the user",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Cannot set isRepeat without dueDate",
            content = @Content(schema = @Schema(hidden = true)))
    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(description = "Unique ID of the task to update", example = "50")
            @PathVariable Long id, @RequestBody UpdateTaskRequestDto dto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updateTask(id, dto, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @Operation(summary = "Update task status",
            description = "Updates only the status of an existing task.",
            tags = {"Task management"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New status for the task",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateStatusRequestDto.class))))
    @ApiResponse(responseCode = "200", description = "Status successfully updated and task returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Task not found with ID.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Task does not belong to the user.",
            content = @Content(schema = @Schema(hidden = true)))
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateStatus(
            @Parameter(description = "Unique ID of the task to update status for", example = "50")
            @PathVariable Long id, @RequestBody UpdateStatusRequestDto dto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updateStatus(id, dto, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @Operation(summary = "Update task priority",
            description = "Updates only the priority level of an existing task.",
            tags = {"Task management"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New priority for the task",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdatePriorityRequestDto.class))))
    @ApiResponse(responseCode = "200", description = "Priority successfully updated and task returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Task not found with ID.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Task does not belong to the user.",
            content = @Content(schema = @Schema(hidden = true)))
    @PatchMapping("/{id}/priority")
    public ResponseEntity<TaskResponseDto> updatePriority(
            @Parameter(description = "Unique ID of the task to update priority for", example = "50")
            @PathVariable Long id, @RequestBody UpdatePriorityRequestDto dto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.updatePriority(id, dto, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @Operation(summary = "Archive a task",
            description = "Sets the status of a task to ARCHIVED. This is for single, non-repeating tasks.",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Task successfully archived and returned.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Task not found with ID.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Cannot archive a task that belongs to another user.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "409", description = "Conflict: Cannot archive a recurring task using this endpoint. Use '/series/archive' instead.",
            content = @Content(schema = @Schema(hidden = true)))
    @DeleteMapping("/{id}/archive")
    public ResponseEntity<TaskResponseDto> archiveTask(
            @Parameter(description = "Unique ID of the task to archive", example = "50")
            @PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        TaskResponseDto taskResponseDto = taskService.archiveTask(id, userId);
        return ResponseEntity.ok(taskResponseDto);
    }
    @Operation(summary = "Archive a recurring task series",
            description = "Sets the status of ALL tasks in a recurring series to ARCHIVED.",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Task series successfully archived and returned.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
    @ApiResponse(responseCode = "404", description = "Task or series not found with ID.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Cannot archive tasks of another user.",
            content = @Content(schema = @Schema(hidden = true)))
    @DeleteMapping("/{id}/series/archive")
    public ResponseEntity<List<TaskResponseDto>> archiveSeries(
            @Parameter(description = "ID of any task within the series to be archived", example = "50")
            @PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        List<TaskResponseDto> taskResponseDtos = taskService.archiveTaskSeries(id, userId);
        return ResponseEntity.ok(taskResponseDtos);
    }
    @Operation(summary = "Get archived tasks",
            description = "Retrieves a paginated list of archived tasks for the authenticated user.",
            tags = {"Task management"})
    @ApiResponse(responseCode = "200", description = "Successfully retrieved archived tasks.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
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
    @Operation(summary = "Permanently delete a single archived task",
            description = "Deletes a single task (must not be repeating and must be ARCHIVED).",
            tags = {"Task management"})
    @ApiResponse(responseCode = "204", description = "Task successfully deleted (No Content).",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "404", description = "Task not found with ID.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Cannot delete tasks of another user.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "409", description = "Conflict: Only archived, non-recurring tasks can be deleted using this endpoint.",
            content = @Content(schema = @Schema(hidden = true)))
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deleteTask(@Parameter(description = "ID of the task to permanently delete", example = "50")
            @PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Permanently delete a full recurring task series",
            description = "Deletes all tasks in a series (all tasks in the series must be ARCHIVED).",
            tags = {"Task management"})
    @ApiResponse(responseCode = "204", description = "Task series successfully deleted (No Content).",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "404", description = "Task or series not found with ID.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Cannot delete tasks of another user.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "409", description = "Conflict: Only fully archived series can be permanently deleted.",
            content = @Content(schema = @Schema(hidden = true)))
    @DeleteMapping("/{id}/series/permanent")
    public ResponseEntity<Void> deleteArchivedTaskSeries(
            @Parameter(description = "ID of any task within the series to be permanently deleted", example = "50")
            @PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        taskService.deleteRepeatTaskSeries(id, userId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Permanently delete multiple archived tasks",
            description = "Performs bulk deletion of single tasks (all tasks must be ARCHIVED and non-recurring).",
            tags = {"Task management"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Task IDs to be permanently deleted",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "integer", format = "int64")))))
    @ApiResponse(responseCode = "204", description = "Tasks successfully deleted (No Content).",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Bad Request: Task IDs list is empty.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "403", description = "Forbidden: Cannot delete tasks of another user.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "409", description = "Conflict: List contains non-archived or recurring tasks.",
            content = @Content(schema = @Schema(hidden = true)))
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteTasks(@RequestBody List<Long> tasksIds,
                                             Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        taskService.deleteTasksBulk(tasksIds,userId);
        return ResponseEntity.noContent().build();
    }

}
