package com.taskService.dto;
import com.taskService.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriorityRequestDto {
    private Priority priority;
}
