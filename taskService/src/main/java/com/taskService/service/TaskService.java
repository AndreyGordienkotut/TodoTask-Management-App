package com.taskService.service;

import com.taskService.config.UserServiceClient;
import com.taskService.dto.*;
import com.taskService.exception.ResourceNotFoundException;
import com.taskService.model.Priority;
import com.taskService.model.Status;
import com.taskService.model.Task;
import com.taskService.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    private TaskResponseDto convertToDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getUserId(),
                task.getTitle(),
                task.getDescription(),
                task.getDate(),
                task.getDueDate(),
                task.getStatus(),
                task.getPriority()
        );
    }
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasks(Long userId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findAllByUserId(userId, pageable);
        return tasks.map(this::convertToDto);
    }
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> searchTasks(Long userId, String keyword, Pageable pageable) {
        Page<Task> tasks = taskRepository.searchByKeyword(userId, keyword, pageable);
        return tasks.map(this::convertToDto);
    }
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to view this task");
        }
        return convertToDto(task);
    }

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto requestDto, Long userId) {
        Task task = Task.builder()
                .userId(userId)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .date(requestDto.getDate())
                .dueDate(requestDto.getDueDate())
                .status(requestDto.getStatus())
                .priority(requestDto.getPriority())
                .build();
        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    public TaskResponseDto updateTask(Long taskId, UpdateTaskRequestDto dto, Long userId) {
        Task existTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!existTask.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        if (dto.getTitle() != null) existTask.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existTask.setDescription(dto.getDescription());
        if (dto.getDate() != null) existTask.setDate(dto.getDate());
        Task savedTask = taskRepository.save(existTask);

        return convertToDto(savedTask);
    }
    public TaskResponseDto updateStatus(Long taskId, UpdateStatusRequestDto dto, Long userId) {
        Task existTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!existTask.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        existTask.setStatus(dto.getStatus());
        Task savedTask = taskRepository.save(existTask);
        return convertToDto(savedTask);
    }
    public TaskResponseDto updatePriority(Long taskId, UpdatePriorityRequestDto dto, Long userId) {
        Task existTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!existTask.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        existTask.setPriority(dto.getPriority());
        Task savedTask = taskRepository.save(existTask);
        return convertToDto(savedTask);
    }
    @Transactional
    public TaskResponseDto archiveTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Cannot archive task of another user");
        }
        task.setStatus(Status.ARCHIVED);
        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);

    }
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getArchivedTasks(Long userId, Pageable pageable) {
        return taskRepository.findAllByUserIdAndStatus(userId,Status.ARCHIVED,pageable)
                .map(this::convertToDto);
    }
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getActivedTasks(Long userId, Pageable pageable) {
        return taskRepository.findAllByUserIdAndStatusNot(userId,Status.ARCHIVED,pageable)
                .map(this::convertToDto);
    }


    public void  deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to view this task");
        }
        if (task.getStatus() != Status.ARCHIVED) {
            throw new IllegalStateException("Only archived tasks can be permanently deleted");
        }
        taskRepository.delete(task);

    }
    @Transactional
    public void deleteTasksBulk(List<Long> taskIds, Long userId) {
        if (taskIds == null || taskIds.isEmpty()) {
            throw new IllegalArgumentException("Task IDs cannot be empty");
        }
        List<Task> tasks = taskRepository.findAllById(taskIds);
        tasks.forEach(task -> {
            if (!task.getUserId().equals(userId)) {
                throw new SecurityException("Cannot delete tasks of another user");
            }
            if (task.getStatus() != Status.ARCHIVED) {
                throw new IllegalStateException("Only archived tasks can be permanently deleted");
            }
        });
        taskRepository.deleteAllInBatch(tasks);
    }
    public List<TaskResponseDto> filterTasks(Long userId, Status status, LocalDate fromDate, LocalDate toDate, Priority priority) {
        Specification<Task> spec = Specification.where(byUserId(userId));
        if (status != null) {
            spec = spec.and(byStatus(status));
        }
        if (priority != null) {
            spec = spec.and(byPriority(priority));
        }
        if (fromDate != null && toDate != null) {
            spec = spec.and(byDateBetween(fromDate, toDate));
        } else if (fromDate != null) {
            spec = spec.and(byDateFrom(fromDate));
        } else if (toDate != null) {
            spec = spec.and(byDateTo(toDate));
        }

        return taskRepository.findAll(spec).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private Specification<Task> byUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    private Specification<Task> byStatus(Status status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Task> byPriority(Priority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    private Specification<Task> byDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.between(root.get("date"), from, to);
    }

    private Specification<Task> byDateFrom(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    private Specification<Task> byDateTo(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to);
    }


}