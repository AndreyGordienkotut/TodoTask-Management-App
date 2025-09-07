package com.taskService.config;

import com.taskService.dto.UserDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserServiceClient {

//    private final WebClient webClient;
//
//    public UserServiceClient(WebClient.Builder webClientBuilder) {
//        // Мы указываем базовый URL для UserService. В реальном проекте этот URL будет браться из конфигурации
//        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/users").build();
//    }

//    public UserDto getUserById(String userId) {
//        // Здесь мы делаем GET-запрос к UserService
//        return webClient.get()
//                .uri("/{userId}", userId)
//                .retrieve()
//                .bodyToMono(UserDto.class)
//                .block();
//    }
}

