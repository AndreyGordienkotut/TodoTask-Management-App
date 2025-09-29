package com.taskService.dto;

import com.taskService.model.Frequency_repeat;
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
public class UpdateTaskRequestDto {
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters")
    private String description;
    private LocalDateTime date;
    private LocalDateTime dueDate;
    private boolean isRepeat;
    private Frequency_repeat frequency_repeat;

}
