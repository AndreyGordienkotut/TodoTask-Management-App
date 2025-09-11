package com.taskService.dto;
import com.taskService.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto {
    private Status status;
}