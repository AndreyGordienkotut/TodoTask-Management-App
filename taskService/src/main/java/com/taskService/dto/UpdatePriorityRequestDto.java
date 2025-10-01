package com.taskService.dto;
import com.taskService.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for updating only the task priority")
public class UpdatePriorityRequestDto {
    @Schema(description = "New priority for the task", example = "HIGH")
    @NotNull(message = "Priority is required")
    private Priority priority;
}
