package com.userService.service;
import by.info_microservice.core.UserVerificationEventDto;
import com.userService.exception.EventPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void sendAccountVerificationEvent(UserVerificationEventDto event) {
        try {
            kafkaTemplate.send("account-verification-events",
                    event.getUserId().toString(),
                    event).get();
            log.info("Successfully published event for userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish verification event for userId={}", event.getUserId(), e);
            throw new EventPublishException("Failed to publish verification event", e);
        }
    }
}

