package com.taskService.config;

import com.taskService.dto.NotificationServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://localhost:8082")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationServiceRequest request);
}