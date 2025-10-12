package com.notificationService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class TelegramSendException extends RuntimeException {
    public TelegramSendException(String message, Throwable cause) {
        super(message, cause);
    }
}