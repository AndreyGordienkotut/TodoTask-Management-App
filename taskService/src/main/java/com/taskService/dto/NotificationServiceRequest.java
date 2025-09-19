package com.taskService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationServiceRequest {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private String channel;
    private String status;
    private LocalDateTime createdAt;

}