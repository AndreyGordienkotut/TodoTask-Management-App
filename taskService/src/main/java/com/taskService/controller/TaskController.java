package com.taskService.controller;

import com.taskService.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

//    private final TaskService taskService;
//
//    // Внедряем TaskService через конструктор
//    public TaskController(TaskService taskService) {
//        this.taskService = taskService;
//    }
//
//    // Пример эндпоинта для создания тестовой задачи
//    @GetMapping("/create-test-task/{userId}")
//    public String createTestTask(@PathVariable String userId) {
//        return taskService.createTestTaskForUser(userId);
//    }
}
