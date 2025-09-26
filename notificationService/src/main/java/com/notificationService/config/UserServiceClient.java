package com.notificationService.config;

import com.notificationService.dto.LinkTelegramRequest;
import com.notificationService.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;
    private WebClient client() {
        return webClientBuilder.build();
    }

    public Optional<UserDto> getUserById(Long userId) {
        try {
            return client()
                .get()
                .uri("http://user-service/api/auth/internal/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(ex -> {
                    log.error("Failed to fetch user by id {}: {}", userId, ex.getMessage());
                    return Mono.empty();
                })
                .blockOptional();
        } catch (Exception e) {
             log.error("Exception in getUserById: {}", e.getMessage(), e);
             return Optional.empty();
        }
    }

    public boolean linkTelegramChatByToken(String token, Long chatId) {
        try {
            String response = client()
                    .post()
                    .uri("http://user-service/api/auth/link-by-token")
                    .bodyValue(new LinkTelegramRequest(token, chatId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            return "Linked".equalsIgnoreCase(response);
        } catch (Exception e) {
            log.error("Failed to link telegram: {}", e.getMessage(), e);
            return false;
        }
    }
}
