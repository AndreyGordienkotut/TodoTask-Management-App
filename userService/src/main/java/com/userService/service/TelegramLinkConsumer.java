package com.userService.service;

//import com.userService.dto.LinkTelegramRequest;
import by.info_microservice.core.LinkTelegramRequest;
import by.info_microservice.core.LinkTelegramResponse;
import com.userService.exception.TelegramLinkFailedException;
import com.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramLinkConsumer {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "telegram-link-requests", groupId = "user-service-group")
    public void consume(LinkTelegramRequest request) {
        log.info("Received telegram link request: {}", request);

        userRepository.findByTelegramLinkToken(request.getToken()).ifPresentOrElse(user -> {
            user.setTelegramChatId(request.getChatId());
            userRepository.save(user);

            kafkaTemplate.send("telegram-link-responses",
                    request.getChatId().toString(),
                    new LinkTelegramResponse(request.getChatId(), true));
        }, () -> {
            log.warn("Invalid token: {}", request.getToken());

            kafkaTemplate.send("telegram-link-responses",
                    request.getChatId().toString(),
                    new LinkTelegramResponse(request.getChatId(), false));
            throw new TelegramLinkFailedException("Invalid Telegram link token: " + request.getToken());
        });
    }
}