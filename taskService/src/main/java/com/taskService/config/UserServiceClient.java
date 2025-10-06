package com.taskService.config;

import com.google.common.net.HttpHeaders;
import com.taskService.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    public UserDto getUserById(Long userId) {
        log.info("getUserById {}, {}", userId, webClientBuilder);
        return webClientBuilder
                .build()
                .get()
                .uri("http://user-service/api/auth/internal/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();

    }
}
