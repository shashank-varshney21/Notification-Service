package com.shashank.notification_service.DTO;

import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationEvent {
    @Id
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String text;
    private LocalDateTime timestamp;
}
