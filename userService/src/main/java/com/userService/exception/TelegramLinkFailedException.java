package com.userService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TelegramLinkFailedException extends RuntimeException {
    public TelegramLinkFailedException(String message) {
        super(message);
    }
}