package com.userService.exception;

import com.userService.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Map;

@ControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {

        Map<String, String> response = Collections.singletonMap("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> BadRequestException(BadRequestException ex) {

        Map<String, String> response = Collections.singletonMap("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}