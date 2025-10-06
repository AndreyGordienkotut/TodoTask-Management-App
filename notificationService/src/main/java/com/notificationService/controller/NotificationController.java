package com.notificationService.controller;

import com.notificationService.dto.NotificationServiceRequest;
import com.notificationService.service.EmailService;
import com.notificationService.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "API for sending notifications (internal service endpoint).")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    @Operation(summary = "Send a notification",
            description = "Initiates the asynchronous sending process for a notification via email or Telegram.",
            tags = {"Notification Management"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the notification to be sent",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationServiceRequest.class))))
    @ApiResponse(responseCode = "200", description = "Notification request successfully accepted for processing.",
            content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., missing message or invalid channel).",
            content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationServiceRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok().build();
    }

}