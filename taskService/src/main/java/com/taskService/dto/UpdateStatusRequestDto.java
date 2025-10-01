package com.taskService.dto;
import com.taskService.model.Status;
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
@Schema(description = "Request object for updating only the task status")
public class UpdateStatusRequestDto {
    @Schema(description = "New status for the task", example = "ARCHIVED")
    @NotNull(message = "Status is required")
    private Status status;
}