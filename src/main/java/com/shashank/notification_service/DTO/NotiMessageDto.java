package com.shashank.notification_service.DTO;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiMessageDto {
    @Id
    private Long id;
    @NotNull
    private String text;
    private LocalDateTime createdAt;

    public NotiMessageDto(String text) {
        this.text = text;
    }

    public NotiMessageDto(String text, LocalDateTime createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }
}
