package com.taskService.dto;
import com.taskService.model.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriorityRequestDto {
    @NotNull(message = "Priority is required")
    private Priority priority;
}
