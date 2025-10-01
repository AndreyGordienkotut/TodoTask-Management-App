package com.taskService.dto;

import com.taskService.model.Frequency_repeat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for partial update of a task's details")
public class UpdateTaskRequestDto {
    @Schema(description = "Title task", example = "Buy milk")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Schema(description = "Description task", example = "Check expiry date")
    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters")
    private String description;

    @Schema(description = "Date create task", example = "2025-09-29T19:36:19")
    private LocalDateTime date;

    @Schema(description = "What date the task must be completed", example = "2025-10-01T10:00:00")
    private LocalDateTime dueDate;

    @Schema(description = "Whether the task will repeat", example = "false")
    private boolean isRepeat;

    @Schema(description = "Frequency the task will repeat", example = "WEEK")
    private Frequency_repeat frequency_repeat;

}
