package com.taskService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TaskNotificationException extends RuntimeException {
    public TaskNotificationException(String message) {
        super(message);
    }
    public TaskNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}