package com.taskService.dto;

import com.taskService.model.Frequency_repeat;
import com.taskService.model.Priority;
import com.taskService.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing information about task")
public class TaskResponseDto {
    @Schema(description = "Identification task", example = "1")
    private Long id;
    @Schema(description = "Identification user", example = "1")
    private Long userId;
    @Schema(description = "Title task", example = "Buy water")
    private String title;
    @Schema(description = "Description task", example = "Buy water in 10 o'clock")
    private String description;
    @Schema(description = "Date create task", example = "2025-09-29 19:36:19")
    private LocalDateTime date;
    @Schema(description = "What date the task must be completed", example = "2025-09-30 20:00:00")
    private LocalDateTime dueDate;
    @Schema(description = "Status task", example = "OVERDUE")
    private Status status;
    @Schema(description = "Priority task", example = "LOW")
    private Priority priority;
    @Schema(description = "Whether the task will repeat", example = "true")
    private boolean isRepeat;
    @Schema(description = "Frequency the task will repeat", example = "DAY")
    private Frequency_repeat frequency_repeat;
}
