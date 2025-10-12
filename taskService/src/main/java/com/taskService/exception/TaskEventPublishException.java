package com.taskService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TaskEventPublishException extends RuntimeException {
    public TaskEventPublishException(String message) {
        super(message);
    }
    public TaskEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}