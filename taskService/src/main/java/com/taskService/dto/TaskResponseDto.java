package com.taskService.dto;

import com.taskService.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDto {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate date;
    private Status status;
}
