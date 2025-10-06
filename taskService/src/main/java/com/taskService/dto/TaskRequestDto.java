package com.taskService.dto;

import com.taskService.model.Frequency_repeat;
import com.taskService.model.Priority;
import com.taskService.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request object for creating a new task")
public class TaskRequestDto {
    @Schema(description = "Title task", example = "Buy water")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Description task", example = "Buy water in 10 o'clock")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Date create task (current date is usually set by backend)", example = "2025-09-29T19:36:19")
    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @Schema(description = "What date the task must be completed", example = "2025-09-30T20:00:00")
    @FutureOrPresent(message = "Due date must be in the present or future")
    private LocalDateTime dueDate;

    @Schema(description = "Status task ", example = "NOT_COMPLETED")
    @NotNull(message = "Status is required")
    private Status status;

    @Schema(description = "Priority task (e.g., LOW)", example = "LOW")
    @NotNull(message = "Priority is required")
    private Priority priority;

    @Schema(description = "Whether the task will repeat", example = "true")
    @NotNull(message = "isRepeat is required")
    private boolean isRepeat;

    @Schema(description = "Frequency the task will repeat (required if isRepeat is true)", example = "DAY")
    private Frequency_repeat frequency_repeat;

}
