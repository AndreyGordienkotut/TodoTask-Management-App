package com.taskService.service;

import com.taskService.config.UserServiceClient;
import com.taskService.dto.UserDto;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    //createTask + abilities add date and time in task
    //update task
    // delete task
    //get all task
    //get task in status
    //
//    private final UserServiceClient userServiceClient;
//
//    public String createTestTaskForUser(String userId) {
//        UserDto user = userServiceClient.getUserById(userId);
//    }


//    private final UserServiceClient userServiceClient;
//
//    // Внедряем UserServiceClient через конструктор
//    public TaskService(UserServiceClient userServiceClient) {
//        this.userServiceClient = userServiceClient;
//    }
//
//    public String createTestTaskForUser(String userId) {
//        // Здесь мы используем наш клиент для получения данных о пользователе
//        UserDto user = userServiceClient.getUserById(userId);
//
//        if (user != null) {
//            // В реальном приложении здесь будет логика создания задачи
//            return "Task created for user: " + user.getName() + " with email: " + user.getEmail();
//        } else {
//            return "User not found with ID: " + userId;
//        }
//    }
}