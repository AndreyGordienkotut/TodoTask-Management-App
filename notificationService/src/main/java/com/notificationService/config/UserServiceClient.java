package com.notificationService.config;

import com.notificationService.dto.LinkTelegramRequest;
import com.notificationService.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    public UserDto getUserById(Long userId) {
        return webClientBuilder
                .build()
                .get()
                .uri("http://user-service/api/auth/internal/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }
    public UserDto getUserByEmail(String email) {
        return webClientBuilder
                .build()
                .get()
                .uri("http://user-service/api/users/by-email?email={email}", email)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }
    public void updateTelegramChatId(Long userId, Long chatId) {
        webClientBuilder
                .build()
                .patch()
                .uri("http://user-service/api/users/{id}/telegram?chatId={chatId}", userId, chatId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
    public boolean linkTelegramChatByToken(String token, Long chatId) {
        try {
            String response = webClientBuilder.build()
                   .post()
                   .uri("http://user-service/api/auth/link-by-token")
                   .bodyValue(new LinkTelegramRequest(token, chatId))
                   .retrieve()
                  .bodyToMono(String.class)
                  .block();

            return "Linked".equalsIgnoreCase(response);
       } catch (Exception e) {
          return false;
       }
    }
}
