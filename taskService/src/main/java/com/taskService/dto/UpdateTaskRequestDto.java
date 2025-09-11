package com.taskService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequestDto {
    private String title;
    private String description;
    private LocalDate date;
}
