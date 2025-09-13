package com.taskService.dto;

import com.taskService.model.Priority;
import com.taskService.model.Status;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequestDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Date is required")
    private LocalDateTime date;
    @FutureOrPresent(message = "Due date must be in the present or future")
    private LocalDateTime dueDate;
    @NotNull(message = "Status is required")
    private Status status;
    @NotNull(message = "Priority is required")
    private Priority priority;

}
