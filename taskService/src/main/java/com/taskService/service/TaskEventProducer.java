package com.taskService.service;

import by.info_microservice.core.TaskEventDto;
import com.taskService.exception.TaskEventPublishException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void sendTaskEvent(TaskEventDto event) {
        try {
            kafkaTemplate.send("task-events-topic", event.getTaskId().toString(), event).get();
        } catch (Exception e) {
            throw new TaskEventPublishException("Failed to publish task event for taskId=" + event.getTaskId(), e);
        }
    }
}
