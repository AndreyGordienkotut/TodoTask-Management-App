package com.notificationService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,name = "user_id")
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;
    @Column(nullable = false)
    private String recipient;
    @Column(nullable = false,name = "recipient_telegram_id")
    private Long recipientTelegramId;
    private String subject;
    @Column(nullable = false)
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Notification_status status;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(nullable = true, name = "sent_at")
    private LocalDateTime sentAt;
    private String error_message;
}
