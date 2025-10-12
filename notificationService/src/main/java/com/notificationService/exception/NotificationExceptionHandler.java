package com.notificationService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Map;

@ControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(InvalidNotificationRequestException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(InvalidNotificationRequestException ex) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", ex.getMessage()));
    }

    @ExceptionHandler({EmailSendException.class, TelegramSendException.class})
    public ResponseEntity<Map<String, String>> handleExternalServiceFailure(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.singletonMap("message", ex.getMessage()));
    }

    @ExceptionHandler(NotificationProcessingException.class)
    public ResponseEntity<Map<String, String>> handleProcessing(NotificationProcessingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", ex.getMessage()));
    }
}