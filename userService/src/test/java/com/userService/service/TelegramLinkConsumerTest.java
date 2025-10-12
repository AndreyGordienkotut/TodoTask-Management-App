package com.userService.service;

import by.info_microservice.core.LinkTelegramRequest;
import by.info_microservice.core.LinkTelegramResponse;
import com.userService.exception.TelegramLinkFailedException;
import com.userService.model.User;
import com.userService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelegramLinkConsumerTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TelegramLinkConsumer consumer;
    private User user;
    private LinkTelegramRequest request;
    private LinkTelegramRequest badRequest;
    @BeforeEach
    void setUp() {
        request = new LinkTelegramRequest("valid-token", 12345L);
        user = new User();
        user.setId(1L);
        badRequest = new LinkTelegramRequest("bad-token", 12345L);

    }

    @Test
    @DisplayName("Success - consume telegram-link-requests")
    void consume_validToken_updatesUserAndSendsResponse() {
        when(userRepository.findByTelegramLinkToken("valid-token"))
                .thenReturn(Optional.of(user));

        consumer.consume(request);

        assertThat(user.getTelegramChatId()).isEqualTo(12345L);
        verify(userRepository).save(user);
        verify(kafkaTemplate).send(
                "telegram-link-responses",
                "12345",
                new LinkTelegramResponse(12345L, true)
        );
    }
    @Test
    @DisplayName("Failure - consume telegram-link-requests with invalid token")
    void consume_invalidToken_sendsFailureResponseAndThrows() {
        when(userRepository.findByTelegramLinkToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThrows(TelegramLinkFailedException.class,
                () -> consumer.consume(badRequest));

        verify(kafkaTemplate).send(
                "telegram-link-responses",
                "12345",
                new LinkTelegramResponse(12345L, false)
        );
        verify(userRepository, never()).save(any());
    }

}
