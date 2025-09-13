package com.taskService.dto;
import com.taskService.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto {
    @NotNull(message = "Status is required")
    private Status status;
}